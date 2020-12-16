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

//import android.content.Context
//import android.util.Log
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import androidx.room.Room
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.espresso.matcher.ViewMatchers.assertThat
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.platform.app.InstrumentationRegistry
//import eu.sofie_iot.smaug.mobile.model.*
//import org.hamcrest.CoreMatchers.*
//import org.junit.After
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.time.Instant
//import java.util.*

//
//@RunWith(AndroidJUnit4::class)
//class SimpleModelsTest {
//
//    @get:Rule
//    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var dao: ModelDao
//    private lateinit var db: Database
//
//    @Before
//    fun createDb() {
//        val context = ApplicationProvider.getApplicationContext<Context>()
//        db = Room.inMemoryDatabaseBuilder(
//            InstrumentationRegistry.getInstrumentation().context, Database::class.java
//        )
//          //  .allowMainThreadQueries()
//            .build()
//        dao = db.models
//
//        Log.i("SimpleModelsTest", "Before: db=$db dao=$dao")
//    }
//
//    @After
//    fun closeDb() {
//        db.close()
//    }
//
//    suspend fun insertMarketplace(
//        id: Long = 0,
//        ledger: LedgerIdentifier = LedgerIdentifier.ETHEREUM_PRIVATE,
//        addr: String = "1234567890"
//    ): Marketplace {
//        val mp = Marketplace(id, SmartContract(ledger, addr))
//
//        return mp.copy(id = dao.insertMarketplaces(mp)[0])
//    }
//
//    var lockerId: Long = 1000
//    fun nextLockerId(): String {
//        lockerId += 1
//        return "locker-$lockerId"
//    }
//
//    suspend fun insertLocker(
//        id: Long = 0,
//        lockerId: String = nextLockerId(),
//        marketplaceId: Long = 1,
//        name: String = "Beef Locker v1"
//    ): Locker {
//        val l = Locker(id = id, uniqueId = lockerId, marketplaceId = marketplaceId, name = name)
//        return l.copy(id = dao.insertLocker(l))
//    }
//
//    val startDate: Instant = Date().toInstant()
//    var tick: Long = 0
//
////    suspend fun insertActivity(
////        id: Long = 0,
////        lockerId: Long,
////        type: ActivityType,
////        time: Instant? = null
////    ): ActivityRecord {
////        // want to ensure monotonic timestamps if multiple added sequentially
////        val timestamp = if (time != null) time else {
////            tick += 1
////            startDate.plusSeconds(tick)
////        }
////        val a = ActivityRecord(id = id, lockerId = lockerId, type = type, time = timestamp)
////        return a.copy(id = dao.insertActivities(a)[0])
////    }
////
////    @Test
////    suspend fun testMarketplace() {
////        val mp: Marketplace = insertMarketplace()
////
////        val all = dao.loadAllMarketplaces()
////
////        assertThat(all.size, equalTo(1))
////        assertThat(all.get(0), equalTo(mp))
////
////        val mp2 = dao.loadMarketplace(LedgerIdentifier.ETHEREUM_DEFAULT, mp.contract.address)
////        assertThat(mp2, `is`(nullValue()))
////
////        val mp3 = dao.loadMarketplace(LedgerIdentifier.ETHEREUM_PRIVATE, mp.contract.address)
////        assertThat(mp3, equalTo(mp))
////    }
////
////    @Test
////    suspend fun testLocker() {
////        val mp = insertMarketplace(id = 1)
////        val l1 = insertLocker(id = 1, marketplaceId = mp.id)
////        val l2 = insertLocker(id = 10, marketplaceId = mp.id)
////
////        val all = LiveDataTestUtil.getValue(dao.loadAllLockers())!!.sortedBy { it.id }
////        assertThat(all.size, equalTo(2))
////        assertThat(all.get(0), equalTo(l1))
////        assertThat(all.get(1), equalTo(l2))
////
////        val ll1 = dao.loadLocker(mp.id, l1.uniqueId)
////        assertThat(ll1, equalTo(l1))
////
////        val ll2 = dao.loadLocker(mp.id, l2.uniqueId)
////        assertThat(ll2, equalTo(l2))
////
////        assertThat(dao.loadLocker(2, l1.uniqueId), `is`(nullValue()))
////        assertThat(dao.loadLocker(2, l2.uniqueId), `is`(nullValue()))
////    }
////
////    @Test
////    suspend fun testLockerActivity() {
////        val mp = insertMarketplace()
////        val l1 = insertLocker(marketplaceId = mp.id)
////        val a1 = insertActivity(lockerId = l1.id, type = ActivityType.PENDING)
////        val a2 = insertActivity(lockerId = l1.id, type = ActivityType.RENT)
////        val a3 = insertActivity(lockerId = l1.id, type = ActivityType.OPENED)
////
////        val l2 = insertLocker(marketplaceId = mp.id)
////        val a4 = insertActivity(lockerId = l2.id, type = ActivityType.PENDING)
////
////        val all = LiveDataTestUtil.getValue(dao.loadLockerActivity(l1.id))!!
////        assertThat(all.size, equalTo(3))
////        assertThat(all, equalTo(listOf(a3, a2, a1)))
////
////        val pending = LiveDataTestUtil.getValue(dao.loadPendingActivity())!!
////        assertThat(pending, equalTo(listOf(a4, a1)))
////    }
//
//    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
//    suspend fun testMarketplaceUniqueness() {
//        val mp = insertMarketplace(ledger = LedgerIdentifier.ETHEREUM_PRIVATE, addr = "abc")
//        insertMarketplace(ledger = LedgerIdentifier.ETHEREUM_PRIVATE, addr = "abc")
//    }
//
//    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
//    suspend fun testLockerUniqueness() {
//        val mp = insertMarketplace()
//        val l = insertLocker(marketplaceId = mp.id, lockerId = "locker1")
//        insertLocker(marketplaceId = mp.id, lockerId = "locker1")
//    }
//
//    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
//    suspend fun testLockerMarketplaceRelation() {
//        insertLocker(marketplaceId = 1)
//    }
//
////    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
////    suspend fun testActivityLockerRelation() {
////        insertActivity(lockerId = 1, type = ActivityType.PENDING)
////    }
//}
