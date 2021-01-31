package com.golovkobalak.sonarapp.ui.home;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.golovkobalak.sonarapp.MainActivity;
import com.golovkobalak.sonarapp.R;
import com.golovkobalak.sonarapp.service.WifiConnector;
import com.golovkobalak.sonarapp.ui.SonarFragment;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.golovkobalak.sonarapp.MainActivity.CONNECTION_IN_PROGRESS;
import static com.golovkobalak.sonarapp.MainActivity.MICROSONAR_PASS;
import static com.golovkobalak.sonarapp.MainActivity.MICROSONAR_SSID;

public class AutoConnectEventHandler implements View.OnClickListener {
    private static final String TAG = AutoConnectEventHandler.class.getSimpleName();
    private final WifiConnector wifiConnector;
    private FragmentManager fragmentManager;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public AutoConnectEventHandler(Context context, FragmentManager fragmentManager) {
        wifiConnector = MainActivity.wifiConnector;//new WifiConnector(context, MainActivity.activity);
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void onClick(final View v) {
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
//                                                    Toast.makeText(v.getRootView().getContext().getApplicationContext(), CONNECTION_IN_PROGRESS, Toast.LENGTH_SHORT).show();
                                    button.setEnabled(false);
                                    button.setText(R.string.butInProgress);
                                    connStatus.setText(R.string.inProcess);
                                }
                            });

                            if (turnOnWIfi(connStatus)) return;
                            if (alreadyConnect(connStatus)) return;
                            configureAccessPoint();
                            if (wifiConnector.isAccessPointAvailable(MICROSONAR_SSID)) {
                                if (wifiConnector.connectTo(MICROSONAR_SSID, MICROSONAR_PASS)) {
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            connStatus.setText(R.string.SucessfullyConnected);
                                        }
                                    });
                                    moveToSonarFragment();
                                    return;
                                }
                            }
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    connStatus.setText(R.string.SonarIsUnavailable);
                                }
                            });
                        }
                    }).get(60, TimeUnit.SECONDS);
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    e.printStackTrace();
                    connStatus.setText(R.string.abort_connection);
                } finally {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            button.setText(R.string.auto_connection);
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
            moveToSonarFragment();
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

    private void moveToSonarFragment() {
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, new SonarFragment());
        fragmentTransaction.commit();
    }
}
