package com.golovkobalak.sonarapp.controller;

import android.content.Context;
import android.util.Log;

import com.golovkobalak.sonarapp.model.GeoSquare;
import com.golovkobalak.sonarapp.service.TrackingService;

import java.util.List;
import java.util.Map;

import io.javalin.Javalin;

public class TrackingController {

    private static final String TAG = TrackingController.class.getName();
    private final Javalin app;
    private final TrackingService trackingService;

    public TrackingController(Context context) {
        this.app = Javalin.create().start(8080);
        this.trackingService = new TrackingService(context);
        app.before(ctx -> {
            Log.d(TAG, ctx.fullUrl());
            Log.d(TAG, ctx.body());
        });
        app.post("/tracking/save", ctx -> {
            final String body = ctx.body();
            trackingService.saveTrackingList(body);
            ctx.status(201);
        });
        app.get("/system/map/cache/dir", ctx -> {
            final String mapCacheDir = trackingService.getMapCacheDir();
            ctx.result(mapCacheDir);
        });
        app.get("/marker", ctx -> {
            final Map<String, List<String>> queryParamMap = ctx.queryParamMap();
            final GeoSquare geoSquare = new GeoSquare(queryParamMap);
            final String markers = trackingService.getMarkers(geoSquare);
            ctx.result(markers);
        });
    }


}
