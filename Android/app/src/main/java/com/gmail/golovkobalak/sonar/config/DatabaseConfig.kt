package com.gmail.golovkobalak.sonar.config

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.gmail.golovkobalak.sonar.model.SonarDataEntity
import com.gmail.golovkobalak.sonar.model.TripEntity
import com.gmail.golovkobalak.sonar.repo.SonarDataEntityRepo
import com.gmail.golovkobalak.sonar.repo.TripEntityRepo

@Database(entities = [SonarDataEntity::class, TripEntity::class], version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration (
            from = 1,
            to = 2,
            spec = DatabaseConfig.AutoMigration::class
        )
    ])
abstract class DatabaseConfig : RoomDatabase() {
    abstract fun sonarDataEntityRepo(): SonarDataEntityRepo
    abstract fun tripEntityRepoRepo(): TripEntityRepo

    companion object {
        lateinit var db: DatabaseConfig
    }

    class AutoMigration: AutoMigrationSpec {   }
}

