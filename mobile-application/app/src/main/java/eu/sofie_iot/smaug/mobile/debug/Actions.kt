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

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import eu.sofie_iot.smaug.mobile.MainActivity
import eu.sofie_iot.smaug.mobile.SmaugMobileApplication
import eu.sofie_iot.smaug.mobile.model.LockerActivity
import eu.sofie_iot.smaug.mobile.model.Rent
import eu.sofie_iot.smaug.nfc.messages.toHex
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

@ExperimentalUnsignedTypes
class DebugActions(val fragment: Fragment) {
    private val app = fragment.requireActivity().application as SmaugMobileApplication
    val main = fragment.activity as MainActivity
    val context = fragment.requireContext()
    val TAG = "DebugActions"

    fun toastNotImplemented() {
        Toast.makeText(context, "Sorry, not yet implemented", Toast.LENGTH_SHORT).show()
    }

    fun tap(id: Long): () -> Unit {
        return {
            val models = app.db.models

            // tap in **main activity scope**, not in fragment scope, as tapping can cause
            // the current fragment to be changed --- terminating the NFC tapping process!!
            main.lifecycleScope.launch {
                val locker = models.lockerById(id)!!
                val marketplace = models.marketplaceById(locker.marketplaceId)!!
                val state = models.activitiesForLocker(id).firstOrNull()?.state ?: LockerActivity.STATE_UNKNOWN
                // TODO fix the bloody data model this kind of crap is littered all over the code
                // and this is actually a tristate, there's "UNKNOWN" as well
                val nfc = MockNfc(locker.uniqueId!!, marketplace.contract.address, state)
                // todo add isrented
                main.handleNfc(nfc)

                Toast.makeText(context, "Simulating tapping locker #$id", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Simulating tapping locker #$id")
            }
        }
    }

    companion object {
        fun randomLockerId() =
            List(10) { (('a'..'f') + ('0'..'9')).random() }.joinToString("")
    }

    fun tapNew(): () -> Unit = {
        val models = app.db.models
        main.lifecycleScope.launch {
            val marketplace = models.marketplaceById(1)!!
            val id = "tap-" + randomLockerId()
            val nfc = MockNfc(id, marketplace.contract.address, LockerActivity.STATE_UNKNOWN)
            main.handleNfc(nfc)
            Toast.makeText(context, "Simulating tapping new locker '$id'", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Simulating tapping new locker '$id'")
        }
    }

    fun beaconNew(): () -> Unit = {
        // we use 0xffff as leader to indicate for fake backend interface this is intentional
        val iid = byteArrayOf(0xff.toByte(), 0xff.toByte()) + Random.nextBytes(4)
        main.onBeaconDiscovered(iid, -10.0, 10) // random other values

        Toast.makeText(context, "Simulating beacon from locker ${iid.toHex()}", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Simulating beacon from locker ${iid.toHex()}")
    }

    fun clearRents(lockerId: Long): () -> Unit {
        return {
            val models = app.db.models
            fragment.lifecycleScope.launch {
                for (rent in models.rentsForLockerBetween(lockerId)) {
                    Log.d(TAG, "deleting rent from locker $lockerId: $rent")
                    models.deleteRent(rent)
                }
            }
        }
    }

    fun rent(lockerId: Long, duration: Duration, start: Duration = Duration.ZERO): () -> Unit {
        return {
            recordRent(lockerId, duration, start)
        }
    }

    private fun recordRent(lockerId: Long, duration: Duration, start: Duration) {
        val models = app.db.models
        val now = Instant.now()
        val rent = Rent(
            lockerId = lockerId,
            start = now.plus(start),
            end = now.plus(start).plus(duration))
        fragment.lifecycleScope.launch {
            models.insertRent(rent)
        }
    }

    private fun recordAction(lockerId: Long, type: Int, state: Int, success: Boolean, message: String?) {
        val models = app.db.models
        val activity = LockerActivity(
            lockerId = lockerId,
            type = type,
            state = state,
            succeeded = success,
            text = message
        )
        fragment.lifecycleScope.launch {
            models.insertActivity(activity)
        }
    }

    fun record(lockerId: Long, type: Int, state: Int, success: Boolean = true, message: String? = null): () -> Unit {
        return {
            recordAction(lockerId, type, state, success, message)
        }
    }

    fun randomRent(): () -> Unit {
        return {
            fragment.lifecycleScope.launch {
                // determine rented lockers and those that are not, by their ids at this stage
                val rented = app.db.models.rentsOver().map { it.lockerId }.distinct()
                val all = app.db.models.allLockers().map { it.id }.distinct()
                val unrented = all.minus(rented)

                Log.d(TAG, "rented=$rented unrented=$unrented")

                fun rent() {
                    val id = unrented.random()
                    Log.d(TAG, "random rent: id=$id")

                    fragment.lifecycleScope.launch {
                        app.db.models.insertRent(Rent(
                            lockerId = id,
                            start = Instant.now(),
                            end = Instant.now().plusSeconds(3600)))
                    }
                }

                fun unrent() {
                    val id = rented.random()
                    Log.d(TAG, "random unrent: id=$id")

                    fragment.lifecycleScope.launch {
                        app.db.models.rentsForLockerBetween(id).forEach {
                            app.db.models.deleteRent(it)
                        }
                    }
                }

                val oracleBit = Random.nextBoolean()

                // decide whether to rent or unrent
                when {
                    unrented.isEmpty() and rented.isEmpty() ->
                        Log.d(TAG, "no lockers, can not randomly rent or unrent!")

                    // forced hand?
                    unrented.isEmpty() and rented.isNotEmpty() -> unrent()
                    unrented.isNotEmpty() and rented.isEmpty() -> rent()

                    // otherwise random
                    oracleBit -> rent()
                    !oracleBit -> unrent()
                }

            }
        }

    }

    fun removeLockers(): (() -> Unit)? = {
        val models = app.db.models
        fragment.lifecycleScope.launch {
            models.deleteLocker(lockers = models.allLockers().toTypedArray())
        }
    }
}