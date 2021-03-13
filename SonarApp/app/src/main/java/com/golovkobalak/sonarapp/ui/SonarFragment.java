package com.golovkobalak.sonarapp.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.golovkobalak.sonarapp.R;
import com.golovkobalak.sonarapp.service.TrackingInterface;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class SonarFragment extends Fragment {
    private WebView webView;
    private WebSettings settings;
    private TrackingInterface trackingInterface;

    public SonarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sonar, container, false);
        webView = root.findViewById(R.id.sonar_view);
        settings = webView.getSettings();
        WebView.setWebContentsDebuggingEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setGeolocationEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        // adb reverse tcp:8080 tcp:8080
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        webView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        webView.getSettings().setGeolocationDatabasePath(this.getActivity().getFilesDir().getPath());
        this.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        webView.loadUrl("file:///android_asset/index.html");
        if (trackingInterface == null) {
            trackingInterface = new TrackingInterface(this.getContext(), "sonar");
        }
        trackingInterface.setActivity("sonar");
        webView.addJavascriptInterface(trackingInterface, "TrackingService");
        webView.loadUrl("file:///android_asset/AngularSonar/index.html");
        // Inflate the layout for this fragment
        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webView.destroy();
    }
}