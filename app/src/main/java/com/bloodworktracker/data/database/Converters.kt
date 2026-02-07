package com.bloodworktracker.data.database

import androidx.room.TypeConverter
import com.bloodworktracker.data.database.entities.ValueStatus
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromValueStatus(status: ValueStatus): String {
        return status.name
    }

    @TypeConverter
    fun toValueStatus(status: String): ValueStatus {
        return ValueStatus.valueOf(status)
    }
}