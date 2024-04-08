package com.gmail.golovkobalak.sonar.config

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gmail.golovkobalak.sonar.model.SonarDataEntity
import com.gmail.golovkobalak.sonar.model.TripEntity
import com.gmail.golovkobalak.sonar.repo.SonarDataEntityRepo
import com.gmail.golovkobalak.sonar.repo.TripEntityRepo
import com.gmail.golovkobalak.sonar.repo.TripWithSonaDataEntityRepo

@Database(entities = [SonarDataEntity::class, TripEntity::class], version = 1)
abstract class DatabaseConfig : RoomDatabase() {
    abstract fun sonarDataEntityRepo(): SonarDataEntityRepo
    abstract fun tripEntityRepoRepo(): TripEntityRepo
    abstract fun tripWithSonaDataEntityRepo(): TripWithSonaDataEntityRepo

    companion object {
        lateinit var db: DatabaseConfig
    }
}