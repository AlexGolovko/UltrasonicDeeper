package com.golovkobalak.sonarapp.service;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.golovkobalak.sonarapp.model.SonarData;
import com.golovkobalak.sonarapp.repository.SonarDataRepository;
import com.google.gson.Gson;

import java.util.Arrays;

public class TrackingInterface {
    private static final String TAG = TrackingInterface.class.getSimpleName();
    private Context context;
    private static Gson gson = new Gson();
    private static SonarDataRepository repo = new SonarDataRepository();
    private String activity;

    public TrackingInterface(Context context, String activity) {
        this.context = context;
        this.activity = activity;
    }

    @JavascriptInterface
    public void saveTrackingList(String data) {
        try {
            final SonarData[] sonarDataArray = gson.fromJson(data, SonarData[].class);
            repo.saveList(Arrays.asList(sonarDataArray));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @JavascriptInterface
    public void downloadMap(String tiles) {
        Log.d(TAG, tiles);
    }

    @JavascriptInterface
    public String getActivity() {
        return activity;
    }
}
