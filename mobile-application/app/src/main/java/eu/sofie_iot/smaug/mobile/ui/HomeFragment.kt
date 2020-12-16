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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.modules.TextModule
import eu.sofie_iot.smaug.mobile.R
import eu.sofie_iot.smaug.mobile.SmaugMobileApplication
import eu.sofie_iot.smaug.mobile.debug.DebugActions
import eu.sofie_iot.smaug.mobile.model.Locker
import eu.sofie_iot.smaug.mobile.Util
import eu.sofie_iot.smaug.mobile.smaugApplication
import eu.sofie_iot.smaug.mobile.ui.views.LockerView
import kotlinx.android.synthetic.main.fragment_home.*
import java.time.Instant

open class SmaugFragment(val layoutId: Int) : Fragment() {
    protected val app by smaugApplication()
    protected val db by lazy { app.db }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutId, container, false)
    }
}

class HomeFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val db by lazy { (application as SmaugMobileApplication).db }

    // TODO: add some sort of periodic refresh here so we can clean out expired rents from
    // the list of active rentals

    // we need to use this later for other purposes as well
    private val activeRents = db.liveModels.rentsOver()

    // now find nearby lockers (the DAO query is a dummy one)
    private val nearbyLockers = db.liveModels.lockersByLocation()

    // This determines which lockers to show, and turns them into another livedata
    // will contain lockers or null if still loading the data
    val activeRentedLockers = activeRents.switchMap {
        val lockerRents =
            it.groupBy { it.lockerId }.mapValues { it.value.sortedBy { it.start } }
        // this is sorted by the first start rent of each locker
        val lockerIds = lockerRents.toList().sortedBy { it.second.first().start }.map { it.first }.toLongArray()
        db.liveModels.lockersByIds(*lockerIds).map {
            it.zip(lockerRents.values)
        }
    }

    // construct a mediated livedata that will join shown rentals and nearby lockers
    val nearbyNotRentedLockers = MediatorLiveData<Pair<List<Long>, List<Locker>>>()

    init {
        nearbyNotRentedLockers.run {
            // initial value
            value = (Pair(emptyList(), emptyList()))

            addSource(activeRents) {
                value = Pair(it.map { it.lockerId }.distinct(), value!!.second)
            }
            addSource(nearbyLockers) {
                value = Pair(value!!.first, it)
            }
        }
    }
}

@ExperimentalUnsignedTypes
class HomeFragment : SmaugFragment(R.layout.fragment_home) {
    final val TAG = "HomeFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // DO NOT MOVE BEAGLE INITIALIZATION FROM HERE! There is strange woo-woo
        // interaction between MediatorLiveData and Beagle which will cause MediatorLiveData
        // to fail if Beagle is initialized after a MediatorLiveData object is created.
        // That is a bug somewhere, but where?
        DebugActions(this).run {
            Beagle.add(
//                TextModule("Tap on locker number ...", TextModule.Type.SECTION_HEADER),
                TextModule("Tap locker 1", TextModule.Type.BUTTON, onItemSelected = tap(1)),
                TextModule("Tap locker 2", TextModule.Type.BUTTON, onItemSelected = tap(2)),
                TextModule("Tap new locker", TextModule.Type.BUTTON, onItemSelected = tapNew()),
                // TextModule("3", TextModule.Type.BUTTON, onItemSelected = tap(3)),
                TextModule("Random (un)rent", TextModule.Type.BUTTON, onItemSelected = randomRent()),
                TextModule("Beacon new locker", TextModule.Type.BUTTON, onItemSelected = beaconNew()),
                TextModule("Remove lockers", TextModule.Type.BUTTON, onItemSelected = removeLockers()),
                lifecycleOwner = viewLifecycleOwner
            )
        }

        val vm: HomeFragmentViewModel by activityViewModels()

        val activeRentals = vm.activeRentedLockers
        val nearbyNotRented = vm.nearbyNotRentedLockers

        val emptyState = object {
            var rented: Int = 0
            set(value) {
                field = value
                check()
            }

            var nearby: Int = 0
            set(value) {
                field = value
                check()
            }
            private fun check() {
                val empty = rented == 0 && nearby == 0
                nolockers_groups.isGone = !empty
                home_scroll.isGone = empty
            }
        }

        activeRentals.observe(viewLifecycleOwner) {
            Log.d(TAG, "active rented lockers changed, now=${Instant.now()}: $it")

            active_rentals.removeAllViews()
            
            active_rentals_some_label.isGone = it.isEmpty()
            active_rentals_none_label.isGone = true

            for ((locker, rents) in it) {
                val lockerView = LockerView(requireContext(), null)
                active_rentals.addView(lockerView)

                Util.updateLockerView(
                    lockerView, locker, rents = rents,
                    action = HomeFragmentDirections.actionLocker(LockerIdentifier(locker.id)),
                    showFavouriteToggle = false)
            }

            emptyState.rented = it.size
        }


        nearbyNotRented.observe(viewLifecycleOwner, Observer { (rented, nearby) ->
            Log.d(TAG, "nearby observer: rented=$rented nearby=$nearby")
            nearby_available.removeAllViews()

            val unrented = nearby.sortedBy { it.name }.filter { it.id !in rented }

            if (unrented.isEmpty())
                nearby_available_label.visibility = View.GONE
            else
                nearby_available_label.visibility = View.VISIBLE

            for (locker in unrented) {
                Log.d(TAG, "showing nearby non-rented locker $locker")

                val lockerView = LockerView(requireContext(), null)
                nearby_available.addView(lockerView)

                // TODO: we want to include other information such as price, how far this is etc.
                Util.updateLockerView(
                    lockerView, locker,
                    action = HomeFragmentDirections.actionLocker(LockerIdentifier(locker.id)),
                    showFavouriteToggle = false)
            }

            emptyState.nearby = unrented.size
        })
    }
}