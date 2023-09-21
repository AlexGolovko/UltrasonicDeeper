package com.gmail.golovkobalak.sonar.util

import android.util.Log
import org.osmdroid.tileprovider.cachemanager.CacheManager

class CacheManagerCallback() : CacheManager.CacheManagerCallback {
    var tilesTotal = 1
    override fun onTaskComplete() {
        Log.d(javaClass.name, "onTaskComplete")
        CacheProgress.updateLoading(false)
        CacheProgress.updateProgress(1.0f)
    }

    override fun updateProgress(progress: Int, currentZoomLevel: Int, zoomMin: Int, zoomMax: Int) {
        val progressDecimal = 1.0f * progress / tilesTotal
        Log.d(javaClass.name, "Progress: $progress, in decimal: $progressDecimal")
        CacheProgress.updateProgress(progressDecimal)
    }

    override fun downloadStarted() {
        Log.d(javaClass.name, "downloadStarted")
        CacheProgress.updateLoading(true)
    }

    override fun setPossibleTilesInArea(total: Int) {
        Log.d(javaClass.name, "Total: $total")
        if (total > 0) tilesTotal = total
        CacheProgress.tilesTotal = total

    }

    override fun onTaskFailed(errors: Int) {
        Log.d(javaClass.name, "onTaskFailed: $errors")
        CacheProgress.updateLoading(false)
    }
}
