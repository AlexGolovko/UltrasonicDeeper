package com.golovkobalak.sonarapp.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.golovkobalak.sonarapp.MainActivity
import io.moquette.BrokerConstants
import io.moquette.proto.messages.PublishMessage
import io.moquette.server.Server
import io.moquette.server.config.MemoryConfig
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import java.io.File
import java.nio.ByteBuffer
import java.util.*

class MqttBrokerService : Service() {

    private val TAG = MqttBrokerService::class.java.name

    private var mqttServer: Server = Server()
    private val client = MyMqttClient()
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        try {
            Log.d(TAG, "MQTT Server Start")
            val memoryConfig = MemoryConfig(Properties())
            val filePath = this.baseContext.filesDir.path + File.separator + "temp.db"
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
            memoryConfig.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, filePath)
            mqttServer.startServer(memoryConfig)
            createLogger()
            internalPublish()
            Log.d(TAG, "MQTT Server Started")
        } catch (e: Exception) {
            Log.d(TAG, "MQTT Server Start failed")
            e.printStackTrace()
        }
    }

    private fun internalPublish() {
        val msg = PublishMessage()
        msg.payload = ByteBuffer.wrap("Hello, World!".toByteArray())
        msg.topicName = "deeper.depth"
        mqttServer.internalPublish(msg)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MQTT Server Stop")
        mqttServer.stopServer()
    }

    private fun createLogger() {
        client.connect()
        client.subscribe("deeper/depth", { topic, message ->
            // Handle the incoming message
            Log.d(TAG, "Received message on topic $topic: ${message.toString()}")
        })
    }
}