package com.golovkobalak.sonarapp.model

import com.golovkobalak.sonarapp.MainActivity
import io.realm.RealmObject

open class SonarData(
    var depth: String, var battery: String, var temperature: String,
    var time: String, var altitude: String, var accuracy: String
) : RealmObject() {
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
}