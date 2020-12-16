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

import com.daveanthonythomas.moshipack.MoshiPack
import org.hamcrest.CoreMatchers.*
import org.hamcrest.core.IsInstanceOf
import org.hamcrest.core.IsNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*
import kotlin.reflect.full.companionObject

class MessagesTest {
    val announce_valid =
        "8084a96c6f636b65725f6964a7736d6175672d33b0636f6e74726163745f61646472657373ad64756d6d792d61646472657373a46e616d65ac536d617274206c6f636b6572aa696d6167655f75726c7390"
        .hexStringToByteArray()

    @Test
    fun announce_decode() {
        val msg = Record.decode(announce_valid) as Announce

        assertThat(msg.contract_address, equalTo("dummy-address"))
        assertThat(msg.locker_id, equalTo("smaug-3"))
        assertThat(msg.name, equalTo("Smart locker"))
        assertThat(msg.image_urls.size, equalTo(0))
        assertThat(msg.open_close_type, equalTo("open-tap-close"))

        // roundtrip
        val msg2 = Record.decode(Record.encode(msg)) as Announce
        assertThat(msg, equalTo(msg2))
    }
}
