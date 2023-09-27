package com.gmail.golovkobalak.sonar.model

import com.gmail.golovkobalak.sonar.MainActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


open class SonarDataEntity(
    var depth: String, var battery: String, var temperature: String,
    var time: String, var altitude: String, var accuracy: String
) {
    constructor() : this("", "", "", "", "", "")


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
        depth = sonarData.depth,
        battery = sonarData.battery,
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