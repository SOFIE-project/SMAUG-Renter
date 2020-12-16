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

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Text
import com.pandulapeter.beagle.common.configuration.toText
import com.pandulapeter.beagle.common.contracts.BeagleListItemContract
import com.pandulapeter.beagle.modules.*
import eu.sofie_iot.smaug.mobile.backend.*
import eu.sofie_iot.smaug.mobile.ledger.EthereumLedger
import eu.sofie_iot.smaug.mobile.ledger.MockLedger
import eu.sofie_iot.smaug.mobile.model.LedgerIdentifier
import eu.sofie_iot.smaug.mobile.model.Locker
import eu.sofie_iot.smaug.mobile.model.LockerActivity
import eu.sofie_iot.smaug.mobile.model.LockerOpenCloseType
import eu.sofie_iot.smaug.mobile.ui.HomeFragmentDirections
import eu.sofie_iot.smaug.mobile.ui.LockerDetailsFragment
import eu.sofie_iot.smaug.mobile.ui.LockerIdentifier
import eu.sofie_iot.smaug.mobile.ui.OpenCloseFragment
import eu.sofie_iot.smaug.nfc.messages.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.apache.commons.math3.filter.DefaultMeasurementModel
import org.apache.commons.math3.filter.DefaultProcessModel
import org.apache.commons.math3.filter.KalmanFilter
import org.web3j.protocol.core.Ethereum
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import kotlin.reflect.KProperty

// TODO: lots of things that could be done with this, list below
//
// * use themes properly instead of hardcoding various stuff
// * pulsing/bobbing animation based on urgency (alert status)


class smaugApplication {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): SmaugMobileApplication {
        val app = when (thisRef) {
            is Fragment -> thisRef.requireActivity().application as SmaugMobileApplication
            is Activity -> thisRef.application as SmaugMobileApplication
            is AndroidViewModel -> thisRef.getApplication() as SmaugMobileApplication
            else -> TODO()
        }
        return app
    }
}

data class BackendUrlSelection(val seq: Int, val desc: String, val url: String): BeagleListItemContract {
    override val id = seq.toString()
    override val title: Text = desc.toText()
}

data class BackendMockSelection(val seq: Int, val desc: String, val be: MockBackend): BeagleListItemContract {
    override val id = seq.toString()
    override val title = desc.toText()
}

