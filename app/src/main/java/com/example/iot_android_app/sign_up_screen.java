package com.example.iot_android_app;

import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class sign_up_screen extends Fragment {

    public sign_up_screen() {
    }

        public String hashPassword(String password) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] encodedHash = digest.digest(password.getBytes());
                StringBuilder hexString = new StringBuilder();
                for (byte b : encodedHash) {
                    hexString.append(String.format("%02x", b));
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        EditText etUsername = view.findViewById(R.id.et_signup_username);
        EditText etEmail = view.findViewById(R.id.et_signup_email);
        EditText etPassword = view.findViewById(R.id.et_signup_password);
        EditText etConfirmPassword = view.findViewById(R.id.et_signup_confirm_password);
        Button btnSignUp = view.findViewById(R.id.btn_signup);
        TextView tvBackToLogin = view.findViewById(R.id.tv_back_to_login);

        btnSignUp.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            String hashed_password = hashPassword(password);
            String confirmPassword = etConfirmPassword.getText().toString();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                DBHandler dbHandler = new DBHandler();
                String response = dbHandler.signUpUser(username, email, hashed_password);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Log.d("SignUpResponse", "Response: " + response);

                    if (response.isEmpty()) {
                        Toast.makeText(getActivity(), "Server error. Try again!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        //Checks if user exists already in db
                        JSONArray jsonResponseArray = new JSONArray(response);
                        JSONObject jsonResponse = jsonResponseArray.getJSONObject(0);
                        int userExists = jsonResponse.getInt("user_exists");

                        if (userExists == 1) {
                            Toast.makeText(getActivity(), "Username or Email already taken!", Toast.LENGTH_SHORT).show();
                        } else {
                            String insertUrl = "https://studev.groept.be/api/a24ib2team102/SignUpApp/" + email + "/" + hashed_password + "/" + username;
                            new Thread(() -> dbHandler.makeGETRequest(insertUrl)).start();
                            Toast.makeText(getActivity(), "Sign-Up Successful!", Toast.LENGTH_SHORT).show();

                            account_screen accountScreenFragment = new account_screen();
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.replace(R.id.frame_layout, accountScreenFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    } catch (JSONException e) {
                        Log.e("SignUpError", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(getActivity(), "Error processing response.", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        tvBackToLogin.setOnClickListener(v -> {
            account_screen accountScreenFragment = new account_screen();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, accountScreenFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}
