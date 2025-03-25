package com.example.iot_android_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

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
        Button confirm = view.findViewById(R.id.btn_confirm_choice);
        Spinner spinner = view.findViewById(R.id.spinner_dispenser);
        EditText myTextField = view.findViewById(R.id.myTextField);
        DBHandler dbHandler = new DBHandler();

        // Initially hide the EditText and Confirm Choice button
        myTextField.setVisibility(View.GONE);
        confirm.setVisibility(View.GONE);

        // Example: Set username (Change this based on actual user data)
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");
        SharedPreferences.Editor editor = prefs.edit();

        tvWelcome.setText("Welcome, " + username + "!");

        // Set up the Spinner with data (Dispenser 1, 2, 3)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.dropdown_items, // Array in strings.xml
                android.R.layout.simple_spinner_dropdown_item
        );
        spinner.setAdapter(adapter);

        // Handle item selection from Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Position corresponds to the selected item index
                // For example, position 0 corresponds to "Dispenser 1", position 1 to "Dispenser 2", and position 2 to "Dispenser 3"

                // Get the selected dispenser number (position + 1 to map to 1, 2, or 3)
                int dispenserNumber = position + 1;
                // Show the selected dispenser number
                Toast.makeText(getContext(), "Selected Dispenser: " + dispenserNumber, Toast.LENGTH_SHORT).show();

                // Show the EditText and Confirm Choice button when an item is selected
                myTextField.setVisibility(View.VISIBLE);
                confirm.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle case when nothing is selected (optional)
            }
        });

        // Confirm Choice Button Logic
        confirm.setOnClickListener(v -> {
            String text = myTextField.getText().toString().trim();  // Get text from EditText

            // Ensure the text is not empty before saving
            if (text.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a valid text.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Process text (replace spaces with "+" for URL compatibility)
            text = text.replaceAll(" ", "+");

            // Save to SharedPreferences
            editor.putString("selected_dispenser_text", text);
            editor.apply();

            // Get the selected dispenser number (again based on the position in the spinner)
            int dispenserNumber = spinner.getSelectedItemPosition() + 1;  // Position + 1 for dispenser number
            String requestUrl = "https://studev.groept.be/api/a24ib2team102/ChangeTeaName/" + text + "/" + dispenserNumber;

            // Make the GET request
            new Thread(() -> dbHandler.makeGETRequest(requestUrl)).start();
            Toast.makeText(getContext(), "Dispenser "+dispenserNumber+" has been changed", Toast.LENGTH_SHORT).show();

            // Optionally, hide the confirm button again or perform other actions
            confirm.setVisibility(View.GONE);
        });

        // Logout Button
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Logged Out!", Toast.LENGTH_SHORT).show();
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
