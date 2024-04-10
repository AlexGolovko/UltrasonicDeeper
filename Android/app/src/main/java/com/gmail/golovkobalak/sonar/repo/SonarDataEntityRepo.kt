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
        "SELECT EXISTS (SELECT 1 FROM SonarDataEntity " +
                "WHERE depth BETWEEN :minDepth AND :maxDepth " +
                "AND latitude BETWEEN :minLatitude AND :maxLatitude " +
                "AND longitude BETWEEN :minLongitude AND :maxLongitude)"
    )
    fun isPointExist(
        minDepth: Double,
        maxDepth: Double,
        minLatitude: Double,
        maxLatitude: Double,
        minLongitude: Double,
        maxLongitude: Double
    ): Boolean


    @Query("SELECT * FROM SonarDataEntity WHERE latitude>:latSouth AND latitude<:latNorth AND longitude>:lonWest AND longitude<:lonEast")
    fun getBy(latSouth: Double?, latNorth: Double?, lonWest: Double?, lonEast: Double?): List<SonarDataEntity>

    @Query("SELECT * FROM SonarDataEntity WHERE tripId=:tripId")
    fun getByTripId(tripId: Long): List<SonarDataEntity>
}
