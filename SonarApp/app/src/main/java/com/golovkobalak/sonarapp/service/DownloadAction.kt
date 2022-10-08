package com.golovkobalak.sonarapp.service

import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import com.golovkobalak.sonarapp.R
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class DownloadAction(
    private val url: String,
    private val dir: String,
    private val file: String,
    private val progressbar: ProgressBar,
    private val uiHandler: Handler,
    private val isCanceled: AtomicBoolean,
    private val threadOkHttpClientConcurrentHashMap: ConcurrentMap<Thread, OkHttpClient>
) : Runnable {
    override fun run() {
        if (isCanceled.get()) {
            return
        }
        val folder = File(dir)
        if (!folder.exists() && !folder.mkdirs()) {
            Log.i(TAG, "directory cannot be created")
            return
        }
        val saveTo = File(dir, file)
        if (saveTo.exists()) {
            updateProgressBar()
            return
        }
        getAndSaveTileSync(saveTo)
    }

    fun getAndSaveTileSync(saveTo: File?) {
        try {
            val `is` = getTile(url)
            FileUtils.copyInputStreamToFile(`is`, saveTo)
            updateProgressBar()
        } catch (e: IOException) {
            Log.w(this.javaClass.name, e)
        }
    }

    @Throws(IOException::class)
    fun getTile(url: String): InputStream {
        val request: Request = Request.Builder()
            .url(url)
            .header("User-Agent", "OkHttp Sonar")
            .build()
        val client = client
        val call = client.newCall(request)
        return call.execute().body!!.byteStream()
    }

    private val client: OkHttpClient
        get() {
            val okHttpClient = threadOkHttpClientConcurrentHashMap[Thread.currentThread()]
            return if (okHttpClient != null) {
                okHttpClient
            } else {
                val client: OkHttpClient = OkHttpClient.Builder().connectionPool(
                    ConnectionPool(
                        32,
                        5,
                        TimeUnit.MINUTES
                    )
                ).build()
                client.dispatcher.maxRequests = 32
                client.dispatcher.maxRequestsPerHost = 32
                threadOkHttpClientConcurrentHashMap[Thread.currentThread()] = client
                client
            }
        }

    private fun updateProgressBar() {
        uiHandler.post {
            progressbar.incrementProgressBy(1)
            if (progressbar.progress >= progressbar.max - 3) {
                progressbar.visibility = View.INVISIBLE
                progressbar.progress = 0
                val button = progressbar.rootView.findViewById<Button>(R.id.download_button)
                button.setText(R.string.download)
            }
        }
    }

    companion object {
        val TAG: String = DownloadAction::class.java.simpleName
    }
}