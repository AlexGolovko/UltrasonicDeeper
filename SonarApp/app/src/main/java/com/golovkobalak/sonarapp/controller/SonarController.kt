package com.golovkobalak.sonarapp.controller

import android.content.res.AssetManager
import android.util.Log
import com.golovkobalak.sonarapp.SonarContext
import com.golovkobalak.sonarapp.model.Config
import io.javalin.Javalin
import io.javalin.config.JavalinConfig
import io.javalin.http.ContentType
import io.javalin.http.ContentType.Companion.getContentTypeByExtension
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException

class SonarController {
    private var app: Javalin? = null
    fun start() {
        app = Javalin.create { javalinConfig: JavalinConfig -> updateConfig(javalinConfig) }
            .start(PORT)
    }

    fun updateConfig(javalinConfig: JavalinConfig) {
        javalinConfig.plugins.enableCors { cors ->
            cors.add { it ->
                it.anyHost()
            }
        }
        val assetManager = SonarContext.assetManager
        val configs: MutableList<Config> = ArrayList()
        try {
            val assetFolderName = "AngularSonar"
            collectConfigFromAsset(assetManager!!, configs, assetFolderName)
            configs.stream() //
                .sorted { a: Config, b: Config -> b.hostedPath.length - a.hostedPath.length } //
                .forEachOrdered { config: Config ->
                    Log.i(this.javaClass.name, "path:" + config.hostedPath)
                    Log.i(this.javaClass.name, "contentType:" + config.contentType.mimeType)
                    javalinConfig.spaRoot.addHandler(config.hostedPath, customHandler = {
                        it.result(config.fileBody)
                        it.contentType(config.contentType)
                    })
                }
        } catch (e: IOException) {
            Log.e(this.javaClass.name, e.message, e)
        }
    }

    @Throws(IOException::class)
    private fun collectConfigFromAsset(
        assetManager: AssetManager,
        configs: MutableList<Config>,
        assetFolderName: String
    ) {
        for (asset in assetManager.list(assetFolderName)!!) {
            if (asset.contains(".")) {
                val contentType = getContentType(asset)
                val fileName = assetFolderName + File.separator + asset
                val hostedPath = getHostedPath(assetFolderName, asset)
                val fileBody = getFileBody(assetManager, fileName)
                configs.add(Config(hostedPath, fileBody, contentType))
            } else {
                collectConfigFromAsset(
                    assetManager,
                    configs,
                    assetFolderName + File.separator + asset
                )
            }
        }
    }

    private fun getHostedPath(assetFolderName: String, asset: String): String {
        val separatorIndex = assetFolderName.indexOf(File.separator)
        var hostedRoot = ""
        if (separatorIndex > 0) {
            hostedRoot = assetFolderName.substring(separatorIndex)
        }
        var hostedPath = hostedRoot + File.separator + asset
        if (asset.equals("index.html", ignoreCase = true)) {
            hostedPath = File.separator
        }
        return hostedPath
    }

    @Throws(IOException::class)
    private fun getFileBody(assetManager: AssetManager, fileName: String): ByteArray {
        val bytes: ByteArray
        BufferedInputStream(assetManager.open(fileName)).use { bis ->
            bytes = ByteArray(bis.available())
            if (bis.read(bytes) == -1) {
                Log.w(this.javaClass.name, "The file:$fileName is empty")
            }
        }
        return bytes
    }

    private fun getContentType(asset: String): ContentType {
        return getContentTypeByExtension(asset.substring(asset.lastIndexOf(".") + 1))!!
    }

    fun destroy() {
        app!!.close()
    }

    companion object {
        private const val PORT = 4242
    }
}