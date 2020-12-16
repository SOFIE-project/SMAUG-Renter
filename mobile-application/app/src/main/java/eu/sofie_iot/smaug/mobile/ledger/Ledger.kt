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

import eu.sofie_iot.smaug.mobile.model.Bid
import kotlinx.coroutines.Deferred
import java.time.Instant


data class TransactionResult(
    val success: Boolean,
    val message: String = "",
    val block: Long,
    val time: Instant,
    val transactionId: String,

    // only for successful rents
    val token: String? = null,
    val rentStart: Instant? = null,
    val rentEnd: Instant? = null)

data class TransactionProgress(val block: Long, val time: Instant)

data class TransactionSetup(val transactionId: String, val time: Instant)

interface Transaction{
    val setup: Deferred<TransactionSetup>
    val progress: Deferred<TransactionProgress>
    val result: Deferred<TransactionResult>
}

interface Ledger {
    // this will **send the bid** to the ledger; it will **not** modify the model, it is
    // caller's responsibility
    suspend fun sendBid(bid: Bid): Transaction

    // this will construct the transaction based on the given outstanding transaction id,
    // used when the payment processor is starting with outstanding bids in the database
    suspend fun getTransaction(id: String): Transaction

    // initiate a transaction cancel operation: note that it is guaranteed that **either** the
    // original transaction **or** the cancel transaction can succeed, but not both
    suspend fun cancelTransaction(tx: Transaction): Transaction

    // ditto for cancels
    suspend fun getCancelTransaction(id: String): Transaction
}