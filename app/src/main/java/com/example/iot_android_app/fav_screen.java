package com.example.iot_android_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class fav_screen extends Fragment {
    private List<orderModel> favs;
    private DBHandler db;
    private String user;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadDrinks();
            handler.postDelayed(this, 5000);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        handler.post(refreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable);
    }


    public fav_screen() {}

    public static fav_screen newInstance() {
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

        loadDrinks();

        for (int i = 1; i <= favs.size(); i++) {
            @SuppressLint("DiscouragedApi") int menuButtonId = getResources().getIdentifier("menuButton" + i, "id", requireContext().getPackageName());
            @SuppressLint("DiscouragedApi") int redoButtonId = getResources().getIdentifier("redoButton" + i, "id", requireContext().getPackageName());
            @SuppressLint("DiscouragedApi") int boxTextId = getResources().getIdentifier("boxText" + i, "id", requireContext().getPackageName());

            ImageButton menuButton = view.findViewById(menuButtonId);
            ImageButton redoButton = view.findViewById(redoButtonId);
            TextView boxText = view.findViewById(boxTextId);

            setupMenu(menuButton, menuBox, menuName, menuInfo, removeFav, boxText);
            setupRedoButton(redoButton, favs.get(i));
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

    private void setupRedoButton(ImageButton redoButton, orderModel model) {
        redoButton.setImageResource(model.getReIcon());
        redoButton.setOnClickListener(view -> {
            if (db.canBeOrdered(model.getType())) {
                db.sendMyOrder();
                Toast.makeText(requireContext(), model.getType() + " ordered", Toast.LENGTH_SHORT).show();
            }
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


        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        user = prefs.getString("username", "Guest");
        db = new DBHandler();
        favs = new ArrayList<>();
        Boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn == false) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new BlockedScreen())
                    .commit();
            return new View(requireContext());
        } else {
            return inflater.inflate(R.layout.fragment_fav_screen, container, false);
        }
    }

    public void loadDrinks() {
        favs.clear();
        new Thread(() -> {
            favs = db.getFavoritesList(user);
        }).start();
    }
}