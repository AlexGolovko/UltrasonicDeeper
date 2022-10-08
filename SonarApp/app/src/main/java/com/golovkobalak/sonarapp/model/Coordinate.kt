package com.golovkobalak.sonarapp.model

class Coordinate {
    var topTile = 0
    var leftTile = 0
    var bottomTile = 0
    var rightTile = 0
    val tilesNumber: Int
        get() = Math.abs((bottomTile - topTile) * (rightTile - leftTile))
}