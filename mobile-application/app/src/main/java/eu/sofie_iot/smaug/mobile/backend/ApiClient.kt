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

import androidx.annotation.Keep
import eu.sofie_iot.smaug.mobile.model.Locker
import eu.sofie_iot.smaug.mobile.model.LockerOpenCloseType
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiClient {
    // iid encoded as hex
    @GET("/api/v1/lockers/")
    suspend fun getLockerByInstanceId(@Query("iid") iid: String): Response<BackendLockerModel>

    // canonical form of locker access
    @GET("/api/v1/lockers/")
    suspend fun getLockerByUniqueId(@Query("uuid") uuid: String): Response<BackendLockerModel>

    @GET("/api/v1/lockers/{id}")
    suspend fun getLockerById(@Path("id") id: Long): Response<BackendLockerModel>

    @GET("/api/marketplace/")
    suspend fun getMarketplace(): Response<BackendMarketplaceModel>

    companion object {
        fun forUrl(url: String): ApiClient =
            Retrofit.Builder()
                .baseUrl(url)
                .client(OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiClient::class.java)
    }
}

@Keep
data class BackendMarketplaceModel(
    val id: Long,
    val ledger: String? = null,
    val address: String? = null,
    val name: String? = null
)

@Keep
data class BackendLockerModel(
    val id: Long,
    val iid: String? = null,
    val uuid: String? = null,
    val marketPlaceId: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val imageUrls: List<String>? = null) // null != empty list, null = "no known value", empty list = no image urls
{
    fun toLocker(marketplaceId: Long): Locker =
        Locker(
            marketplaceId = marketplaceId,
            uniqueId = uuid,
            instanceId = iid,
            name = name ?: "<missing name>",
            description = description,
            imageUrls = imageUrls,
            // TODO: fixme in model
            type = LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH)
}

