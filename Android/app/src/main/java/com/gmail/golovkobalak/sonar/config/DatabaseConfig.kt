package com.gmail.golovkobalak.sonar.config

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gmail.golovkobalak.sonar.model.SonarDataEntity
import com.gmail.golovkobalak.sonar.repo.SonarDataEntityRepo

@Database(entities = [SonarDataEntity::class], version = 1)
abstract class DatabaseConfig : RoomDatabase() {
    abstract fun sonarDataEntityRepo(): SonarDataEntityRepo

    companion object {
        lateinit var db: DatabaseConfig
    }
}