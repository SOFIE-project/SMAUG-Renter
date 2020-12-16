/*
 * Copyright 2020 Ericsson AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package eu.sofie_iot.smaug.mobile

import android.os.Handler
import android.os.Looper
import android.util.Log
import eu.sofie_iot.smaug.mobile.ledger.Ledger
import eu.sofie_iot.smaug.mobile.ledger.Transaction
import eu.sofie_iot.smaug.mobile.model.Bid
import eu.sofie_iot.smaug.mobile.model.Database
import eu.sofie_iot.smaug.mobile.model.Rent
import kotlinx.coroutines.*
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class PaymentProcessor(val db: Database) {
    private val TAG = "PaymentProcessor"

    init {
        Log.d(TAG, "PaymentProcessor init: $this")
    }

    private var completionHandler: ((Bid) -> Unit)? = null

    fun setCompletionHandler(handler: (Bid) -> Unit): Unit {
        Log.d(TAG, "setCompletionHandler: handler=$handler")
        completionHandler = handler
    }

    private val ledgers = ConcurrentHashMap<Long, Ledger>()

    fun setMarketplaceLedger(marketplaceId: Long, ledger: Ledger?) {
        if (ledger != null)
            ledgers.put(marketplaceId, ledger)
        else
            ledgers.remove(marketplaceId)
    }

    // values we use for bid.state field
    private val STATE_INITIAL = 0
    private val STATE_TX_SENDING = 5
    private val STATE_TX_SENT = 10
    private val STATE_TX_CANCEL = 20 // cancel
    private val STATE_REDEEM_SENT = 30
    private val STATE_COMPLETE = 100

    // payment processor background thread (coroutine)
    //
    // This will watch the list of pending payments, and progresses them through its own
    // state machine. Specifically:
    //
    // * new payments are dispatched
    // * progress of pending payments is monitored
    // * completed payments are recorded into locker record, and UI is triggered if necessary (snack)
    // * old payments are cleared eventually (after some delay to make sure UI can maintain
    //   livedata monitoring of the payment)

    data class BidState(var tx: Transaction?, var cancelTx: Transaction?)

    private val bidStates = ConcurrentHashMap<Long, BidState>()

    private suspend fun processBid(db: Database, bid: Bid): Unit {
        Log.d(TAG, "processBid: handling $bid")

        val ledger = ledgers.get(bid.marketplaceId)

        if (ledger == null) {
            Log.e(TAG, "could not find ledger for bid referring to marketplace ${bid.marketplaceId}, completed with error")

            db.models.updateBid(bid.copy(
                completed = true,
                completedAt = Instant.now(),
                succeeded = false,
                resultMessage = "Error: bid referred to market without backing ledger"))
            return
        }

        suspend fun get(maybeId: String?) =
            maybeId?.let { id -> ledger.getTransaction(id) }

        // check if we already have one
        val state = bidStates.getOrPut(bid.id) { BidState(get(bid.transactionId), get(bid.cancelTransactionId)) }

        // make them easier to access, though
        val (tx, cancelTx) = state

        val updatedBid: Bid = when (bid.state) {
            // State is STATE_INITIAL, we have now to send the transaction. There should
            // not exist yet a transaction or cancel.
            STATE_INITIAL -> {
                if (!(tx == null && cancelTx == null)) {
                    error("Assertion failed: ${tx} != null || ${cancelTx} != null")
                }

                // note that a sent bid may not yet have a tx id, so there's SENDING step next
                state.tx = ledger.sendBid(bid)

                Log.d(TAG, "tx ${state.tx} being sent to $ledger")
                bid.copy(state = STATE_TX_SENDING)
            }
            STATE_TX_SENDING -> {
                if (tx == null) {
                    error("Assertion failed: ${tx} == null")
                }

                if (tx.setup.isCompleted) {
                    val setup = tx.setup.getCompleted()
                    Log.d(TAG, "tx $tx setup completed, id: ${setup.transactionId}")

                    bid.copy(state = STATE_TX_SENT, transactionId = setup.transactionId, transactionSent = setup.time)
                } else
                    bid
            }

            STATE_TX_SENT -> {
                if (!(tx != null && cancelTx == null)) {
                    error("Assertion failed: $tx == null || $cancelTx != null")
                }

                // If tx is both progressed and completed, this will split them into two
                // distinct steps. This is acceptable.
                if (tx.progress.isCompleted && bid.transactionBlock == null) {
                    val progress = tx.progress.getCompleted()
                    Log.d(TAG, "tx $tx progress completed: $progress")
                    bid.copy(transactionBlock = progress.block, transactionIncluded = progress.time)
                } else if (tx.result.isCompleted) {
                    val result = tx.result.await()
                    Log.d(TAG, "tx $tx result completed: $result")

                    // TODO: check if bid didn't win, and redeem escrow if so
                    bid.copy(state = STATE_COMPLETE,
                        succeeded = result.success,
                        resultMessage = result.message,
                        resultTransactionBlock = result.block,
                        resultTransactionId = result.transactionId,
                        resultTransactionIncluded = result.time
                    )
                } else
                    bid
            }
            STATE_TX_CANCEL -> {
                if (BuildConfig.DEBUG && !(tx != null && cancelTx != null)) {
                    error("Assertion failed: $tx == null || $cancelTx == null")
                }
                stepCheckCancelTransaction(bid, tx, cancelTx)
            }
            STATE_REDEEM_SENT -> {
                if (BuildConfig.DEBUG && tx == null) {
                    error("Assertion failed: $tx == null")
                }
                stepCheckRedeemTransaction(bid)
            }
            STATE_COMPLETE -> {
                // check if the result is success, do we add a record for the locker or not
                if (bid.succeeded && tx != null) {
                    val result = tx.result.getCompleted()

                    Log.d(TAG, "STATE_COMPLETE: succeeded, result=$result")

                    val rent = Rent(lockerId = bid.lockerId,
                        bidId = bid.id,
                        start = result.rentStart!!,
                        end = result.rentEnd!!)
                    Log.d(TAG, "Adding rent record: $rent")

                    // TODO: probably should do this in transaction WITH bid update
                    db.models.insertRent(rent)
                }

                bid.copy(completed = true, completedAt = Instant.now())
            }
            else -> stepInvalid(bid)
        }

        if (updatedBid != bid) {
            Log.d(
                TAG,
                "processBid: bid ${bid.id} state ${bid.state}-->${updatedBid.state}"
            )

            db.models.updateBid(updatedBid)

            if (updatedBid.state == STATE_COMPLETE) {
                Log.d(TAG, "bid completed, handler: $completionHandler for bid $updatedBid")
                completionHandler?.let {
                    Handler(Looper.getMainLooper()).post {
                        it(updatedBid)
                    }
                }
            }
        }
    }

    private fun stepInvalid(bid: Bid): Bid = bid.copy(
        state = STATE_COMPLETE,
        succeeded = false,
        resultMessage = "Encountered invalid internal payment state ${bid.state}")

    private fun stepComplete(bid: Bid, tx: Transaction?, cancelTx: Transaction?): Bid {
        return bid.copy(succeeded = false, resultMessage = "This should not happen")
    }

    private fun stepCheckRedeemTransaction(bid: Bid): Bid {
        // FIXME: we need some other result description, "succeeded" != "bid succeeded"
        return bid.copy(state = STATE_COMPLETE, succeeded = false, resultMessage = "Escrow redeemed")
    }

    private fun stepCheckCancelTransaction(bid: Bid, tx: Transaction?, cancelTx: Transaction?): Bid {
        return bid.copy(state = STATE_COMPLETE, succeeded = false, resultMessage = "Transaction cancelled")
    }

    // This is not optimal, it is bad for battery .for example. Preferably we should use
    // a livedata to detect changes, and launch async handlers for those. But this is more
    // straightfoward, i.e. polling.
    private suspend fun process()  = coroutineScope {
        Log.d(TAG, "process: starting")

        val bidsBeingHandled = ConcurrentHashMap<Long, Job>()

        while (true) {
            val pending = db.models.pendingBids()

            if (pending.size > 0 || bidsBeingHandled.size > 0)
                Log.d(
                    TAG,
                    "process: ${pending.size} pending bids, ${bidsBeingHandled.size} being handled"
                )

            // check which we are **not** currently handling and launch on them
            pending.forEach { bid ->
                if (!bidsBeingHandled.containsKey(bid.id)) {
                    val task = async {
                        processBid(db, bid)
                    }

                    bidsBeingHandled.put(bid.id, task)
                    Log.d(TAG, "launched $task for ${bid.id}")
                }
            }

            // make a pass over handlers, see if any of them completed -- we may end up restarting
            // some but that's not a problem
            bidsBeingHandled.forEach { id, job ->
                if (job.isCompleted) {
                    Log.d(TAG, "job $job completed for $id")
                    bidsBeingHandled.remove(id)
                }
            }

            delay(1000L)
        }
    }

    private suspend fun cleanup() {
        Log.d(TAG, "cleanup")

        for (bid in db.models.completedBids()) {
            Log.d(TAG, "cleanup: completed bid: $bid")
        }

        Log.d(TAG, "cleanup finished")
    }

    fun start(scope: CoroutineScope) {
        scope.launch {
            Log.d(TAG, "start: launching in scope $scope")
            withContext(Dispatchers.IO) {
                // cleanup will run once, we can run it only occasionally i.e. application startup
                cleanup()

                // process will keep running indefinitely
                process()
            }
        }
    }

    fun stop() {
        Log.d(TAG, "stop: not yet implemented")
    }
}