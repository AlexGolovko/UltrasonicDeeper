package com.golovkobalak.sonarapp.model;

public class Coordinate {
    public int topTile;
    public int leftTile;
    public int bottomTile;
    public int rightTile;

    public int getTilesNumber() {
        return Math.abs((bottomTile - topTile) * (rightTile - leftTile));
    }
}
