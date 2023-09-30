package com.gmail.golovkobalak.sonar.model

import com.gmail.golovkobalak.sonar.MainActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class SonarDataEntity(
    var depth: Float, var battery: Float, var temperature: String,
    var time: String, var altitude: String, var accuracy: String
) {
    constructor() : this(Float.NaN, Float.NaN, "", "", "", "")


    var sessionId = MainActivity.SESSION_ID
    var altitudeAccuracy: String? = null
    var heading: String? = null
    var latitude = 0.0
    var longitude = 0.0
    var speed: String? = null

    object Field {
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
    }

    constructor(sonarData: SonarData) : this(
        depth = sonarData.depth.toFloat(),
        battery = sonarData.battery.toFloat(),
        temperature = sonarData.temperature,
        time = getCurrentTime(), // You can set the time value here as needed.
        altitude = "", // You can set the altitude value here as needed.
        accuracy = "" // You can set the accuracy value here as needed.
    )

}

private fun getCurrentTime(): String {
    val currentTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return currentTime.format(formatter)
}