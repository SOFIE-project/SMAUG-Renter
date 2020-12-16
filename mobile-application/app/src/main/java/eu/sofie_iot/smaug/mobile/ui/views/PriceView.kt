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
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import eu.sofie_iot.smaug.mobile.R
import eu.sofie_iot.smaug.mobile.Util
import kotlinx.android.synthetic.main.price_view.view.*
import java.math.BigDecimal
import java.math.BigInteger

class PriceView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val TAG = "PriceView"

    // weis
    var amount: BigInteger = BigInteger.ZERO
        set(value) {
            field = value
            update()
        }

    // this is in ETHERS, not weis
    var rate: Float = 0.0f
    set(value) {
        field = value
        update()
    }

    var showPrimary: Boolean = true
    set(value) {
        field = value
        update()
    }

    init {
        View.inflate(context, R.layout.price_view, this)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PriceView,
            0, 0
        ).apply {
            try {
                amount = BigInteger(getString(R.styleable.PriceView_amount) ?: "0")
                rate = getFloat(R.styleable.PriceView_exchangeRate, 0.0f)
                showPrimary = getBoolean(R.styleable.PriceView_primary, true)

                Log.d(TAG, "amount=$amount rate=$rate primary=$showPrimary")
            } finally {
                recycle()
            }
        }

        setOnClickListener {
            Log.d(TAG, "tapped, changing primary view")
            showPrimary = !showPrimary
        }

        // TODO preferably we want to persist the selected showPrimary and re-use it later
    }

    private fun update() {

        // turn wei into ethers
        val ethAmount = Util.weiToEth(amount)
        // turn wei amount to actual rate
        val rateAmount = ethAmount * rate

        Log.d(TAG, "update: amount=$amount rate=$rate showPrimary=$showPrimary ethAmount=$ethAmount rateAmount=$rateAmount")

        if (showPrimary) {
            primary_amount.text = "%.2f".format(rateAmount)
            primary_unit.text = "€"
            secondary_amount.text = resources.getString(R.string.eth_secondary, ethAmount, rate)
        } else {
            primary_amount.text = "%.4f".format(ethAmount)
            primary_unit.text = "ETH"
            secondary_amount.text = resources.getString(R.string.eth_primary, rateAmount, rate)
        }
    }
}