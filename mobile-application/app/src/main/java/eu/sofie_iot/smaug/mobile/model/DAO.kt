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

import androidx.lifecycle.LiveData
import androidx.room.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

// Instant.MAX will cause overflow errors somewhere, use 1000 years in the future as a
// substitute for end of the universe.
val MILLENIA = Instant.now().plusSeconds(1000 * 365 * 24 * 60 * 60L)

/**
 * How the database is to be used:
 *
 * 1. The base model is in normal form, consisting of Locker, ActivityRecord, Marketplace, etc.
 * 2. We provide base operations for these in a LiveModelDao (using LiveData) and ModelDao (using
 *    coroutines). Note that only ModelDao has methods that change state.
 * 3. Additional collections of multiple objects such as all lockers with their currently active
 *    rents, all activities for a locker etc. are provided by their own classes that abstract
 *    a lot of the complexity
 * 4. Those are accessed via LiveAggregateDao and AggregateDao DAOs.
 */

@Dao
interface LiveModelDao {
    //--------------------------------------------------------------------------------------------
    // Locker
    @Query("select * from locker")
    fun allLockers(): LiveData<List<Locker>>
    val lockers get() = allLockers()

    @Query("select * from locker where id = :id")
    fun lockerById(id: Long): LiveData<Locker?>

    @Query("select * from locker where marketplace_id = :marketplaceId and locker_uuid = :uuid")
    fun lockerByUuid(marketplaceId: Long, uuid: String): LiveData<Locker?>

    // This is one later for geolocation of nearby lockers, just a dummy all-query now
    @Query("select * from locker limit 10")
    fun lockersByLocation(): LiveData<List<Locker>>

    @Query("select * from locker where id in (:ids)")
    fun lockersByIds(vararg ids: Long): LiveData<List<Locker>>

    //--------------------------------------------------------------------------------------------
    // Locker dependents
    @Query("select * from bids where locker_id = :lockerId and created_at > :after order by created_at")
    fun bidsForLocker(lockerId: Long, after: Instant = Instant.EPOCH): LiveData<List<Bid>>

    @Query("select * from bids where locker_id = :lockerId and not completed")
    fun openBidsForLocker(lockerId: Long): LiveData<List<Bid>>

    @Query("select * from bids where id = :bidId")
    fun bidById(bidId: Long): LiveData<Bid?>

    @Query("select * from bids where not completed")
    fun pendingBids(): LiveData<List<Bid>>

    @Query("select * from bids where completed")
    fun completedBids(): LiveData<List<Bid>>

    // this requires the rent to be WITHIN after-before range
    @Query("select * from rents where locker_id = :lockerId and start_time >= :after and end_time <= :before order by start_time")
    fun rentsForLockerBetween(lockerId: Long, after: Instant = Instant.EPOCH, before: Instant = MILLENIA): LiveData<List<Rent>>

    // this requires the point in time to be within the rent period
    @Query("select * from rents where locker_id = :lockerId and start_time <= :time and 'end' <= :time order by start_time")
    fun rentsForLockerAt(lockerId: Long, time: Instant = Instant.now()): LiveData<List<Rent>>

    @Query("select * from activities where locker_id = :lockerId and time >= :after order by time desc limit :limit")
    fun activitiesForLocker(lockerId: Long, after: Instant = Instant.EPOCH, limit: Int = 1000): LiveData<List<LockerActivity>>

    // Rents overall
    @Query("select * from rents where start_time >= :after and end_time <= :before order by start_time")
    fun rentsBetween(after: Instant = Instant.EPOCH, before: Instant = MILLENIA): LiveData<List<Rent>>

    // overlaps for specific locker
    @Query("select * from rents where " +
            "locker_id = :lockerId and (" +
            // rent start is within the range
            "(start_time >= :start and start_time <= :end) or" +
            // rent end is within the range
            "(end_time >= :start and end_time <= :end) or " +
            // rent starts before range and ends after range (i.e. range is within the rent period)
            "(start_time <= :start and end_time >= :end) " +
            ")"
            + "order by start_time"
    )
    fun rentsForLockerOver(lockerId: Long, start: Instant = Instant.now(), end: Instant = MILLENIA): LiveData<List<Rent>>

