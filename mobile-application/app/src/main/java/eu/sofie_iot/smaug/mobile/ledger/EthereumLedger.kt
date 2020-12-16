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

package eu.sofie_iot.smaug.mobile.ledger

import android.util.Log
import eu.sofie_iot.smaug.mobile.model.Bid
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.Web3ClientVersion
import org.web3j.protocol.http.HttpService
import kotlinx.coroutines.launch
import org.web3j.crypto.Credentials
import org.web3j.tx.gas.DefaultGasProvider

// note that the ledger interface is expected to be stateless, all input parameters contain
// all state that is necessary, it's the PaymentProcessor which will handle actual state
// storage, and will do a either sendBid or getTransaction when it needs info on a
// particular bid/tx.
class EthereumLedger(url: String, val contractAddress: String, val privateKey: String,) : Ledger {
    private val TAG = "EthereumLedger"
    val web3 = Web3j.build(HttpService(url))
    var clientVersion: String? = null

    // we will always start with validating the ledger contract, and will set up a
    // deferred checker for that, so one should always wait for the result first
    val ready = CompletableDeferred<Boolean>()

    val credentials = Credentials.create(privateKey)

    val contract = SMAUGMarketPlaceABI.load(contractAddress, web3, credentials, DefaultGasProvider())

    init {
        Log.d(TAG, "init: ready=$ready credentials=$credentials contract=$contract")

        GlobalScope.launch {
            web3.web3ClientVersion().flowable().subscribe(
                {
                    Log.d(TAG, "version: ${it.web3ClientVersion}")
                    clientVersion = it.web3ClientVersion
                },
                { Log.e(TAG, "Failed retrieving client version", it) })

            web3.ethBlockNumber().flowable().subscribe(
                { Log.d(TAG, "block: ${it.blockNumber}") },
                { Log.e(TAG, "Failed retrieving client version", it) })

            contract.getMarketInformation()
                .flowable()
                .subscribe(
                    {
                        Log.d(TAG, "getMarketInformation: status=${it.component1()} owner=${it.component2()}")
                    },
                    {
                        Log.e(TAG, "getMarketInformation: got an error", it)
                        ready.complete(false)
                    })

            contract.getType()
                .flowable()
                .subscribe(
                    { Log.d(TAG, "getType: status=${it.component1()} type=${it.component2()}") },
                    { Log.e(TAG, "getType: got an error", it) })
        }
    }

    override suspend fun sendBid(bid: Bid): Transaction {
        TODO("Not yet implemented")
    }

    override suspend fun getTransaction(id: String): Transaction {
        TODO("Not yet implemented")
    }

    override suspend fun cancelTransaction(tx: Transaction): Transaction {
        TODO("Not yet implemented")
    }

    override suspend fun getCancelTransaction(id: String): Transaction {
        TODO("Not yet implemented")
    }
}