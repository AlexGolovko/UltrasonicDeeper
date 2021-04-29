package com.golovkobalak.sonarapp.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.golovkobalak.sonarapp.R;
import com.golovkobalak.sonarapp.model.Coordinate;
import com.golovkobalak.sonarapp.model.GeoSquare;
import com.golovkobalak.sonarapp.model.Marker;
import com.golovkobalak.sonarapp.model.SonarData;
import com.golovkobalak.sonarapp.repository.SonarDataRepository;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;

public class TrackingInterface {
    private static final String TAG = TrackingInterface.class.getSimpleName();
    private final Context context;
    private static final Gson gson = new Gson();
    private static final SonarDataRepository repo = new SonarDataRepository();
    private static final int poolSize = Runtime.getRuntime().availableProcessors() * 4;
    private static final ExecutorService pool = new ThreadPoolExecutor(2, poolSize, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());//Executors.newFixedThreadPool(poolSize);//
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());
    private String activity;
    private static final AtomicBoolean isCanceled = new AtomicBoolean(false);
    private static final ConcurrentHashMap<Thread, OkHttpClient> THREAD_OK_HTTP_CLIENT_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

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

    public void downloadMap(final String tiles, final ProgressBar progressbar) {
        isCanceled.set(false);
        String formatTiles = tiles.substring(1, tiles.length() - 1).replaceAll("\\\\", "");
        Log.d(TAG, "downloadMap: formatTiles" + formatTiles);
        //topTile: 178065, leftTile: 315293, bottomTile: 178339, rightTile: 315499
        Coordinate coordinate = null;
        try {
            coordinate = gson.fromJson(formatTiles, Coordinate.class);
        } catch (Exception e) {
            Log.e(TAG, "downloadMap: " + formatTiles, e);
        }
        if (coordinate == null) {
            cancelDownloadMap();
            return;
        }
        final int tilesSize = coordinate.getTilesNumber();
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, tilesSize + " tiles will be download and cached", Toast.LENGTH_LONG).show();
            }
        });
        progressbar.setMax(tilesSize);
        final Coordinate finalCoordinate = coordinate;
        for (int firstLevelTile = coordinate.topTile; firstLevelTile < coordinate.bottomTile; firstLevelTile++) {
            for (int secondLevelTile = coordinate.leftTile; secondLevelTile < coordinate.rightTile; secondLevelTile++) {
                //download https://a.tile.openstreetmap.org/19/315066/177906.png "https://a.tile.openstreetmap.org/19/"+secondLevelTile+"/"+firstLevelTile+".png"
                //file:///data/user/0/com.golovkobalak.sonarapp/files/Tiles/19/315066/177906.png
                final String url = "https://a.tile.openstreetmap.org/19/" + secondLevelTile + "/" + firstLevelTile + ".png";
                final String dir = context.getFilesDir() + "/Tiles/19/" + secondLevelTile;
                final String file = firstLevelTile + ".png";
                pool.submit(new DownloadAction(url, dir, file, progressbar, uiHandler, isCanceled, THREAD_OK_HTTP_CLIENT_CONCURRENT_HASH_MAP));
            }
        }
    }

    @JavascriptInterface
    public String getActivity() {
        return activity;
    }

    @JavascriptInterface
    public String getMapCacheDir() {
        String filesPath = context.getFilesDir().getAbsolutePath();
        return "file://" + filesPath + "/Tiles";
    }

    @JavascriptInterface
    public String findMarkers(String data) {
        final ArrayList<Marker> list = new ArrayList<>();
        Log.d(TAG, "findMarkers: " + data);
        //"{"north":49.960455723200724,"east":36.34042262789566,"south":49.955769014252176,"west":36.33620619532426}"
        try {
            final GeoSquare geoSquare = gson.fromJson(data, GeoSquare.class);
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

    public void cancelDownloadMap() {
        isCanceled.set(true);
//        CLIENT.dispatcher().cancelAll();
    }

    public void setActivity(String activityName) {
        activity = activityName;
    }

    private void updateProgressBar(final ProgressBar progressbar) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                progressbar.incrementProgressBy(1);
                if (progressbar.getProgress() >= progressbar.getMax() - 3) {
                    progressbar.setVisibility(View.INVISIBLE);
                    progressbar.setProgress(0);
                    final Button button = progressbar.getRootView().findViewById(R.id.download_button);
                    button.setText(R.string.download);
                }
            }
        });
    }
}
