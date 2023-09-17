package com.gmail.golovkobalak.sonar

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.gmail.golovkobalak.sonar.service.LocationHelper
import com.gmail.golovkobalak.sonar.ui.theme.SonarTheme
import com.gmail.golovkobalak.sonar.util.CacheManagerCallback
import com.gmail.golovkobalak.sonar.util.CacheProgress
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker



class MapActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(javaClass.name, Environment.getExternalStorageDirectory().absolutePath)
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            SonarTheme {
                MapScreen()
            }
        }
    }
}

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    Surface(modifier = Modifier.fillMaxSize()) {
        // Create a Box to stack the WebView and the button on top of each other
        Box(modifier = Modifier.fillMaxSize()) {
            val mapView = mapView()
            // Collect the progress value and update the state
            val progressFlow = CacheProgress.getProgressFlow()
            LaunchedEffect(progressFlow) {
                progressFlow.collect { currentProgress ->
                    progress = currentProgress
                }
            }
            // Collect the loading state and update isLoading
            val loadingFlow = CacheProgress.isLoadingFlow()
            LaunchedEffect(loadingFlow) {
                loadingFlow.collect { isLoadingValue ->
                    isLoading = isLoadingValue
                }
            }

            // Progress bar
            if (isLoading) {
                // Progress bar (LinearProgressIndicator)
                LinearProgressIndicator(
                    progress = progress, // Replace 0.5f with the actual progress value (0.0 to 1.0)
                    color = Color.Green, // Customize the color of the progress bar
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomEnd) // Align the progress bar to the top-center
                )
            }

            // "Download" button on top layer
            Button(
                onClick = {
                    // Implement your button's click action here
                    scope.launch(Dispatchers.Default) {
                        cacheMap(mapView, context)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Button Clicked", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomEnd) // Align the button to the bottom-end (top layer)
            ) {
                Text("Download detailed map", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun mapView(): MapView {
    val context = LocalContext.current
    Configuration.getInstance()
    Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
    val mapView = MapView(context)
    configureMapView(mapView)
    val marker = Marker(mapView)
    mapView.overlays.add(marker)

    mapView.setUseDataConnection(true) // Enable map interaction
    mapView.setMultiTouchControls(true) // Enable multi-touch gestures
    var locationUpdate = 0;
    LaunchedEffect(LocationHelper.lastLocation) {
        LocationHelper.lastLocation.collect { newLocation ->
            val geoPoint = GeoPoint(newLocation.latitude, newLocation.longitude)
            marker.position = geoPoint
            if (locationUpdate < 3) {
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                mapView.controller.setCenter(geoPoint)
                locationUpdate++;
            }
        }
    }
    AndroidView(modifier = Modifier.fillMaxSize(),
        factory = {
            mapView
        })
    return mapView
}

fun cacheMap(mapView: MapView, context: Context) {
    val cacheManager = CacheManager(mapView)
    cacheManager.downloadAreaAsyncNoUI(context, mapView.boundingBox, 19, 19, CacheManagerCallback())
}

fun configureMapView(mapView: MapView) {
    mapView.setTileSource(TileSource())
    mapView.setMultiTouchControls(true)
    mapView.setHorizontalMapRepetitionEnabled(true);
    mapView.setVerticalMapRepetitionEnabled(false);
    mapView.setScrollableAreaLimitLatitude(MapView.getTileSystem().maxLatitude, MapView.getTileSystem().minLatitude, 0);
    mapView.controller.setZoom(16.0);
    mapView.minZoomLevel = 3.0
}

fun TileSource(): OnlineTileSourceBase {
    //Custom tile source without flag no bulk
    val tileSource: OnlineTileSourceBase = XYTileSource(
        "Mapnik",
        0, 19, 256, ".png", arrayOf(
            "https://a.tile.openstreetmap.org/",
            "https://b.tile.openstreetmap.org/",
            "https://c.tile.openstreetmap.org/"
        ), "© OpenStreetMap contributors",
        TileSourcePolicy(
            2,
            TileSourcePolicy.FLAG_NO_PREVENTIVE
                    or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                    or TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
        )
    )
    return tileSource;
}

@Preview(showBackground = true)
@Composable
fun MapActivityPreview() {
    SonarTheme {
        MapScreen()
    }
}