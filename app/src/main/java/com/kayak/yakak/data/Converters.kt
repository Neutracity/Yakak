package com.kayak.yakak.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {
    // Pour LocalDate
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.toString()
    }

    // Pour LocalDateTime
    @TypeConverter
    fun fromFullTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateToFullTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }

    // Pour Location (on le transforme en String "lat,long")
    @TypeConverter
    fun fromLocation(location: Location): String {
        return "${location.latitude},${location.longitude}"
    }

    @TypeConverter
    fun toLocation(value: String): Location {
        val parts = value.split(",")
        return Location(parts[0].toDouble(), parts[1].toDouble())
    }
}