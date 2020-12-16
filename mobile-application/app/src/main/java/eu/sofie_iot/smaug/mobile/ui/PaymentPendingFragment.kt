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

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eu.sofie_iot.smaug.mobile.R
import kotlinx.android.synthetic.main.fragment_payment_pending.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class PaymentPendingFragment : SmaugFragment(R.layout.fragment_payment_pending) {
    private val TAG = "PaymentPendingFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: PaymentPendingFragmentArgs by navArgs()

        app.db.liveModels.bidById(args.bid.bidId ).observe(viewLifecycleOwner) { bid ->
            Log.d(TAG, "bid change: $bid (bidid ${args.bid.bidId})")

            if (bid == null) {
                Log.d(TAG, "bid not found")
                // TODO add toast or snack notification if bid == null? (note that
                // mainactivity will react to paymentprocessor results and produce
                // success info using snackbar, we don't have to handle completion here)
                findNavController().popBackStack()
            } else {
                // note: if bid is completed, we'll return after a short delay
                if (bid.completed) {
                    lifecycleScope.launch {
                        delay(30000L)

                        Log.d(TAG, "bid completed, popping back after delay")
                        findNavController().popBackStack()
                    }
                }

                pending_price.amount = bid.amount
                pending_price.rate = PaymentFragment.euroPerEth // FIXME

                // determine short text

                val (short, long, drawable) = bid.run {
                    // ordering here is relevant, we go from top to down
                    when {
                        // cancel pending? (
                        cancelTransactionId != null -> Triple(
                            "Cancelling transaction",
                            "Cancelling original transaction $transactionId with " +
                                    "cancel transaction $cancelTransactionId",
                            R.drawable.payment_canceling)

                        // yep
                        resultTransactionId != null -> Triple(
                            if (succeeded) "Success" else "Failure",
                            "Results in transaction $resultTransactionId in block $resultTransactionBlock, " +
                                    "remote message:\n" +
                                    "${resultMessage ?: "<none available>"}",
                            if (succeeded)
                                R.drawable.payment_success
                            else
                                R.drawable.payment_failure)

                        // have transaction block?
                        transactionBlock != null -> Triple(
                            "Payment sent",
                            "Transaction $transactionId, included in block $transactionBlock.\n" +
                                    "Now waiting for marketplace to process payment.",
                            R.drawable.payment_processing)

                        // have id, but no block?
                        transactionId != null -> Triple(
                            "Sending payment",
                            "Transaction $transactionId, pending block",
                            R.drawable.payment_sending)

                        // otherwise we're still preparing the actual payment
                        else -> Triple("Preparing payment",
                            "Payment is being prepared.",
                            R.drawable.payment_preparing)
                    }
                }

                pending_short_description.text = short
                pending_long_description.text = long
                pending_image.setImageDrawable(resources.getDrawable(drawable))

                // if the image is animatable, start it
                (pending_image.drawable as? AnimationDrawable)?.apply {
                    setEnterFadeDuration(250)
                    setExitFadeDuration(250)
                    start()
                }
            }
        }
    }
}
