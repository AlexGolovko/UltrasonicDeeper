package com.golovkobalak.sonarapp.service

import android.util.Log
import com.golovkobalak.sonarapp.SonarContext.filesDirAbsPath
import com.golovkobalak.sonarapp.model.GeoSquare
import com.golovkobalak.sonarapp.model.Marker
import com.golovkobalak.sonarapp.model.SonarData
import com.golovkobalak.sonarapp.repository.RepoFactory
import com.google.gson.Gson
import java.util.*

class TrackingService {
    fun saveTrackingList(data: String) {
        Log.i(TAG, "saveTrackingList: $data")
        try {
            val sonarDataArray = gson.fromJson(data, Array<SonarData>::class.java)
            repo.saveList(Arrays.asList(*sonarDataArray))
        } catch (e: Exception) {
            Log.w(this.javaClass.name, e)
        }
    }

    val mapCacheDir: String
        get() = "file://" + filesDirAbsPath + "/Tiles"

    fun getMarkers(geoSquare: GeoSquare?): List<Marker> {
        val list = ArrayList<Marker>()
        try {
            val markers = repo.findByGeoSquare(geoSquare)
            for (marker in markers) {
                list.add(Marker(marker.depth!!, marker.latitude, marker.longitude))
            }
        } catch (e: Exception) {
            Log.w(this.javaClass.name, e)
        }
        return list
    }

    fun save(message: String) {
        try {
            Log.i(TAG, "save: $message")
            try {
                val sonarData = gson.fromJson(message, SonarData::class.java)
                if (LocationHelper.getLastLocation() != null) {
                    sonarData.latitude = LocationHelper.getLastLocation().latitude
                    sonarData.longitude = LocationHelper.getLastLocation().longitude
                    sonarData.accuracy = LocationHelper.getLastLocation().accuracy.toString()
                    sonarData.speed = LocationHelper.getLastLocation().speed.toString()
                }

                repo.save(sonarData)
            } catch (e: Exception) {
                Log.w(this.javaClass.name, e)
            }
        } catch (e: Exception) {
            Log.w(this.javaClass.name, e)
        }
    }

    companion object {
        private val TAG = TrackingService::class.java.simpleName
        private val gson = Gson()
        private val repo = RepoFactory().sonarDataRepository();
    }
}