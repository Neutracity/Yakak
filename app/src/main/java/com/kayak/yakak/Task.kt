package com.kayak.yakak

import java.time.LocalDate
import kotlin.random.Random

data class Location(
    val latitude: Double,
    val longitude: Double,
)
data class Task (
    val id : Int = Random.nextInt(),
    val name : String = "Title",
    val description : String = "Description",
    val isCompleted : Boolean = false,
    val expirationDate: LocalDate = LocalDate.now(),
    val creationDate: LocalDate = LocalDate.now(),
    val finishedDate: LocalDate? = null,
    val icon: String = "",
    val location : Location = Location(0.0,0.0)
)