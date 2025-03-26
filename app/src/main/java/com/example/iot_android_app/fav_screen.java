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
import java.util.Objects;

public class fav_screen extends Fragment {
    private List<orderModel> favs;
    private DBHandler db;
    private String user;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadDrinks(getView());
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

        loadDrinks(view);
    }

    private void setupMenu(orderModel order, ImageButton menuButton, CardView menuBox, EditText menuName, TextView menuInfo, ImageButton removeFromFav, TextView boxText) {
        menuButton.setOnClickListener(view -> {
            menuBox.setVisibility(View.VISIBLE);
            menuName.setText(boxText.getText().toString());
            menuInfo.setText(order.toStringNoDate());

            removeFromFav.setOnClickListener(v -> {
                new Thread(() -> {
                    db.switchFavorite(user, order.getType(), order.getShots(), order.getSugar(), order.getTemp());
                }).start();
                Toast.makeText(requireContext(), menuName.getText().toString() + " removed from favorite", Toast.LENGTH_SHORT).show();
            });

            menuName.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    //TODO: save name method
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        /*SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        user = prefs.getString("username", "Guest");*/
        user = "shlok";
        db = new DBHandler();
        favs = new ArrayList<>();
        /*Boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn == false) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new BlockedScreen())
                    .commit();
            return new View(requireContext());
        } else {
            return inflater.inflate(R.layout.fragment_fav_screen, container, false);
        }*/
        return inflater.inflate(R.layout.fragment_fav_screen, container, false);
    }

    public void loadDrinks(View view) {
        favs.clear();
        new Thread(() -> {
            List<orderModel> newFavs = db.getFavoritesList(user);
            handler.post(() -> {
                favs.addAll(newFavs);
                updateUI(view);
            });
        }).start();
    }

    private void updateUI(View view) {

        CardView menuBox = view.findViewById(R.id.menuBox);
        EditText menuName = view.findViewById(R.id.menuName);
        TextView menuInfo = view.findViewById(R.id.menuInfo);
        ImageButton removeFav = view.findViewById(R.id.removeFromFav);

        requireActivity().runOnUiThread(() -> {
            for (int i = 1; i <= 6; i++) {
                int boxId = getResources().getIdentifier("box" + i, "id", requireContext().getPackageName());
                int menuButtonId = getResources().getIdentifier("menuButton" + i, "id", requireContext().getPackageName());
                int redoButtonId = getResources().getIdentifier("redoButton" + i, "id", requireContext().getPackageName());
                int boxTextId = getResources().getIdentifier("boxText" + i, "id", requireContext().getPackageName());

                ImageButton menuButton = view.findViewById(menuButtonId);
                ImageButton redoButton = view.findViewById(redoButtonId);
                TextView boxText = view.findViewById(boxTextId);
                ViewGroup box = view.findViewById(boxId);

                try {
                    orderModel cur = favs.get(i-1);
                    boxText.setText(cur.getAlias());
                    setupRedoButton(redoButton, cur);
                    setupMenu(cur, menuButton, menuBox, menuName, menuInfo, removeFav, boxText);
                } catch (Exception e) {
                    redoButton.setEnabled(false);
                    redoButton.setImageResource(R.drawable.redo_unav);
                    menuButton.setVisibility(View.GONE);
                    menuButton.setEnabled(false);
                    box.setVisibility(View.GONE);
                }
            }
        });
    }
}