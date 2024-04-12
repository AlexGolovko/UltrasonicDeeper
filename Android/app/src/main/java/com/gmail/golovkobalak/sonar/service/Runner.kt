package com.gmail.golovkobalak.sonar.service

import android.content.Context
import kotlinx.coroutines.*

object Runner {
    val locationHelper: LocationHelper = LocationHelper
    val job = Job()
    val scope = CoroutineScope(Dispatchers.Default + job)
    fun start(baseContext: Context) {
        scope.launch {
            while (true) {
                if (locationHelper.start(baseContext)) {
                    return@launch
                }
                delay(1000)
            }
        }
    }

    fun stop() {
        job.cancel()
        locationHelper.stopLocationUpdates()
    }
}