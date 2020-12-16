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

package eu.sofie_iot.smaug.mobile.model

import androidx.room.*
import java.math.BigInteger
import java.time.Instant

@Entity(tableName = "bids",
    indices = arrayOf(
        Index(value = ["locker_id"])
    ),
    foreignKeys = arrayOf(
        ForeignKey(
            entity = Locker::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("locker_id"),
            onDelete = ForeignKey.CASCADE
        )
    ))
data class Bid(
    // references
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "locker_id") val lockerId: Long,
    @ColumnInfo(name = "marketplace_id") val marketplaceId: Long,
    @ColumnInfo(name = "created_at") val created: Instant = Instant.now(),

    //--------------------------------------------------------------
    // these define the bid and payment details

    // amount of wei to be bid/paid
    @ColumnInfo(name = "amount") val amount: BigInteger = BigInteger.ZERO,

    // how much the bidder thinks this is in minutes (for checks)
    @ColumnInfo(name = "amount_minutes") val amountMinutes: Long? = null,

    // when the bid is starting, NULL = immediately when possible
    @ColumnInfo(name = "bid_start") val bidStart: Instant? = null,

    // other info needs to follow, above is for instant rent, but we'll need bid
    // start/end etc. for more complex setups later

    //--------------------------------------------------------------
    // if NULL, not completed yet, otherwise indicates if success or failure
    @ColumnInfo(name = "succeeded") val succeeded: Boolean = false,

    // whether this is and when this was completed, either succeeded or failed
    // `succeeded` is guaranteed to be non-null when completed is true
    @ColumnInfo(name = "completed") val completed: Boolean = false,
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null,

    // result text for success or failure, most useful for failures
    @ColumnInfo(name = "result_message") val resultMessage: String? = null,

    //--------------------------------------------------------------
    // everything below is mostly internal for the payment processor task
    //
    // NOTE: This makes a heavy assumption that we are operating on an Ethereum ledger. We
    // could try to abstract that away, but the underlying assumption of SMAUG **is** that
    // we're on a Ethereum-based ledger, so such abstraction is unnecessary given the
    // assumptions.

    // always start in state 0, we could refactor this into a separate table but that's not
    // useful -- this really only has a meaning INSIDE PaymentProcessor, otherwise just
    // observe other fields
    @ColumnInfo(name = "state") val state: Int = 0,

    // transaction sent
    @ColumnInfo(name = "bid_transaction_id") val transactionId: String? = null,
    @ColumnInfo(name = "bid_transaction_sent_at") val transactionSent: Instant? = null,

    // block number of the block where transaction was recorded
    @ColumnInfo(name = "bid_transaction_block") val transactionBlock: Long? = null,
    @ColumnInfo(name = "bid_transaction_block_at") val transactionIncluded: Instant? = null,

    // completion transaction, i.e. which transaction which indicates our result (via events)
    @ColumnInfo(name = "result_transaction_id") val resultTransactionId: String? = null,
    @ColumnInfo(name = "result_transaction_block") val resultTransactionBlock: Long? = null,
    @ColumnInfo(name = "result_transaction_block_at") val resultTransactionIncluded: Instant? = null,


    // others we should add later in a fully complete implementation:
    // escrow release transaction for failing or timeouted bids
    // ...
    @ColumnInfo(name = "cancel_transaction_id") val cancelTransactionId: String? = null,
    @ColumnInfo(name = "cancel_transaction_sent_at") val cancelTranasctionSent: Instant? = null,
    @ColumnInfo(name = "cancel_transaction_block") val cancelTransactionBlock: Long? = null,
    @ColumnInfo(name = "cancel_transaction_block_at") val cancelTransactionIncluded: Instant? = null
)