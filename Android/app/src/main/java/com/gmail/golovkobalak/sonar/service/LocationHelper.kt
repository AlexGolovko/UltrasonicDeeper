package com.gmail.golovkobalak.sonar.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.*

object LocationHelper {
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private val _lastLocation = MutableStateFlow(Location("KyivCenter").apply {
        latitude = 50.4501 // Kyiv's latitude
        longitude = 30.5234 // Kyiv's longitude
        altitude = 0.0 // Altitude in meters
        bearing = 0.0f // Bearing in degrees
        speed = 0.0f // Speed in meters per second
    })
    val lastLocation: StateFlow<Location> = _lastLocation

    // Update the location when it changes
    fun updateLocation(location: Location) {
        Log.d(this.javaClass.name, "location is updated: " + location)
        _lastLocation.value = location
    }

    init {
        createLocationRequest()
        createLocationCallback()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.Builder(100)
            .setMaxUpdateAgeMillis(500)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null && hasLocationChangedByMeters(lastLocation.value, location, 2.0)) {
                    updateLocation(location)
                }
            }
        }
    }

    private fun haversineDistance(location1: Location, location2: Location): Double {
        val lat1 = Math.toRadians(location1.latitude)
        val lon1 = Math.toRadians(location1.longitude)
        val lat2 = Math.toRadians(location2.latitude)
        val lon2 = Math.toRadians(location2.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))

        // Radius of the Earth in meters (mean value)
        val earthRadius = 6371000.0

        return earthRadius * c
    }

    fun hasLocationChangedByMeters(location1: Location, location2: Location, meters: Double): Boolean {
        val distance = haversineDistance(location1, location2)
        return distance >= meters
    }

    fun start(context: Context): Boolean {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(javaClass.name, "No permission to start")
            return false
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient!!.requestLocationUpdates(locationRequest!!, locationCallback!!, Looper.getMainLooper())
        return true
    }

    fun stopLocationUpdates() {
        fusedLocationClient!!.removeLocationUpdates(locationCallback!!)
    }
}