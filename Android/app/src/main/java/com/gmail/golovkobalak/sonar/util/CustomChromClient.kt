package com.gmail.golovkobalak.sonar.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CustomChromClient(private val context: Context) : WebChromeClient() {
    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        // Check if the app already has the location permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, allow geolocation
            callback?.invoke(origin, true, false)
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                context as ComponentActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )

            // The geolocation request will be handled in onRequestPermissionsResult
        }
    }

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1002
    }
}
