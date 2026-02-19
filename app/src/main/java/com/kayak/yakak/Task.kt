package com.kayak.yakak

import java.util.Date

data class Task (
    var name : String = "",
    var description : String = "",
    var isCompleted : Boolean = false,
    var expirationDate : Date = Date(),
    var creationDate : Date = Date(),
    var icon: String = "",
)