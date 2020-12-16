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
import java.time.Instant

@Entity(tableName = "rents",
    indices = arrayOf(
        Index(value = ["locker_id"]),
        Index(value = ["bid_id"])
    ),
    foreignKeys = arrayOf(
        ForeignKey(
            entity = Locker::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("locker_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Bid::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("bid_id"),
            onDelete = ForeignKey.CASCADE
        )
    ))
data class Rent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "locker_id") val lockerId: Long,

    // some rents may be "given", without bids associated with them
    @ColumnInfo(name = "bid_id") val bidId: Long? = null,

    @ColumnInfo(name = "start_time") val start: Instant,
    @ColumnInfo(name = "end_time")  val end: Instant
) {
    fun within(time: Instant) =
        start.isBefore(time) && end.isAfter(time)
}