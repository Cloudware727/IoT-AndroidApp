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

public class sign_up_screen extends Fragment {

    public sign_up_screen() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        // Initialize UI components
        EditText etUsername = view.findViewById(R.id.et_signup_username);
        EditText etEmail = view.findViewById(R.id.et_signup_email);
        EditText etPassword = view.findViewById(R.id.et_signup_password);
        EditText etConfirmPassword = view.findViewById(R.id.et_signup_confirm_password);
        Button btnSignUp = view.findViewById(R.id.btn_signup);
        TextView tvBackToLogin = view.findViewById(R.id.tv_back_to_login);

        // Sign-Up Button Click
        btnSignUp.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Store user details in database
                Toast.makeText(getActivity(), "Account Created Successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        // Back to Login Button Click
        tvBackToLogin.setOnClickListener(v -> {
             account_screen accountScreenFragment = new account_screen();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, accountScreenFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}


