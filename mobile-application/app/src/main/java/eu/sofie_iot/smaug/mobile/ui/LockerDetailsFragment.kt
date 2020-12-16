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

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.modules.TextModule
import com.smarteist.autoimageslider.SliderViewAdapter
import com.squareup.picasso.Picasso
import eu.sofie_iot.smaug.mobile.R
import eu.sofie_iot.smaug.mobile.Util
import eu.sofie_iot.smaug.mobile.debug.DebugActions
import eu.sofie_iot.smaug.mobile.ui.views.HistoryLiveData
import eu.sofie_iot.smaug.mobile.ui.views.RentedUntilView
import kotlinx.android.synthetic.main.fragment_locker_details.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.Instant


class SliderAdapter(val context: Context) : SliderViewAdapter<SliderAdapter.Holder>() {
    private val TAG = "SlideAdapter"

    inner class Holder(val view: View) : SliderViewAdapter.ViewHolder(view) {
        val image = view.findViewById<ImageView>(R.id.images_item_image)

        init {
            Log.d(TAG, "holder: $view ($image)")
        }
    }

    var imageUrls: List<String> = emptyList()
        set(value) {
            Log.d(TAG, "imageUrls = $imageUrls")
            field = value
            notifyDataSetChanged()
        }

    override fun getCount(): Int = imageUrls.size

    override fun onCreateViewHolder(parent: ViewGroup?): Holder {
        Log.d(TAG, "onCreateViewHolder: parent=$parent")
        val inflate: View =
            LayoutInflater.from(context).inflate(R.layout.locker_images_item, null)
        return Holder(inflate)
    }

    override fun onBindViewHolder(holder: Holder?, position: Int) {
        val url = imageUrls[position]

        Log.d(TAG, "onBindViewHolder: holder=$holder position=$position: url=$url")

        Picasso.get()
            .load(url)
            .into(holder!!.image)
    }
}


@ExperimentalUnsignedTypes
class LockerDetailsFragment : SmaugFragment(R.layout.fragment_locker_details) {
    private val TAG = "LockerDetailsFragment"

    private lateinit var fav_on: MenuItem
    private lateinit var fav_off: MenuItem


    var lockerId: Long? = null

    companion object {
        val openHistoryLines = HashMap<Long, Boolean>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: LockerDetailsFragmentArgs by navArgs()
        val id = args.locker.lockerId
        lockerId = id

        // schedule locker detail info update
        app.refreshLocker(id)

        DebugActions(this).run {
            Beagle.add(
                TextModule("Locker debug controls", TextModule.Type.SECTION_HEADER),
                TextModule(text = "Simulate tap", TextModule.Type.BUTTON, onItemSelected = tap(id)),
                TextModule(
                    text = "Rent for 1h",
                    type = TextModule.Type.BUTTON,
                    onItemSelected = rent(id, Duration.ofHours(1))
                ),
                TextModule(
                    text = "Rent in 5m",
                    type = TextModule.Type.BUTTON,
                    onItemSelected = rent(
                        id,
                        Duration.ofHours(1),
                        Duration.ofMinutes(5)
                    )
                ),
                TextModule(
                    text = "Remove rents",
                    type = TextModule.Type.BUTTON,
                    onItemSelected = clearRents(id)
                ),
                lifecycleOwner = viewLifecycleOwner
            )
        }

        val adapter = SliderAdapter(requireContext())
        locker_images.setSliderAdapter(adapter)

        // now need to fetch locker data etc. from the database, potentially including
        // slow calls to backend or remotes
        val models = db.liveModels
        val locker = models.lockerById(id)


        locker.observe(viewLifecycleOwner) {
            Log.d(TAG, "locker data changed: $it")
            Util.updateLockerView(locker_view, it!!)

            fav_on.isVisible = it.favourite
            fav_off.isVisible = !it.favourite
            history_rv.collapsed = it.historyCollapsed

            adapter.imageUrls = it.imageUrls ?: emptyList()
        }

        val rents =  models.rentsForLockerOver(id)

        // move to openclose only we have a rent
        fun goToOpenClose() {
            if (rents.value?.filter { it.within(Instant.now()) }?.isNotEmpty() ?: false)
                view.findNavController().navigate(
                    LockerDetailsFragmentDirections.actionOpenClose(LockerIdentifier(id))
                )
        }

        // clicking on any on of these goes to the open/close page
        status_view.setOnClickListener { goToOpenClose() }
        locker_view.setOnClickListener { goToOpenClose() }

        // active rent
        rents.observe(viewLifecycleOwner) {
            Log.d(TAG, "locker rents data changed: $it")
            locker_rents.removeAllViews()
            for (rent in it) {
                Log.d(TAG, "adding rent: $rent")
                RentedUntilView(requireContext(), null).let {
                    locker_rents.addView(it)
                    Util.updateLockerRent(it, rent)
                    it.setOnClickListener { goToOpenClose() }
                }
            }
        }

        // based on most recent update the status list
        models.activitiesForLocker(id, limit = 1).observe(viewLifecycleOwner) {
            Util.updateLockerStatus(status_view, it.firstOrNull())
        }

        val history = HistoryLiveData(
            models.activitiesForLocker(id),
            models.bidsForLocker(id),
            models.rentsForLocker(id)
        )

        history.map { it.sortedByDescending { it.time } }
            .observe(viewLifecycleOwner) {
                history_rv.lines = it
            }

        val openBid = db.liveModels.openBidsForLocker(id).map { it.firstOrNull() }

        locker_view.setFavouriteClickListener {
            lifecycleScope.launch {
                val models = db.models
                val locker = models.lockerById(id)!!.let { it.copy(favourite = !it.favourite)}
                models.updateLocker(locker)

                Log.d(TAG, "toggled favourite for locker $id, now ${locker.favourite}")
            }
        }

        // TODO: enable only if rentable and/or update text (bid vs. rent)
        locker_rent_now.setOnClickListener {
            val action = LockerDetailsFragmentDirections.actionPayment(args.locker)
            findNavController().navigate(action)
        }

        // make the payment button hidden until we know whether we have pending bids or not
        locker_rent_now.isGone = true

        openBid.observe(viewLifecycleOwner) { bid ->
            Log.d(TAG, "first bis for locker: $bid")

            locker_rent_now.isGone = bid != null
            open_bid.isGone = bid == null

            // we'll only ever consider one..

            if (bid != null) {
                // open_bid.text = "Bid ${bid.id} tx ${bid.transactionId} block ${bid.transactionBlock} pending"
                open_bid.text = "Bid pending ..."
                open_bid.setOnClickListener {
                    val action = LockerDetailsFragmentDirections.actionPaymentPending(
                        BidIdentifier(
                            bidId = bid.id
                        )
                    )
                    findNavController().navigate(action)
                }
            }
        }

        history_rv.labelOnClickListener = {
            lifecycleScope.launch {
                app.db.models.updateLockerHistoryCollapsed(
                    lockerId!!,
                    !(locker.value?.historyCollapsed ?: true)
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(TAG, "onCreateOptionsMenu: menu=$menu inflater=$inflater")
        inflater.inflate(R.menu.locker_details_menu, menu)

        fav_off = menu.findItem(R.id.app_bar_favourite_off)
        fav_on = menu.findItem(R.id.app_bar_favourite_on)
    }

    private fun setLockerFavourite(favourite: Boolean): Unit {
        runBlocking {
            app.db.models.updateLockerFavourite(lockerId!!, favourite)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.app_bar_favourite_on -> {
                setLockerFavourite(false)
                true
            }
            R.id.app_bar_favourite_off -> {
                setLockerFavourite(true)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}