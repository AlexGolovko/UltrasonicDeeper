package com.golovkobalak.sonarapp;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {
    public static final String SESSION_ID = String.valueOf(System.currentTimeMillis());
    private static final String MICROSONAR_SSID = "microsonar";
    private static final String MICROSONAR_PASS = "microsonar";
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);
    public static final String CONNECTION_IN_PROGRESS = "Connection in progress";

    final String TAG = "SonarApp";
    private TextView connStatus;
    private WifiConnector wifiConnector;
    private WifiScanReceiver wifiScanReceiver;
    private Button connectButton;
    private boolean isFirstClick = true;

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
        connectButton = findViewById(R.id.button_sonar);
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

        wifiScanReceiver = new WifiScanReceiver(MICROSONAR_SSID);
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void connect(View view) {
        Toast.makeText(getApplicationContext(), CONNECTION_IN_PROGRESS, Toast.LENGTH_SHORT).show();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectButton.setEnabled(false);
                    }
                });
                final Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        {
                            if (changeAccessPoint()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(MainActivity.this, SonarActivity.class);
                                        startActivity(intent);
                                    }
                                });
                                return true;
                            }
                            return false;
                        }
                    }
                });
                try {
                    future.get(60, TimeUnit.SECONDS);
                    final Boolean isConnected = future.get(1, TimeUnit.DAYS);
                    if (isConnected) {
                        log("Completed successfully");
                    } else {
                        log("Connection problem");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    log("Timed out. Cancelling the runnable...");
                    future.cancel(true);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectButton.setEnabled(true);
                    }
                });

            }
        });
    }


    public boolean changeAccessPoint() {
        log("change Access Point");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connStatus.setText(R.string.inProcess);
            }
        });
        if (wifiConnector.isWifiDisabled()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connStatus.setText(R.string.WifiIsDisabled);
                }
            });
            if (!wifiConnector.enableWifi()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connStatus.setText("Wifi can't be enabled\n Please turn on WIFi yourself");
                    }
                });
                return false;
            }
        }
        if (wifiConnector.isConnectedTo(MICROSONAR_SSID)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connStatus.setText(R.string.AlreadyConnected);
                }
            });
            log((String) getText(R.string.AlreadyConnected));
            return true;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connStatus.setText(R.string.NotPermit);
                }
            });
            log((String) getText(R.string.NotPermit));
            return false;
        }
        if (!(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q))
            wifiConnector.configureSonarAccessPoint(MICROSONAR_SSID, MICROSONAR_PASS);
        if (wifiConnector.isAccessPointAvailable(MICROSONAR_SSID)) {
            if (wifiConnector.connectTo(MICROSONAR_SSID, MICROSONAR_PASS)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connStatus.setText(R.string.SucessfullyConnected);
                    }
                });
                return true;
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connStatus.setText(R.string.SonarIsUnavailable);
            }
        });
        return false;
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

    public void manualConnect(View view) {
        connStatus.setText(R.string.manual_connect_description);
        if (isFirstClick) {
            isFirstClick = false;
            return;
        }
        isFirstClick = true;
        Intent intent = new Intent(MainActivity.this, SonarActivity.class);
        startActivity(intent);
    }
}
