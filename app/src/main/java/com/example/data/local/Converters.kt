package com.example.data.local

import androidx.room.TypeConverter
import com.google.firebase.Timestamp

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Timestamp?): Long? {
        return value?.seconds?.times(1000)?.plus(value.nanoseconds / 1000000)
    }

    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? {
        return value?.let {
            val seconds = it / 1000
            val nanos = ((it % 1000) * 1000000).toInt()
            Timestamp(seconds, nanos)
        }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString("||") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("||").filter { it.isNotBlank() }
    }
}
