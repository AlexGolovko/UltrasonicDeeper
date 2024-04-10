package com.gmail.golovkobalak.sonar.model


data class TripWithSonarData(
    val sessionId: String,
    val date: String,
    val sonarData: List<SonarDataEntity>
) {
    constructor() : this("", "", listOf())
}
