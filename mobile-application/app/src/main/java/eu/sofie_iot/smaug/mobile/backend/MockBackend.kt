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

package eu.sofie_iot.smaug.mobile.backend

import android.util.Log
import eu.sofie_iot.smaug.mobile.debug.DebugActions
import eu.sofie_iot.smaug.nfc.messages.toHex
import okhttp3.ResponseBody
import retrofit2.Response
import kotlin.random.Random


object RandomBackendLockerFactory {
    fun newLocker(id: String? = null, iid: String? = null): BackendLockerModel {
        return BackendLockerModel(
            id = Random.nextLong(),
            iid = iid ?: (byteArrayOf(0x00.toByte()) + Random.nextBytes(5)).toHex(),
            uuid = id ?: "bcn-" + DebugActions.randomLockerId(),
            marketPlaceId = 1,
            name = randomName(),
            description = randomDescription(),
            imageUrls = randomImageUrls())
    }

    private val lockerNamePrefixes = arrayOf(
        "small",
        "large",
        "stainless",
        "cardboard",
        "wooden",
        "biggly"
    )

    private val lockerNameSuffixes = arrayOf(
        "locker",
        "box",
        "crate",
        "container",
        "chest"
    )

    fun randomName(): String =
        (lockerNamePrefixes.random() + " " + lockerNameSuffixes.random()).capitalize()

    private val descriptionParts = listOf(
        listOf("Ground floor", "1st floor", "Cafeteria", "Auditorium entrance"),
        listOf(", "),
        listOf("main building", "undergraduate center", "dormitory"),
        listOf(", "),
        listOf("Otakaari", "Servin Mökin Tie", "Maarintie"),
        listOf(" "),
        listOf("1", "2", "6", "8", "10")
    )

    fun randomDescription(): String =
        descriptionParts.map { it.random() }.joinToString("")

    private val imageUrlParts = listOf(
        // at least one locker image
        listOf(
            "daniel-tuttle-2Yuqu4Ljih8-unsplash.jpg",
            "eric-zhu-yIAmkXbsRf8-unsplash.jpg",
            "jan-bottinger-XSVH2n2ek6Y-unsplash.jpg",
            "jason-zhang-0O2KIm_AKP0-unsplash.jpg",
            "kelli-mcclintock-GopRYASfsOc-unsplash.jpg",
            "lena-de-fanti-W4HtdHYqmUg-unsplash.jpg",
            "nikko-osaka-WzZjyThDoR8-unsplash.jpg").map { "locker/$it"},

        // zero or one interior shots
        listOf(
            "roberto-nickson-tleCJiDOri0-unsplash.jpg",
            "nrd-c3tNiAb098I-unsplash.jpg",
            "nastuh-abootalebi-yWwob8kwOCk-unsplash.jpg",
            "mikhail-derecha-q-XTB-YTsho-unsplash.jpg",
            "lycs-architecture-aKij95Mmus8-unsplash.jpg",
            "jean-philippe-delberghe-Gdufctyvzjo-unsplash.jpg",
            "dan-gold-opIZa6gWsFs-unsplash.jpg",
            "benjamin-child-0sT9YhNgSEs-unsplash.jpg"
        ).map { it?.let { "interior/$it" } },

        // zero or one exterior shots
        listOf(
            "vadim-sherbakov-d6ebY-faOO0-unsplash.jpg",
            "luke-stackpoole-eWqOgJ-lfiI-unsplash.jpg",
            "jorge-fernandez-salas-_FbFFfWfZ3E-unsplash.jpg",
            "jezael-melgoza-JRm9Fj7PES8-unsplash.jpg",
            "hardik-pandya-HjzL2rJyGW4-unsplash.jpg",
            "erika-fletcher-MZxqc6n9qCw-unsplash.jpg",
            "erik-mclean-IFH9YtTLuAM-unsplash.jpg",
            "eric-sharp-JdzHrfX4l4Q-unsplash.jpg",
            "azlan-baharudin-n4l7fXjhAOQ-unsplash.jpg",
            "aubrey-odom-hI8Kdw0CkF0-unsplash.jpg"
        ).map { it?.let { "exterior/$it" } }
    )

    private val urlPrefix = "https://smaug-marketplace-b8c7153ef9389a7cd65d.s3.eu-central-1.amazonaws.com/images/"

    fun randomImageUrls(prefix: String = urlPrefix): List<String> =
        imageUrlParts.map { it.random() }.filterNotNull().map { "$prefix$it"}

}

class MockBackend(val marketplace: BackendMarketplaceModel, lockers: List<BackendLockerModel> = emptyList()) {
    private val TAG = "MockBackend"

    val lockers = lockers.toMutableList()

    fun addLocker(locker: BackendLockerModel): Unit {
        lockers.add(locker)
    }

    fun maybeNewLocker(id: String?, iid: String?): BackendLockerModel? {
        Log.d(TAG, "maybeNewLocker: id $id iid $iid")

        val model = when {
            id != null && id.startsWith("tap-") -> RandomBackendLockerFactory.newLocker(id, iid)
            iid != null && iid.startsWith("ffff") -> RandomBackendLockerFactory.newLocker(id, iid)
            else -> null
        }

        // make sure it is stored in case we retrieve it later, like during model refresh
        if (model != null)
            lockers.add(model)

        return null
    }

    fun apiClient(): ApiClient {
        fun <T> result(obj: T?): Response<T> =
            if (obj == null)
                Response.error(404, ResponseBody.create(null, byteArrayOf()))
            else
                Response.success(obj)

        return object : ApiClient {
            override suspend fun getLockerByInstanceId(iid: String): Response<BackendLockerModel> =
                result(maybeNewLocker(null, iid) ?: lockers.find { it.iid == iid })

            override suspend fun getLockerByUniqueId(uuid: String): Response<BackendLockerModel> =
                result(maybeNewLocker(uuid, null) ?: lockers.find { it.uuid == uuid })

            override suspend fun getLockerById(id: Long): Response<BackendLockerModel> =
                result(lockers.find { it.id == id })

            override suspend fun getMarketplace(): Response<BackendMarketplaceModel> =
                result(marketplace)
        }
    }
}