package com.golovkobalak.sonarapp.ui.dashboard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.golovkobalak.sonarapp.R;
import com.golovkobalak.sonarapp.SonarContext;
import com.golovkobalak.sonarapp.service.MapService;

public class DashboardFragment extends Fragment {
    public static final String TAG = DashboardFragment.class.getSimpleName();
    private DashboardViewModel dashboardViewModel;
    private WebSettings settings;
    private WebView webView;
    private MapService mapService;
    private DownloadClickListener downloadClickListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        webView = root.findViewById(R.id.map_view);
//        webView = (WebView) findViewById(R.id.webViewJS);
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

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("DashboardFragment", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        webView.getSettings().setGeolocationDatabasePath(this.getActivity().getFilesDir().getPath());
        this.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        SonarContext.CURRENT_ACTIVITY = SonarContext.Activity.LOAD;
        webView.loadUrl("file:///android_asset/AngularSonar/index.html");
        final Button downloadButton = (Button) root.findViewById(R.id.download_button);
        if (mapService == null) {
            mapService = new MapService(this.getContext());
        }
        if (downloadClickListener == null)
            downloadClickListener = new DownloadClickListener(mapService);
        downloadButton.setOnClickListener(downloadClickListener);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webView.destroy();
        mapService.cancelDownloadMap();
    }
}
