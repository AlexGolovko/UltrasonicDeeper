package com.gmail.golovkobalak.sonar.service.sonar

import android.util.Log
import com.gmail.golovkobalak.sonar.config.SonarConfiguration
import kotlinx.coroutines.channels.Channel
import okhttp3.*
import java.util.*

object SonarService {

    private val client by lazy { OkHttpClient() }
    private var webSocket: WebSocket? = null
    val sonarChannel = Channel<String>()

    fun connect() {
        if (webSocket != null) {
            return
        }
        val request: Request = Request.Builder()
            .url(SonarConfiguration.SONAR_URL)
            .build()

        client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                SonarService.webSocket = webSocket
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                sonarChannel.trySend(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                SonarService.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(javaClass.name, t.toString()+ t.stackTraceToString())
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

    fun close() {
        webSocket?.close(1000, "Normal closure")
    }
}