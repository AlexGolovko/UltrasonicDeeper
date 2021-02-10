package com.golovkobalak.sonarapp.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.ProgressBar;

import com.golovkobalak.sonarapp.R;
import com.golovkobalak.sonarapp.model.Coordinate;
import com.golovkobalak.sonarapp.model.SonarData;
import com.golovkobalak.sonarapp.repository.SonarDataRepository;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TrackingInterface {
    private static final String TAG = TrackingInterface.class.getSimpleName();
    private Context context;
    private static Gson gson = new Gson();
    private static SonarDataRepository repo = new SonarDataRepository();
    private String activity;
    private List<AsyncTask> downloadTasks = Collections.emptyList();
    private boolean isCanceled = false;

    public TrackingInterface(Context context, String activity) {
        this.context = context;
        this.activity = activity;
    }

    public TrackingInterface() {

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

    //    @JavascriptInterface
    public void downloadMap(String tiles, final ProgressBar progressbar) {
        isCanceled = false;
        tiles = tiles.replaceFirst("\"", "");
        tiles = tiles.substring(0, tiles.length() - 1);
        tiles = tiles.replaceAll("\\\\", "");
        //topTile: 178065, leftTile: 315293, bottomTile: 178339, rightTile: 315499
        final Coordinate coordinate = gson.fromJson(tiles, Coordinate.class);
        final int tilesSize = coordinate.getTilesNumber();
        progressbar.setMax(tilesSize);
        downloadTasks = new ArrayList(tilesSize);
        for (int firstLevelTile = coordinate.topTile; firstLevelTile < coordinate.bottomTile; firstLevelTile++) {
            for (int secondLevelTile = coordinate.leftTile; secondLevelTile < coordinate.rightTile; secondLevelTile++) {
                if (isCanceled) return;
                //download https://a.tile.openstreetmap.org/19/315066/177906.png "https://a.tile.openstreetmap.org/19/"+secondLevelTile+"/"+firstLevelTile+".png"
                //file:///data/user/0/com.golovkobalak.sonarapp/files/Tiles/19/315066/177906.png
                String url = "https://a.tile.openstreetmap.org/19/" + secondLevelTile + "/" + firstLevelTile + ".png";
                String dir = context.getFilesDir() + "/Tiles/19/" + secondLevelTile;
                String file = firstLevelTile + ".png";
                downloadTasks.add(new DownloadTask(new DownloadTask.DownloadListener() {
                    @Override
                    public void onDownloadComplete(File filename) {
                        Log.d(TAG, "onDownloadComplete: " + filename.exists());
                        progressbar.incrementProgressBy(1);
                        if (progressbar.getProgress() >= tilesSize) {
                            progressbar.setVisibility(View.INVISIBLE);
                            progressbar.setProgress(0);
                            final Button button = progressbar.getRootView().findViewById(R.id.download_button);
                            button.setText(R.string.download);
                        }
                    }

                    @Override
                    public void onDownloadFailure(String msg) {
                        Log.d(TAG, "onDownloadFailure: " + msg);
                    }
                }).execute(url, dir, file));
            }
        }
    }

    @JavascriptInterface
    public String getActivity() {
        return activity;
    }

    @JavascriptInterface
    public String getMapCacheDir(){
        String filesPath = context.getFilesDir().getAbsolutePath();
        return "file://" + filesPath + "/Tiles";
    }

    public void cancelDownloadMap() {
        isCanceled = true;
        for (AsyncTask downloadTask : downloadTasks) {
            downloadTask.cancel(false);
        }
    }
}
