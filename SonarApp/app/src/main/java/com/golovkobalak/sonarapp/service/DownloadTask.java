package com.golovkobalak.sonarapp.service;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadTask extends AsyncTask<String, Integer, Boolean> {

    public static final String TAG = DownloadTask.class.getSimpleName();

    public interface DownloadListener {
        void onDownloadComplete(File filename);

        void onDownloadFailure(final String msg);
    }

    private final DownloadListener listener;
    private String msg;
    private File saveTo;

    public DownloadTask(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params == null || params.length < 2) {
            msg = "Incomplete parameters";
            return false;
        }
        String sUrl = params[0];
        String dir = params[1];
        String fileName = params[2];
        final File folder = new File(dir);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Log.i(TAG, "directory cannot be created");
                return false;
            }
        }
        saveTo = new File(dir, fileName);

        if (saveTo.exists()) {
            return true;
        }
        Log.d(TAG, "doInBackground: " + saveTo.getAbsolutePath());
        try {
            URL url = new URL(sUrl);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream is = new BufferedInputStream(url.openStream());
            OutputStream os = new FileOutputStream(saveTo);
            byte buffer[] = new byte[512];
            int count;
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            os.flush();
            os.close();
            is.close();
            return true;
        } catch (MalformedURLException e) {
            msg = "Invalid URL";
        } catch (IOException e) {
            Log.e(TAG, TAG, e);
            msg = "No internet connection";
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (!result) {
            if (listener != null) listener.onDownloadFailure(msg);
            return;
        }
        if (listener != null) listener.onDownloadComplete(saveTo);
    }
}