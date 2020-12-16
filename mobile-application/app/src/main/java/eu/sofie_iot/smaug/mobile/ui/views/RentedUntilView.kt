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
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.GradientDrawable
import android.text.format.DateUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import eu.sofie_iot.smaug.mobile.R
import eu.sofie_iot.smaug.mobile.Util.parseDateTime
import kotlinx.android.synthetic.main.rented_until_view.view.*
import java.time.Duration
import java.time.Instant
import kotlin.math.absoluteValue

class RentedUntilView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val TAG = "RentedUntilView"

    var startTime: Instant? = null
    var endTime: Instant? = null

    init {
        View.inflate(context, R.layout.rented_until_view, this)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RentedUntilView,
            0, 0
        ).apply {
            try {
                setRentTime(
                    parseDateTime(getString(R.styleable.RentedUntilView_from)),
                    parseDateTime(getString(R.styleable.RentedUntilView_until))
                )
            } finally {
                recycle()
            }
        }
    }

    fun rentStart() = startTime
    fun rentEnd() = endTime
    fun setRentTime(s: Instant?, e: Instant?) {
        startTime = s
        endTime = e
        updateText()
    }

    private var stop = false
    private val updater = Runnable {
        Log.d(TAG, "updated: stop=$stop")
        if (stop)
            return@Runnable
        updateText()
    }

    private fun updateText() {
        val now = Instant.now()
        val start = startTime
        val end = endTime

        // two main branches here, either the rent is in the future... determine next point to
        // look for
        val (future, next, duration) = when {
            start != null && now.isBefore(start) -> Triple(
                true,
                start,
                Duration.between(start, now)
            )
            end != null -> Triple(false, end, Duration.between(now, end))
            else -> return // nothing to do here
        }

        Log.d(
            TAG,
            "now=$now start=$start end=$end => future=$future duration=$duration"
        )

        // TODO: calculate how much there is time and when it is getting closer to zero,
        // change color, and when very nearby, start throbbing etc. animation
        val dt = when (duration?.toHours()) {
            null -> "unknown"
            in 0..23L -> DateUtils.formatDateTime(
                context,
                next.toEpochMilli(),
                DateUtils.FORMAT_SHOW_TIME
            ) + " (" + DateUtils.getRelativeTimeSpanString(
                next.toEpochMilli(),
                now.toEpochMilli(),
                0,
                0
            ) + ")"
            else -> DateUtils.formatDateTime(
                context, next.toEpochMilli(),
                DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE
            )
        }

        status_time.text = dt

        val color = (if (future)
            R.color.untilFuture
        else when (duration?.toMinutes()) {
            null -> R.color.untilNormal
            in -10000000000000..0L -> R.color.untilAlert
            in 0..4L -> R.color.untilAlert
            in 5..29L -> R.color.untilWarning
            in 30..119L -> R.color.untilNotice
            else -> R.color.untilNormal
        })

        val bg = layout.background as GradientDrawable
        Log.d(TAG, "bg=$bg color=$color getcolor=${context.getColor(color)}")
        bg.colorFilter = PorterDuffColorFilter(context.getColor(color), PorterDuff.Mode.SRC_ATOP)

        val untilUpdateSecs: Long? = when (duration?.toMinutes()?.absoluteValue) {
            null -> null
            in 0..2L -> 1
            in 2..60L -> 15
            in 50..(60L * 24) -> 60
            else -> 3600
        }

        Log.d(
            TAG,
            "in ${duration?.toMinutes()} minutes, untilUpdateSecs=$untilUpdateSecs handler=$handler"
        )

        if (handler != null) {
            handler.removeCallbacks(updater)

            if (untilUpdateSecs != null)
                handler.postDelayed(updater, untilUpdateSecs * 1000)
        }

        status_text.text = when {
            future -> context.getString(R.string.rent_future)
            duration?.isNegative ?: false -> context.getString(R.string.rent_expired)
            else -> context.getString(R.string.rent_until)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateText()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop = true // ensure runnable knows to stop cleanly
        Log.d(TAG, "detached")
    }
}

