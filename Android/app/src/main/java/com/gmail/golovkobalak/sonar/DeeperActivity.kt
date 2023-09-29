@file:OptIn(ExperimentalTextApi::class)

package com.gmail.golovkobalak.sonar

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.scale
import androidx.lifecycle.lifecycleScope
import com.gmail.golovkobalak.sonar.DeeperActivity.Companion.depthGradient
import com.gmail.golovkobalak.sonar.DeeperActivity.Companion.greenArrow
import com.gmail.golovkobalak.sonar.DeeperActivity.Companion.redArrow
import com.gmail.golovkobalak.sonar.DeeperActivity.Companion.yellowArrow
import com.gmail.golovkobalak.sonar.model.CircleDrawable
import com.gmail.golovkobalak.sonar.model.SonarDataException
import com.gmail.golovkobalak.sonar.service.LocationHelper
import com.gmail.golovkobalak.sonar.service.sonar.SonarDataFlow
import com.gmail.golovkobalak.sonar.service.sonar.SonarService
import com.gmail.golovkobalak.sonar.util.CacheManagerUtil
import com.gmail.golovkobalak.sonar.util.OsmDroidConfiguration
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.DecimalFormat
import kotlin.math.roundToInt

class DeeperActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch { SonarService.connect() }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Initialize OSMdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        redArrow = getArrowPic(R.drawable.arrow_red)
        yellowArrow = getArrowPic(R.drawable.arrow_yellow)
        greenArrow = getArrowPic(R.drawable.arrow_green)
        setContent {
            MaterialTheme {
                DeeperActivityContent()
            }
        }
    }

    private fun getArrowPic(arrowPic: Int): BitmapDrawable {
        val icon = BitmapFactory.decodeResource(resources, arrowPic)
        val scaledBitmap = icon.scale(icon.width, icon.height, false)
        return BitmapDrawable(scaledBitmap)
    }

    companion object {
        lateinit var redArrow: Drawable;
        lateinit var yellowArrow: Drawable;
        lateinit var greenArrow: Drawable;
        var depthGradient = arrayOf(
            intArrayOf(172, 238, 246),
            intArrayOf(157, 236, 186),
            intArrayOf(96, 144, 144),
            intArrayOf(192, 225, 233),
            intArrayOf(179, 217, 228),
            intArrayOf(166, 209, 223),
            intArrayOf(154, 201, 218),
            intArrayOf(141, 193, 214),
            intArrayOf(129, 185, 210),
            intArrayOf(117, 177, 206),
            intArrayOf(104, 169, 202),
            intArrayOf(92, 161, 198),
            intArrayOf(80, 153, 194),
            intArrayOf(68, 145, 190),
            intArrayOf(56, 137, 186),
            intArrayOf(43, 128, 182),
            intArrayOf(28, 120, 178),
            intArrayOf(7, 112, 173),
            intArrayOf(0, 104, 169),
            intArrayOf(0, 95, 164),
            intArrayOf(0, 87, 159),
            intArrayOf(0, 79, 154),
            intArrayOf(0, 70, 148),
            intArrayOf(0, 62, 142),
            intArrayOf(0, 54, 136),
            intArrayOf(0, 45, 129),
            intArrayOf(0, 36, 122),
            intArrayOf(0, 27, 115),
            intArrayOf(0, 16, 107),
            intArrayOf(0, 4, 99)
        )
    }
}

