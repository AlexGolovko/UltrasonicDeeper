package com.golovkobalak.sonarapp.model;

import com.golovkobalak.sonarapp.MainActivity;

import io.realm.RealmObject;

public class SonarData extends RealmObject {
    private String sessionId = MainActivity.SESSION_ID;
    private String depth;
    private String battery;
    private String temperature;
    private String time;
    private String accuracy;
    private String altitude;
    private String altitudeAccuracy;
    private String heading;
    private double latitude;
    private double longitude;
    private String speed;

    public String getDepth() {
        return depth;
    }

    public void setDepth(String depth) {
        this.depth = depth;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getAltitudeAccuracy() {
        return altitudeAccuracy;
    }

    public void setAltitudeAccuracy(String altitudeAccuracy) {
        this.altitudeAccuracy = altitudeAccuracy;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public static class Field {
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
    }
}
