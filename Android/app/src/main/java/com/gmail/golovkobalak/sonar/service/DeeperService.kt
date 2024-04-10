package com.gmail.golovkobalak.sonar.service

import android.util.Log
import com.gmail.golovkobalak.sonar.MainActivity
import com.gmail.golovkobalak.sonar.config.DatabaseConfig
import com.gmail.golovkobalak.sonar.deeper.DeeperViewModel
import com.gmail.golovkobalak.sonar.model.SonarData
import com.gmail.golovkobalak.sonar.model.SonarDataEntity
import com.gmail.golovkobalak.sonar.model.SonarDataException
import com.gmail.golovkobalak.sonar.model.TripEntity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate

private const val PRECISE_VALUE = 0.00001f

class DeeperService(val deeperViewModel: DeeperViewModel) {
    private val gson = Gson()
    private var isMessageReceived = false
    private var sonarDataEntityRepo = DatabaseConfig.db.sonarDataEntityRepo()
    private var tripEntityRepo = DatabaseConfig.db.tripEntityRepoRepo()
    private var dispatcher: CoroutineDispatcher = Dispatchers.Default

    suspend fun handleSonarMessage() {
        var tripEntityId: Long
        withContext(dispatcher) {
            val tripEntity = getTripEntity()
            tripEntityId = tripEntity.id;
        }
        while (true) {
            val text = WebsocketService.sonarChannel.receive()
            isMessageReceived = true
            Log.d(Thread.currentThread().name, text)
            try {
                val sonarData = gson.fromJson(text, SonarData::class.java)
                if ("200" == (sonarData.status)) {
                    val sonarDataEntity = SonarDataEntity(
                        sonarData,
                        LocationHelper.lastLocation.value.latitude,
                        LocationHelper.lastLocation.value.longitude,
                        LocationHelper.lastLocation.value.altitude,
                        LocationHelper.lastLocation.value.accuracy,
                        tripEntityId
                    )
                    withContext(dispatcher) {
                        deeperViewModel.updateDepth(sonarDataEntity)

                        val isPointExist = sonarDataEntityRepo.isPointExist(
                            sonarDataEntity.depth - 0.1,
                            sonarDataEntity.depth + 0.1,
                            sonarDataEntity.latitude - PRECISE_VALUE,
                            sonarDataEntity.latitude + PRECISE_VALUE,
                            sonarDataEntity.longitude - PRECISE_VALUE,
                            sonarDataEntity.longitude + PRECISE_VALUE
                        )
                        if (!isPointExist) {
                            sonarDataEntityRepo.insert(sonarDataEntity)
                        }
                    }
                }
                if ("300" == (sonarData.status)) {
                    withContext(dispatcher) {
                        deeperViewModel.updateSonarDataError(SonarDataException(message = SonarDataException.FAILED_TO_MEASURE))
                    }
                }
            } catch (e: Exception) {
                withContext(dispatcher) {
                    deeperViewModel.updateSonarDataError(
                        SonarDataException(
                            message = SonarDataException.FAILED_TO_UNKNOWN_REASON, e
                        )
                    )
                }
            }
        }
    }

    suspend fun checkForMessage() {
        var isNoMessageSent = true
        while (true) {
            delay(1000)
            isNoMessageSent = if (!isMessageReceived) {
                if (isNoMessageSent) {
                    deeperViewModel.updateSonarDataError(SonarDataException(message = "Sonar is not send message"))
                }
                true
            } else {
                false
            }
        }
    }

    private fun getTripEntity(): TripEntity {
        val tripEntityOpt = tripEntityRepo.getBy(MainActivity.SESSION_ID)
        val tripEntity = tripEntityOpt.orElseGet {
            val newTripEntity = TripEntity(sessionId = MainActivity.SESSION_ID, date = LocalDate.now())
            tripEntityRepo.insert(newTripEntity)
            tripEntityRepo.getBy(MainActivity.SESSION_ID).get()
        }
        return tripEntity
    }

}
