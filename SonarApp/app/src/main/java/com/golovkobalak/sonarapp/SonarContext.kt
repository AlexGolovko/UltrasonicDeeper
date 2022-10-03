package com.golovkobalak.sonarapp

import android.content.res.AssetManager

object SonarContext {
    @JvmStatic
    var assetManager: AssetManager? = null

    @JvmStatic
    var filesDirAbsPath: String? = null
}