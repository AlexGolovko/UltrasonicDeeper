@file:OptIn(ExperimentalTextApi::class)

package com.gmail.golovkobalak.sonar

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.scale
import androidx.lifecycle.viewModelScope
import com.gmail.golovkobalak.sonar.DeeperActivity.Companion.depthGradient
import com.gmail.golovkobalak.sonar.DeeperActivity.Companion.greenArrow
import com.gmail.golovkobalak.sonar.DeeperActivity.Companion.redArrow
import com.gmail.golovkobalak.sonar.deeper.DeeperViewModel
import com.gmail.golovkobalak.sonar.model.CircleDrawable
import com.gmail.golovkobalak.sonar.service.DeeperService
import com.gmail.golovkobalak.sonar.service.LocationHelper
import com.gmail.golovkobalak.sonar.service.sonar.SonarService
import com.gmail.golovkobalak.sonar.util.CacheManagerUtil
import com.gmail.golovkobalak.sonar.util.OsmDroidConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.roundToInt

class DeeperActivity : ComponentActivity() {
    private lateinit var deeperService: DeeperService;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val deeperViewModel = DeeperViewModel()

        deeperService = DeeperService(deeperViewModel)
        deeperViewModel.viewModelScope.launch {
            withContext(Dispatchers.IO) {
                SonarService.connect()
                deeperService.handleSonarMessage()

            }
        }
        deeperViewModel.viewModelScope.launch {
            withContext(Dispatchers.IO) {
                deeperService.checkForMessage()
            }
        }

        LocationHelper.start(this)
        // Initialize OSMdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        redArrow = getArrowPic(R.drawable.arrow_red)
        yellowArrow = getArrowPic(R.drawable.arrow_yellow)
        greenArrow = getArrowPic(R.drawable.arrow_green)
        setContent {
            MaterialTheme {
                DeeperActivityContent(deeperViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocationHelper.stopLocationUpdates()
        SonarService.close()

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


@Composable
fun DeeperActivityContent(deeperViewModel: DeeperViewModel) {
    LaunchedEffect(deeperViewModel.sonarDataEntity) {
        deeperViewModel.sonarDataEntity.collect {
            if (it.depth.isNaN()) return@collect
            CacheManagerUtil.currPositionMarker.icon = greenArrow
            val circleMarker = Marker(CacheManagerUtil.mapView)
            //TODO check that for this position marker with this depth (+-0.5m) exist
            circleMarker.position = CacheManagerUtil.currPositionMarker.position
            val circleDrawable = CircleDrawable.create(200, generateBlueGradient(it.depth))
            circleMarker.icon = circleDrawable
            circleMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            CacheManagerUtil.mapView.overlays.add(CacheManagerUtil.mapView.overlays.size - 1, circleMarker)
        }
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

    Row(
        modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround
    ) {
        MapComponent(Modifier.weight(1f))
        LegendComponent(Modifier.weight(0.1f))
        CanvasComponent(deeperViewModel, Modifier.weight(1f))
    }
}

@Composable
fun LegendComponent(modifier: Modifier) {
    val textMeasure = rememberTextMeasurer()
    val stringBuilder = StringBuilder()
    depthGradient.forEachIndexed { index, colors ->
        stringBuilder.append("$index\n")
    }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawRect(Color.White)

        val rowHeight = size.height / depthGradient.size
        depthGradient.forEachIndexed { index, colors ->
            val rect = Rect(
                left = 0f,
                top = index * rowHeight,
                right = 20.dp.toPx(),
                bottom = (index + 1) * rowHeight
            )

            drawRect(
                color = Color(
                    red = colors[0] / 255f,
                    green = colors[1] / 255f,
                    blue = colors[2] / 255f,
                    alpha = 1f
                ),
                topLeft = rect.topLeft,
                size = rect.size
            )
            val legendDepth = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                ) {
                    append(stringBuilder.toString())

                }

            }
            val legend = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                ) {
                    append("${index + 1}")

                }

            }
            drawText(textMeasurer = textMeasure, text = legend, topLeft = rect.topRight)
        }
    }
}

@Composable
fun MapComponent(modifier: Modifier) {
    Box(
        modifier = modifier, contentAlignment = Alignment.Center
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
                        MapView.getTileSystem().maxLatitude, MapView.getTileSystem().minLatitude, 0
                    )
                    controller.setZoom(19.0)
                    minZoomLevel = 10.0
                    CacheManagerUtil.mapView = this
                    val marker = Marker(this)
                    marker.icon = redArrow
                    marker.setInfoWindow(null)
                    CacheManagerUtil.mapView.overlays.add(marker)
                    CacheManagerUtil.currPositionMarker = marker
                    Log.d(DeeperActivity::class.java.name, "mapView redo")
                }
            }, modifier = Modifier.fillMaxSize()
        )
    }
}

fun generateBlueGradient(value: Double): Int {
    val depth = value.roundToInt()
    if (depth > 29) {
        val rgb = depthGradient[29]
        return android.graphics.Color.rgb(rgb[0], rgb[1], rgb[2])
    }
    val rgb = depthGradient[depth]
    return android.graphics.Color.rgb(rgb[0], rgb[1], rgb[2])
}

@Composable
fun CanvasComponent(deeperViewModel: DeeperViewModel, modifier: Modifier) {
    val textMeasure = rememberTextMeasurer()

    val sonarDataEntityState = deeperViewModel.sonarDataEntity.collectAsState()
    val sonarDataEntity = sonarDataEntityState.value

    val text = buildAnnotatedString {
        var depthText = sonarDataEntity.depth.toString()
        if (sonarDataEntity.depth < 10) {
            depthText = " ${sonarDataEntity.depth}"
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
                color = Color.Black, fontSize = 30.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
            )
        ) {
            append("\n    ${sonarDataEntity.battery} %")
        }
    }
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        // Draw custom graphics on the canvas
        drawRect(Color.White)
        val list = deeperViewModel.list
        val maxDepth = deeperViewModel.maxDepth
        if (list.size == 0) {
            return@Canvas
        }
        val dp = size.height.toDp() / 2 - 10.dp
        val canvasWidth = size.width
        val canvasHeight = size.height
        // Define the stroke style with a thicker line
        val lineWidth = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()).width
        if (list.size == 1) {
            val depthListMaxSize = canvasWidth / lineWidth
            deeperViewModel.updateMaxSize(depthListMaxSize)
        }
        val max = maxDepth * 1.1
        list.forEachIndexed { index, sonarData ->
            val startEndX = canvasWidth - (lineWidth / 2 + index * lineWidth)
            val endLineY = (sonarData.depth * canvasHeight / max).toFloat()
            val orangeColor = Color(1.0f, 0.5f, 0.0f, 1.0f)
            drawLine(
                start = Offset(x = startEndX, y = canvasHeight),
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
            textMeasurer = textMeasure, text = text, topLeft = Offset(dp.toPx(), 0.dp.toPx())
        )
    }
}