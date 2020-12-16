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

import eu.sofie_iot.smaug.mobile.backend.BackendLockerModel
import eu.sofie_iot.smaug.mobile.backend.BackendMarketplaceModel
import eu.sofie_iot.smaug.mobile.backend.RandomBackendLockerFactory
import eu.sofie_iot.smaug.mobile.model.*
import java.time.Instant

object Scaffolding {
    val DEFAULT_MARKETPLACE_ID = 1L

    val DEFAULT_MARKETPLACES = listOf(
        Marketplace(
            id = DEFAULT_MARKETPLACE_ID,
            name = "Sample marketplace,",
            // TODO: nsid
            // TODO: backend URL
            contract = SmartContract(LedgerIdentifier.ETHEREUM_DEFAULT, "dummy-address")
        )
    )

    // use activity start labels sometime in the past
    private val now = Instant.now()
    private val ago = now.minusSeconds(3600)

    val DEFAULT_LOCKERS = listOf(
        Locker(
            id = 1,
            uniqueId = "locker-1",
            marketplaceId = 1,
            name = "Small locker with mobile charger",
            description = "1st floor, OIH, Maarintie 6, Otaniemi\n\nMicro-USB, USB-C and Lightning charger cables available.",
            favourite = true,
            type = LockerOpenCloseType.TAP_OPEN_TAP_CLOSE,
            lastSeenBackend = ago,
            lastSeenBeacon = ago,
            // image = a2ba("chest.jpg")
            imageUrls = RandomBackendLockerFactory.randomImageUrls()
        ),
        // note: locker-2 has more info in mock BE for the purpose of validating
        // be refresh functionality, i.e. if name or description are updated visibly
        Locker(
            id = 2,
            uniqueId = "locker-2",
            marketplaceId = 1,
            name = "Locker",
            // description = "Smaller locker in entry hallway at the main building, Otakaari 1, Otaniemi",
            type = LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH_DELAY,
            lastSeenNfc = now,
            favourite = false,
            // image = a2ba("cs_building.jpg")
            imageUrls = RandomBackendLockerFactory.randomImageUrls()
        ),
        Locker(
            id = 3,
            uniqueId = "locker-3",
            marketplaceId = 1,
            name = "Large locker",
            description = "Larger locker in entry hallway at the main building, Otakaari 1, Otaniemi",
            type = LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH,
            favourite = false,
            lastSeenBeacon = ago,
            lastSeenNfc = ago,
            lastSeenBackend = ago,
            // image = a2ba("cs_building.jpg")
            imageUrls = RandomBackendLockerFactory.randomImageUrls()
        ),
//                Locker(
//                    id = 4,
//                    uniqueId = "locker-4",
//                    marketplaceId = 1,
//                    name = "Locker example #4",
//                    type = LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH,
//                    favourite = false,
//                    // image = a2ba("chest.jpg")
//                    imageUrls = RandomBackendLockerFactory.randomImageUrls()
//                )
//                Locker(
//                    id = 5,
//                    uniqueId = "locker-5",
//                    marketplaceId = 1,
//                    name = "Locker example #5",
//                    type = LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH,
//                    favourite = false,
//                    image = a2ba("chest.jpg")
//                )
//                Locker(
//                    id = 6,
//                    uniqueId = "locker-6",
//                    marketplaceId = 1,
//                    name = "Locker example #6",
//                    type = LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH,
//                    favourite = false,
//                    image = a2ba("chest.jpg")
//                ),
//                Locker(
//                    id = 7,
//                    uniqueId = "locker-7",
//                    marketplaceId = 1,
//                    name = "Locker example #7",
//                    type = LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH,
//                    favourite = false,
//                    image = a2ba("chest.jpg")
//                )
    )

    // bids: locker 4 has one outstanding bid
    val DEFAULT_BIDS = emptyList<Bid>()
    //            val bids = listOf(
//                Bid(
//                    id = 1,
//                    lockerId = 4,
//                    transactionId = "M4-001",
//                    transactionBlock = 5,
//                    state = 10,
//                    completed = false
//                )
//            )
    // locker 1 rented now, locker 2 was rented in past, locker 3 has two rents in
    // the future, locker 4 none
    val DEFAULT_RENTS = listOf(
        Rent(
            lockerId = 1,
            start = now.minusSeconds(1800),
            end = now.plusSeconds(1800)
        ),
        Rent(
            lockerId = 2,
            start = now.minusSeconds(3600),
            end = now.minusSeconds(300)
        ),
        Rent(
            lockerId = 3,
            start = now.plusSeconds(25 * 3600 + 300),
            end = now.plusSeconds(25 * 3600 + 800)
        ),
        Rent(
            lockerId = 3,
            start = now.plusSeconds(32 * 3600),
            end = now.plusSeconds(35 * 3600)
        )
    )

