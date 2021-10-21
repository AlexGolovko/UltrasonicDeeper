package com.golovkobalak.sonarapp;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.golovkobalak.sonarapp.logger.Logger;
import com.golovkobalak.sonarapp.service.WifiConnector;
import com.golovkobalak.sonarapp.service.WifiScanReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    public static MainActivity activity;
    public static WifiConnector wifiConnector;
    public static WifiScanReceiver wifiScanReceiver;
    public static final String SESSION_ID = String.valueOf(System.currentTimeMillis());
    public static final String MICROSONAR_SSID = "microsonar";
    public static final String MICROSONAR_PASS = "microsonar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        Realm.init(this.getBaseContext());
        Logger.init(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
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
        if (wifiConnector == null) {
            wifiConnector = new WifiConnector(getApplicationContext(), this);
        }
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

        wifiScanReceiver = new WifiScanReceiver(MICROSONAR_SSID);
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        log("request code: " + requestCode);
        if (requestCode == 1) {
            synchronized (wifiConnector.getLock()) {
                wifiConnector.getLock().notifyAll();
            }
        }
    }

    public void upload(View view) {
    }

    public void log(String log) {
        Log.d(TAG, log);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiScanReceiver);
    }
}