fun roundToDecimalPlace(value: Float, decimalPlaces: Int): Float {
    val df = DecimalFormat("#.${"#".repeat(decimalPlaces)}")
    return df.format(value).toFloat()
}

@Composable
fun DeeperActivityContent() {
    var depth by remember { mutableStateOf(Float.NaN) }
    var batteryLevel by remember { mutableStateOf(Float.NaN) }
    val depthList by remember { mutableStateOf(mutableListOf<Float>()) }


    LaunchedEffect(SonarDataFlow.SonarDataFlow) {
        SonarDataFlow.SonarDataFlow.collect { sonarData ->
            CacheManagerUtil.currPositionMarker.icon = greenArrow
            if ("" != sonarData.depth) {
                depth = roundToDecimalPlace(sonarData.depth.toFloat(), 1)
                batteryLevel = roundToDecimalPlace(sonarData.battery.toFloat(), 2)
                val circleMarker = Marker(CacheManagerUtil.mapView)
                circleMarker.position = CacheManagerUtil.currPositionMarker.position
                val circleDrawable = CircleDrawable.create(300, generateBlueGradient(sonarData.depth.toFloat()))
                circleMarker.icon = circleDrawable
                CacheManagerUtil.mapView.overlays.add(CacheManagerUtil.mapView.overlays.size - 1, circleMarker)
                depthList.add(0, depth)
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // First half of the screen for the OSMdroid map
        MapComponent(modifier = Modifier.weight(1f))

        CanvasComponent(depth, batteryLevel, depthList, modifier = Modifier.weight(1f))
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
                    marker.icon = redArrow
                    CacheManagerUtil.mapView.overlays.add(marker)
                    CacheManagerUtil.currPositionMarker = marker
                    Log.d(DeeperActivity::class.java.name, "mapView redo")
                }
            },

            modifier = Modifier.fillMaxSize()
        )
    }
    LaunchedEffect(LocationHelper.lastLocation) {
        LocationHelper.lastLocation.collect { newLocation ->
            val geoPoint = GeoPoint(newLocation.latitude, newLocation.longitude)
            CacheManagerUtil.currPositionMarker.position = geoPoint
            CacheManagerUtil.currPositionMarker.setInfoWindow(null)
            CacheManagerUtil.currPositionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            CacheManagerUtil.mapView.controller.setCenter(geoPoint)
            CacheManagerUtil.currPositionMarker.rotation = 45 - newLocation.bearing
        }
    }
    LaunchedEffect(SonarDataFlow.SonarDataErrorFlow) {
        SonarDataFlow.SonarDataErrorFlow.collect { error ->
            if (error.message?.contains(SonarDataException.FAILED_TO_MEASURE) == true) {
                CacheManagerUtil.currPositionMarker.icon = yellowArrow
            } else {
                CacheManagerUtil.currPositionMarker.icon = redArrow
            }
        }
    }

}

fun generateBlueGradient(value: Float): Int {
    val depth = value.roundToInt()
    if (depth > 29) {
        val rgb = depthGradient[29]
        return android.graphics.Color.rgb(rgb[0], rgb[1], rgb[2])
    }
    val rgb = depthGradient[depth]
    return android.graphics.Color.rgb(rgb[0], rgb[1], rgb[2])
}

@Composable
fun CanvasComponent(depth: Float, batteryLevel: Float, depthList: MutableList<Float>, modifier: Modifier) {
    val textMeasure = rememberTextMeasurer()
    val text = buildAnnotatedString {
        var depthText = "$depth"
        if (depth < 10) {
            depthText = " $depth"
        }
        withStyle(
            style = SpanStyle(
                color = Color.Black,
                fontSize = 75.sp,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        ) {
            append(depthText)
        }
        withStyle(
            style = SpanStyle(
                color = Color.Black,
                fontSize = 30.sp,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        ) {
            append(" m")
        }
        withStyle(
            style = SpanStyle(
                color = Color.Black,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        ) {
            append("\n    $batteryLevel %")
        }
    }
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        // Draw custom graphics on the canvas
        drawRect(Color.White)
        if (depthList.size == 0) {
            return@Canvas
        }
        val dp = size.height.toDp() / 2;
        drawText(
            textMeasurer = textMeasure,
            text = text,
            topLeft = Offset(dp.toPx(), 0.dp.toPx())
        )
        val canvasWidth = size.width
        val canvasHeight = size.height
        // Define the stroke style with a thicker line
        val lineWidth =
            androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx()).width

        val depthListMaxSize = canvasWidth / lineWidth
        if (depthList.size > depthListMaxSize) {
            for (index in depthList.size-1 downTo depthListMaxSize.toInt()) {
                depthList.removeAt(index)
            }
        }

        val max = depthList.max() * 1.1f
        depthList.forEachIndexed { index, depth ->
            val startEndX = canvasWidth - (lineWidth / 2 + index * lineWidth)
            val startY = canvasHeight
            val endLineY = depth * canvasHeight / max
            val orangeColor = Color(1.0f, 0.5f, 0.0f, 1.0f)
            drawLine(
                start = Offset(x = startEndX, y = startY),
                end = Offset(x = startEndX, y = endLineY),
                color = Color.Gray,
                strokeWidth = lineWidth,
            )
            drawLine(
                start = Offset(x = startEndX, y = endLineY + 10.dp.toPx()),
                end = Offset(x = startEndX, y = endLineY),
                color = orangeColor,
                strokeWidth = lineWidth,
            )
        }
        drawText(
            textMeasurer = textMeasure,
            text = text,
            topLeft = Offset(dp.toPx(), 0.dp.toPx())
        )
    }
}