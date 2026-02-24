package com.kayak.yakak

import java.util.Date
import kotlin.random.Random
import kotlin.uuid.Uuid

data class Task (
    var id : Int = Random.nextInt(),
    var name : String = "",
    var description : String = "",
    var isCompleted : Boolean = false,
    var expirationDate : Date = Date(),
    var creationDate : Date = Date(),
    var icon: String = "",
)