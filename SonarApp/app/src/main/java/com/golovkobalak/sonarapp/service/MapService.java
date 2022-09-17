package com.golovkobalak.sonarapp.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.golovkobalak.sonarapp.R;
import com.golovkobalak.sonarapp.model.Coordinate;
import com.golovkobalak.sonarapp.repository.SonarDataRepository;
import com.google.gson.Gson;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;

public class MapService {
    private static final String TAG = MapService.class.getSimpleName();
    private final Context context;
    private static final Gson gson = new Gson();
    private static final SonarDataRepository repo = new SonarDataRepository();
    private static final int poolSize = Runtime.getRuntime().availableProcessors() * 4;
    private static final ExecutorService pool = new ThreadPoolExecutor(2, poolSize, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());//Executors.newFixedThreadPool(poolSize);//
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());
    private static final AtomicBoolean isCanceled = new AtomicBoolean(false);
    private static final ConcurrentHashMap<Thread, OkHttpClient> THREAD_OK_HTTP_CLIENT_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    public MapService(Context context) {
        this.context = context;
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


    public void cancelDownloadMap() {
        isCanceled.set(true);
//        CLIENT.dispatcher().cancelAll();
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
