package com.gmail.golovkobalak.sonar.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val sessionId: String,
    val date: String,
) {
    constructor(sessionId: String, date: LocalDate) : this(0, sessionId, date.toString())
}