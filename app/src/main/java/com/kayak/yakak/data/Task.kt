package com.kayak.yakak.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

data class Location(
    val latitude: Double,
    val longitude: Double,
)

@Entity(tableName = "tasks")
data class Task (
    @PrimaryKey(autoGenerate = true)
    val id : Int = Random.nextInt(),
    val name : String = "",
    val description : String = "",
    val isCompleted : Boolean = false,
    val expirationDate: LocalDate = LocalDate.now(),
    val creationDate: LocalDate = LocalDate.now(),
    val finishedDate: LocalDateTime? = null,
    val icon: String = "",
    @Embedded
    val location : Location = Location(0.0,0.0)
)