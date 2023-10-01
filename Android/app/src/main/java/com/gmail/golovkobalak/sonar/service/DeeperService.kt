package com.gmail.golovkobalak.sonar.service

import android.util.Log
import com.gmail.golovkobalak.sonar.DeeperActivity
import com.gmail.golovkobalak.sonar.config.DatabaseConfig
import com.gmail.golovkobalak.sonar.deeper.DeeperViewModel
import com.gmail.golovkobalak.sonar.model.SonarData
import com.gmail.golovkobalak.sonar.model.SonarDataEntity
import com.gmail.golovkobalak.sonar.model.SonarDataException
import com.gmail.golovkobalak.sonar.service.sonar.SonarService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class DeeperService(val deeperViewModel: DeeperViewModel) {
    private val gson = Gson()
    private var isMessageReceived = false
    private var sonarDataEntityRepo = DatabaseConfig.db.sonarDataEntityRepo()

    suspend fun handleSonarMessage() {
        val all = sonarDataEntityRepo.getAll()
        all.forEach { it ->
            Log.d(DeeperActivity::class.simpleName, it.toString())
        }
        while (true) {
            val text = SonarService.sonarChannel.receive()
            isMessageReceived = true;
            Log.d(Thread.currentThread().name, text)
            try {
                val sonarData = gson.fromJson(text, SonarData::class.java)
                if ("200" == (sonarData.status)) {
                    val sonarDataEntity = SonarDataEntity(
                        sonarData,
                        LocationHelper.lastLocation.value.latitude.toString(),
                        LocationHelper.lastLocation.value.longitude.toString(),
                        LocationHelper.lastLocation.value.accuracy.toString()
                    )
                    withContext(Dispatchers.Main) {
                        deeperViewModel.updateDepth(sonarDataEntity)
                    }
                    if (!sonarDataEntityRepo.isPointExist(
                            sonarDataEntity.depth, sonarDataEntity.altitude, sonarDataEntity.longitude
                        )
                    ) {
                        sonarDataEntityRepo.insert(sonarDataEntity)
                    }
                }
                if ("300" == (sonarData.status)) {
                    withContext(Dispatchers.Main) {
                        deeperViewModel.updateSonarDataError(SonarDataException(message = SonarDataException.FAILED_TO_MEASURE))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
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
            if (!isMessageReceived) {
                if (isNoMessageSent) {
                    deeperViewModel.updateSonarDataError(SonarDataException(message = "Sonar is not send message"))
                }
                isNoMessageSent = true
            } else {
                isNoMessageSent = false
            }
        }
    }


}
