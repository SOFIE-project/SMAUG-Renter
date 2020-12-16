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
import java.time.Instant

class LockerStatusView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val TAG = "LockerStatusView"

    var status: LockerStatus = LockerStatus.UNKNOWN
    var time: Instant? = null
    var message: String? = null

    init {
        View.inflate(context, R.layout.locker_status_view, this)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LockerStatusView,
            0, 0
        ).apply {
            try {
                val status = LockerStatus.find(getString(R.styleable.LockerStatusView_status))
                val time = parseDateTime(getString(R.styleable.LockerStatusView_statusTime))

                Log.d(TAG, "status=$status time=$time")

                setLockerStatus(status)
                setLockerStatusTime(time)
            } finally {
                recycle()
            }
        }
    }

    fun lockerStatusTime(): Instant? = time
    fun lockerStatus(): LockerStatus = status

    fun setLockerStatusTime(d: Instant?) {
        time = d
        updateText()
    }

    fun setLockerStatus(s: LockerStatus) {
        status = s
        updateText()
    }

    fun setLockerMessage(m: String?) {
        message = m
        updateText()
    }

    private fun updateText() {
        val now = Instant.now()
        val t = time?.let {
            DateUtils.formatDateTime(
                context,
                it.toEpochMilli(),
                DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE
            )
        } ?: "unknown"
        val (statusText, timeText, color) = when (status) {
            LockerStatus.UNKNOWN -> Triple("UNKNOWN", "", R.color.untilAlert)
            LockerStatus.CLOSED -> Triple("CLOSED", "at $t", R.color.untilAlert)
            LockerStatus.OPENED -> Triple("OPEN", "at $t", R.color.untilNotice)
            LockerStatus.WAS_CLOSED -> Triple("CLOSED", "at $t", R.color.untilWarning)
            LockerStatus.WAS_OPEN -> Triple("OPEN", "at $t", R.color.untilNormal)
        }

        val extraText = when {
            timeText.isNotBlank() && message != null -> "\n" + message
            timeText.isBlank() && message != null -> message
            else -> ""
        }

        status_text.text = statusText
        status_time.text = timeText + extraText
        
        val bg = layout.background as GradientDrawable
        Log.d(TAG, "bg=$bg color=$color getcolor=${context.getColor(color)}")
        bg.colorFilter = PorterDuffColorFilter(context.getColor(color), PorterDuff.Mode.SRC_ATOP)

        //layout.setBackgroundColor(context.getColor(color))

        invalidate()
        requestLayout()
    }
}

enum class LockerStatus(val string: String) {
    UNKNOWN("unknown"),
    OPENED("opened"),
    CLOSED("closed"),
    WAS_OPEN("was_open"),
    WAS_CLOSED("was_closed");

    companion object {
        fun find(s: String?, default: LockerStatus = UNKNOWN): LockerStatus {
            for (l in LockerStatus.values()) {
                if (l.string == s)
                    return l
            }

            return default
        }
    }
}
