package com.gmail.golovkobalak.sonar.service

import android.content.Context
import android.content.res.AssetManager

object Runner {
    val locationHelper: LocationHelper = LocationHelper
    fun start(assets: AssetManager, baseContext: Context) {
//        GlobalContext.assetManager = assets
//        GlobalContext.filesDirAbsPath = baseContext.filesDir.absolutePath
        locationHelper.start(baseContext)
    }

}