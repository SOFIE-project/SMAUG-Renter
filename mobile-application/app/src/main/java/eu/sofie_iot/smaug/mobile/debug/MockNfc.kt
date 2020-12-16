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

package eu.sofie_iot.smaug.mobile.debug

import android.util.Log
import eu.sofie_iot.smaug.mobile.NfcInterface
import eu.sofie_iot.smaug.mobile.model.LockerActivity
import eu.sofie_iot.smaug.nfc.messages.*

class MockNfc(
    private val lockerId: String, private val contractAddress: String,
    private var state: Int = LockerActivity.STATE_UNKNOWN
) : NfcInterface {
    private val TAG = "MockNfc"

    override fun select(df: ByteArray): Record? {
        return Announce(locker_id = lockerId, contract_address = contractAddress)
    }

    override fun communicate(r: Record): Record? {
        Log.d(TAG, "communicate: $r")

        fun num2state(state: Int) = when (state) {
            LockerActivity.STATE_OPEN -> "open"
            LockerActivity.STATE_CLOSED -> "closed"
            else -> "unknown"
        }

        val reply = when {
            r is Verify -> VerifySuccess()
            r is Query -> QuerySuccess(state = num2state(state))
            r is Open -> {
                state = LockerActivity.STATE_OPEN
                OpenSuccess(state = num2state(state))
            }
            r is Close -> {
                state = LockerActivity.STATE_CLOSED
                CloseSuccess(state = num2state(state))
            }
            else -> null
        }

        Log.d(TAG, "replying to $r with $reply")

        return reply
    }

    override fun close() {
    }
}