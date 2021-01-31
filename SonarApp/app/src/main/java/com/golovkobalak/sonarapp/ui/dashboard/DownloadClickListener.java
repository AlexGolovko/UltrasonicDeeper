package com.golovkobalak.sonarapp.ui.dashboard;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import com.golovkobalak.sonarapp.R;
import com.golovkobalak.sonarapp.service.TrackingInterface;

public class DownloadClickListener implements View.OnClickListener {
    private static final String TAG = DownloadClickListener.class.getSimpleName();
    private final TrackingInterface trackingService;

    public DownloadClickListener(TrackingInterface trackingService) {
        this.trackingService = trackingService;
    }

    @Override
    public void onClick(View v) {
        final Button button = v.getRootView().findViewById(R.id.download_button);
        if (v.getResources().getString(R.string.download).equalsIgnoreCase(button.getText().toString())) {
            button.setText(R.string.cancel);
            downloadTiles(v);
        } else {
            button.setText(R.string.download);
            cancelDownloading(v);
        }
    }

    private void downloadTiles(View mapFragment) {
        final WebView mapView = mapFragment.getRootView().findViewById(R.id.map_view);
        final ProgressBar progressbar = mapFragment.getRootView().findViewById(R.id.download_progress);
        progressbar.setVisibility(View.VISIBLE);
        mapView.evaluateJavascript("javascript:java.getTilesFromJava()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.d(TAG, value);
                trackingService.downloadMap(value, progressbar);
            }
        });


    }

    private void cancelDownloading(View mapFragment) {
        final ProgressBar progressbar = (ProgressBar) mapFragment.getRootView().findViewById(R.id.download_progress);
        progressbar.setVisibility(View.INVISIBLE);
        progressbar.setProgress(0);
        trackingService.cancelDownloadMap();
    }
}
