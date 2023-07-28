package com.golovkobalak.sonarapp.service;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class LocationHelper {
    public static Location CURR_LOCATION;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long MIN_TIME_BETWEEN_UPDATES = 100; // Minimum time between location updates (in milliseconds)
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.1f; // Minimum distance between location updates (in meters)
    private static final String TAG = LocationHelper.class.getName();

    private Context context;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private HandlerThread handlerThread;
    private Handler backgroundHandler;

    public LocationHelper(Context context) {
        this.context = context;
        handlerThread = new HandlerThread("LocationHandlerThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
    }

    public void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, start getting the location
            startLocationUpdatesInBackground();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, start getting the location
                startLocationUpdatesInBackground();
            } else {
                // Permission is denied, handle accordingly
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdatesInBackground() {
        backgroundHandler.post(this::startLocationUpdates);
    }

    private void startLocationUpdates() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Log.d(TAG, "startLocationUpdates:");
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Handle the new location here
                CURR_LOCATION = location;
                // Do something with latitude and longitude, e.g., update UI, send to server, etc.
                Log.d(TAG, "long:" + longitude);
                Log.d(TAG, "lat:" + latitude);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        // Request location updates
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BETWEEN_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListener);
        } catch (SecurityException e) {
            Log.d(TAG, "startLocationUpdates failed: ");
            e.printStackTrace();
        }
    }

    public void stopLocationUpdates() {
        // Don't forget to stop location updates when needed
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }

        if (handlerThread != null) {
            handlerThread.quit();
        }
    }
}
