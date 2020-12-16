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

package eu.sofie_iot.smaug.nfc.messages

import android.util.Log
import com.daveanthonythomas.moshipack.MoshiPack
import eu.sofie_iot.smaug.nfc.messages.RecordTypes.*

fun byteArrayOf(vararg elements: Int): ByteArray =
    elements.map { it.toByte() }.toByteArray()

fun ByteArray.toHex() =
    this.map { String.format("%02x", (it.toInt() and 0xff)) }.joinToString(separator = "")

@ExperimentalUnsignedTypes
fun UByteArray.toHex() =
    this.map { String.format("%02x", (it.toInt() and 0xff)) }.joinToString(separator = "")

fun String.hexStringToByteArray() = ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

enum class RecordTypes(val code: Int) {
    ANNOUNCE_RECORD(0b10_000_000),
    VERIFY_RECORD(0b00_000_001),
    VERIFY_SUCCESS_RECORD(0b10_000_001),
    VERIFY_ERROR_RECORD(0b11_000_001),
    ECHO_RECORD(0b00_100_000),
    ECHO_SUCCESS_RECORD(0b10_100_000),
    QUERY_RECORD(0b00_000_010),
    QUERY_SUCCESS_RECORD(0b10_000_010),
    QUERY_ERROR_RECORD(0b11_000_010),
    OPEN_RECORD(0b00_000_011),
    OPEN_SUCCESS_RECORD(0b10_000_011),
    OPEN_ERROR_RECORD(0b11_000_011),
    CLOSE_RECORD(0b00_000_100),
    CLOSE_SUCCESS_RECORD(0b10_000_100),
    CLOSE_ERROR_RECORD(0b11_000_100)
}

abstract class Record {
    companion object {
        private val m = MoshiPack()

        val ANNOUNCE_RECORD = 0b10_000_000
        val VERIFY_RECORD = 0b00_000_001
        val VERIFY_SUCCESS_RECORD = 0b10_000_001
        val VERIFY_ERROR_RECORD = 0b11_000_001
        val ECHO_RECORD = 0b00_100_000
        val ECHO_SUCCESS_RECORD = 0b10_100_000
        val ECHO_ERROR_RECORD = 0b11_100_000
        val QUERY_RECORD = 0b00_000_010
        val QUERY_SUCCESS_RECORD = 0b10_000_010
        val QUERY_ERROR_RECORD = 0b11_000_010
        val OPEN_RECORD = 0b00_000_011
        val OPEN_SUCCESS_RECORD = 0b10_000_011
        val OPEN_ERROR_RECORD = 0b11_000_011
        val CLOSE_RECORD = 0b00_000_100
        val CLOSE_SUCCESS_RECORD = 0b10_000_100
        val CLOSE_ERROR_RECORD = 0b11_000_100

        private inline fun <reified T : Record> e(c: Int, o : T) : ByteArray =
            byteArrayOf(c) + m.packToByteArray(o)

        fun encode(record: Record): ByteArray {
            return when (record) {
                is Announce -> e(ANNOUNCE_RECORD, record)
                is Verify -> e(VERIFY_RECORD, record)
                is VerifySuccess -> e(VERIFY_SUCCESS_RECORD, record)
                is VerifyError -> e(VERIFY_ERROR_RECORD, record)
                is Echo -> e(ECHO_RECORD, record)
                is EchoSuccess -> e(ECHO_SUCCESS_RECORD, record)
                is EchoError -> e(ECHO_ERROR_RECORD, record)
                is Query -> e(QUERY_RECORD, record)
                is QuerySuccess -> e(QUERY_SUCCESS_RECORD, record)
                is QueryError -> e(QUERY_ERROR_RECORD, record)
                is Open -> e(OPEN_RECORD, record)
                is OpenSuccess -> e(OPEN_SUCCESS_RECORD, record)
                is OpenError -> e(OPEN_ERROR_RECORD, record)
                is Close -> e(CLOSE_RECORD, record)
                is CloseSuccess -> e(CLOSE_SUCCESS_RECORD, record)
                is CloseError -> e(CLOSE_ERROR_RECORD, record)
                else -> throw Exception("record $record is not known encodeable type")
            }
        }

        private inline fun <reified T:Record> d(p : ByteArray): T = m.unpack<T>(p)

        @OptIn(ExperimentalUnsignedTypes::class)
        fun decode(data: ByteArray): Record {
            val code = data[0].toUByte().toInt()
            val payload = data.drop(1).toByteArray()

            return when (code.toInt()) {
                ANNOUNCE_RECORD -> d<Announce>(payload)
                VERIFY_RECORD -> d<Verify>(payload)
                VERIFY_SUCCESS_RECORD -> d<VerifySuccess>(payload)
                VERIFY_ERROR_RECORD -> d<VerifyError>(payload)
                ECHO_RECORD -> d<Echo>(payload)
                ECHO_SUCCESS_RECORD -> d<EchoSuccess>(payload)
                ECHO_ERROR_RECORD -> d<EchoError>(payload)
                QUERY_RECORD -> d<Query>(payload)
                QUERY_SUCCESS_RECORD -> d<QuerySuccess>(payload)
                QUERY_ERROR_RECORD -> d<QueryError>(payload)
                OPEN_RECORD -> d<Open>(payload)
                OPEN_SUCCESS_RECORD -> d<OpenSuccess>(payload)
                OPEN_ERROR_RECORD -> d<OpenError>(payload)
                CLOSE_RECORD -> d<Close>(payload)
                CLOSE_SUCCESS_RECORD -> d<CloseSuccess>(payload)
                CLOSE_ERROR_RECORD -> d<CloseError>(payload)
                else -> throw Exception("code 0x${code.toString(16)} is not known decodeable type")
            }
        }
    }
}

