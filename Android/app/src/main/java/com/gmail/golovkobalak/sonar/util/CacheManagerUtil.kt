package com.gmail.golovkobalak.sonar.util

import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

object CacheManagerUtil{
    lateinit var currPositionMarker: Marker
    lateinit var cacheManager: CacheManager
    lateinit var mapView: MapView
}