    // locker 1 has been queried only, locker 2 has been opened and closed in the past,
    // no activities for locker 3, locker 4 none
    val DEFAULT_ACTIVITIES = listOf(
        LockerActivity(
            lockerId = 1,
            type = LockerActivity.ACTIVITY_QUERY,
            time = now.minusSeconds(0),
            state = LockerActivity.STATE_CLOSED
        ),
        LockerActivity(
            lockerId = 2,
            type = LockerActivity.ACTIVITY_OPEN,
            time = now.minusSeconds(1800),
            state = LockerActivity.STATE_OPEN
        ),
        LockerActivity(
            lockerId = 2,
            type = LockerActivity.ACTIVITY_CLOSE,
            time = now.minusSeconds(900),
            state = LockerActivity.STATE_OPEN
        ),
        LockerActivity(
            lockerId = 2,
            type = LockerActivity.ACTIVITY_OPEN,
            time = now.minusSeconds(800),
            state = LockerActivity.STATE_OPEN
        ),
        LockerActivity(
            lockerId = 2,
            type = LockerActivity.ACTIVITY_QUERY,
            time = now.minusSeconds(700),
            state = LockerActivity.STATE_OPEN
        )
    )

    val DEFAULT_BACKENDLOCKER_MODELS = listOf<BackendLockerModel>(
        // this is matched to locker-2 in prepopulated, which lacks some data, but is available from here
        BackendLockerModel(1, "0000deadbeef", "locker-2", 1, "Medium locker",
            "Smaller locker in entry hallway at the main building, Otakaari 1, Otaniemi"),
        BackendLockerModel(2, "000102030405", "slartibartfast", 1, "Sinihampaan arkku"),

        // demo lockers
        BackendLockerModel(3, "ff0000000002", "smaug-2", 1,
            "Medium-sized locker with casters",
            "Near the central plaza, Ericsson office in Jorvas, Finland.",
            listOf(
                // "https://smaug-marketplace-b8c7153ef9389a7cd65d.s3.eu-central-1.amazonaws.com/images/locker/nikko-osaka-WzZjyThDoR8-unsplash.jpg",
                "https://smaug-marketplace-b8c7153ef9389a7cd65d.s3.eu-central-1.amazonaws.com/images/locker/demo_locker.jpg",
                "https://smaug-marketplace-b8c7153ef9389a7cd65d.s3.eu-central-1.amazonaws.com/images/exterior/ericsson_jorvas.jpeg"
            )),
        BackendLockerModel(4, "ff0000000003", "smaug-3", 1, "Hidey hidey locker",
            "You gotta find me first! Very small, almost nonexistent storage space located very near you.",
            // ensure we always have the same set of images
            listOf("https://smaug-marketplace-b8c7153ef9389a7cd65d.s3.eu-central-1.amazonaws.com/images/locker/eric-zhu-yIAmkXbsRf8-unsplash.jpg",
                "https://smaug-marketplace-b8c7153ef9389a7cd65d.s3.eu-central-1.amazonaws.com/images/interior/nastuh-abootalebi-yWwob8kwOCk-unsplash.jpg",
                "https://smaug-marketplace-b8c7153ef9389a7cd65d.s3.eu-central-1.amazonaws.com/images/exterior/erika-fletcher-MZxqc6n9qCw-unsplash.jpg"))
    )

    val DEFAULT_MARKETPLACE_CONTRACT_ADDRESS = "0xbcaAFEEA5F90d310f7B284c8348412DDc02C267b"

    val DEFAULT_PRIVATE_KEY = "0xc4257e64796367d27b62d727f8d4b5ab7e42ee220a238e984f04b3c13d1bcf9f"

    val DEFAULT_BACKENDMARKETPLACE_MODEL = BackendMarketplaceModel(
        id = DEFAULT_MARKETPLACE_ID,
        ledger = "ethereum",
        address = DEFAULT_MARKETPLACE_CONTRACT_ADDRESS,
        name = "Default marketplace"
    )
}