package com.golovkobalak.sonarapp.controller

import android.util.Log
import com.golovkobalak.sonarapp.model.GeoSquare
import com.golovkobalak.sonarapp.service.TrackingService
import com.google.gson.Gson
import io.javalin.Javalin
import io.javalin.config.JavalinConfig
import io.javalin.http.Context
import io.javalin.http.Handler


class TrackingController {
    private var app: Javalin? = null
    fun start() {
        val javalin = Javalin.create { config: JavalinConfig ->
            config.plugins.enableCors { cors ->
                cors.add { it ->
                    it.anyHost()
                }
            }
        }.start(PORT)//registerPlugin(CorsPlugin.forAllOrigins());
        val trackingService = TrackingService()
        javalin.before(Handler { ctx: Context ->
            Log.d(TAG, ctx.fullUrl())
            Log.d(TAG, ctx.body())
        })
        //POST http://localhost:8080/tracking/save
        javalin.post("/tracking", Handler { ctx: Context ->
            val body = ctx.body()
            trackingService.saveTrackingList(body)
            ctx.status(201)
        })
        //GET http://localhost:8080/system/map/cache/dir
        javalin.get("/system/mapCacheDir", Handler { ctx: Context ->
            val mapCacheDir = trackingService.mapCacheDir
            ctx.result(gson.toJson(mapCacheDir))
        })
        //GET http://localhost:8080/marker?north=49.960455723200724&east=36.34042262789566&south=49.955769014252176&west=36.33620619532426
        javalin.get("/marker", Handler { ctx: Context ->
            val queryParamMap = ctx.queryParamMap()
            val geoSquare = GeoSquare(queryParamMap)
            val markers = trackingService.getMarkers(geoSquare)
            ctx.result(gson.toJson(markers))
        })
        javalin.after(Handler { ctx: Context ->
            Log.d(TAG, ctx.fullUrl())
            Log.d(TAG, ctx.body())
        })
        app = javalin
        Log.i(this.javaClass.name, "end creation")
    }

    fun destroy() {
        app?.close()
    }

    companion object {
        private val TAG = TrackingController::class.java.name
        private val gson = Gson()
        const val PORT = 8080
    }
}