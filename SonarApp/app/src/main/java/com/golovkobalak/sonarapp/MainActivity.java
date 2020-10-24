package com.golovkobalak.sonarapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {
    public static final String SESSION_ID = String.valueOf(System.currentTimeMillis());
    private static final String MICROSONAR_SSID = "microsonar";
    private static final String MICROSONAR_SSID_QUOTED = "\"" + MICROSONAR_SSID + "\"";
    private static final String MICROSONAR_PASS_QUOTED = "\"microsonar\"";
    private static final String MICROSONAR_PASS = "microsonar";
    final String TAG = "SonarApp";
    private TextView connStatus;
    private WifiConnector wifiConnector;

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
        wifiConnector = new WifiConnector(getApplicationContext(), this);
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
        final Button connectButton = (Button) findViewById(R.id.button_sonar);
        connectButton.setEnabled(false);
        Toast.makeText(getApplicationContext(), "Connection in progress", Toast.LENGTH_SHORT).show();
        if (changeAP()) {
            connStatus.setText(R.string.text_description);
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        while (!wifiConnector.isConnectedTo(MICROSONAR_SSID)) {
                            log("Not connected yet");
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
        connectButton.setEnabled(true);
    }

    public boolean changeAP() {
        log("changeAP");
        connStatus.setText(R.string.inProcess);
        if (wifiConnector.isWifiDisabled()) {
            connStatus.setText(R.string.WifiIsDisabled);
            if (!wifiConnector.enableWifi()) {
                connStatus.setText("Wifi can't be enabled\n Please turn on WIFi yourself");
                return false;
            }
        }
        if (wifiConnector.isConnectedTo(MICROSONAR_SSID)) {
            connStatus.setText(R.string.AlreadyConnected);
            log((String) getText(R.string.AlreadyConnected));
            return true;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            connStatus.setText(R.string.NotPermit);
            log((String) getText(R.string.NotPermit));
            return false;
        }
        if (!(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q))
            wifiConnector.configureSonarAccessPoint(MICROSONAR_SSID, MICROSONAR_PASS);
        //TODO it should in another place ?
//            wifiConnector.showSuggestion(MICROSONAR_SSID, MICROSONAR_PASS);


        if (wifiConnector.isAccessPointAvailable(MICROSONAR_SSID)) {
            if (wifiConnector.connectTo(MICROSONAR_SSID, MICROSONAR_PASS)) {
                connStatus.setText(R.string.SucessfullyConnected);
                return true;
            }
        }
        connStatus.setText(R.string.SonarIsUnavailable);
        return false;
    }

    private void turnOffMobileData() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

            }
        }

    }

    public void upload(View view) {
    }

    public void log(String log) {
        Log.d(TAG, log);
    }
}
