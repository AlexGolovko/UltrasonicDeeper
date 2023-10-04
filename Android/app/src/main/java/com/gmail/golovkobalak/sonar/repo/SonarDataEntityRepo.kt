package com.gmail.golovkobalak.sonar.repo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gmail.golovkobalak.sonar.model.SonarDataEntity

@Dao
interface SonarDataEntityRepo {

    @Insert
    fun insert(entity: SonarDataEntity)

    @Query("SELECT * FROM SonarDataEntity")
    fun getAll(): List<SonarDataEntity>

    @Query(
        "SELECT EXISTS (SELECT 1 FROM SonarDataEntity WHERE (depth > :depth-0.2 OR depth < :depth+0.2 )" +
                "AND (altitude > :altitude-0.00001 OR altitude < :altitude+0.00001)" +
                "AND (longitude > :longitude-0.00001 OR longitude < :longitude+0.00001))"
    )
    fun isPointExist(depth: Float, altitude: String, longitude: String): Boolean

    @Query("SELECT * FROM SonarDataEntity WHERE latitude>:latSouth AND latitude<:latNorth AND longitude>:lonWest AND longitude<:lonEast")
    fun getBy(latSouth: Double?, latNorth: Double?, lonWest: Double?, lonEast: Double?): List<SonarDataEntity>
}
