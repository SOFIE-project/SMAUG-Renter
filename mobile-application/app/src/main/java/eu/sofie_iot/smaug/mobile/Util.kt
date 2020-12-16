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

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import androidx.core.view.isVisible
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.squareup.picasso.Picasso
import eu.sofie_iot.smaug.mobile.backend.BackendLockerModel
import eu.sofie_iot.smaug.mobile.model.Bid
import eu.sofie_iot.smaug.mobile.model.Locker
import eu.sofie_iot.smaug.mobile.model.LockerActivity
import eu.sofie_iot.smaug.mobile.model.Rent
import eu.sofie_iot.smaug.mobile.ui.views.LockerStatus
import eu.sofie_iot.smaug.mobile.ui.views.LockerStatusView
import eu.sofie_iot.smaug.mobile.ui.views.LockerView
import eu.sofie_iot.smaug.mobile.ui.views.RentedUntilView
import java.math.BigDecimal
import java.math.BigInteger
import java.text.DateFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.*

object Util {
    private val TAG = "Util"

    private val WEI_IN_ETH = BigDecimal("1000000000000000000")


    fun parseDateTime(s: String?) =
        when (s) {
            null -> null
            // these are primarily for debugging and development without the need to include a full
            // time parser library
            "30s" -> Instant.now().plusSeconds(30)
            "10m" -> Instant.now().plusSeconds(600)
            "2h" -> Instant.now().plusSeconds(7200)
            else ->
                try {
                    DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault()).parse(s)
                        ?.let { Instant.from(it) }
                } catch (e: DateTimeParseException) {
                    Log.e("Util", "Could not parse '$s': $e")
                    null
                }
        }

    private fun lockerDistanceMaybe(locker: Locker): Double? {
        Log.d(TAG, "lockerDistanceMaybe: lastseen=${locker.lastSeenBeacon}")
        // do not show if locker has not been seen within the last minute, it is probably
        // out of range then
        if (locker.lastSeenBeacon == null ||
            locker.bleTxPower == null ||
            locker.bleRssi == null ||
            locker.lastSeenBeacon.isBefore(Instant.now().minusSeconds(60)))
            return null

        val ratio = (locker.bleTxPower.toDouble() - locker.bleRssi) / 20.0
        val distance = Math.pow(10.0, ratio)

        Log.d(TAG, "txpower=${locker.bleTxPower} rssi=${locker.bleRssi} ratio=$ratio distance=$distance")

        return distance


//
//        val ratio = locker.smoothedRSSI / locker.beTxPower.toDouble()
//
//        if (ratio < 1.0)
//            return Math.pow(ratio, 10.0)
//
//        // magic voodoo constants
//        return .89976 * Math.pow(ratio, 7.7095) + 0.111
    }

    fun updateLockerViewFromLocker(view: LockerView, locker: Locker) {
        view.apply {
            setLockerImageUrls(locker.imageUrls)
            setLockerName(locker.name)
            setLockerText(locker.description)
            setLockerIsFavourite(locker.favourite)
            setLockerSeen(
                locker.lastSeenBeacon != null,
                locker.lastSeenNfc != null,
                locker.lastSeenBackend != null)

            setLockerDistance(lockerDistanceMaybe(locker))

            // TODO: use the text lines for owner information, address, or something else
        }
    }

    // update the locker view from given data, trying to figure out the most useful way to show
    // the given data --- if you do not want owner info to be visible, then don't pass that
    // data ...
    fun updateLockerView(
        view: LockerView,
        locker: Locker,
        owner: Any? = null,
        pricing: Any? = null,
        rents: List<Rent> = emptyList(),
        bids: List<Bid> = emptyList(),
        action: NavDirections? = null,
        rent: Rent? = null,
        showFavouriteToggle: Boolean = true
    ) {
        view.apply {
            setLockerImageUrls(locker.imageUrls)
            setLockerName(locker.name)
            setLockerIsFavourite(locker.favourite, showFavouriteToggle)
            setLockerSeen(
                locker.lastSeenBeacon != null,
                locker.lastSeenNfc != null,
                locker.lastSeenBackend != null)

            setLockerDistance(lockerDistanceMaybe(locker))

            if (owner != null) {
                TODO()
            }

            if (pricing != null) {
                TODO()
            }

            if (bids.size > 0) {
                TODO()
            }

            val rentText =
                if (rents.size > 0 || rent != null) {
                    val firstRent = (rents.firstOrNull() ?: rent)!!

                    (if (firstRent.start <= Instant.now())
                        "Rented until ${formatUntil(firstRent.end)}"
                    else
                        "Rent starts at ${formatUntil(firstRent.start)}") +
                            if (rents.size > 1) "\n(multiple rentals)" else ""
                } else ""

            setLockerText(
                (if (locker.description != null && locker.description.isNotBlank()) locker.description + "\n\n" else "") +
                        rentText)

            if (action != null) {
                setOnClickListener {
                    it.findNavController().navigate(action)
                }
            }
        }
    }

    private fun formatUntil(time: Instant): String {
        val now = Instant.now()
        val duration = Duration.between(time, now)

        return when (duration.toHours()) {
            in 0..23 ->
                DateUtils.formatSameDayTime(
                    time.toEpochMilli(), now.toEpochMilli(),
                    3, 3).toString()
            else ->
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date.from(time))
        }
    }

    fun updateLockerStatus(view: LockerStatusView, activity: LockerActivity?) {
        Log.d(TAG, "updateLockerStatus: activity=$activity")

        if (activity == null) {
            view.isVisible = false
            return

        }

        view.run {
            isVisible = true

            fun a(okAction: LockerStatus, otherwise: LockerStatus) =
                if (activity.succeeded && activity.type != LockerActivity.ACTIVITY_QUERY)
                    okAction
                else
                    otherwise

            val status = when (activity.state) {
                LockerActivity.STATE_OPEN -> a(LockerStatus.OPENED, LockerStatus.WAS_OPEN)
                LockerActivity.STATE_CLOSED -> a(LockerStatus.CLOSED, LockerStatus.WAS_CLOSED)
                else -> LockerStatus.UNKNOWN
            }

            setLockerStatus(status)
            setLockerStatusTime(activity.time)
            setLockerMessage(activity.text)
        }
    }

    fun updateLockerRent(view: RentedUntilView, rent: Rent?) {
        if (rent == null) {
            view.isVisible = false
            return
        }

        view.run {
            isVisible = true
            setRentTime(rent.start, rent.end)
        }
    }

    val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())

    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())

    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())

    fun timeShort(time: Instant, reference: Instant? = null): String {
        if (reference == null)
            return dateTimeFormatter.format(time)

        if (dateFormatter.format(time) == dateFormatter.format(reference))
            return timeFormatter.format(time)

        return dateTimeFormatter.format(time)
    }

    val longDateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
        .withZone(ZoneId.systemDefault())

    fun timeLong(time: Instant): String = longDateTimeFormatter.format(time)

    fun weiToEth(amount: BigInteger): Float =
        amount.toBigDecimal().divide(WEI_IN_ETH).toFloat()

    fun timeRelativeShort(context: Context, time: Instant): String =
        DateUtils.getRelativeDateTimeString(context, time.toEpochMilli(),
            DateUtils.DAY_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0).toString()
}