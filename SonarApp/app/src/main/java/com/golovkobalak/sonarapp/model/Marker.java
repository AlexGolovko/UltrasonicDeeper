package com.golovkobalak.sonarapp.model;

public class Marker {
    private String depth;
    private double latitude;
    private double longitude;

    public Marker(String depth,  double latitude, double longitude) {
        this.depth = depth;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
