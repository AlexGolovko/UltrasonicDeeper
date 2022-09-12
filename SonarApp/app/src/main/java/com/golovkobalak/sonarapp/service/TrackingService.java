package com.golovkobalak.sonarapp.service;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.golovkobalak.sonarapp.model.GeoSquare;
import com.golovkobalak.sonarapp.model.Marker;
import com.golovkobalak.sonarapp.model.SonarData;
import com.golovkobalak.sonarapp.repository.SonarDataRepository;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;

public class TrackingService {
    private static final String TAG = TrackingService.class.getSimpleName();
    private final Context context;
    private static final Gson gson = new Gson();
    private static final SonarDataRepository repo = new SonarDataRepository();
    private static final AtomicBoolean isCanceled = new AtomicBoolean(false);
    private static final ConcurrentHashMap<Thread, OkHttpClient> THREAD_OK_HTTP_CLIENT_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    public TrackingService(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void saveTrackingList(String data) {
        Log.i(TAG, "saveTrackingList: " + data);
        try {
            final SonarData[] sonarDataArray = gson.fromJson(data, SonarData[].class);
            repo.saveList(Arrays.asList(sonarDataArray));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public String getMapCacheDir() {
        String filesPath = context.getFilesDir().getAbsolutePath();
        return "file://" + filesPath + "/Tiles";
    }

    @JavascriptInterface
    public String findMarkers(String data) {
        Log.d(TAG, "findMarkers: " + data);
        //"{"north":49.960455723200724,"east":36.34042262789566,"south":49.955769014252176,"west":36.33620619532426}"
        final GeoSquare geoSquare = gson.fromJson(data, GeoSquare.class);
        return getMarkers(geoSquare);
    }

    public String getMarkers(GeoSquare geoSquare) {
        final ArrayList<Marker> list = new ArrayList<>();
        try {
            final List<SonarData> markers = repo.findByGeoSquare(geoSquare);
            for (SonarData marker : markers) {
                list.add(new Marker(marker.getDepth(), marker.getLatitude(), marker.getLongitude()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String response = gson.toJson(list);
        Log.d(TAG, "response:" + response);
        return response;
    }
}
