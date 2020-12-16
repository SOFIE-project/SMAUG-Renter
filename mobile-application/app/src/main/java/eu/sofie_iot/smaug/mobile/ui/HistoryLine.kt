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

import eu.sofie_iot.smaug.mobile.Util
import eu.sofie_iot.smaug.mobile.model.Bid
import eu.sofie_iot.smaug.mobile.model.LockerActivity
import eu.sofie_iot.smaug.mobile.model.Rent
import java.time.Instant

data class HistoryLine(val id: Long, val time: Instant, val title: String, val description: String) {
    companion object {
        fun Bid.asHistoryLine() = HistoryLine(
            0x1000000L or id,
            completedAt ?: created,
            when {
                completed && succeeded -> "Bid succeeded"
                completed && !succeeded -> "Bid failed"
                transactionBlock != null -> "Bid sent (block ${transactionBlock})"
                transactionId != null -> "Bid sent (block pending)"
                else -> "Bid being prepared"
            },
            listOf(
                "%.4f ETH".format(Util.weiToEth(amount)),
                transactionId?.let { "transaction $it" },
                transactionBlock?.let {
                    "in block $it" +
                            (transactionSent?.let {
                                " at " + Util.timeShort(it, created)
                            } ?: "")
                },
                resultMessage?.let { "Result: $resultMessage" },
                resultTransactionId?.let { "transaction $it" },
                resultTransactionBlock?.let {
                    "in block $it" +
                            (resultTransactionIncluded?.let { " at " + Util.timeShort(it, created) }
                                ?: "")
                },
            ).filter { it != null && it.isNotBlank() }.joinToString("\n")
        )

        fun Rent.asHistoryLine() = HistoryLine(
            0x2000000L or id,
            start,
            "Rent until ${Util.timeShort(end, start)} ",
            "${Util.timeLong(start)} to ${Util.timeLong(end)}"
        )

        fun LockerActivity.asHistoryLine() = HistoryLine(
            0x4000000L or id, time, when {
                succeeded -> when (state) {
                    LockerActivity.STATE_OPEN -> "Locker open"
                    LockerActivity.STATE_CLOSED -> "Locker closed"
                    else -> "Locker state unknown"
                }
                !succeeded -> when (type) {
                    LockerActivity.ACTIVITY_QUERY -> "Failed reading"
                    LockerActivity.ACTIVITY_OPEN -> "Failed opening"
                    LockerActivity.ACTIVITY_CLOSE -> "Failed closing"
                    else -> "Failed"
                }
                else -> error("this really ought not to happen")
            }, ""
        )
    }
}