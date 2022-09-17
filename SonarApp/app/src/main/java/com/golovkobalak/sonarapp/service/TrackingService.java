package com.golovkobalak.sonarapp.service;

import android.util.Log;

import com.golovkobalak.sonarapp.SonarContext;
import com.golovkobalak.sonarapp.model.GeoSquare;
import com.golovkobalak.sonarapp.model.Marker;
import com.golovkobalak.sonarapp.model.SonarData;
import com.golovkobalak.sonarapp.repository.SonarDataRepository;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrackingService {
    private static final String TAG = TrackingService.class.getSimpleName();
    private static final Gson gson = new Gson();
    private static final SonarDataRepository repo = new SonarDataRepository();

    public void saveTrackingList(String data) {
        Log.i(TAG, "saveTrackingList: " + data);
        try {
            final SonarData[] sonarDataArray = gson.fromJson(data, SonarData[].class);
            repo.saveList(Arrays.asList(sonarDataArray));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMapCacheDir() {
        return "file://" + SonarContext.FILES_DIR_ABS_PATH + "/Tiles";
    }

    public List<Marker> getMarkers(GeoSquare geoSquare) {
        final ArrayList<Marker> list = new ArrayList<>();
        try {
            final List<SonarData> markers = repo.findByGeoSquare(geoSquare);
            for (SonarData marker : markers) {
                list.add(new Marker(marker.getDepth(), marker.getLatitude(), marker.getLongitude()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
