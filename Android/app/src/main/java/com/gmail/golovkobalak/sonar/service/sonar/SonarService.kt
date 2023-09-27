package com.gmail.golovkobalak.sonar.service.sonar

import android.util.Log
import com.gmail.golovkobalak.sonar.config.SonarConfiguration
import com.gmail.golovkobalak.sonar.model.SonarData
import com.gmail.golovkobalak.sonar.model.SonarDataEntity
import com.gmail.golovkobalak.sonar.model.SonarDataException
import com.gmail.golovkobalak.sonar.model.SonarDataException.Companion.FAILED_TO_MEASURE
import com.gmail.golovkobalak.sonar.model.SonarDataException.Companion.FAILED_TO_UNKNOWN_REASON
import com.gmail.golovkobalak.sonar.service.LocationHelper
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.*

object SonarService {
    private var isMessageReceived = false
    private val client by lazy { OkHttpClient() }
    private var webSocket: WebSocket? = null

    init {
        GlobalScope.launch {
            checkForMessage()
        }
    }

    fun connect() {
        val request: Request = Request.Builder()
            .url(SonarConfiguration.SONAR_URL)
            .build()

        client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                SonarService.webSocket = webSocket
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                isMessageReceived = true;
                Log.d(javaClass.name,text)
                try {
                    val sonarData = Gson().fromJson(text, SonarData::class.java)
                    if ("200" == (sonarData.status)) {
                        val sonarDataEntity = SonarDataEntity(sonarData)
                        sonarDataEntity.latitude = LocationHelper.lastLocation.value.latitude
                        sonarDataEntity.longitude = LocationHelper.lastLocation.value.longitude
                        SonarDataFlow.SonarDataFlow.value = sonarDataEntity
                    }
                    if ("300" == (sonarData.status)) {
                        SonarDataFlow.SonarDataErrorFlow.value =
                            SonarDataException(message = FAILED_TO_MEASURE)
                    }
                } catch (e: Exception) {
                    SonarDataFlow.SonarDataErrorFlow.value = SonarDataException(message = FAILED_TO_UNKNOWN_REASON, e)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                SonarService.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(javaClass.name, t.toString())
                reconnect()
            }
        })
        Log.d(javaClass.name, "Connect done")
    }

    private fun reconnect() {
        // Delay the reconnection attempt using a timer
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                Log.d(javaClass.name, "Attempt to reconnect")
                connect() // Attempt to reconnect
            }
        }, 1000)
    }


    private suspend fun checkForMessage() {
        var isNoMessageSent = true
        while (true) {
            delay(1000)
            if (!isMessageReceived) {
                if (isNoMessageSent) {
                    SonarDataFlow.SonarDataErrorFlow.value = SonarDataException(message = "Sonar is not send message")
                }
                isNoMessageSent = true
            } else {
                isNoMessageSent = false
            }
        }
    }

    fun close() {
        webSocket?.close(1000, "Normal closure")
    }
}