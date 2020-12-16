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

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.pandulapeter.beagle.Beagle
import eu.sofie_iot.smaug.mobile.backend.*
import eu.sofie_iot.smaug.mobile.debug.DebugDatabase
import eu.sofie_iot.smaug.mobile.ledger.Ledger
import eu.sofie_iot.smaug.mobile.ledger.MockLedger
import eu.sofie_iot.smaug.mobile.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.time.Instant

class SmaugMobileApplication : Application() {
    private val TAG = "SmaugMobileApplication"

    // use var as we may change the endpoint at runtime
    var api: ApiClient? = null

    // db reference
    lateinit var db: Database

    // and the payment processor
    lateinit var paymentProcessor: PaymentProcessor

    // this is the default mock backend we're using
    private val mockBackend = MockBackend(
        Scaffolding.DEFAULT_BACKENDMARKETPLACE_MODEL,
        Scaffolding.DEFAULT_BACKENDLOCKER_MODELS)

    private val refreshes = Channel<Long>()

    override fun onCreate() {
        super.onCreate()
        initializeDatabase()

        // default is to use the mock backend -- to be FIXED as different ledgers have
        // different BEs...
        api = mockBackend.apiClient()
        // api = ApiClient.forUrl("http://10.1.50.179:61234/api/v1/")

        // set up the default mock ledger for the default marketplace
        paymentProcessor = PaymentProcessor(db)
        paymentProcessor.setMarketplaceLedger(Scaffolding.DEFAULT_MARKETPLACE_ID, MockLedger())
        paymentProcessor.start(GlobalScope)

        Beagle.initialize(this)

        // now that we have api and pp, let's refresh all backend information
        GlobalScope.launch {
            fullBackendRefresh()
            refreshWorker()
        }
    }

    private suspend fun refreshWorker() {
        while (true) {
            val id = refreshes.receive()
            val locker = db.models.lockerById(id)
            val before = Instant.now().minusSeconds(15)

            if (locker == null)
                continue

            Log.d(TAG, "refresh: checking locker id=${locker.id} mid=${locker.marketplaceId} uid=${locker.uniqueId} iid=${locker.instanceId}")

            if (locker.lastSeenBackend != null && locker.lastSeenBackend.isAfter(before)) {
                Log.d(TAG, "refresh: was updated within 15 seconds, not redoing ...")
                continue
            }

            val response = api?.let {
                when {
                    locker.marketId != null -> it.getLockerById(locker.marketId)
                    locker.uniqueId != null -> it.getLockerByUniqueId(locker.uniqueId)
                    locker.instanceId != null -> it.getLockerByInstanceId(locker.instanceId)
                    else -> null
                }
            }

            if (response == null || !response.isSuccessful) {
                Log.d(TAG, "refresh: no response from backend or not known")
                continue
            }

            val model = response.body()

            // if we have model, let's see if we should update anything
            val updatedLocker = model?.let {
                locker.copy(
                    uniqueId = it.uuid ?: locker.uniqueId,
                    instanceId = it.iid ?: locker.instanceId,
                    // important: locker.id is **OUR** database's id, not the one used by the
                    // marketplace
                    marketId = it.id ?: locker.id,
                    name = it.name ?: locker.name,
                    imageUrls = it.imageUrls ?: locker.imageUrls,
                    description = it.description ?: locker.description
                )
            }

            if (updatedLocker != null && locker != updatedLocker) {
                Log.d(TAG, "refresh: locker=$locker model=$model changed: $updatedLocker")
                db.models.updateLocker(updatedLocker)
            } else {
                Log.d(TAG, "refresh: nothing to update")
            }
        }
    }

    // this is occasionally called from elsewhere as well
    fun fullBackendRefresh() {
        runBlocking {
            for (locker in db.models.allLockers())
                refreshLocker(locker.id)
        }
    }

    fun refreshLocker(id: Long) = GlobalScope.launch {
        refreshes.send(id)
    }

    override fun onTerminate() {
        super.onTerminate()
        paymentProcessor?.stop()
    }


    private fun initializeDatabase() {
        Log.d(TAG, "Initializing database")

        // TODO: change to a flag or something to switch to a persistent database?

        // we're recreating database contents for each run, so can use inmemory as well
        db = Room.inMemoryDatabaseBuilder(applicationContext, Database::class.java).build()
        Log.d(TAG, "database: $db")

        // Note, the proper way would be to use createAsset during
        // creation (https://developer.android.com/training/data-storage/room/prepopulate)
        runBlocking {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Populating database")
                DebugDatabase.populateEmptyDatabase(db, resources)
            }
        }
    }
}
