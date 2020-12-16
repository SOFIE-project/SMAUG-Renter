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

@Entity(tableName = "activities",
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
data class LockerActivity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "locker_id") val lockerId: Long,
    @ColumnInfo(name = "time") val time: Instant = Instant.now(),
    @ColumnInfo(name = "succeeded") val succeeded: Boolean = true,
    // activity type
    @ColumnInfo(name = "type") val type: Int,
    // resulting state, i.e. what it is after the operation
    @ColumnInfo(name = "state") val state : Int,
    // any freeform text, for example error message
    @ColumnInfo(name = "text") val text: String? = null

) {
    companion object {
        val ACTIVITY_QUERY = 0
        val ACTIVITY_OPEN = 1
        val ACTIVITY_CLOSE = 2

        val STATE_UNKNOWN = 0
        val STATE_OPEN = 1
        val STATE_CLOSED = 2
    }

}