    // this requires the rent period to OVERLAP the given interval
    @Query("select * from rents where " +
            // rent start is within the range
            "(start_time >= :start and start_time <= :end) or" +
            // rent end is within the range
            "(end_time >= :start and end_time <= :end) or " +
            // rent starts before range and ends after range (i.e. range is within the rent period)
            "(start_time <= :start and end_time >= :end) "
            + "order by start_time"
    )
    fun rentsOver(start: Instant = Instant.now(), end: Instant = MILLENIA): LiveData<List<Rent>>

    @Query("select * from rents where locker_id = :lockerId")
    fun rentsForLocker(lockerId: Long): LiveData<List<Rent>>

    //--------------------------------------------------------------------------------------------
    // Marketplace
    @Query("select * from marketplace")
    fun allMarketplaces(): LiveData<List<Marketplace>>
    val marketplaces get() = allMarketplaces()

    @Query("select * from marketplace where id = :marketplaceId")
    fun marketplaceById(marketplaceId: Long): LiveData<Marketplace?>

    @Query("select * from marketplace where ledger = :ledger and address = :contractAddress")
    fun marketplaceByContract(ledger: LedgerIdentifier, contractAddress: String): LiveData<Marketplace?>

    @Query("select * from activities")
    fun allActivities(): LiveData<List<LockerActivity>>
    @Query("select * from bids")
    fun allBids(): LiveData<List<Bid>>
    @Query("select * from rents")
    fun allRents(): LiveData<List<Rent>>
}

@Entity
data class BidState(
    @ColumnInfo(name="id") val id: Long,
    @ColumnInfo(name="state") val state: Int
)

data class LockerFavourite(
    val id: Long,
    val favourite: Boolean)

data class LockerHistoryCollapsed(
    val id: Long,
    val historyCollapsed: Boolean)


@Dao
interface ModelDao {
    //--------------------------------------------------------------------------------------------
    // Locker
    @Query("select * from locker")
    suspend fun allLockers(): List<Locker>

    @Query("select * from locker where id = :id")
    suspend fun lockerById(id: Long): Locker?

    // TODO: would have to figure out potentially what to do with multiple marketplaces
    @Query("select * from locker where locker_iid = :iid")
    suspend fun lockerByIid(iid: String): Locker?

    @Query("select * from locker where marketplace_id = :marketplaceId and locker_uuid = :uuid")
    suspend fun lockerByUuid(marketplaceId: Long, uuid: String): Locker?

    @Insert
    suspend fun insertLocker(locker: Locker): Long

    @Update
    suspend fun updateLocker(locker: Locker): Unit

    @Delete
    suspend fun deleteLocker(vararg lockers: Locker): Unit

    // This is one later for geolocation of nearby lockers, just a dummy all-query now
    @Query("select * from locker")
    suspend fun lockersByLocation(): List<Locker>

    @Update(entity = Locker::class)
    suspend fun updateLockerFavourite(lockerFavourite: LockerFavourite)

    suspend fun updateLockerFavourite(lockerId: Long, favourite: Boolean) =
        updateLockerFavourite(LockerFavourite(lockerId, favourite))

    @Update(entity = Locker::class)
    suspend fun updateLockerHistoryCollapsed(lockerCollapsed: LockerHistoryCollapsed)

    suspend fun updateLockerHistoryCollapsed(lockerId: Long, collapsed: Boolean) =
        updateLockerHistoryCollapsed(LockerHistoryCollapsed(lockerId, collapsed))

    //--------------------------------------------------------------------------------------------
    // Locker dependents, return in time order, all can be restricted to only "most recent"
    // after a timestamp
    @Query("select * from bids where locker_id = :lockerId and created_at > :after order by created_at")
    suspend fun bidsForLocker(lockerId: Long, after: Instant = Instant.EPOCH): List<Bid>

