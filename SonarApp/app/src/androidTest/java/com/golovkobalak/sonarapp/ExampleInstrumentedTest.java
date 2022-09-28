package com.golovkobalak.sonarapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.golovkobalak.sonarapp.controller.SonarController;
import com.golovkobalak.sonarapp.controller.TrackingController;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.golovkobalak.sonarapp", appContext.getPackageName());
    }

    @Test
    public void testTrackingController() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final TrackingController trackingController = new TrackingController();
        URL url = null;
        try {
            url = new URL("http://localhost:8080/system/mapCacheDir");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            final int responseCode = con.getResponseCode();
            assertEquals(200, responseCode);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testSonarController() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final SonarController sonarController = new SonarController();
        URL url = null;
        try {
            url = new URL("http://localhost:4242/root");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            final int responseCode = con.getResponseCode();
            final String responseMessage = con.getResponseMessage();
            assertEquals(200, responseCode);
            Log.i(this.getClass().getName(), "responseCode:" + responseCode);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}
