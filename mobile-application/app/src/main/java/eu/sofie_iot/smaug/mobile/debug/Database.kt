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

package eu.sofie_iot.smaug.mobile.debug

import android.content.res.Resources
import android.util.Log
import eu.sofie_iot.smaug.mobile.Scaffolding
import eu.sofie_iot.smaug.mobile.backend.RandomBackendLockerFactory
import eu.sofie_iot.smaug.mobile.model.*
import eu.sofie_iot.smaug.mobile.model.Database
import java.time.Instant

@ExperimentalUnsignedTypes
object DebugDatabase {
    final val TAG = "DebugDatabase"
    suspend fun populateEmptyDatabase(db: Database, resources: Resources) {
        // TODO: only for development time
        db.clearAllTables()

        //val dao = db.myDao()
        val models = db.models

        if (models.allMarketplaces().isEmpty()) {
            Log.i(TAG, "Populating empty database")

            val marketplaces = Scaffolding.DEFAULT_MARKETPLACES
            val lockers = Scaffolding.DEFAULT_LOCKERS
            val bids = Scaffolding.DEFAULT_BIDS
            val rents = Scaffolding.DEFAULT_RENTS
            val activities = Scaffolding.DEFAULT_ACTIVITIES

            Log.i(TAG, "marketplaces=$marketplaces lockers=$lockers bids=$bids rents=$rents activities=$activities")

            for (marketplace in marketplaces)
                models.insertMarketplaces(marketplace)

            for (locker in lockers)
                models.insertLocker(locker)

            for (bid in bids)
                models.insertBid(bid)

            for (rent in rents)
                models.insertRent(rent)

            for (activity in activities)
                models.insertActivity(activity)
        }
    }
}
