package com.example.iot_android_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class account_screen extends Fragment {

    public account_screen() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_screen, container, false);

        // Initialize views
        EditText etUsername = view.findViewById(R.id.et_username);
        EditText etPassword = view.findViewById(R.id.et_password);
        Button btnLogin = view.findViewById(R.id.btn_login);
        TextView tvSignUp = view.findViewById(R.id.tv_sign_up);

        // Handle Login Button Click
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter username and password", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Replace with actual login validation
                Toast.makeText(getActivity(), "Login Successful!", Toast.LENGTH_SHORT).show();
            }
        });

        // Navigate to Sign-Up Screen (Using Fragment Transaction)
        tvSignUp.setOnClickListener(v -> {
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