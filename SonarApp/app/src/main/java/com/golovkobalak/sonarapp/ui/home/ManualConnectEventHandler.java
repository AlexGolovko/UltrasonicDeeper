package com.golovkobalak.sonarapp.ui.home;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.golovkobalak.sonarapp.R;
import com.golovkobalak.sonarapp.ui.SonarFragment;

public class ManualConnectEventHandler implements View.OnClickListener {
    private FragmentManager fragmentManager;

    public ManualConnectEventHandler(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void onClick(View v) {
        final TextView description = v.getRootView().findViewById(R.id.textDescription);
        description.setText(R.string.manual_connect_description);
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, new SonarFragment());
        fragmentTransaction.commit();
    }
}
