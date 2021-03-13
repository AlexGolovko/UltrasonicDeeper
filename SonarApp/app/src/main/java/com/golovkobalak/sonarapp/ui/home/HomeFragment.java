package com.golovkobalak.sonarapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.golovkobalak.sonarapp.MainActivity;
import com.golovkobalak.sonarapp.R;
import com.golovkobalak.sonarapp.SonarActivity;

public class HomeFragment extends Fragment {
    private static boolean isFirstClick = true;
    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        root.findViewById(R.id.button_auto_connect).setOnClickListener(new AutoConnectEventHandler(this, this.getFragmentManager()));
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