@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {
    companion object {
        val TAG = "MainActivity"
    }

    private var adapter: NfcAdapter? = null
    private var bt: BluetoothAdapter? = null
    private val app by smaugApplication()
    private val DEFAULT_JWT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpZCI6IjM2ZGNlNjBiMzg4YjA2NDUyNmI5MDJhOGRjMzIyM2NhNGMxMWFmNWYiLCJqdGkiOiIzNmRjZTYwYjM4OGIwNjQ1MjZiOTAyYThkYzMyMjNjYTRjMTFhZjVmIiwiaXNzIjoiTktHS3RjTndzc1RvUDVmN3Voc0VzNCIsImF1ZCI6InNvZmllLWlvdC5ldSIsInN1YiI6Im15ZGlkIiwiZXhwIjoxNTgxMzQyNDE4LCJpYXQiOjE1ODEzMzg4MTgsInRva2VuX3R5cGUiOiJiZWFyZXIiLCJzY29wZSI6bnVsbH0.XSyQTgTt1WByT46NJLwrlcU3BUXzWf4MDZE3M4bLAh3HwFAwD6Dhi1IVeLAxNscc0bCgS-3KgyD1fdtiiJH7WktQIc269OLNxhnaXun_LxEYrWQCRHIFb0Je8Eg6CvdOB3shrlNZHmVELe6gaU0tQJ0-cdBbuz0udq_Mou1WLEwe6vp3mfgLiuTe2pT4wVI2PldvmUujeH6IpEop1nESYVA06pK6nV08d1RW7c_sRPgJdpSGGv-QhRcxBjDowkUs9J0OaTtGlExKhMv_17P96EskyOqCHku6RyydFccYbd5tl-Wh-9MqI4Me8z3BBSKPiIvQ2mo5OMcBmI0WwXb6jw"
    private var accessToken: String = DEFAULT_JWT_TOKEN

    // the uuid is magic uuid that matches Eddystone-UID frames
    private val serviceUid = ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb")

    // the SMAUG Eddystone-UID namespace ID
    private val smaugNID = listOf(0xb8, 0xc7, 0x15, 0x3e, 0xf9, 0x38, 0x9a, 0x7c, 0xd6, 0x5d).map { it.toByte()}.toByteArray()

    // in-memory RSSI kalman filters for macs -- we store it in mainactivity and not application
    // it is acceptable to lose this information
    private val rssiFilters = HashMap<String, KalmanFilter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(
            TAG,
            "onCreate: savedInstanceState=$savedInstanceState intent=$intent"
        )

        setContentView(R.layout.main_activity)

        val navController = findNavController(R.id.nav_host_fragment)
        val drawerLayout = findViewById<DrawerLayout>(R.id.main_layout)
        val appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        nav_view.getHeaderView(0).findViewById<MaterialTextView>(R.id.about_version)
            .text = BuildConfig.VERSION_NAME

        findViewById<NavigationView>(R.id.nav_view).setupWithNavController(navController)

        setSupportActionBar(toolbar)
        toolbar.setupWithNavController(
            navController,
            appBarConfiguration
        )

        // handle rest of the intent asynchronously, we're expecting only NDEF announces at this point
        lifecycleScope.launch {
            processIntent(intent)
        }

        debug_button.setOnClickListener {
            Log.d(TAG, "debug button clicked")
            Beagle.show()
        }

        data class AccessToken(val mode: Int, val description: String, val value: String): BeagleListItemContract {
            override val id = mode.toString()
            override val title = description.toText()
        }

        data class MockLedgerSelection(
            val index: Int,
            val description: String,
            val mode: MockLedger.Mode,
            val setupDelay: Long,
            val progressDelay: Long,
            val transactionDelay: Long
        ): BeagleListItemContract {
            override val id = index.toString()
            override val title = description.toText()
        }

        data class EthereumLedgerSelection(
            val index: Int,
            val description: String,
            val url: String,
            val contract: String,
            val privateKey: String
        ): BeagleListItemContract {
            override val id = index.toString()
            override val title = description.toText()
        }

        Beagle.set(
            PaddingModule(),
            HeaderModule(title = "Debug menu"),
            TextModule("Global", TextModule.Type.SECTION_HEADER),
            TextModule("Demo page", TextModule.Type.BUTTON, onItemSelected = {
                findNavController(R.id.nav_host_fragment).navigate(R.id.demoFragment)
            }),
            SingleSelectionListModule(title = "Access token", items = listOf(
                AccessToken(0, "Prebaked JWT token", DEFAULT_JWT_TOKEN),
                AccessToken(1, "Mock token: State", "10state;9999"),
                AccessToken(2, "Mock token: State & Open", "1;state,unlock;9999"),
                AccessToken(3, "Mock token: All", "1;all;9999"),
                AccessToken(4, "Mock token: Invalid", "0;;9999")
            ),
                initiallySelectedItemId = "3",
                onSelectionChanged = {
                    Log.d(TAG, "debug access token selection: $it")
                    accessToken = it?.value ?: DEFAULT_JWT_TOKEN
                }),
            DividerModule(),
            SingleSelectionListModule(title = "Backend", items = listOf(
                BackendUrlSelection(0, "10.1.50.179", "http://10.1.50.179:61234/api/v1/"),
                BackendMockSelection(1, "mock", MockBackend(Scaffolding.DEFAULT_BACKENDMARKETPLACE_MODEL, Scaffolding.DEFAULT_BACKENDLOCKER_MODELS))
            ),
                initiallySelectedItemId = "1",
                onSelectionChanged = {
                    app.api = when (it) {
                        is BackendUrlSelection -> ApiClient.forUrl(it.url)
                        is BackendMockSelection -> it.be.apiClient()
                        else -> TODO("This should never occur")
                    }
                    // initiate full refresh on BE change
                    app.fullBackendRefresh()
                }
            ),
            DividerModule(),
            SingleSelectionListModule(
                title = "Ledger selection",
                items = listOf(
                    MockLedgerSelection(
                        0,
                        "No delay (mock)",
                        MockLedger.Mode.MODE_ACCEPT,
                        0L,
                        0L,
                        0L
                    ),
                    MockLedgerSelection(
                        1,
                        "Normal delays (mock)",
                        MockLedger.Mode.MODE_ACCEPT,
                        5000L,
                        7000L,
                        15000L
                    ),
                    MockLedgerSelection(
                        2,
                        "Reject no delay (mock)",
                        MockLedger.Mode.MODE_REJECT,
                        5000L,
                        7000L,
                        15000L
                    ),
                    MockLedgerSelection(
                        3,
                        "Reject normal delays (mock)",
                        MockLedger.Mode.MODE_ACCEPT,
                        0L,
                        0L,
                        0L
                    ),
                    EthereumLedgerSelection(4,
                        "10.1.50.179",
                        "http://10.1.50.179:8545",
                        Scaffolding.DEFAULT_MARKETPLACE_CONTRACT_ADDRESS,
                        Scaffolding.DEFAULT_PRIVATE_KEY)
                ),
                initiallySelectedItemId = "1",
                onSelectionChanged = {
                    when (it) {
                        is MockLedgerSelection ->
                            app.paymentProcessor.setMarketplaceLedger(
                                Scaffolding.DEFAULT_MARKETPLACE_ID,
                                MockLedger(
                                    mode = it.mode,
                                    setupDelay = it.setupDelay,
                                    progressDelay = it.progressDelay,
                                    transactionDelay = it.transactionDelay
                                ))

                        is EthereumLedgerSelection ->
                            app.paymentProcessor.setMarketplaceLedger(
                                Scaffolding.DEFAULT_MARKETPLACE_ID,
                                EthereumLedger(it.url, it.contract, it.privateKey))
                    }
                })
        )


        adapter = NfcAdapter.getDefaultAdapter(this)

        if (adapter == null) {
            Log.d(TAG, "No NFC adapter on this device -- emulator?")
            Toast.makeText(
                applicationContext,
                "No NFC reader detected on this device",
                Toast.LENGTH_LONG
            ).show()
        }

        bt = BluetoothAdapter.getDefaultAdapter()

        if (bt == null) {
            Log.d(TAG, "No BT adapter on this device -- cannot scan for beacons")
            Toast.makeText(
                applicationContext, "No Bluetooth detected, cannot scan for lockers",
                Toast.LENGTH_LONG
            ).show()
        } else if (!bt!!.isEnabled) {
            Log.d(TAG, "BT is not enabled")
            // TODO: don't force BT to be enabled, instead give user info that BT not enabled
            // and BT discovery is not operable (UI hint)
        }

        // TODO: check if BLE is supported or not (packagemanager missingFeature check)

        permissionsBuilder(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        ).build().send { result ->
            Log.d(TAG, "permissions: $result")
        }

        Log.d(TAG, "app.paymentProcessor=${app.paymentProcessor}")

        // FIXME: this is wrong, we may have multiple payment processors for different ledgers ...
        // or just one with multiple ledgers? Dunno... needs to be refactored
        // react to completed bids
        app.paymentProcessor.setCompletionHandler { bid ->
            // TODO: handle won bids vs. errored bids differently, allow opening of
            // the locker etc.
            Log.d(TAG, "completion for bid: $bid")
            val snack = Snackbar.make(
                inner_layout,
                "Bid ${bid.id} completed: ${bid.resultMessage ?: "N/A"}",
                Snackbar.LENGTH_LONG
            )
            snack.setAction(R.string.view, View.OnClickListener {
                Log.d(TAG, "Snackback bid click")

                nav_host_fragment.findNavController().popBackStack(R.id.homeFragment, false)
                nav_host_fragment.findNavController().navigate(
                    HomeFragmentDirections.actionLocker(
                        LockerIdentifier(bid.lockerId)
                    )
                )
            })
            snack.show()
        }
    }

    // The RSSI to distance estimation method we use is mostly based on https://stackoverflow.com/a/61653469,
    // but adapted to Commons Math library. No code has been directly copied.

    private val KALMAN_R = 0.125
    private val KALMAN_Q = 0.5
    private val kalmanProcessModel = DefaultProcessModel(
        // State transition matrix A
        arrayOf(doubleArrayOf(1.0)),
        // Control matrix B
        arrayOf(doubleArrayOf(0.0)),
        // Process noise Q
        arrayOf(doubleArrayOf(KALMAN_Q))
    )
    private val kalmanMeasurementModel = DefaultMeasurementModel(
        // measurement matrix H
        arrayOf(doubleArrayOf(1.0)),
        // measurement noise R
        arrayOf(doubleArrayOf(KALMAN_R))
    )

    val leScanner = object : ScanCallback() {
        fun handleRecord(result: ScanResult, record: ScanRecord) {
            Log.d(
                TAG,
                "handleRecord: result addr ${result.device.address} rssi ${result.rssi} uuids ${record.serviceUuids} bytes ${record.bytes.toHex()} (len ${record.bytes.size})"
            )
            val data = record.getServiceData(serviceUid)

            if (data == null || data.size < 18 || data[0] != 0.toByte()) {
                Log.d(TAG, "Invalid or missing data frame: ${data?.toHex()} (len ${data?.size})")
                return
            }

            val nid = data.slice(2..11).toByteArray()
            val iid = data.slice(12..17).toByteArray()

            Log.d(TAG, "handleRecord: Eddystone-UID record, nid=${nid.toHex()} iid=${iid.toHex()}")

            if (!nid.contentEquals(smaugNID)) {
                Log.d(TAG, "handleRecord: Not a SMAUG NID frame")
                return
            }

            Log.d(TAG, "Eddystone-UID SMAUG NID frame detected")


            // determine tx power, use result one if available, otherwise eddystone beacon one
            val txPower = when {
                result.txPower != ScanResult.TX_POWER_NOT_PRESENT -> result.txPower
                else -> data[1].toInt()
            }

            // perform RSSI evaluation, use kalman filter
            val filter = rssiFilters.get(result.device.address) ?: let {
                val filter = KalmanFilter(kalmanProcessModel, kalmanMeasurementModel)
                rssiFilters.put(result.device.address, filter)
                filter
            }

            Log.d(TAG, "filter=$filter")
            filter.correct(doubleArrayOf(result.rssi.toDouble()))

            val rssi = filter.stateEstimation[0]
            Log.d(
                TAG,
                "${result.device.address}: latest rssi=${result.rssi} rssi estimate=$rssi txpower=$txPower"
            )

            this@MainActivity.runOnUiThread { onBeaconDiscovered(iid, rssi, txPower) }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(TAG, "scanFailed: $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result == null)
                return

            Log.d(
                TAG,
                "scanResult: ${result.device.address}: rssi ${result.rssi} txpwr ${result.txPower}"
            )

            if (result.scanRecord != null)
                handleRecord(result, result.scanRecord!!)

        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            Log.d(TAG, "batchScanResults: $results")

            if (results == null)
                return

            for (result in results)
                if (result.scanRecord != null)
                    handleRecord(result, result.scanRecord!!)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: enabling reader mode (adapter=$adapter bt=$bt)")
        adapter?.enableReaderMode(
            this, this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )

        bt?.bluetoothLeScanner?.startScan(
            listOf(ScanFilter.Builder().setServiceUuid(serviceUid).build()),
            ScanSettings.Builder().build(),
            leScanner
        )
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: disabling reader mode")
        adapter?.disableReaderMode(this)
        bt?.bluetoothLeScanner?.stopScan(leScanner)
    }

    fun onBeaconDiscovered(iid: ByteArray, rssi: Double, txPower: Int) {
        Log.d(TAG, "Beacon discovered: ${iid.toHex()}")

        // ok start looking at what to do
        lifecycleScope.launch {
            handleBeacon(iid, rssi, txPower)
        }
    }

    // TODO same comment as with handleNfc, this should be a separate objcect
    suspend fun handleBeacon(iid: ByteArray, rssi: Double, txPower: Int) {
        val models = app.db.models
        val api = app.api

        if (api == null)
            return

        // make iid into conventional hex string, it is used instead of pure byte array hereafter
        val iidHex = iid.toHex()

        val now = Instant.now()

        // TODO: add temporary blacklisting for IIDs we are fetching
        val locker = when(val locker = models.lockerByIid(iidHex)) {
            null -> {
                Log.d(TAG, "No locker found for iid $iidHex, querying backend")
                val beLocker = api.getLockerByInstanceId(iidHex)
                Log.d(TAG, "Backend locker result: $beLocker")
                if (!beLocker.isSuccessful) {
                    // TODO: change blacklist to longer so we're not constantly querying same IID
                    Log.d(TAG, "Failed fetching locker info from backend")
                    return
                }

                val model = beLocker.body()!!

                Log.d(TAG, "got locker: $model")

                // maybe should check if we have this iid as another uuid already
                val id = models.insertLocker(model.toLocker(1).copy(
                    bleRssi = rssi,
                    bleTxPower = txPower,
                    lastSeenBeacon = now,
                    lastSeenBackend = now))

                models.lockerById(id)!!
            }
            else -> locker
        }

        Log.d(TAG, "handleBeacon: locker=$locker now=$now rssi=$rssi txPower=$txPower")

        // we will end up doing this every time after the first one, so checking "do we really
        // need to do this" is saving one db update, not worth it, just go for it instead always
        models.updateLocker(locker.copy(lastSeenBeacon = now, bleRssi = rssi, bleTxPower = txPower))
    }

    override fun onTagDiscovered(tag: Tag?) {
        Log.d(TAG, "Tag discovered: $tag")

        if (tag == null)
            return

        val nfc = SmaugNfc.get(tag) as? NfcInterface

        if (nfc == null) {
            Log.d(TAG, "This is not a SMAUG NFC tag")
            return
        }

        val activity = this

        lifecycleScope.launch {
            Log.d(TAG, "Handling NFC tag $nfc in coroutine")
            handleNfc(nfc)

            Log.d(TAG, "Nothing to do with $nfc, just chilling now ...")

            nfc.close()

            adapter?.disableReaderMode(activity)
            delay(1000L)
            Log.d(TAG, "finished waiting")
            adapter?.enableReaderMode(
                activity, activity,
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null
            )
        }
    }

    val SMAUG_AID = "eu.sofie-iot.smaug.locker.1".toByteArray()

    // This is a massive function, but it makes use of the fact that it is a suspend/coroutine
    // function and can block, if necessary. The logic here has many steps, but is linear in
    // nature --- it will start, and then complete, without loops. All the user input is known
    // when we detect an NFC tag, and we can run the interaction with the device to
    // completion.
    //
    // There are a few inputs that affect how the logic runs:
    //
    // - tap mode (default, open, close, query)
    // - bound locker (null or specific locker)
    //
    // The main category of steps can be split into five phases:
    //
    // a) handshake, i.e. send DF SELECT, expect Announce -- fetch locker or create if needed
    // b) decide whether to continue and/or to show the locker details (UI state change)
    // c) verify token
    // d) query state (if default or query mode)
    // e) open/close action (if not query mode)

    var tapAction: TapAction = TapAction.DEFAULT
    var tapLockerId: Long? = null

    // TODO: refactor this into a state management class that is instantiated with nfc, dao, etc.
    // that can be tested in isolation
    suspend fun handleNfc(nfc: NfcInterface) {
        Log.d(TAG, "handleNfc: this=$this nfc=$nfc tapLockerId=$tapLockerId tapAction=$tapAction")
                //val dao = db.myDao()
        val models = app.db.models
        val now = Instant.now()
        val msg = (nfc.select(SMAUG_AID) as? Announce)

        if (msg == null) {
            Log.d(TAG, "Failed to connect or received unexpected message from $nfc")
            return
        }

        // SELECT DF and expect Announce

        // fetch marketplace
        // FIXME: blindly uses default ledger
        val market = models.marketplaceByContract(
            LedgerIdentifier.ETHEREUM_DEFAULT,
            msg.contract_address
        )

        // if no marketplace, we don't do now much, maybe later we'll handle automatic marketplace
        // recognition and registration, but for now they must be pre-registered or manually
        // added
        if (market == null) {
            Toast.makeText(
                applicationContext,
                "Marketplace '${msg.contract_address}' not recognized",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // Check if we need to either create a new locker entry, or update the current one
        val locker = models.lockerByUuid(market.id, msg.locker_id).let {
            when {
                it == null -> {
                    Log.d(TAG, "no locker record for ${msg.locker_id}, creating")
                    val id = models.insertLocker(
                        Locker(
                            marketplaceId = market.id,
                            uniqueId = msg.locker_id,
                            name = msg.name,
                            type = when (msg.open_close_type) {
                                "open-tap-close" -> LockerOpenCloseType.TAP_OPEN_TAP_CLOSE
                                "open-push-close" -> LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH
                                "open-delay-push-close" -> LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH_DELAY
                                else -> LockerOpenCloseType.TAP_OPEN_TAP_CLOSE
                            },
                            lastSeenNfc = now
                        )
                    )

                    models.lockerById(id)!!
                }
                else -> it
            }
        }

        if (locker.lastSeenNfc == null || locker.lastSeenNfc.isBefore(now)) {
            models.updateLocker(locker.copy(lastSeenNfc = now))
        }

        Log.d(TAG, "Locker: $locker")

//        // FIXME: backend check should be asynchronous!
//        val api = app.api
//
//        if (api != null) {
//            val beLocker = api.getLockerByUniqueId(locker.uniqueId)
//
//            Log.d(TAG, "backend locker info for ${locker.uniqueId}: $beLocker")
//            // this is ugly, but works for now
//            if (beLocker.isSuccessful) {
//                Log.d(TAG, "backend locker response: ${beLocker.body()}")
//                beLocker.body()?.let {
//                    models.updateLocker(
//                        locker.copy(
//                            lastSeenBackend = now,
//                            instanceId = it.iid ?: locker.instanceId,
//                            name = it.name ?: locker.name,
//                            description = it.description ?: locker.description,
//                            imageUrls = it.imageUrls ?: locker.imageUrls
//                            // FIXME: others too
//                        )
//                    )
//                }
//            }
//        }

        // schedule an update
        app.refreshLocker(locker.id)

        // if we have a bound locker we want to tap, ignore any other locker
        if (tapLockerId != null && tapLockerId != locker.id) {
            Log.d(TAG, "have bound locker $tapLockerId, but tapped ${locker.id}, ignoring")
            Toast.makeText(
                applicationContext,
                "The tapped locker is not the one",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // if we are *not* bound, navigate at this point to the locker details page (if
        // we *are* bound that means we're in the locker open/close page and it is already
        // open, we don't want to change that)
        if (tapLockerId == null) {
            Log.d(TAG, "(maybe) navigating to locker $locker details page")

            val visible = nav_host_fragment.childFragmentManager.fragments.last().let {
                Log.d(TAG, "currently showing fragment $it")
                when (it) {
                    is LockerDetailsFragment -> it.lockerId == locker.id
                    is OpenCloseFragment -> it.lockerId == locker.id
                    else -> false
                }
            }

            if (!visible) {
                Log.d(TAG, "Not currently showing this locker, navigating to details")

                val nav = findNavController(nav_host_fragment.id)
                nav.popBackStack(R.id.homeFragment, false)
                nav.navigate(HomeFragmentDirections.actionLocker(LockerIdentifier(locker.id)))
            }
        }

        // do we have an active rent with the locker?
        if (models.rentsForLockerOver(locker.id, end = Instant.now()).size == 0) {
            Log.d(TAG, "No active rents for this locker, ignoring")
            return
        }

        Log.d(TAG, "Have active rent for locker, continuing to verification")

        val verify = let {
            // TODO: here we'll need the actual access token for successfull bid later on
            val response = nfc.communicate(Verify(token = accessToken))

            if (response == null || response !is VerifySuccess) {
                Log.d(TAG, "Failed to verify request")
                // FIXME: this should be recorded as communication error and shown in locker details
                Toast.makeText(
                    applicationContext,
                    "Failed authenticating to the locker",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            response
        }

        Log.d(TAG, "Verification succeeded: ${verify.message}")

        var activity: LockerActivity?

        fun state2num(state: String?, default: Int = LockerActivity.STATE_UNKNOWN) =
            when (state) {
                "open" -> LockerActivity.STATE_OPEN
                "closed" -> LockerActivity.STATE_CLOSED
                else -> default
            }

        // let's always query status before doing something else
        val queriedState = let {
            val response = nfc.communicate(Query())

            if (response is QueryError && response.message.length > 0) {
                Log.d(TAG, "Query error: ${response.message}")

                models.insertActivity(
                    LockerActivity(
                        lockerId = locker.id,
                        type = LockerActivity.ACTIVITY_QUERY,
                        state = LockerActivity.STATE_UNKNOWN,
                        succeeded = false,
                        text = response.message
                    )
                )
                return
            }

            if (response !is QuerySuccess) {
                Log.d(TAG, "General communication failure when querying locker status")
                return
            }

            val query = response

            // record type for the results of this query
            val state = state2num(query.state)

            // this will be the default activity to record unless we actually open or close
            activity = LockerActivity(
                lockerId = locker.id,
                type = LockerActivity.ACTIVITY_QUERY,
                state = state,
                succeeded = true
            )

            state
        }

        Log.d(TAG, "current state: state=$queriedState tapAction=$tapAction")

        // TODO: would also need to identify here the locker interface type, useless to send CLOSE
        // to a locker that cannot close by itself

        // determine action to take: default action is to always open, unless we explicitly know
        // locker to be closed (this means an unknown state results in open as default action)
        if ((tapAction == TapAction.DEFAULT && queriedState != LockerActivity.STATE_OPEN) || tapAction == TapAction.OPEN) {
            val response = nfc.communicate(Open())
            Log.d(TAG, "sent open, response: $response")

            activity = when (response) {
                is OpenSuccess ->
                    LockerActivity(
                        lockerId = locker.id,
                        state = state2num(response.state),
                        succeeded = true,
                        type = LockerActivity.ACTIVITY_OPEN
                    )
                is OpenError ->
                    LockerActivity(
                        lockerId = locker.id,
                        state = state2num(response.state, queriedState),
                        succeeded = false,
                        text = response.message,
                        type = LockerActivity.ACTIVITY_OPEN
                    )
                else ->
                    LockerActivity(
                        lockerId = locker.id,
                        succeeded = false,
                        state = queriedState,
                        type = LockerActivity.ACTIVITY_OPEN,
                        text = "Missing or invalid response from locker when trying to open"
                    )
            }
        } else if ((tapAction == TapAction.DEFAULT && queriedState == LockerActivity.STATE_OPEN) || tapAction == TapAction.CLOSE) {
            val response = nfc.communicate(Close())
            Log.d(TAG, "sent close, response: $response")

            activity = when (response) {
                is CloseSuccess ->
                    LockerActivity(
                        lockerId = locker.id,
                        state = state2num(response.state),
                        succeeded = true,
                        type = LockerActivity.ACTIVITY_CLOSE
                    )
                is CloseError ->
                    LockerActivity(
                        lockerId = locker.id,
                        state = state2num(response.state, queriedState),
                        succeeded = false,
                        text = response.message,
                        type = LockerActivity.ACTIVITY_CLOSE
                    )
                else ->
                    LockerActivity(
                        lockerId = locker.id,
                        succeeded = false,
                        state = queriedState,
                        type = LockerActivity.ACTIVITY_CLOSE,
                        text = "Missing or invalid response from locker when trying to close"
                    )
            }
        } else {
            // QUERY in effect
            Log.d(TAG, "Nothing to do!")
        }

        activity?.let {
            models.insertActivity(it)

            if (!it.succeeded && it.text != null) {
                Toast.makeText(
                    applicationContext,
                    "Failed in operation: ${it.text}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // TODO: the SNEP/NDEF thing on android doesn't really work, most of this has to be
    // reworked to use HCE instead. That'll be later, though.

    private suspend fun processIntent(intent: Intent?) {
        Log.d(TAG, "processIntent: intent=$intent path=${intent?.data?.path}")

        if (intent == null)
            return
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: $intent")

        lifecycleScope.launch {
            processIntent(intent)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "context item selected: $item")
        return super.onContextItemSelected(item)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "menu item selected: $item")
        return when (item.itemId) {
            R.id.license -> {
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))
                true
            }
            else ->  super.onOptionsItemSelected(item)
        }
    }
}

enum class TapAction {
    DEFAULT,
    OPEN,
    CLOSE,
    QUERY
}
