package com.gmail.golovkobalak.sonar.service

import android.content.Context
import android.content.res.AssetManager
import com.gmail.golovkobalak.sonar.config.GlobalContext

object Runner {
    val locationHelper: LocationHelper = LocationHelper
    fun start(assets: AssetManager, baseContext: Context) {
        GlobalContext.assetManager = assets
        GlobalContext.filesDirAbsPath = baseContext.filesDir.absolutePath
        locationHelper.start(baseContext)
    }

}