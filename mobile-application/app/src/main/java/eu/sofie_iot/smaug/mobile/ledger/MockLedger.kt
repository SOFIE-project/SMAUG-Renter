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

package eu.sofie_iot.smaug.mobile.ledger

import android.util.Log
import eu.sofie_iot.smaug.mobile.model.Bid
import kotlinx.coroutines.*
import java.time.Instant

// This mock ledger uses simple timekeeping mechanism to idenfity when a transaction
// can move to next state.

class MockLedger(var mode: MockLedger.Mode = MockLedger.Mode.MODE_ACCEPT, var setupDelay: Long = 5000L, var progressDelay: Long = 7000L, var transactionDelay: Long = 15000) : Ledger {
    val TAG = "MockLedger"

    var block: Long = 0L

    enum class Mode {
        MODE_ACCEPT,
        MODE_REJECT,
        MODE_IGNORE_TX,
        MODE_IGNORE_UNTIL_CANCEL,
        MODE_IGNORE_ALL
    }

    data class Tx(override val setup: Deferred<TransactionSetup>, override val progress: Deferred<TransactionProgress>, override val result: Deferred<TransactionResult>): Transaction {
        override fun toString(): String {
            return "Tx<$setup,$progress,$result>"
        }
    }

    // we'll just reuse same txs if necessary
    private val txs = HashMap<String, Transaction>()

    private fun randomId(): String =
        List(64) { (('a' .. 'f') + ('0' .. '9')).random() }.joinToString("")

    override suspend fun sendBid(bid: Bid): Transaction = txFor(bid)
    override suspend fun getTransaction(id: String): Transaction {
        if (txs.containsKey(id))
            return txs[id]!!

        // otherwise just produce a dummy transaction
        return Tx(CompletableDeferred(), CompletableDeferred(), CompletableDeferred())
    }

    private suspend fun txFor(bid: Bid): Transaction {
        val id = randomId()

        // create transaction with current settings

        val tx = when (mode) {
            Mode.MODE_ACCEPT -> {
                val setup = GlobalScope.async {
                    delay(setupDelay)
                    val setup = TransactionSetup(id, Instant.now())
                    Log.d(TAG, "MODE_ACCEPT: after setup delay: $setup")
                    setup
                }
                val progress = GlobalScope.async {
                    Log.d(TAG, "MODE_ACCEPT: waiting for $setup before handling progress")
                    setup.await()
                    Log.d(TAG, "MODE_ACCEPT: entering $progressDelay ms delay for progress")
                    delay(progressDelay)
                    val progress = TransactionProgress(nextBlock(), Instant.now())
                    Log.d(TAG, "MODE_ACCEPT: completing progress $progress")
                    progress
                }
                val result = GlobalScope.async {
                    Log.d(TAG, "MODE_ACCEPT: waiting for $progress before handling result")
                    progress.await()

                    Log.d(TAG, "MODE_ACCEPT: entering $transactionDelay ms delay after progress")
                    delay(transactionDelay)

                    val start = bid.bidStart ?: Instant.now()

                    val result = TransactionResult(
                        success = true,
                        block = nextBlock(),
                        message = "Bid accepted",
                        token = randomId(),
                        transactionId = randomId(),
                        time = Instant.now(),

                        // TODO: fix these to come from the bid
                        rentStart = start,
                        rentEnd = start.plusSeconds((bid.amountMinutes ?: 15) * 60)
                    )
                    Log.d(TAG, "MODE_ACCEPT: delayed, result: $result")
                    result
                }
                Log.d(TAG, "MODE_ACCEPT: returning $progress, $result")

                Tx(setup, progress, result)
            }
            Mode.MODE_REJECT -> {
                val setup = GlobalScope.async {
                    delay(setupDelay)
                    TransactionSetup(id, Instant.now())
                }
                val progress =  GlobalScope.async {
                    setup.await()
                    TransactionProgress(nextBlock(), Instant.now())
                }
                val result = GlobalScope.async {
                    progress.await()
                    TransactionResult(
                        success = false,
                        block = nextBlock(),
                        message = "Locker not available",
                        transactionId = randomId(),
                        time = Instant.now()
                    )
                }
                Tx(setup, progress, result)
            }
            // this will never progress
            Mode.MODE_IGNORE_ALL -> Tx(CompletableDeferred(), CompletableDeferred(), CompletableDeferred())
            else -> TODO("Not yet implemented")
        }

        txs.put(id, tx)
        return tx
    }

    private fun nextBlock(): Long {
        block += 1
        return block
    }

    override suspend fun cancelTransaction(tx: Transaction): Transaction {
        TODO("Not yet implemented")
    }

    override suspend fun getCancelTransaction(id: String): Transaction {
        TODO("Not yet implemented")
    }
}