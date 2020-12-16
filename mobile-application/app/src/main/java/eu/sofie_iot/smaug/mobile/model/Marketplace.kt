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


enum class LedgerIdentifier {
    /** Default is whatever the application is condfigured for */
    ETHEREUM_DEFAULT,
    ETHEREUM_HOMESTEAD,
    ETHEREUM_RINKEYBY,
    ETHEREUM_PRIVATE
}

data class SmartContract(
    @ColumnInfo(name = "ledger")
    val ledger: LedgerIdentifier,

    @ColumnInfo(name = "address")
    val address: String
)



@Entity(
    tableName = "marketplace",
    indices = arrayOf(Index(value = ["ledger", "address"], unique = true))
)
data class Marketplace(
    @PrimaryKey(autoGenerate = true) val id: Long,

    /** smart contract identifier for this locker */
    @Embedded val contract: SmartContract,

    /** fancy name used by the marketplace itself */
    @ColumnInfo
    val name: String? = null

    // potentially later things like protocol version etc.
)