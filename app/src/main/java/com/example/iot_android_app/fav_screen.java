package com.example.iot_android_app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import java.util.Objects;

public class fav_screen extends Fragment {

    public fav_screen() {}

    public static fav_screen newInstance(String param1, String param2) {
        fav_screen fragment = new fav_screen();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView topTextView = view.findViewById(R.id.topTextView);
    }

    private void setupMenu(ImageButton menuButton, String elementName) {
        menuButton.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(requireContext(), menuButton);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.popup_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> handleMenuClick(item, elementName));
            popup.show();
        });
    }

    private boolean handleMenuClick(MenuItem item, String elementName) {
        if (getActivity() == null) return false;

        int itemId = item.getItemId();
        String message = null;

        if (itemId == R.id.info) {
            message = "Info about " + elementName;
        } else if (itemId == R.id.redo) {
            message = "Redo action for " + elementName;
        }

        if (message != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Checks if user is logged in
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        Boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn == false) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new BlockedScreen())
                    .commit();
            return new View(requireContext());
        } else {

            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_fav_screen, container, false);
        }
    }
}