package com.golovkobalak.sonarapp.model;

import java.util.List;
import java.util.Map;

public class GeoSquare {
    public double north;
    public double east;
    public double south;
    public double west;

    public GeoSquare(Map<String, List<String>> queryParamMap) {
        if (queryParamMap == null) return;
        //"{"north":49.960455723200724,"east":36.34042262789566,"south":49.955769014252176,"west":36.33620619532426}"
        north = Double.parseDouble(queryParamMap.get("north").get(0));
        east = Double.parseDouble(queryParamMap.get("east").get(0));
        south = Double.parseDouble(queryParamMap.get("south").get(0));
        west = Double.parseDouble(queryParamMap.get("west").get(0));
    }
}
