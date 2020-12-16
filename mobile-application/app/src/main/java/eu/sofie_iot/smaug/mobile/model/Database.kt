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

package eu.sofie_iot.smaug.mobile.model

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import java.math.BigInteger
import java.time.Instant


class Converters {
    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun toLedgerIdentifier(value: Int) = enumValues<LedgerIdentifier>()[value]

    @TypeConverter
    fun fromLedgerIdentifier(value: LedgerIdentifier) = value.ordinal

    @TypeConverter
    fun toLockerType(value: Int) = enumValues<LockerOpenCloseType>()[value]

    @TypeConverter
    fun fromLockerType(value: LockerOpenCloseType) = value.ordinal

    @TypeConverter
    fun fromBigInteger(value: BigInteger) = value.toString()

    @TypeConverter
    fun toBigInteger(value: String) = BigInteger(value)

    @TypeConverter
    fun listToJson(value: List<String>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String): List<String>? = Gson().fromJson(value, Array<String>::class.java)?.toList()
}



@Database(
    entities = arrayOf(
        Locker::class, Marketplace::class, //ActivityRecord::class,
        Bid::class, Rent::class, LockerActivity::class),
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun liveModelsDao(): LiveModelDao
    abstract fun modelsDao(): ModelDao

    private var _liveModels: LiveModelDao? = null
    private var _models: ModelDao? = null

    val liveModels: LiveModelDao
        get() {
        if (_liveModels == null)
            _liveModels = liveModelsDao()
        return _liveModels!!
    }
    val models: ModelDao
        get() {
        if (_models == null)
            _models = modelsDao()
        return _models!!
    }
}