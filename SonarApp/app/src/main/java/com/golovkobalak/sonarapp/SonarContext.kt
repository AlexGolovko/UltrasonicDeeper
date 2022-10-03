package com.golovkobalak.sonarapp;

import android.content.res.AssetManager;

public class SonarContext {
    public static AssetManager getAssetManager() {
        return assetManager;
    }

    public static void setAssetManager(AssetManager assetManager) {
        SonarContext.assetManager = assetManager;
    }

    public static String getFilesDirAbsPath() {
        return filesDirAbsPath;
    }

    public static void setFilesDirAbsPath(String filesDirAbsPath) {
        SonarContext.filesDirAbsPath = filesDirAbsPath;
    }

    private static AssetManager assetManager;
    private static String filesDirAbsPath;

    private SonarContext() {
    }
}
