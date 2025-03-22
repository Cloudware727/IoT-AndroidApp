package com.example.iot_android_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BlockedScreen extends Fragment {

    public BlockedScreen() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_blocked_screen, container, false);

        // Button references
        Button btnLogin = view.findViewById(R.id.btn_login);
        Button btnSignUp = view.findViewById(R.id.btn_signup);

        // Login button action
        btnLogin.setOnClickListener(v -> {
            BottomNavigationView navView = requireActivity().findViewById(R.id.bottomNavigationView);
            navView.setSelectedItemId(R.id.account_screen);
        });

        // SignUp button action
        btnSignUp.setOnClickListener(v -> {
            BottomNavigationView navView = requireActivity().findViewById(R.id.bottomNavigationView);
            navView.setSelectedItemId(R.id.account_screen);

            sign_up_screen signUpFragment = new sign_up_screen();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, signUpFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}
