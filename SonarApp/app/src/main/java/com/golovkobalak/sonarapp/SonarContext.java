package com.golovkobalak.sonarapp;

public class SonarContext {
    public static Activity CURRENT_ACTIVITY = Activity.LOAD;
    public static String FILES_DIR_ABS_PATH;

    public enum Activity {
        LOAD, MAP
    }
}
