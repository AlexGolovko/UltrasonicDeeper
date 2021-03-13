package com.golovkobalak.sonarapp.ui.home;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.golovkobalak.sonarapp.MainActivity;
import com.golovkobalak.sonarapp.R;
import com.golovkobalak.sonarapp.SonarActivity;
import com.golovkobalak.sonarapp.service.WifiConnector;
import com.golovkobalak.sonarapp.ui.SonarFragment;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.golovkobalak.sonarapp.MainActivity.MICROSONAR_PASS;
import static com.golovkobalak.sonarapp.MainActivity.MICROSONAR_SSID;

public class AutoConnectEventHandler implements View.OnClickListener {
    private static final String TAG = AutoConnectEventHandler.class.getSimpleName();
    private final WifiConnector wifiConnector;
    private FragmentManager fragmentManager;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private boolean isFirstClick = true;
    private final HomeFragment homeFragment;

    public AutoConnectEventHandler(HomeFragment homeFragment, FragmentManager fragmentManager) {
        this.homeFragment = homeFragment;
        wifiConnector = MainActivity.wifiConnector;//new WifiConnector(context, MainActivity.activity);
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void onClick(final View v) {
        if (isFirstClick) {
            isFirstClick = false;
            autoConnect(v);
        } else {
            Intent intent = new Intent(v.getContext(), SonarActivity.class);
            homeFragment.startActivity(intent);
        }

    }

    public void autoConnect(final View v) {
        final Button button = v.getRootView().findViewById(R.id.button_auto_connect);
        final TextView connStatus = v.getRootView().findViewById(R.id.textDescription);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "onClick: " + v.toString());
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    button.setEnabled(false);
                                    button.setText(R.string.butInProgress);
                                    connStatus.setText(R.string.inProcess);
                                }
                            });

                            if (turnOnWIfi(connStatus)) return;
                            if (alreadyConnect(connStatus)) {
                                moveToSonarActivity(v);
                                return;}
                            configureAccessPoint();
                            if (wifiConnector.isAccessPointAvailable(MICROSONAR_SSID)) {
                                if (wifiConnector.connectTo(MICROSONAR_SSID, MICROSONAR_PASS)) {
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            connStatus.setText(R.string.SucessfullyConnected);
                                        }
                                    });
                                    moveToSonarActivity(v);
                                    return;
                                }
                            }
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    connStatus.setText(R.string.manual_connect_description);
                                }
                            });
                        }
                    }).get(60, TimeUnit.SECONDS);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            button.setText(R.string.connect);
                        }
                    });
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    e.printStackTrace();
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            connStatus.setText(R.string.abort_connection_manual_connect_description);
                            button.setText(R.string.manual_connect);
                        }
                    });
                } finally {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //button.setText(R.string.auto_connection);
                            button.setEnabled(true);
                        }
                    });
                }
            }
        });
    }


    private void configureAccessPoint() {
        if (!(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q))
            wifiConnector.configureSonarAccessPoint(MICROSONAR_SSID, MICROSONAR_PASS);
    }

    private boolean alreadyConnect(final TextView connStatus) {
        if (wifiConnector.isConnectedTo(MICROSONAR_SSID)) {
            Log.d(TAG, "onClick: wifiConnector.isConnectedTo(MICROSONAR_SSID)");
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    connStatus.setText(R.string.AlreadyConnected);
                }
            });
           // moveToSonarFragment();
            return true;
        }
        return false;
    }

    private boolean turnOnWIfi(final TextView connStatus) {
        if (wifiConnector.isWifiDisabled()) {
            Log.d(TAG, "onClick: Wifi disabled");
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    connStatus.setText(R.string.WifiIsDisabled);
                }
            });
            final boolean isWifiEnabled = wifiConnector.enableWifi();
            if (!isWifiEnabled) {
                Log.d(TAG, "onClick: !wifiConnector.enableWifi()");
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        connStatus.setText("Wifi can't be enabled\n Please turn on WIFi yourself");
                    }
                });
                return true;
            }
        }
        return false;
    }

    private void moveToSonarActivity(View v) {
        Intent intent = new Intent(v.getContext(), SonarActivity.class);
        homeFragment.startActivity(intent);
    }

    @Deprecated
    private void moveToSonarFragment() {
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, new SonarFragment());
        fragmentTransaction.commit();
    }
}
