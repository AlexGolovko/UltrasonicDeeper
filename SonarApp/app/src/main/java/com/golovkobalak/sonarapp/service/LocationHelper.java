package com.golovkobalak.sonarapp.service;

import android.content.Context;
import android.location.Location;
import android.os.Looper;

import android.util.Log;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {

    public static Location CURR_LOCATION; // Static field to store the latest location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public LocationHelper(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        createLocationRequest();
        createLocationCallback();
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // Update interval in milliseconds (e.g., every 1 seconds)
        locationRequest.setFastestInterval(500); // Fastest update interval in milliseconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // High accuracy mode
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        CURR_LOCATION = location; // Update the static field with the latest location
                    }
                }
            }
        };
    }

    public void start() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // Method to get the last known location from the static field
    public static Location getLastLocation() {
        return CURR_LOCATION;
    }
}
