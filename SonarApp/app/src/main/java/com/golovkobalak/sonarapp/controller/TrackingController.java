package com.golovkobalak.sonarapp.controller;

import android.util.Log;

import com.golovkobalak.sonarapp.model.GeoSquare;
import com.golovkobalak.sonarapp.model.Marker;
import com.golovkobalak.sonarapp.service.TrackingService;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import io.javalin.Javalin;

public class TrackingController {

    private static final String TAG = TrackingController.class.getName();
    private static final Gson gson = new Gson();
    public static final int PORT = 8080;
    private final Javalin app;
    private final TrackingService trackingService;

    public TrackingController() {
        this.app = Javalin.create().start(PORT);
        this.trackingService = new TrackingService();
        app.before(ctx -> {
            Log.d(TAG, ctx.fullUrl());
            Log.d(TAG, ctx.body());
        });
        //POST http://localhost:8080/tracking/save
        app.post("/tracking/save", ctx -> {
            final String body = ctx.body();
            trackingService.saveTrackingList(body);
            ctx.status(201);
        });
        //GET http://localhost:8080/system/map/cache/dir
        app.get("/system/map/cache/dir", ctx -> {
            final String mapCacheDir = trackingService.getMapCacheDir();
            ctx.result(mapCacheDir);
        });
        //GET http://localhost:8080/marker?north=49.960455723200724&east=36.34042262789566&south=49.955769014252176&west=36.33620619532426
        app.get("/marker", ctx -> {
            final Map<String, List<String>> queryParamMap = ctx.queryParamMap();
            final GeoSquare geoSquare = new GeoSquare(queryParamMap);
            final List<Marker> markers = trackingService.getMarkers(geoSquare);

            ctx.result(gson.toJson(markers));
        });
    }


}