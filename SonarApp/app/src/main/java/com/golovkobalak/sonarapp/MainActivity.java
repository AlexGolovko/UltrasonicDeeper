package com.golovkobalak.sonarapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String MICROSONAR_SSID = "\"microsonar\"";
    private static final String MICROSONAR_PASS = "\"microsonar\"";
    private TextView connStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        connStatus = findViewById(R.id.textDescription);

    }

    public void connect(View view) {
        if (changeAP()) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        while (!isConnected(MainActivity.this)) {
                            Thread.sleep(500);
                        }
                        Intent intent = new Intent(MainActivity.this, SonarActivity.class);
                        startActivity(intent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            };
            t.start();
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }

    public boolean changeAP() {
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            if (!wifiManager.isWifiEnabled()) {
                connStatus.setText(R.string.WifiIsDisabled);
                wifiManager.setWifiEnabled(true);
            }
            while (!isConnected(MainActivity.this)) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            configureSonarNetwork(wifiManager);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                connStatus.setText(R.string.NotPermit);
                return false;
            }
            if (MICROSONAR_SSID.equalsIgnoreCase(wifiManager.getConnectionInfo().getSSID())) {
                connStatus.setText(R.string.AlreadyConnected);
                return true;
            }
            if (isSonarAvailable(wifiManager)) {
                final List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                for (final WifiConfiguration i : list) {
                    if (MICROSONAR_SSID.equalsIgnoreCase(i.SSID)) {
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();
                        connStatus.setText(R.string.SucessfullyConnected);
                        return true;
                    }
                }
            }
            connStatus.setText(R.string.SonarIsUnavailable);
            return false;
        }
        connStatus.setText(R.string.WifiManagerIsUnavailable);
        return false;
    }

    private boolean isSonarAvailable(WifiManager wifiManager) {
        final List<ScanResult> scanResults = wifiManager.getScanResults();
        if (scanResults != null) {
            for (final ScanResult scanResult : scanResults) {
                if (MICROSONAR_SSID.equalsIgnoreCase(scanResult.SSID)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void configureSonarNetwork(WifiManager wifiManager) {
        boolean isWifiConfigured = false;
        for (final WifiConfiguration network : wifiManager.getConfiguredNetworks()) {
            if (MICROSONAR_SSID.equalsIgnoreCase(network.SSID)) {
                isWifiConfigured = true;
                break;
            }
        }
        if (!isWifiConfigured) {
            final WifiConfiguration wfc = createWifiConfiguration();
            wifiManager.addNetwork(wfc);
        }
    }

    private WifiConfiguration createWifiConfiguration() {
        final WifiConfiguration wfc = new WifiConfiguration();
        wfc.SSID = MICROSONAR_SSID;
        wfc.preSharedKey = MICROSONAR_PASS;
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
}
