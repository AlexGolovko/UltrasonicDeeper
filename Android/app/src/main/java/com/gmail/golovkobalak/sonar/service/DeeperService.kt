package com.gmail.golovkobalak.sonar.service

import android.util.Log
import com.gmail.golovkobalak.sonar.deeper.DeeperViewModel
import com.gmail.golovkobalak.sonar.model.SonarData
import com.gmail.golovkobalak.sonar.model.SonarDataEntity
import com.gmail.golovkobalak.sonar.model.SonarDataException
import com.gmail.golovkobalak.sonar.service.sonar.SonarService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class DeeperService(val deeperViewModel: DeeperViewModel) {
    private val gson = Gson()
    private var isMessageReceived = false

    suspend fun handleSonarMessage() {
        while (true) {
            val text = SonarService.sonarChannel.receive()
            isMessageReceived = true;
            Log.d(Thread.currentThread().name, text)
            try {
                val sonarData = gson.fromJson(text, SonarData::class.java)
                if ("200" == (sonarData.status)) {
                    val sonarDataEntity = SonarDataEntity(sonarData)
                    sonarDataEntity.depth = roundToDecimalPlace(sonarDataEntity.depth, 1)
                    sonarDataEntity.battery = roundToDecimalPlace(sonarDataEntity.battery, 2)
                    sonarDataEntity.latitude = LocationHelper.lastLocation.value.latitude
                    sonarDataEntity.longitude = LocationHelper.lastLocation.value.longitude
                    withContext(Dispatchers.Main) {
                        deeperViewModel.updateDepth(sonarDataEntity)
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

    private fun roundToDecimalPlace(value: Float, decimalPlaces: Int): Float {
        val df = DecimalFormat("#.${"#".repeat(decimalPlaces)}")
        return df.format(value).toFloat()
    }

}
