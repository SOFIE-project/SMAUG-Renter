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


enum class LockerOpenCloseType {
    TAP_OPEN_CLOSE_PUSH,
    TAP_OPEN_CLOSE_PUSH_DELAY,
    TAP_OPEN_TAP_CLOSE
}

@Entity(
    tableName = "locker",
    indices = arrayOf(
        Index(value = ["locker_uuid", "marketplace_id"], unique = true),
        Index(value = ["marketplace_id"])
    ),
    foreignKeys = arrayOf(
        ForeignKey(
            entity = Marketplace::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("marketplace_id"),
            onDelete = ForeignKey.CASCADE
        )
    )
)
data class Locker(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** actual locker id as defined within the marketplace */
    @ColumnInfo(name = "locker_uuid")
    val uniqueId: String? = null,

    @ColumnInfo(name = "locker_iid")
    val instanceId: String? = null,

    @ColumnInfo(name = "locker_mid")
    val marketId: Long? = null,

    /** what marketplace is this locker part of */
    @ColumnInfo(name = "marketplace_id")
    val marketplaceId: Long,

    /** descriptive name of the locker */
    @ColumnInfo
    val name: String,

    /** longer description of the locker */
    @ColumnInfo
    val description: String? = null,

    /** primary image of the locker -- it may have multiple ones, but those are dynamically
     * loaded from the network, but we want to keep a "primary" image that is shown as the
     * icon for the locker
     */
//    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
//    val image: ByteArray? = null,
    @ColumnInfo(name = "image_urls")
    val imageUrls: List<String>? = null,

    /** is this locker favourited? */
    val favourite: Boolean = false,

    /** how does the locker open or close? */
    val type: LockerOpenCloseType = LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH,

    // TODO: image URLs (other images? or do we just dynamically load this from the server instead?)
    // TODO: geolocation information
    // TODO: locker owner
    // TODO: locker pricing and rental type
    // TODO: how this locker has been identified, i.e. via marketplace search, BT beacon, tap (or any combination of them)
    // ...

    // timestamps for last activity regarding NFC, BLE and BE
    val lastSeenNfc: Instant? = null,
    val lastSeenBeacon: Instant? = null,
    val lastSeenBackend: Instant? = null,

    // smoothed RSSI
    val bleRssi: Double? = null,
    // last BLE tx power seen
    val bleTxPower: Int? = null,

    // store ui state
    val historyCollapsed: Boolean = true
) {
    override fun toString(): String {
        // don't include the image field in tostring
        return "Locker(id=$id, lockerId=$uniqueId, marketplaceId=$marketplaceId, name=$name, favourite=$favourite)"
    }
}