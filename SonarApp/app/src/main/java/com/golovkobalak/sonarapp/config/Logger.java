package com.golovkobalak.sonarapp.config;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

public class Logger {
    private static final Logger INSTANCE = new Logger();

    private Logger() {

    }

    public static void init(File externalFilesDir) {
        final File logFile = new File(externalFilesDir.getAbsolutePath() + File.separator + LocalDateTime.now().toString() + "_cat.log");
        Log.i(Logger.class.getName(), logFile.getAbsolutePath());
        INSTANCE.syncLog(logFile);
        INSTANCE.initLogcatCaptureLogs(logFile);
    }

    private void initLogcatCaptureLogs(File file) {
        try {
            if (!file.exists() && !file.createNewFile()) {
                Log.w(this.getClass().getName(), "Problem with creation file");
            }
            Log.i(Logger.class.getName(), file.getAbsolutePath());
            String cmd = "logcat -f" + file.getAbsolutePath();//-d
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            Log.w(this.getClass().getName(), e);
        }
    }


    private void syncLog(File file) {
        try {
            if (file.exists()) {
                final List<String> log = Files.readAllLines(Paths.get(file.getPath()));
                log.forEach(lg -> Log.w(this.getClass().getName(), lg));
            }
        } catch (IOException e) {
            Log.w(this.getClass().getName(), e);
        }
    }
}
