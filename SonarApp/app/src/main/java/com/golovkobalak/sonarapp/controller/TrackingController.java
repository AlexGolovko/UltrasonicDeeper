package com.golovkobalak.sonarapp.controller;

import android.util.Log;

import com.golovkobalak.sonarapp.SonarContext;
import com.golovkobalak.sonarapp.model.GeoSquare;
import com.golovkobalak.sonarapp.model.Marker;
import com.golovkobalak.sonarapp.service.TrackingService;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;

public class TrackingController {

    private static final String TAG = TrackingController.class.getName();
    private static final Gson gson = new Gson();
    public static final int PORT = 8080;
    private Javalin app;
    private final TrackingService trackingService;

    public TrackingController() {
        this.trackingService = new TrackingService();
        try (Javalin appVar = Javalin.create(JavalinConfig::enableCorsForAllOrigins).start(PORT)) {
            appVar.before(ctx -> {
                Log.d(TAG, ctx.fullUrl());
                Log.d(TAG, ctx.body());
            });
            //POST http://localhost:8080/tracking/save
            appVar.post("/tracking", ctx -> {
                final String body = ctx.body();
                trackingService.saveTrackingList(body);
                ctx.status(201);
            });
            //GET http://localhost:8080/system/map/cache/dir
            appVar.get("/system/mapCacheDir", ctx -> {
                final String mapCacheDir = trackingService.getMapCacheDir();
                ctx.result(gson.toJson(mapCacheDir));
            });
            //GET http://localhost:8080/marker?north=49.960455723200724&east=36.34042262789566&south=49.955769014252176&west=36.33620619532426
            appVar.get("/marker", ctx -> {
                final Map<String, List<String>> queryParamMap = ctx.queryParamMap();
                final GeoSquare geoSquare = new GeoSquare(queryParamMap);
                final List<Marker> markers = trackingService.getMarkers(geoSquare);

                ctx.result(gson.toJson(markers));
            });
            appVar.get("/activity", ctx -> {
                ctx.result("{\"activity\":\"" + SonarContext.CURRENT_ACTIVITY + "\"}");
            });
            appVar.after(ctx -> {
                Log.d(TAG, ctx.fullUrl());
                Log.d(TAG, ctx.body());
            });
            app = appVar;
        } catch (Exception e) {
           Log.w(this.getClass().getName(), e);
        }
    }

    public void destroy() {
        app.close();
    }
}
