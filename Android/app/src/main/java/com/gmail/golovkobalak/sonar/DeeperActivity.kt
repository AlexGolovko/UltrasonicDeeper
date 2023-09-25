package com.gmail.golovkobalak.sonar

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.scale
import com.gmail.golovkobalak.sonar.service.LocationHelper
import com.gmail.golovkobalak.sonar.util.CacheManagerUtil
import com.gmail.golovkobalak.sonar.util.OsmDroidConfiguration
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class DeeperActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Initialize OSMdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        setContent {
            MaterialTheme {
                DeeperActivityContent()
            }
        }
    }
}

@Composable
fun DeeperActivityContent() {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // First half of the screen for the OSMdroid map
        MapComponent(modifier = Modifier.weight(1f))

        // Second half of the screen for the canvas
        CanvasComponent(modifier = Modifier.weight(1f))
    }
}

@Composable
fun MapComponent(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    // Configure the MapView as needed
                    setTileSource(OsmDroidConfiguration.baseTileSource())
                    setUseDataConnection(true) // Enable map interaction
                    setMultiTouchControls(true)
                    setHorizontalMapRepetitionEnabled(true)
                    setVerticalMapRepetitionEnabled(false)
                    setScrollableAreaLimitLatitude(
                        MapView.getTileSystem().maxLatitude,
                        MapView.getTileSystem().minLatitude,
                        0
                    )
                    controller.setZoom(16.0)
                    minZoomLevel = 3.0
                    CacheManagerUtil.mapView = this
                    val marker = Marker(this)
                    val arrowPic = R.drawable.arrow_red
                    val icon = BitmapFactory.decodeResource(resources, arrowPic)
                    val scaledBitmap = icon.scale(icon.width, icon.height, false)
                    marker.icon = BitmapDrawable(scaledBitmap)
                    CacheManagerUtil.mapView.overlays.add(marker)
                    CacheManagerUtil.currPositionMarker = marker
                }
            },

            modifier = Modifier.fillMaxSize()
        )
    }
    LaunchedEffect(LocationHelper.lastLocation) {
        LocationHelper.lastLocation.collect { newLocation ->
            val geoPoint = GeoPoint(newLocation.latitude, newLocation.longitude)
            CacheManagerUtil.currPositionMarker.position = geoPoint
            CacheManagerUtil.currPositionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            CacheManagerUtil.mapView.controller.setCenter(geoPoint)
            CacheManagerUtil.currPositionMarker.rotation = 45 - newLocation.bearing
        }
    }
}


@Composable
fun CanvasComponent(modifier: Modifier) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        // Draw custom graphics on the canvas
        drawRect(Color.Blue)
        drawCircle(Color.Red, radius = size.minDimension / 4)
    }
}
