package com.golovkobalak.sonarapp.config;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

public class Logger {
    private static Logger INSTANCE;

    private Logger() {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void init(File externalFilesDir) {
        if (INSTANCE == null) {
            synchronized (Logger.class) {
                INSTANCE = new Logger();
                final File logFile = new File(externalFilesDir.getAbsolutePath() + "/" + LocalDateTime.now().toString() + "_cat.log");
                Log.i(Logger.class.getName(), logFile.getAbsolutePath());
                INSTANCE.syncLog(logFile);
                INSTANCE.initLogcatCaptureLogs(logFile);
            }
        }
    }

    private void initLogcatCaptureLogs(File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            Log.i(Logger.class.getName(), file.getAbsolutePath());
            String cmd = "logcat -f" + file.getAbsolutePath();//-d
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void syncLog(File file) {
        try {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && file.exists()) {
                final List<String> log = Files.readAllLines(Paths.get(file.getPath()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
