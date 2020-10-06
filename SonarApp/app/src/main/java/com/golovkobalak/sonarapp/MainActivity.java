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
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {
    public static final String SESSION_ID = String.valueOf(System.currentTimeMillis());
    private static final String MICROSONAR_SSID = "microsonar";
    private static final String MICROSONAR_SSID_QUOTED = "\"" + MICROSONAR_SSID + "\"";
    private static final String MICROSONAR_PASS = "\"microsonar\"";
    final String TAG = "SonarApp";
    private TextView connStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Realm.init(this.getBaseContext());
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
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show();
        }
    }

    public void connect(View view) {
        //TODO FOR MANUAL TESTING
//        Intent intent = new Intent(MainActivity.this, SonarActivity.class);
//        startActivity(intent);
//        if (true) {
//            return;
//        }
        //
        Toast.makeText(getApplicationContext(), "Connection in progress", Toast.LENGTH_SHORT).show();
        if (changeAP()) {
            connStatus.setText(R.string.text_description);
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        while (connected(MainActivity.this)) {
                            Thread.sleep(1000);
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

    public static boolean connected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo == null || networkInfo.getState() != NetworkInfo.State.CONNECTED;
    }

    public boolean changeAP() {
        log("changeAP");
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        connStatus.setText(R.string.inProcess);
        if (wifiManager == null) {
            log("WiFi Manager == null");
            connStatus.setText(R.string.WifiManagerIsUnavailable);
            return false;
        }
        if (!wifiManager.isWifiEnabled()) {
            connStatus.setText(R.string.WifiIsDisabled);
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        while (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            try {
                log("wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (wifiManager.getConnectionInfo().getFrequency() > 1) {

        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            connStatus.setText(R.string.NotPermit);
            return false;
        }
        if (MICROSONAR_SSID_QUOTED.equalsIgnoreCase(wifiManager.getConnectionInfo().getSSID())) {
            connStatus.setText(R.string.AlreadyConnected);
            return true;
        }
        configureSonarNetwork(wifiManager);
        if (isSonarAvailable(wifiManager)) {
            final List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (final WifiConfiguration configuration : list) {
                if (MICROSONAR_SSID_QUOTED.equalsIgnoreCase(configuration.SSID)) {
                    log("Reconnect to " + configuration.SSID);
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(configuration.networkId, true);
                    log("Reconnected : " + wifiManager.reconnect());
                    connStatus.setText(R.string.SucessfullyConnected);
                    return true;
                }
            }
        }
        connStatus.setText(R.string.SonarIsUnavailable);
        return false;
    }

    private boolean isSonarAvailable(WifiManager wifiManager) {
        final List<ScanResult> scanResults = wifiManager.getScanResults();
        if (scanResults != null) {
            for (final ScanResult scanResult : scanResults) {
                if (MICROSONAR_SSID.equalsIgnoreCase(scanResult.SSID)) {
                    log("Sonar Available");
                    return true;
                }
            }
        }
        log("Sonar is not available");
        return false;
    }


    private void configureSonarNetwork(WifiManager wifiManager) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            connStatus.setText(R.string.NotPermit);
            return;
        }
        boolean isWifiConfigured = false;
        final List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        if (list != null) {
            for (final WifiConfiguration network : list) {
                if (MICROSONAR_SSID_QUOTED.equalsIgnoreCase(network.SSID)) {
                    isWifiConfigured = true;
                    break;
                }
            }
        }
        if (!isWifiConfigured) {
            final WifiConfiguration wfc = createWifiConfiguration();
            wifiManager.addNetwork(wfc);
        }
    }

    private WifiConfiguration createWifiConfiguration() {
        final WifiConfiguration wfc = new WifiConfiguration();
        wfc.SSID = MICROSONAR_SSID_QUOTED;
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

    public void upload(View view) {
    }

    public void log(String log) {
        Log.d(TAG, log);
    }
}