data class Announce(
    val contract_address: String,
    val locker_id: String,
    val name: String = "",
    val image_urls: List<String> = emptyList(),
    val open_close_type: String = "open-tap-close"
) : Record()

data class Verify(val token: String) : Record()
data class VerifySuccess(val message: String = "") : Record()
data class VerifyError(val message: String) : Record()
data class Echo(val message: String) : Record()
data class EchoSuccess(val message: String) : Record()
data class EchoError(val message: String) : Record()
class Query : Record()
data class QuerySuccess(val state: String = "unknown") : Record()
data class QueryError(val message: String) : Record()
class Open : Record()
data class OpenSuccess(val state: String) : Record()
data class OpenError(val message: String, val state: String? = null) : Record()
class Close : Record()
data class CloseSuccess(val state: String) : Record()
data class CloseError(val message: String, val state: String? = null) : Record()

//
//@ExperimentalUnsignedTypes
//object Records {
//    private val TAG = "Records"
//    private val m = MoshiPack()
//
//    fun decode(data: ByteArray): Record {
//        val payload = data.drop(1).toByteArray()
//        val type = data[0].toUByte().toInt()
//
//        Log.d(TAG, "decode: type=${type} payload=${payload.toHex()}")
//
//        val r = when (type) {
//            ANNOUNCE_RECORD.code -> m.unpack<Announce>(payload)
//            VERIFY_SUCCESS_RECORD.code -> m.unpack<VerifySuccess>(payload)
//            VERIFY_ERROR_RECORD.code -> m.unpack<VerifyError>(payload)
//            ECHO_SUCCESS_RECORD.code -> m.unpack<EchoResponse>(payload)
//            QUERY_ERROR_RECORD.code -> m.unpack<QueryError>(payload)
//            QUERY_SUCCESS_RECORD.code -> m.unpack<QuerySuccess>(payload)
//            OPEN_SUCCESS_RECORD.code -> m.unpack<OpenSuccess>(payload)
//            OPEN_ERROR_RECORD.code -> m.unpack<OpenError>(payload)
//            CLOSE_SUCCESS_RECORD.code -> m.unpack<CloseSuccess>(payload)
//            CLOSE_ERROR_RECORD.code -> m.unpack<CloseError>(payload)
//            else -> TODO()
//        }
//
//        return r
//    }
//
//    fun encode(r: Record): ByteArray {
//        Log.d(TAG, "encode: r=$r class=${r::class}")
//        val data = byteArrayOf(r.type.code) + when {
//            r is Announce -> m.packToByteArray(r)
//            r is Verify -> m.packToByteArray(r)
//            r is Echo -> m.packToByteArray(r)
//            r is Query -> m.packToByteArray(r)
//            r is Open -> m.packToByteArray(r)
//            r is Close -> m.packToByteArray(r)
//            else -> TODO()
//        }
//
//        Log.d(TAG, "Encoded $r as: ${data.toHex()}")
//        return data
//    }
//}


