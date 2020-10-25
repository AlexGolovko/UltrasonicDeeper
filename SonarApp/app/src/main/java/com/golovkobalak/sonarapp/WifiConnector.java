package com.golovkobalak.sonarapp;

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

import java.util.Collections;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class WifiConnector {
    private final WifiManager wifiManager;
    private final Context ctx;
    private final AppCompatActivity activity;
    final String TAG = "SonarApp";

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
        final Object lock = new Object();
        final ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest build = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(build, new ConnectivityManager.NetworkCallback() {
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
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            final Intent wifiIntent = new Intent(Settings.Panel.ACTION_WIFI);
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

    public boolean isAccessPointAvailable(String ssid) {
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            final WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier
                    .Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(pass)
//                    .setBssid(MacAddress.fromString("DC:4F:22:7D:CF:57"))
                    .build();


            final NetworkRequest nr = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(wifiNetworkSpecifier)
                    .build();

            final ConnectivityManager cm = (ConnectivityManager)
                    ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
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
                                lock.notify();
                            }
                            throw new RuntimeException("Network is unavailable");
                        }
                    };
            try {
                if (cm == null) {
                    log("ConnectivityManager is null");
                    return false;
                }

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
        final String ssidQuoted = "\"" + ssid + "\"";
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
            return false;
        final List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (final WifiConfiguration configuration : list) {
            if (ssidQuoted.equalsIgnoreCase(configuration.SSID)) {
                log("Reconnect to " + configuration.SSID);
                wifiManager.disconnect();
                wifiManager.enableNetwork(configuration.networkId, true);
                log("Reconnected : " + wifiManager.reconnect());
                int connTemp = 0;
                while (!isConnectedTo(ssid) && connTemp <= 20) {
                    try {
                        ++connTemp;
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (connTemp > 20) {
                    break;
                }
                return true;
            }
        }
        return false;
    }


}

