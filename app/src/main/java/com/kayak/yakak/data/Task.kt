package com.kayak.yakak.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import kotlin.random.Random

enum class RecurrenceFrequency {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY
}

data class Location(
    val latitude: Double,
    val longitude: Double,
)

@Entity(tableName = "tasks")
data class Task (
    @PrimaryKey(autoGenerate = true)
    val id: Int = Random.nextInt(),
    val name: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val expirationDate: LocalDateTime = LocalDateTime.now(),
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val finishedDate: LocalDateTime? = null,
    val reminderList: List<LocalDateTime> = emptyList(),
    val icon: String = "",
    @Embedded
    val location: Location = Location(0.0,0.0),
    
    val isBirthday: Boolean = false,
    val recurrence: RecurrenceFrequency = RecurrenceFrequency.NONE,
    val streakCount: Int = 0,
    val lastCompletedDate: LocalDateTime? = null,
    val profileImageUri: String? = null,
    val showInCalendar: Boolean = true,
    val isAllDay: Boolean = true
)