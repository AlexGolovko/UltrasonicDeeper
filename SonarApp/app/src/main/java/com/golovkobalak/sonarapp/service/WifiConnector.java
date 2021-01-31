package com.golovkobalak.sonarapp.service;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.WIFI_SERVICE;

public class WifiConnector {
    private final WifiManager wifiManager;
    private final Context ctx;
    private final AppCompatActivity activity;
    final String TAG = WifiConnector.class.getSimpleName();
    private final Serializable lock = new ReentrantLock();

    public WifiConnector(Context applicationContext, AppCompatActivity mainActivity) {
        this.ctx = applicationContext;
        this.activity = mainActivity;
        this.wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(WIFI_SERVICE);
    }

    public void log(String log) {
        Log.d(TAG, log);
    }

    public boolean isWifiDisabled() {
        return wifiManager == null || !wifiManager.isWifiEnabled();
    }

    private boolean isWifiEnabled() {
        return wifiManager != null && wifiManager.isWifiEnabled();
    }


    public boolean enableWifi() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        final NetworkRequest build = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        final ConnectivityManager.NetworkCallback availableCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onUnavailable() {
                super.onUnavailable();
                log("wifi is disabled");
                synchronized (lock) {
                    lock.notifyAll();
                }
            }

            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                log("wifi enabled");
                synchronized (lock) {
                    lock.notifyAll();
                }
            }

        };
        connectivityManager.registerNetworkCallback(build, availableCallback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            final Intent wifiIntent = new Intent(Settings.Panel.ACTION_WIFI);
            wifiIntent.putExtra("lock", lock);
            activity.startActivityForResult(wifiIntent, 1);
        } else {
            wifiManager.setWifiEnabled(true);
        }

        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        connectivityManager.unregisterNetworkCallback(availableCallback);
        return isWifiEnabled();
    }


    public boolean isConnectedTo(final String ssid) {
        return ("\"" + ssid + "\"").equalsIgnoreCase(wifiManager.getConnectionInfo().getSSID());
    }

    public void configureSonarAccessPoint(String ssid, String pass) {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        boolean isWifiConfigured = false;
        final String ssidQuoted = "\"" + ssid + "\"";
        final List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();//Deprecated
        if (list != null) {
            for (final WifiConfiguration network : list) {
                if (ssidQuoted.equalsIgnoreCase(network.SSID)) {
                    isWifiConfigured = true;
                    break;
                }
            }
        }
        if (!isWifiConfigured) {
            final String passQuoted = "\"" + pass + "\"";
            final WifiConfiguration wfc = createWifiConfiguration(ssidQuoted, passQuoted);
            wifiManager.addNetwork(wfc);
        }

    }

    private WifiConfiguration createWifiConfiguration(String ssidQuoted, String passQuoted) {
        final WifiConfiguration wfc = new WifiConfiguration();
        wfc.SSID = ssidQuoted;
        wfc.preSharedKey = passQuoted;
        wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        return wfc;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean showSuggestion(String ssid, String pass) {
        final WifiNetworkSuggestion suggestion =
                new WifiNetworkSuggestion.Builder()
                        .setSsid(ssid)
                        .setWpa2Passphrase(pass)
                        .build();
        final int status = wifiManager.addNetworkSuggestions(Collections.singletonList(suggestion));

        return status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS || status == WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE;

    }

    public boolean isAccessPointAvailable(final String ssid) {
        wifiManager.startScan();
        synchronized (WifiScanReceiver.getLock()) {
            try {
                final boolean scanState = wifiManager.startScan();
                if (scanState) {
                    log("isAccessPointAvailable: take lock");
                    WifiScanReceiver.getLock().wait();
                    log("isAccessPointAvailable: WifiScanReceiver.getLock().get(): " + WifiScanReceiver.getLock().get());
                    return WifiScanReceiver.getLock().get();
                }
                log("scanState: " + scanState);
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final List<ScanResult> scanResults = wifiManager.getScanResults();
        if (scanResults != null) {
            for (final ScanResult scanResult : scanResults) {
                if (ssid.equalsIgnoreCase(scanResult.SSID)) {
                    log("AP Available");
                    return true;
                }
            }
        }
        log("AP is not available");
        return false;
    }


    public boolean connectTo(final String ssid, String pass) {
        final Object lock = new Object();
        final ConnectivityManager cm = (ConnectivityManager)
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            final WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier
                    .Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(pass)
                    .build();
            final NetworkRequest nr = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(wifiNetworkSpecifier)
                    .build();
            ConnectivityManager.NetworkCallback networkCallback = new
                    ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            super.onAvailable(network);
                            Log.d(TAG, "onAvailable:" + network);
                            cm.bindProcessToNetwork(network);
                            synchronized (lock) {
                                lock.notifyAll();
                            }
                        }

                        @Override
                        public void onUnavailable() {
                            super.onUnavailable();
                            Log.d(TAG, "unAvailable");
                            synchronized (lock) {
                                lock.notifyAll();
                            }
                            throw new RuntimeException("Network is unavailable");
                        }
                    };
            try {
                cm.requestNetwork(nr, networkCallback);
                synchronized (lock) {
                    lock.wait();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        final ConnectivityManager.NetworkCallback connectionCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                if (isConnectedTo(ssid)) {
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            }
        };
        final NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        cm.registerNetworkCallback(networkRequest, connectionCallback);

        final String ssidQuoted = "\"" + ssid + "\"";
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
            return false;
        final List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (final WifiConfiguration configuration : list) {
            if (ssidQuoted.equalsIgnoreCase(configuration.SSID)) {
                log("Reconnect to " + configuration.SSID);
                //wifiManager.disconnect();
                wifiManager.enableNetwork(configuration.networkId, true);
                log("Reconnected : " + wifiManager.reconnect());

                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }
        return false;
    }

    public Serializable getLock() {
        return lock;
    }

}

