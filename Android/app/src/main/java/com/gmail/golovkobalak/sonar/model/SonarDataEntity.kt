package com.gmail.golovkobalak.sonar.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gmail.golovkobalak.sonar.MainActivity
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Entity
data class SonarDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val depth: Float,
    val battery: Float,
    val temperature: String,
    val time: String,
    val altitude: String,
    val longitude: String,
    val accuracy: String
) {
    constructor() : this(0, Float.NaN, Float.NaN, "", "", "", "", "")

    var sessionId = MainActivity.SESSION_ID
    var altitudeAccuracy: String? = null
    var heading: String? = null
    var latitude = 0.0
    var speed: String? = null

    constructor(sonarData: SonarData, altitude: String, longitude: String, accuracy: String) : this(
        id = 0,
        depth = roundToDecimalPlace(sonarData.depth.toFloat(), 1),
        battery = roundToDecimalPlace(sonarData.battery.toFloat(), 2),
        temperature = sonarData.temperature,
        time = getCurrentTime(),
        altitude = altitude,
        longitude = longitude,
        accuracy = accuracy
    )

    override fun toString(): String {
        return "SonarDataEntity(id=$id,sessionId=$sessionId depth=$depth, battery=$battery, " +
                "temperature='$temperature', time='$time', altitude='$altitude', " +
                "longitude='$longitude',accuracy='$accuracy')"
    }
}

private fun getCurrentTime(): String {
    val currentTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return currentTime.format(formatter)
}

private fun roundToDecimalPlace(value: Float, decimalPlaces: Int): Float {
    val df = DecimalFormat("#.${"#".repeat(decimalPlaces)}")
    return df.format(value).toFloat()
}

