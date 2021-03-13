package com.golovkobalak.sonarapp.service;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.golovkobalak.sonarapp.R;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DownloadAction implements Runnable {
    public static final String TAG = DownloadAction.class.getSimpleName();
    private final String url;
    private final String dir;
    private final String file;
    private final ProgressBar progressbar;
    private final Handler uiHandler;
    private final AtomicBoolean isCanceled;
    private ConcurrentHashMap<Thread, OkHttpClient> threadOkHttpClientConcurrentHashMap;

    public DownloadAction(String url, String dir, String file, ProgressBar progressbar, Handler uiHandler, AtomicBoolean isCanceled, ConcurrentHashMap<Thread, OkHttpClient> threadOkHttpClientConcurrentHashMap) {
        this.url = url;
        this.dir = dir;
        this.file = file;
        this.progressbar = progressbar;
        this.uiHandler = uiHandler;
        this.isCanceled = isCanceled;
        this.threadOkHttpClientConcurrentHashMap = threadOkHttpClientConcurrentHashMap;
    }

    @Override
    public void run() {
        if (isCanceled.get()) {
            return;
        }
        final File folder = new File(dir);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Log.i(TAG, "directory cannot be created");
                return;
            }
        }
        final File saveTo = new File(dir, file);
        if (saveTo.exists()) {
            updateProgressBar();
            return;
        }
        getAndSaveTileSync(saveTo);
    }

    public void getAndSaveTileSync(File saveTo) {
        try {
            final InputStream is = getTile(url);
            FileUtils.copyInputStreamToFile(is, saveTo);
            updateProgressBar();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public InputStream getTile(final String url) throws IOException, InterruptedException {
        final Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "OkHttp Sonar")
                .build();
        final OkHttpClient client = getClient();
        final Call call = client.newCall(request);
        return call.execute().body().byteStream();
    }

    private OkHttpClient getClient() {
        final OkHttpClient okHttpClient = threadOkHttpClientConcurrentHashMap.get(Thread.currentThread());
        if (okHttpClient != null) {
            return okHttpClient;
        } else {
            final OkHttpClient client = new OkHttpClient.Builder().connectionPool(new ConnectionPool(32, 5, TimeUnit.MINUTES)).build();
            client.dispatcher().setMaxRequests(32);
            client.dispatcher().setMaxRequestsPerHost(32);
            threadOkHttpClientConcurrentHashMap.put(Thread.currentThread(), client);
            return client;
        }
    }

    private void updateProgressBar() {
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