package com.gmail.golovkobalak.sonar.service

import android.content.Context
import android.content.res.AssetManager
import com.gmail.golovkobalak.sonar.config.GlobalContext
import com.gmail.golovkobalak.sonar.controller.SonarController
import com.gmail.golovkobalak.sonar.controller.TilesController

object Runner {
    val sonarController: SonarController = SonarController()
    val tilesController: TilesController = TilesController()
    fun start(assets: AssetManager, baseContext: Context) {
        GlobalContext.assetManager = assets
        GlobalContext.filesDirAbsPath = baseContext.filesDir.absolutePath
        sonarController.start()
        tilesController.start()
    }

}