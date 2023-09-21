package com.gmail.golovkobalak.sonar.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
//        Log.d(this.javaClass.name, "location is updated: " + location)
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
                if (location != null) {
                    updateLocation(location)
                }
            }
        }
    }

    fun start(context: Context?) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient!!.requestLocationUpdates(locationRequest!!, locationCallback!!, Looper.getMainLooper())
    }

    fun stopLocationUpdates() {
        fusedLocationClient!!.removeLocationUpdates(locationCallback!!)
    }
}