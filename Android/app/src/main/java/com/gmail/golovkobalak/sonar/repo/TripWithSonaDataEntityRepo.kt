package com.gmail.golovkobalak.sonar.repo

import androidx.room.Dao
import androidx.room.Query
import com.gmail.golovkobalak.sonar.model.TripWithSonarData
import java.util.*

@Dao
interface TripWithSonaDataEntityRepo {

    @Query("SELECT * FROM TripEntity")
    fun getAll(): List<TripWithSonarData>


    @Query("SELECT * FROM TripEntity WHERE sessionId=:sessionId")
    fun getBy(sessionId: String): Optional<TripWithSonarData>
}
