package com.example.iot_android_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class LoggedInFragment extends Fragment {

    public LoggedInFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logged_in, container, false);

        // UI Components
        TextView tvWelcome = view.findViewById(R.id.tv_welcome);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        // Example: Set username (Change this based on actual user data)
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");

        tvWelcome.setText("Welcome, " + username + "!");

        // Logout Button
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Logged Out!", Toast.LENGTH_SHORT).show();

            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Navigate back to login screen
            account_screen loginFragment = new account_screen();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, loginFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}
