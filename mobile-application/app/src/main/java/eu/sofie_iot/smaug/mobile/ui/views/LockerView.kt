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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.squareup.picasso.Picasso
import eu.sofie_iot.smaug.mobile.R
import kotlinx.android.synthetic.main.locker_view.view.*

open class LockerView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val TAG = "LockerView"

//    var distance: Double? = null
    var imageId: Int = 0
    var name: String = ""
//    var favourite: Boolean = false
    var text: String = ""

    init {
        View.inflate(context, R.layout.locker_view, this)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LockerView,
            0, 0
        ).apply {
            try {
                setLockerImageResource(
                    getResourceId(
                        R.styleable.LockerView_icon,
                        R.drawable.ic_image_placeholder
                    )
                )
                setLockerName(getString(R.styleable.LockerView_name) ?: name)
//                setLockerIsFavourite(getBoolean(R.styleable.LockerView_isFavourite, favourite))
                setLockerText(getString(R.styleable.LockerView_text) ?: text)

//                locker_seen_ble.isGone = true
//                locker_seen_be.isGone = true
//                locker_seen_nfc.isGone = true
//                locker_favourite_icon_on.isGone = true
//                locker_favourite_icon_off.isGone = false

                setLockerShowIcon(getBoolean(R.styleable.LockerView_showIcon, true))

                Log.d(TAG, "image=$imageId name=$name text=$text")
            } finally {
                recycle()
            }
        }
    }

    private fun setLockerShowIcon(show: Boolean) {
        locker_image.isGone = !show
    }

    fun setFavouriteClickListener(l: ((View) -> Unit)?) {
//        locker_favourite_icon_off.setOnClickListener(l)
//        locker_favourite_icon_on.setOnClickListener(l)
    }

    fun lockerImageId(): Int = imageId
    fun setLockerImageId(id: Int) {
        imageId = id
        // TODO: actually change the image
        invalidate()
        requestLayout()
    }

    fun lockerName(): String = name
    fun setLockerName(n: String) {
        name = n
        locker_name.text = name
        invalidate()
        requestLayout()
    }

//    fun lockerIsFavourite(): Boolean = favourite
    fun setLockerIsFavourite(f: Boolean, showToggle: Boolean = true) {
//        favourite = f
//
//        locker_favourite_icon_on.isGone = !f
//        // off is visible if toggle is enabled, and not favourite, otherwise it is hidden
//        locker_favourite_icon_off.isGone = !(showToggle && !f)
//
//        invalidate()
//        requestLayout()
    }

    fun setLockerSeen(ble: Boolean, nfc: Boolean, be: Boolean) {
//        locker_seen_ble.isGone = !ble
//        locker_seen_nfc.isGone = !nfc
//        locker_seen_be.isGone = !be
    }

    fun lockerText(): String = text
    fun setLockerText(s: String?) {
        text = s ?: ""
        locker_text.text = text
        locker_text.isGone = s == null
        invalidate()
        requestLayout()
    }

    fun setLockerImageBitmap(b: Bitmap?) {
        if (b == null)
            setLockerImageResource(android.R.color.transparent)
        else
            locker_image.setImageBitmap(b)
    }

    fun setLockerImageResource(r: Int) {
        locker_image.setImageResource(r)
    }

    fun setLockerImageBase64(s: String?) {
        setLockerImageBytes(s?.let { Base64.decode(s, Base64.DEFAULT) })
    }

    fun setLockerImageBytes(b: ByteArray?) {
        setLockerImageBitmap(b?.let { BitmapFactory.decodeByteArray(it, 0, it.size) })
    }

    fun setLockerDistance(d: Double?) {
//        Log.d(TAG, "setLockerDistance: d=$d")
//        distance = d
//
//        locker_distance.isGone = d == null
//        locker_distance.text = when {
//            d == null -> ""
//            d < 5.0 -> "<5m"
//            d < 10.0 -> "<10m"
//            d < 20.0 -> "<20m"
//            d < 50.0 -> "<50m"
//            else -> ">50m"
//        }
//
//        invalidate()
//        requestLayout()
    }

    fun setLockerImageUrls(imageUrls: List<String>?) {
        if (imageUrls == null || imageUrls.size == 0) {
            setLockerImageResource(R.drawable.ic_image_placeholder)
            return
        }

        Log.d(TAG, "loading image ${imageUrls[0]}")

        Picasso.get()
            .load(imageUrls[0])
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_baseline_broken_image_24)
            .into(locker_image)
    }
}

