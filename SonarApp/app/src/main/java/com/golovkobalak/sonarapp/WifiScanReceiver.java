package com.golovkobalak.sonarapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Context.WIFI_SERVICE;

public class WifiScanReceiver extends BroadcastReceiver {
    private final String SSID;
    private static final AtomicBoolean isAvailable = new AtomicBoolean(false);

    public WifiScanReceiver(String ssid) {
        this.SSID = ssid;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SonarApp", "onReceive");
        boolean success = intent.getBooleanExtra(
                WifiManager.EXTRA_RESULTS_UPDATED, false);
        if (success) {
            final List<ScanResult> scanResults = ((WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE)).getScanResults();
            if (scanResults != null) {
                for (final ScanResult scanResult : scanResults) {
                    if (SSID.equalsIgnoreCase(scanResult.SSID)) {
                        synchronized (isAvailable) {
                            Log.d("SonarApp", "AP Available");
                            isAvailable.set(true);
                            isAvailable.notifyAll();
                            break;
                        }
                    }
                }

            }
        }
        synchronized (isAvailable) {
            isAvailable.notifyAll();
        }

    }

    public static AtomicBoolean getLock() {
        return isAvailable;
    }
}
