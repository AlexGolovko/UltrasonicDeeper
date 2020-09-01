package com.golovkobalak.sonarapp;

import android.graphics.Point;
import android.os.RemoteException;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityTestRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void init(){
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        try {
            if (!uiDevice.isScreenOn()) {
                uiDevice.wakeUp();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void checkContainerIsDisplayed() {
        ActivityScenario<MainActivity> scenario = activityTestRule.getScenario();
        onView(ViewMatchers.withId(R.id.container))
                .check(matches(isDisplayed()));

    }
}