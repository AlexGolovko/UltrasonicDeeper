package com.gmail.golovkobalak.sonar.model

import androidx.room.Embedded
import androidx.room.Relation


data class TripWithSonarData(
    @Embedded val trip: TripEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val sonarDataEntities: List<SonarDataEntity>
)
