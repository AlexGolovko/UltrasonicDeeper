package com.gmail.golovkobalak.sonar.config

import android.content.res.AssetManager

object GlobalContext {
    @JvmStatic
    var assetManager: AssetManager? = null

    @JvmStatic
    var filesDirAbsPath: String? = null
}