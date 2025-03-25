package com.example.iot_android_app;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import java.util.List;

public class fav_screen extends Fragment {
    private List<orderModel> favs;

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

        CardView menuBox = view.findViewById(R.id.menuBox);
        EditText menuName = view.findViewById(R.id.menuName);
        TextView menuInfo = view.findViewById(R.id.menuInfo);
        ImageButton removeFav = view.findViewById(R.id.removeFromFav);

        for (int i = 1; i <= 6; i++) {
            int menuButtonId = getResources().getIdentifier("menuButton" + i, "id", requireContext().getPackageName());
            int redoButtonId = getResources().getIdentifier("redoButton" + i, "id", requireContext().getPackageName());
            int boxTextId = getResources().getIdentifier("boxText" + i, "id", requireContext().getPackageName());

            ImageButton menuButton = view.findViewById(menuButtonId);
            ImageButton redoButton = view.findViewById(redoButtonId);
            TextView boxText = view.findViewById(boxTextId);

            setupMenu(menuButton, menuBox, menuName, menuInfo, removeFav, boxText);
            setupRedoButton(redoButton, boxText.toString());
        }
    }

    private void setupMenu(ImageButton menuButton, CardView menuBox, EditText menuName, TextView menuInfo, ImageButton removeFromFav, TextView boxText) {
        DBHandler db = new DBHandler();
        menuButton.setOnClickListener(view -> {
            menuBox.setVisibility(View.VISIBLE);
            menuName.setText(boxText.getText().toString());
            menuInfo.setText("Details about " + boxText.getText().toString());

            removeFromFav.setOnClickListener(v -> {
                db.switchFavorite();//menuName.getText().toString());
                Toast.makeText(requireContext(), "Updated favorites for " + menuName.getText().toString(), Toast.LENGTH_SHORT).show();
            });

            menuName.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    db.saveCurName();//menuName.getText().toString());
                }
            });
        });
    }

    private void closeOtherMenus() {
        for (int i = 1; i <= 6; i++) {
            int menuBoxId = getResources().getIdentifier("menuBox" + i, "id", requireContext().getPackageName());
            CardView menuBox = getView().findViewById(menuBoxId);
            if (menuBox != null) {
                menuBox.setVisibility(View.GONE);
            }
        }
    }

    private void setupRedoButton(ImageButton redoButton, String elementName) {
        DBHandler db = new DBHandler();
        redoButton.setOnClickListener(view -> {
            db.sendMyOrder();
            Toast.makeText(requireContext(), "Redoing " + elementName, Toast.LENGTH_SHORT).show();
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

        /*// Checks if user is logged in
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
        }*/
        return inflater.inflate(R.layout.fragment_fav_screen, container, false);
    }
}