package com.gmail.golovkobalak.sonar.service

import android.util.Log
import com.gmail.golovkobalak.sonar.config.DatabaseConfig
import com.gmail.golovkobalak.sonar.generateBlueGradient
import com.gmail.golovkobalak.sonar.model.CircleDrawable
import kotlinx.coroutines.channels.Channel
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

object MapService {
    val scrollEventChannel = Channel<ScrollEvent>()
    val zoomEventChannel = Channel<ZoomEvent>()
    val mutableList: MutableList<Long> = mutableListOf()


    suspend fun handleScrollEvents() {
        while (true) {
            val scrollEvent = scrollEventChannel.receive()
            Log.d(MapService.javaClass.name, scrollEvent.toString())
            val mapView = scrollEvent.source
            val boundingBox = mapView.boundingBox

            val sonarDataEntityRepo = DatabaseConfig.db.sonarDataEntityRepo()
            val sonarDataEntities = sonarDataEntityRepo.getBy(
                boundingBox?.latSouth,
                boundingBox?.latNorth,
                boundingBox?.lonWest,
                boundingBox?.lonEast
            )
            sonarDataEntities.forEach { sonarDataEntity ->
                if (!mutableList.contains(sonarDataEntity.id)) {
                    mutableList.add(sonarDataEntity.id)
                    val circleMarker = Marker(mapView)
                    val circleDrawable = CircleDrawable.create(200, generateBlueGradient(sonarDataEntity.depth))
                    circleMarker.icon = circleDrawable
                    circleMarker.position =
                        GeoPoint(sonarDataEntity.latitude, sonarDataEntity.longitude.toDouble())
                    circleMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

                }
            }

        }
    }

    suspend fun handleEventEvents() {
        while (true) {
            val zoomEvent = zoomEventChannel.receive()
            Log.d(MapService.javaClass.name, zoomEvent.toString())
        }
    }
}
