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

package eu.sofie_iot.smaug.mobile.ui.views

import android.content.Context
import android.text.format.DateUtils
import android.util.AttributeSet
import eu.sofie_iot.smaug.mobile.R
import eu.sofie_iot.smaug.mobile.Util.parseDateTime
import java.text.DateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class RentedLockerView(context: Context, attrs: AttributeSet?) : LockerView(context, attrs) {
    var rentStart: Instant? = null
    var rentEnd: Instant? = null

    init {
//        View.inflate(context, R.layout.locker_view, this)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RentedLockerView,
            0, 0
        ).apply {
            try {
                val start = parseDateTime(getString(R.styleable.RentedLockerView_rentStart))
                val end = parseDateTime(getString(R.styleable.RentedLockerView_rentEnd))

                setLockerRent(start, end)
            } finally {
                recycle()
            }
        }
    }

    fun lockerRentStart() = rentStart
    fun lockerRentEnd() = rentEnd

    fun setLockerRent(start: Instant?, end: Instant?) {
        rentStart = start
        rentEnd = end

        updateText()
    }

    private fun updateText() {
        val now = Instant.now()
        val start = rentStart
        val end = rentEnd

//        smaug:text="Rent is active\nAvailable to 2.1.2020 00:00 (7 hours)" />
//        smaug:text="Available at 12:00 (1h20m)\nRent length 2 hours (until 14:00)" />

        val formatter = DateTimeFormatter.ofLocalizedDateTime(
            FormatStyle.SHORT
        )
            .withLocale(Locale.getDefault())
            .withZone(TimeZone.getDefault().toZoneId())

        val text =
            when {
                start == null ->
                    "unknown start"
                end == null ->
                    "unknown end"
                end.isBefore(now) ->
                    // in the past
                    "in past"
                start.isBefore(now) ->
                    // currently running
                    "Accessible since ${DateUtils.formatSameDayTime(
                        start.toEpochMilli(),
                        now.toEpochMilli(),
                        DateFormat.SHORT,
                        DateFormat.SHORT
                    )}\nAvailable to ${DateUtils.formatSameDayTime(
                        end.toEpochMilli(),
                        now.toEpochMilli(),
                        DateFormat.SHORT,
                        DateFormat.SHORT
                    )}"
                else ->
                    // in future
                    "Available from ${DateUtils.formatSameDayTime(
                        start.toEpochMilli(),
                        now.toEpochMilli(),
                        DateFormat.SHORT,
                        DateFormat.SHORT
                    )}\nRent ends at ${DateUtils.formatSameDayTime(
                        end.toEpochMilli(),
                        now.toEpochMilli(),
                        DateFormat.SHORT,
                        DateFormat.SHORT
                    )}"
            }

        setLockerText(text)
    }
}