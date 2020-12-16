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

import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import android.util.Log
import eu.sofie_iot.smaug.nfc.messages.Record
import eu.sofie_iot.smaug.nfc.messages.toHex
import kotlin.experimental.or

interface NfcInterface {
    fun select(df: ByteArray): Record?
    fun communicate(r: Record): Record?
    fun close(): Unit
}


@ExperimentalUnsignedTypes
class SmaugNfc private constructor(val iso: IsoDep, val id: ByteArray) : NfcInterface {
    private val TAG = "SmaugNfc"
    private val MAX_FRAME_LENGTH = 200
    private val CONT_BIT = 0b00100000.toByte()

    // SELECT DF SMAUG-AID
    // (0x00, 0xA4, 0x04, 0x00, 0x07, 0xA0, 0x00, 0x00, 0x02, 0x47, 0x10, 0x01

    // 0x00 = CLA b0000 XXXX (ISO/IEC 7816 spec) where XXXX = 00 CC (no secure messaging
    //        indication), and CC = 00 (no logical channels are used / channel #0 selected)
    // 0xA4 = INS SELECT FILE comamnd (see 6.11.3 in ISO 7816 part 4)
    // 0x04 = P1 b0000 0100 "direct selection by DF name"
    // 0x00 = P2 b0000 --00 "first record"
    // 0x07 = LC length = 7 bytes
    // 0xa0 = data field byte 1
    // 0x00 = data field byte 2
    // 0x00 = data field byte 3
    // 0x02 = data field byte 4
    // 0x47 = data field byte 5
    // 0x10 = data field byte 6
    // 0x01 = data field byte 7
    // LE = empty (omitted)

    private fun transceive(data: ByteArray): Record? {
        try {
            val type = data[0]
            val chunks = data.drop(1).chunked(MAX_FRAME_LENGTH)
            val finalFrame = chunks.last()
            val contFrames = chunks.dropLast(1)
            val contType = type or CONT_BIT

            if (contFrames.size > 0)
                Log.d(TAG, "Sending ${data.size} byte frame in ${chunks.size} frames (${chunks.map{it.size}})")

            for (frame in contFrames) {
                val reply = iso.transceive(byteArrayOf(contType) + frame)

                if (reply.size != 1) {
                    Log.e(TAG, "Received non-1 length (${reply.size} bytes) reply to continuation frame: ${reply.toHex()}")
                    return null
                }

                if (reply[0] != contType) {
                    Log.e(TAG, "Received invalid reply type, expected ${contType}, got ${reply[0]}")
                    return null
                }
                // keep sending
            }

            // final chunk, potentially also the only one at this point
            val reply = iso.transceive(byteArrayOf(type) + finalFrame)
            val record = Record.decode(reply)
            Log.d(TAG, "<<< ${reply.toHex()} (${reply.size} bytes) = $record")
            return record
        } catch (e: TagLostException) {
            Log.d(TAG, "Tag lost")
            return null
        }
    }

    override fun select(df: ByteArray): Record? {
        val data = ubyteArrayOf(0x00u, 0xa4u, 0x04u, 0x00u, df.size.toUByte()).toByteArray() + df
        Log.d(TAG, ">>> (select) ${data.toHex()} (${data.size} bytes)")
        return transceive(data)
    }

    override fun communicate(r: Record): Record? {
        val data = Record.encode(r)
        Log.d(TAG, ">>> ($r) ${data.toHex()} (${data.size} bytes)")
        return transceive(data)
    }

    override fun close() {
        iso.close()
    }

    companion object {
        fun get(tag: Tag): SmaugNfc? {
            val iso = IsoDep.get(tag)
            if (iso == null)
                return null

            iso.connect()

            return SmaugNfc(iso, tag.id)
        }
    }
}
