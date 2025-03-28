package com.example.iot_android_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class fav_screen extends Fragment {
    private List<orderModel> favs;
    private DBHandler db;
    private String user;
    private int disableThr = 5;

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
            if (menuBox.getVisibility() == View.VISIBLE && menuInfo.getText().toString().equals(order.toStringNoDate()))
                menuBox.setVisibility(View.GONE);
            else {
                menuBox.setVisibility(View.VISIBLE);
                menuName.setText(boxText.getText().toString());
                menuInfo.setText(order.toStringNoDate());
            }

            menuName.setOnEditorActionListener((v, actionId, event) -> {
                menuName.clearFocus();
                return false;
            });

            removeFromFav.setOnClickListener(v -> {
                new Thread(() -> {
                    db.switchFavorite(user, order.getType(), order.getShots(), order.getSugar(), order.getTemp());
                    View rootView = getView();
                    if (rootView != null) {
                        requireActivity().runOnUiThread(() -> loadDrinks(rootView));
                    }
                }).start();
                menuBox.setVisibility(View.GONE);
                Toast.makeText(requireContext(), menuName.getText().toString() + " removed from favorite", Toast.LENGTH_SHORT).show();

            });

            menuName.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    new Thread(() -> {
                        db.saveCurAlias(user, order.getType(), order.getShots(), order.getSugar(), order.getTemp(),
                                menuName.getText().toString());
                        View rootView = getView();
                        if (rootView != null) {
                            requireActivity().runOnUiThread(() -> loadDrinks(rootView));
                        }
                    }).start();
                }
            });
        });
    }

    private void setupRedoButton(ImageButton redoButton, orderModel model) {
        redoButton.setImageResource(model.getReIcon());
        redoButton.setOnClickListener(view -> {
            SharedPreferences prefs = getContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
            if (prefs.getInt("machine_busy", 1) == 1){
                Toast.makeText(getActivity(), "Machine is busy! Please try again later!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (db.canBeOrdered(model.getType())) {
                new Thread(() -> {
                    String settingsJSON = db.getSettings();
                    try {
                        JSONArray array = new JSONArray(settingsJSON);
                        for (int i = 0; i < 3; i++) {
                            JSONObject curObject = array.getJSONObject(i);
                            String neededType = model.getType();
                            String curType = curObject.getString("name");
                            if (curType.equals(neededType) &&
                                    curObject.getInt("level") >= disableThr) {
                                BrewConfiguration drink =
                                        new BrewConfiguration(curObject.getInt("dispenser"),
                                                model.getType(), model.getShots(),
                                                model.getSugar(), model.getTemp());
                                drink.sendOrder(getActivity(), getContext());
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).start();
                new Handler().postDelayed(() ->{
                            db.saveMachineOrderId(getActivity(), getContext());
                        }, 1000
                );
                Toast.makeText(requireContext(), model.getType() + " ordered", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        user = prefs.getString("username", "Guest");
        db = new DBHandler();
        db.startSettingsUpdater();
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

    public void loadDrinks(View view) {
        favs.clear();
        new Thread(() -> {
            List<orderModel> newFavs = db.getFavoritesList(user);
            favs.addAll(newFavs);
            updateUI(view);
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