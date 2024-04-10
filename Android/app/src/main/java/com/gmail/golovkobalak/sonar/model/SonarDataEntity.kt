package com.gmail.golovkobalak.sonar.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Entity
data class SonarDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val depth: Double,
    val battery: Double,
    val temperature: Double,
    val time: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val tripId: Long
) {
    constructor() : this(0, Double.NaN, Double.NaN, Double.NaN, "", Double.NaN, Double.NaN, Double.NaN, Float.NaN, Long.MIN_VALUE)

    constructor(sonarData: SonarData, latitude: Double, longitude: Double, altitude: Double, accuracy: Float, tripId: Long) : this(
        id = 0,
        depth = roundToDecimalPlace(sonarData.depth.toDouble(), 1),
        battery = roundToDecimalPlace(sonarData.battery.toDouble(), 2),
        temperature = sonarData.temperature.toDouble(),
        time = getCurrentTime(),
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        tripId=tripId
    )

    override fun toString(): String {
        return gson.toJson(this)
    }

    companion object {
        var gson = Gson()
    }
}

private fun getCurrentTime(): String {
    val currentTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return currentTime.format(formatter)
}

private fun roundToDecimalPlace(value: Double, decimalPlaces: Int): Double {
    val df = DecimalFormat("#.${"#".repeat(decimalPlaces)}")
    return df.format(value).toDouble()
}


