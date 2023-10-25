package com.gmail.golovkobalak.sonar.service

import android.util.Log
import com.gmail.golovkobalak.sonar.config.DatabaseConfig
import com.gmail.golovkobalak.sonar.generateBlueGradient
import com.gmail.golovkobalak.sonar.model.CircleDrawable
import com.gmail.golovkobalak.sonar.util.CacheManagerUtil
import kotlinx.coroutines.channels.Channel
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.util.concurrent.CancellationException

object MapService {
    val scrollEventChannel = Channel<ScrollEvent>()
    val zoomEventChannel = Channel<ZoomEvent>()
    val loadedMarkersList: MutableList<Long> = mutableListOf()


    private val sonarDataEntityRepo = DatabaseConfig.db.sonarDataEntityRepo()

    suspend fun handleScrollEvents() {
        while (true) {
            try {
                val scrollEvent = scrollEventChannel.receive()
                Log.d(MapService.javaClass.name, scrollEvent.toString())
                val mapView = scrollEvent.source
                val boundingBox = mapView.boundingBox
                logBoundingBoxValues(boundingBox)
                val sonarDataEntities = sonarDataEntityRepo.getBy(
                    boundingBox?.latSouth, boundingBox?.latNorth, boundingBox?.lonWest, boundingBox?.lonEast
                )
                sonarDataEntities.stream().parallel().filter { sonarDataEntity ->
                    !loadedMarkersList.contains(sonarDataEntity.id)
                }.forEach { sonarDataEntity ->
                    loadedMarkersList.add(sonarDataEntity.id)
                    val circleMarker = Marker(CacheManagerUtil.mapView)
                    val circleDrawable = CircleDrawable.create(200, generateBlueGradient(sonarDataEntity.depth))
                    circleMarker.icon = circleDrawable
                    circleMarker.position = GeoPoint(sonarDataEntity.latitude, sonarDataEntity.longitude)
                    circleMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    CacheManagerUtil.mapView.overlays.add(circleMarker)
                }
            } catch (e: CancellationException) {
                Log.d("handleScrollEvents", e.toString())
                return
            } catch (e: Exception) {
                Log.e("handleScrollEvents", e.toString())
            }

        }
    }

    suspend fun handleEventEvents() {
        while (true) {
            val zoomEvent = zoomEventChannel.receive()
            Log.d(MapService.javaClass.name, zoomEvent.toString())
        }
    }

    fun logBoundingBoxValues(boundingBox: BoundingBox?) {
        Log.d("BoundingBoxValues", "latSouth: ${boundingBox?.latSouth}")
        Log.d("BoundingBoxValues", "latNorth: ${boundingBox?.latNorth}")
        Log.d("BoundingBoxValues", "lonWest: ${boundingBox?.lonWest}")
        Log.d("BoundingBoxValues", "lonEast: ${boundingBox?.lonEast}")
    }

    fun resetCachedMarkers() {
        loadedMarkersList.clear()
    }

}
