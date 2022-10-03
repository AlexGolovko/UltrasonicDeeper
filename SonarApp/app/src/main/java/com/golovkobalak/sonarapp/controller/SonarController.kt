package com.golovkobalak.sonarapp.controller;

import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.golovkobalak.sonarapp.SonarContext;
import com.golovkobalak.sonarapp.model.Config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import io.javalin.http.ContentType;

public class SonarController {

    private static final int PORT = 4242;
    private Javalin app;

    public void start() {
        this.app = Javalin.create(this::updateConfig).start(PORT);
    }


    public void updateConfig(JavalinConfig javalinConfig) {
        javalinConfig.enableCorsForAllOrigins();
        final AssetManager assetManager = SonarContext.getAssetManager();
        final List<Config> configs = new ArrayList<>();
        try {
            final String assetFolderName = "AngularSonar";
            collectConfigFromAsset(assetManager, configs, assetFolderName);
            configs.stream()//
                    .sorted((a, b) -> b.getHostedPath().length() - (a.getHostedPath().length()))//
                    .forEachOrdered(config -> {
                        Log.i(this.getClass().getName(), "path:" + config.getHostedPath());
                        Log.i(this.getClass().getName(), "contentType:" + config.getContentType().getMimeType());
                        javalinConfig.addSinglePageHandler(config.getHostedPath(), ctx -> {
                            ctx.result(config.getFileBody());
                            ctx.contentType(config.getContentType());
                        });
                    });
        } catch (IOException e) {
            Log.e(this.getClass().getName(), e.getMessage(), e);
        }
    }

    private void collectConfigFromAsset(AssetManager assetManager, List<Config> configs, String assetFolderName) throws IOException {
        for (String asset : assetManager.list(assetFolderName)) {
            if (asset.contains(".")) {
                final ContentType contentType = getContentType(asset);
                final String fileName = assetFolderName + File.separator + asset;
                String hostedPath = getHostedPath(assetFolderName, asset);

                final byte[] fileBody = getFileBody(assetManager, fileName);
                configs.add(new Config(hostedPath, fileBody, contentType));
            } else {
                collectConfigFromAsset(assetManager, configs, assetFolderName + File.separator + asset);
            }
        }
    }

    @NonNull
    private String getHostedPath(String assetFolderName, String asset) {
        final int separatorIndex = assetFolderName.indexOf(File.separator);
        String hostedRoot = "";
        if (separatorIndex > 0) {
            hostedRoot = assetFolderName.substring(separatorIndex);
        }
        String hostedPath = hostedRoot + File.separator + asset;
        if (asset.equalsIgnoreCase("index.html")) {
            hostedPath = File.separator;
        }
        return hostedPath;
    }

    @NonNull
    private byte[] getFileBody(AssetManager assetManager, String fileName) throws IOException {
        final byte[] bytes;
        try (BufferedInputStream bis = new BufferedInputStream(assetManager.open(fileName))) {
            bytes = new byte[bis.available()];
            if (bis.read(bytes) == -1) {
                Log.w(this.getClass().getName(), "The file:" + fileName + " is empty");
            }
        }
        return bytes;
    }

    private ContentType getContentType(String asset) {
        return ContentType.getContentTypeByExtension(asset.substring(asset.lastIndexOf(".") + 1));
    }

    public void destroy() {
        app.close();
    }
}
