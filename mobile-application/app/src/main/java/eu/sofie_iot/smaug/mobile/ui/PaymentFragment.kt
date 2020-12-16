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

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.kevalpatel2106.rulerpicker.RulerValuePickerListener
import eu.sofie_iot.smaug.mobile.R
import eu.sofie_iot.smaug.mobile.model.Bid
import kotlinx.android.synthetic.main.fragment_payment.*
import kotlinx.coroutines.runBlocking
import java.math.BigInteger
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

class PaymentFragmentViewModel: ViewModel() {
    val lengthMinutes = MutableLiveData<Int>(60)
}


class PaymentFragment : SmaugFragment(R.layout.fragment_payment) {
    private val TAG = "PaymentFragment"

    companion object {
        // wei per minute rate
        // TODO this comes from contract, fix not hardcoded
        val perMinuteRate = BigInteger("100000000000000")

        // ETH to € exchange rate
        // TODO: fetch this from some online source
        val euroPerEth = 324.93f // 2020-11-03 rate
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: LockerDetailsFragmentArgs by navArgs()
        val id = args.locker.lockerId
        val quanta = 15 // FIXME make this parameter of the locker

        val vm: PaymentFragmentViewModel by viewModels()

        price.rate = euroPerEth

        vm.lengthMinutes.observe(viewLifecycleOwner) {
            Log.d(TAG, "length value changed: $it")

            val now = Instant.now().truncatedTo(ChronoUnit.MINUTES)
            val until = now.plusSeconds(it.toLong() * 60)

            when {
                it in 0..59 -> {
                    rental_length.text = it.toString()
                    rental_length_units.text = "minutes"
                }
                else -> {
                    rental_length.text = "${it / 60}:${"%02d".format(it % 60)}"
                    rental_length_units.text = "hours"
                }

            }
            rental_until.text = "Until " + DateTimeFormatter.ofLocalizedDateTime(
                FormatStyle.MEDIUM,
                FormatStyle.SHORT
            )
                .withZone(ZoneId.systemDefault())
                .format(until)

            price.amount = it.toBigInteger().multiply(perMinuteRate)
        }

        rental_length_selection.selectValue(vm.lengthMinutes.value!! / quanta)

        rental_length_selection.setValuePickerListener(object : RulerValuePickerListener {
            override fun onValueChange(value: Int) {
                if (vm.lengthMinutes.value != value)
                    vm.lengthMinutes.postValue(quanta * value)
            }

            override fun onIntermediateValueChange(value: Int) {
                if (vm.lengthMinutes.value != value)
                    vm.lengthMinutes.postValue(quanta * value)
            }
        })

        cancel_payment.setOnClickListener {
            TODO("cancel")
        }

        start_payment.setOnClickListener {
            Log.d(TAG, "starting payment")

            // whatever we do, make sure the button is disabled until we navigate away, or fail
            try {
                it.isEnabled = false

                // this is actually entirely intentional to run in blocking this operation
                val id = runBlocking {
                    val locker = db.models.lockerById(id)

                    if (locker == null)
                        TODO("This should never occur")

                    // record to locker info that we've starting an op
                    // switch to payment pending view with that payment info

                    // because this is deeply async operation, we do it like this:
                    //
                    // separate pending info table
                    // separate task in application watching that
                    // it has its own state machine to manage pending payments
                    // anyone interested in the pending payment can query a live data of it

                    val bid = Bid(
                        lockerId = locker.id,
                        marketplaceId = locker.marketplaceId,
                        amount = vm.lengthMinutes.value!!.toBigInteger().multiply(perMinuteRate),
                        amountMinutes = vm.lengthMinutes.value!!.toLong()
                    )

                    Log.d(TAG, "inserting bid: $bid")
                    app.db.models.insertBid(bid)
                }

                val action = PaymentFragmentDirections.actionPaymentPending(BidIdentifier(id))
                Log.d(TAG, "navigating to pending with $action")
                findNavController().navigate(action)
            } finally {
                it.isEnabled = true
            }
        }
    }
}
