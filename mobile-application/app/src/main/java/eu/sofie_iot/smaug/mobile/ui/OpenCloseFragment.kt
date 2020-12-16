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

package eu.sofie_iot.smaug.mobile.ui

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.navArgs
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.modules.TextModule
import eu.sofie_iot.smaug.mobile.*
import eu.sofie_iot.smaug.mobile.R
import eu.sofie_iot.smaug.mobile.debug.DebugActions
import eu.sofie_iot.smaug.mobile.model.*
import kotlinx.android.synthetic.main.fragment_open_close.*
import java.time.Instant

// TODO: add videmodel to store the selection preference

class OpenCloseFragmentViewModel(id: Long, application: Application) : AndroidViewModel(application) {
    private val app by smaugApplication()

    val locker = app.db.liveModels.lockerById(id)
    val rent = app.db.liveModels.rentsForLockerOver(id, start = Instant.now(), end = Instant.now()).map {
        Log.d("OpenCloseFragment", "rents changed for $id: $it")
        it.sortedBy { it.start }.firstOrNull()
    }

    // rentedLockerMode is null when the locker does not have valid rent, and non-null
    // with the locker tap type and current selected tap action if rented
    val rentedLockerMode = MediatorLiveData<Pair<LockerOpenCloseType, TapAction>?>()
    val action = MutableLiveData<TapAction?>()

    init {
        rentedLockerMode.run {
            value = null
            fun update() {
                val locker = locker.value
                val rent = rent.value
                val action = action.value

                when {
                    locker != null && rent != null && action != null ->
                        postValue(Pair(locker.type, action))
                    else ->
                        postValue(null)
                }

            }
            addSource(locker) { update() }
            addSource(rent) { update() }
            addSource(action) { update() }
        }
    }

    val lastActivity = app.db.liveModels.activitiesForLocker(id, limit = 1).map { it.firstOrNull() }
}

class OpenCloseFragmentViewModelFactory(private val id: Long, private val application: Application): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = OpenCloseFragmentViewModel(id, application) as T
}

@ExperimentalUnsignedTypes
class OpenCloseFragment : SmaugFragment(R.layout.fragment_open_close) {
    private val TAG = "OpenCloseFragment"

    var lockerId: Long? = null
    val args: OpenCloseFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = args.locker.lockerId
        lockerId = id

        Log.d(TAG, "locker=$id")

        DebugActions(this).run {
            Beagle.add(
                TextModule("Locker controls", TextModule.Type.SECTION_HEADER),
                // this one would be shared with locker_details, so reuse that code
                TextModule( "Simulate tap", TextModule.Type.BUTTON, onItemSelected = tap(id)),
                TextModule("Record management", TextModule.Type.SECTION_HEADER),
                TextModule(
                    "Opened", TextModule.Type.BUTTON,
                    onItemSelected = record(id, LockerActivity.ACTIVITY_OPEN, LockerActivity.STATE_OPEN, true, "locker opened")
                ),
                TextModule(
                     "Closed", TextModule.Type.BUTTON,
                    onItemSelected = record(id, LockerActivity.ACTIVITY_CLOSE, LockerActivity.STATE_CLOSED, true, "locker closed")
                ),
                TextModule(
                     "Is Open", TextModule.Type.BUTTON,
                    onItemSelected = record(id, LockerActivity.ACTIVITY_QUERY, LockerActivity.STATE_OPEN, true, "locker is open")
                ),
                TextModule(
                     "Is Closed", TextModule.Type.BUTTON,
                    onItemSelected = record(id, LockerActivity.ACTIVITY_QUERY, LockerActivity.STATE_CLOSED, true, "locker is closed")
                ),
                TextModule(
                     "Open Failure", TextModule.Type.BUTTON,
                    onItemSelected = record(id, LockerActivity.ACTIVITY_OPEN, LockerActivity.STATE_UNKNOWN, false, "error during opening")
                ),
                lifecycleOwner = viewLifecycleOwner
            )
        }

        val main = activity as MainActivity
        main.tapLockerId = id
        main.tapAction = TapAction.DEFAULT

        Log.d(TAG, "main=$main tapLockerId=${main.tapLockerId}")

        val vm: OpenCloseFragmentViewModel by viewModels { OpenCloseFragmentViewModelFactory(id, app) }

        vm.locker.observe(viewLifecycleOwner) {
            Log.d(TAG, "locker changed: $it")

            if (it != null) {
                // the initial selected action type depends on the locker type
                if (vm.action.value == null) {
                    val action = when (it.type) {
                        LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH -> TapAction.OPEN
                        LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH_DELAY -> TapAction.OPEN
                        LockerOpenCloseType.TAP_OPEN_TAP_CLOSE -> TapAction.DEFAULT
                    }
                    vm.action.postValue(action)
                }
            }
        }

        fun updateLocker() {
            val locker = vm.locker.value
            val rent = vm.rent.value

            if (locker != null)
                Util.updateLockerView(locker_info, locker, rent = rent)
        }

        vm.locker.observe(viewLifecycleOwner) { updateLocker() }
        vm.rent.observe(viewLifecycleOwner) { updateLocker() }

        //
//        vm.rent.observe(viewLifecycleOwner) {
//            Log.d(TAG, "locker current rent changed: $it")
//            Util.updateLockerStatusView(locker_status, it)
//        }

        val allModeControls = setOf(mode_default, mode_open, mode_close, mode_query)
        val modeActions = mapOf(
            TapAction.DEFAULT to mode_default,
            TapAction.QUERY to mode_query,
            TapAction.OPEN to mode_open,
            TapAction.CLOSE to mode_close
        )
        val actionModes = modeActions.entries.associate { (k, v) -> v.id to k }
        val modeControls = mapOf(
            LockerOpenCloseType.TAP_OPEN_TAP_CLOSE to Pair(
                allModeControls, R.string.open_help_open_close),
            LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH_DELAY to Pair(
                setOf(mode_open, mode_query), R.string.open_help_close_delay),
            LockerOpenCloseType.TAP_OPEN_CLOSE_PUSH to Pair(
                setOf(mode_open, mode_query), R.string.open_help_close_delay))

        // hide and disable by default all controls
        allModeControls.forEach { it.isVisible = false }

        vm.rentedLockerMode.observe(viewLifecycleOwner) {
            Log.d(TAG, "rented locker mode changed: $it")

            if (it == null) {
                help_text.text = getString(R.string.open_help_no_rent)
            } else {
                val (type, action) = it
                val (enabled, textId) = modeControls.get(type)!!
                val checked = modeActions.get(action)!!

                Log.d(TAG, "type $type action=$action -> $enabled $textId $checked")

                for (control in allModeControls) {
                    control.isEnabled = control in enabled
                    control.isVisible = control in enabled
                }

                help_text.text = getString(textId)
                mode_controls.check(checked.id)
                main.tapAction = it.second

            }
        }

        mode_controls.addOnButtonCheckedListener { _, id, checked ->
            val action = actionModes.get(id)!!
            if (checked)
                vm.action.postValue(action)

            Log.d(TAG, "checked id=$id action=$action checked=$checked")
        }


        vm.lastActivity.observe(viewLifecycleOwner) {
            Log.d(TAG, "last activity changed: $it")
            Util.updateLockerStatus(locker_status, it)
        }
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")

        val main = activity as MainActivity
        main.tapLockerId = null
        main.tapAction = TapAction.DEFAULT

        super.onDestroyView()
    }
}