    @Query("select * from bids where not completed")
    suspend fun pendingBids(): List<Bid>

    @Query("select * from bids where completed")
    suspend fun completedBids(): List<Bid>

    @Update(entity = Bid::class)
    suspend fun updateBidState(state: BidState)

    // this requires the rent to be WITHIN after-before range
    @Query("select * from rents where locker_id = :lockerId and start_time >= :after and end_time <= :before order by start_time")
    suspend fun rentsForLockerBetween(lockerId: Long, after: Instant = Instant.EPOCH, before: Instant = MILLENIA): List<Rent>

    // this requires the point in time to be within the rent period
    @Query("select * from rents where locker_id = :lockerId and start_time <= :time and end_time <= :time order by start_time")
    suspend fun rentsForLockerAt(lockerId: Long, time: Instant = Instant.now()): List<Rent>

    // overlaps for specific locker
    @Query("select * from rents where " +
            "locker_id = :lockerId and (" +
            // rent start is within the range
            "(start_time >= :start and start_time <= :end) or" +
            // rent end is within the range
            "(end_time >= :start and end_time <= :end) or " +
            // rent starts before range and ends after range (i.e. range is within the rent period)
            "(start_time <= :start and end_time >= :end) " +
            ")"
            + "order by start_time"
    )
    suspend fun rentsForLockerOver(lockerId: Long, start: Instant = Instant.now(), end: Instant = MILLENIA): List<Rent>

    @Query("select * from activities where locker_id = :lockerId and time >= :after order by time desc limit :limit")
    suspend fun activitiesForLocker(lockerId: Long, after: Instant = Instant.EPOCH, limit: Int = 1000): List<LockerActivity>

    @Insert
    suspend fun insertBid(bid: Bid): Long

    // bids can be updated when their result is known
    @Update
    suspend fun updateBid(bid: Bid): Unit

    // user may want to clean out failed bids from record
    @Delete
    suspend fun deleteBid(bid: Bid): Unit

    // Rents overall
    @Query("select * from rents where start_time > :after and end_time < :before order by start_time")
    suspend fun rentsBetween(after: Instant = Instant.EPOCH, before: Instant = MILLENIA): List<Rent>

    // this requires the rent period to OVERLAP the given interval
    @Query("select * from rents where " +
            // rent start is within the range
            "(start_time >= :start and start_time <= :end) or" +
            // rent end is within the range
            "(end_time >= :start and end_time <= :end) or " +
            // rent starts before range and ends after range (i.e. range is within the rent period)
            "(start_time <= :start and end_time >= :end) "
            + "order by start_time"
    )
    suspend fun rentsOver(start: Instant = Instant.now(), end: Instant = MILLENIA): List<Rent>


    @Insert
    suspend fun insertRent(rent: Rent): Long

    // this is only used for debugging
    @Delete
    suspend fun deleteRent(rent: Rent): Unit

    @Insert
    suspend fun insertActivity(activity: LockerActivity): Long

    // activities are more or less permanent record, but old entries may be purged
    @Delete
    suspend fun deleteActivity(activity: LockerActivity): Unit

    //--------------------------------------------------------------------------------------------
    // Marketplace
    @Query("select * from marketplace")
    suspend fun allMarketplaces(): List<Marketplace>

    @Query("select * from marketplace where id = :marketplaceId")
    suspend fun marketplaceById(marketplaceId: Long): Marketplace?

    @Query("select * from marketplace where ledger = :ledger and address = :contractAddress")
    suspend fun marketplaceByContract(ledger: LedgerIdentifier, contractAddress: String): Marketplace?

    @Insert
    suspend fun insertMarketplaces(vararg marketplaces: Marketplace): List<Long>

    @Update
    suspend fun updateMarketplaces(vararg marketplaces: Marketplace): Unit


}

@Dao
interface LiveAggregateDao {
}