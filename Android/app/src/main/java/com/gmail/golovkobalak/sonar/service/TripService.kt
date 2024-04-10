package com.gmail.golovkobalak.sonar.service

import com.gmail.golovkobalak.sonar.config.DatabaseConfig
import com.gmail.golovkobalak.sonar.model.TripWithSonarData

object TripService {
    private val tripRepo = DatabaseConfig.db.tripEntityRepoRepo();
    private val sonarDataRepo = DatabaseConfig.db.sonarDataEntityRepo();

    fun getTripWithSonarDataBySessionId(sessionId: String): TripWithSonarData {
        return tripRepo.getBy(sessionId)
            .map {
                val tripId = it.id
                val list = sonarDataRepo.getByTripId(tripId)
                TripWithSonarData(it.sessionId, it.date, list)
            }.orElse(TripWithSonarData())
    }
}