package com.gmail.golovkobalak.sonar.repo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gmail.golovkobalak.sonar.model.TripEntity
import java.util.*

@Dao
interface TripEntityRepo {

    @Insert
    fun insert(entity: TripEntity)

    @Query("SELECT * FROM TripEntity")
    fun getAll(): List<TripEntity>


    @Query("SELECT * FROM TripEntity WHERE sessionId=:sessionId")
    fun getBy(sessionId: String): Optional<TripEntity>
}
