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

public class account_screen extends Fragment {

    public account_screen() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_screen, container, false);

        EditText etUsername = view.findViewById(R.id.et_username);
        EditText etPassword = view.findViewById(R.id.et_password);
        Button btnLogin = view.findViewById(R.id.btn_login);
        TextView tvSignUp = view.findViewById(R.id.tv_sign_up);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter username and password", Toast.LENGTH_SHORT).show();
            } else {
                new Thread(() -> {
                    DBHandler dbHandler = new DBHandler();
                    String response = dbHandler.LogInUser(username, password);

                    if (getActivity() == null) return;

                    getActivity().runOnUiThread(() -> {
                        Log.d("LoginResponse", "Response: " + response);

                        if (response.isEmpty()) {
                            Toast.makeText(getActivity(), "Server error. Try again!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            Log.d("LoginResponse", "Raw Response: " + response);  // Debugging line

                            JSONArray jsonResponse = new JSONArray(response);  // Ensure it's a JSON array

                            if (jsonResponse.length() == 0) {
                                Toast.makeText(getActivity(), "Invalid server response!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            JSONObject firstObject = jsonResponse.getJSONObject(0);  // Get first object
                            if (!firstObject.has("match_found")) {
                                Toast.makeText(getActivity(), "Invalid JSON structure!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            int matchFound = firstObject.getInt("match_found");  // Extract value

                            if (matchFound == 1) {
                                Toast.makeText(getActivity(), "Login Successful!", Toast.LENGTH_SHORT).show();
                                navigateToLoggedInScreen();
                            } else {
                                Toast.makeText(getActivity(), "Invalid username or password!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("LoginError", "JSON Parsing Error: " + e.getMessage());
                            Toast.makeText(getActivity(), "Error processing response.", Toast.LENGTH_SHORT).show();
                        }

                    });
                }).start();
            }
        });

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

    private void navigateToLoggedInScreen() {
        LoggedInFragment loggedInFragment = new LoggedInFragment();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, loggedInFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
