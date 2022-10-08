package com.golovkobalak.sonarapp.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import com.golovkobalak.sonarapp.model.Coordinate
import com.google.gson.Gson
import okhttp3.OkHttpClient
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean

class MapService(private val context: Context) {
    fun downloadMap(tiles: String, progressbar: ProgressBar) {
        isCanceled.set(false)
        val formatTiles = tiles.substring(1, tiles.length - 1).replace("\\\\".toRegex(), "")
        Log.d(TAG, "downloadMap: formatTiles$formatTiles")
        //topTile: 178065, leftTile: 315293, bottomTile: 178339, rightTile: 315499
        var coordinate: Coordinate? = null
        try {
            coordinate = gson.fromJson(formatTiles, Coordinate::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "downloadMap: $formatTiles", e)
        }
        if (coordinate == null) {
            cancelDownloadMap()
            return
        }
        val tilesSize = coordinate.tilesNumber
        uiHandler.post {
            Toast.makeText(
                context,
                "$tilesSize tiles will be download and cached",
                Toast.LENGTH_LONG
            ).show()
        }
        progressbar.max = tilesSize
        for (firstLevelTile in coordinate.topTile until coordinate.bottomTile) {
            for (secondLevelTile in coordinate.leftTile until coordinate.rightTile) {
                //download https://a.tile.openstreetmap.org/19/315066/177906.png "https://a.tile.openstreetmap.org/19/"+secondLevelTile+"/"+firstLevelTile+".png"
                //file:///data/user/0/com.golovkobalak.sonarapp/files/Tiles/19/315066/177906.png
                val url = "https://a.tile.openstreetmap.org/19/$secondLevelTile/$firstLevelTile.png"
                val dir = context.filesDir.toString() + "/Tiles/19/" + secondLevelTile
                val file = "$firstLevelTile.png"
                pool.submit(
                    DownloadAction(
                        url,
                        dir,
                        file,
                        progressbar,
                        uiHandler,
                        isCanceled,
                        THREAD_OK_HTTP_CLIENT_CONCURRENT_HASH_MAP
                    )
                )
            }
        }
    }

    fun cancelDownloadMap() {
        isCanceled.set(true)
    }

    companion object {
        private val TAG = MapService::class.java.simpleName
        private val gson = Gson()
        private val poolSize = Runtime.getRuntime().availableProcessors() * 4
        private val pool: ExecutorService = ThreadPoolExecutor(
            2,
            poolSize,
            5,
            TimeUnit.MINUTES,
            LinkedBlockingQueue()
        ) //Executors.newFixedThreadPool(poolSize);//
        private val uiHandler = Handler(Looper.getMainLooper())
        private val isCanceled = AtomicBoolean(false)
        private val THREAD_OK_HTTP_CLIENT_CONCURRENT_HASH_MAP =
            ConcurrentHashMap<Thread, OkHttpClient>()
    }
}