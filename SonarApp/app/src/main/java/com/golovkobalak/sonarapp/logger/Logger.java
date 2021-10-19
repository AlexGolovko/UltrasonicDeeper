package com.golovkobalak.sonarapp.logger;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import tech.gusavila92.websocketclient.WebSocketClient;

public class Logger {
    private static final Logger INSTANCE = new Logger();
    public static final String WEB_SOCKET = "WebSocket";
    private URI uri;
    private WebSocketClient webSocketClient;

    private Logger() {
        initLogcatCaptureLogs();
        initWebSocketLogging();
    }

    public static void init() {
        Log.i(Logger.class.getName(), "init: " + INSTANCE);
    }

    private void initLogcatCaptureLogs() {
        try {
            File filename = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/LogCatLog.log");
            if (!filename.exists()) {
                filename.createNewFile();
            }
            Log.i(Logger.class.getName(), filename.getAbsolutePath());
            String cmd = "logcat -d -f" + filename.getAbsolutePath();
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initWebSocketLogging() {
    }
}
