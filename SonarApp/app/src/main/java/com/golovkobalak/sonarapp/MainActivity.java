package com.golovkobalak.sonarapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.golovkobalak.sonarapp.controller.TrackingController;
import com.golovkobalak.sonarapp.logger.Logger;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    public static MainActivity activity;
    public static final String SESSION_ID = String.valueOf(System.currentTimeMillis());
    public static final String MICROSONAR_SSID = "microsonar";
    private TrackingController trackingController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        Realm.init(this.getBaseContext());
        Logger.init(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
        super.onCreate(savedInstanceState);
        trackingController = new TrackingController(this.getBaseContext());
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void upload(View view) {
    }

    public void log(String log) {
        Log.d(TAG, log);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
