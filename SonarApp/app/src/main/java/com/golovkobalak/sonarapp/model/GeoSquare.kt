package com.golovkobalak.sonarapp.model

class GeoSquare(queryParamMap: Map<String, List<String>>) {
    @JvmField
    var north: Double

    @JvmField
    var east: Double

    @JvmField
    var south: Double

    @JvmField
    var west: Double

    init {
        //"{"north":49.960455723200724,"east":36.34042262789566,"south":49.955769014252176,"west":36.33620619532426}"
        north = queryParamMap["north"]!![0].toDouble()
        east = queryParamMap["east"]!![0].toDouble()
        south = queryParamMap["south"]!![0].toDouble()
        west = queryParamMap["west"]!![0].toDouble()
    }
}