package com.example.iot_android_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class home_screen extends Fragment {

    private RecyclerView rvCoffeeCarousel;
    private CoffeeCarouselAdapter adapter;
    private List<Coffee> coffeeList;
    private PagerSnapHelper snapHelper;
    private int startButtonDisableThresh = 10;
    private Handler handler = new Handler();
    private Runnable runnable;
    private int intervalMachineBusyCheck = 10000;
    private SharedViewModel sharedViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing to disable back/swipe-back
            }
        });

        DBHandler db = new DBHandler();
        View view = inflater.inflate(R.layout.fragment_home_screen, container, false);

        coffeeList = new ArrayList<>();
        coffeeList.add(new Coffee(1, " ", 100, R.drawable.herbs_tea_bg));
        coffeeList.add(new Coffee(2, " ", 100, R.drawable.lemon_tea_bg));
        coffeeList.add(new Coffee(3, " ", 100, R.drawable.instant_coffee_bg));

        SharedPreferences prefs = getContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        BrewConfiguration brewConfiguration = new BrewConfiguration(1, coffeeList.get(0).getName(), 2, 0, 70);

        SharedPreferences prefs2 = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        for (int i = 0; i < coffeeList.size(); i++) {
            String path = prefs2.getString("dispenser_image_path_" + i, null);
            if (path != null) {
                coffeeList.get(i).setImagePath(path);
            }
        }

        rvCoffeeCarousel = view.findViewById(R.id.rvCoffeeCarousel);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCoffeeCarousel.setLayoutManager(layoutManager);
        adapter = new CoffeeCarouselAdapter(coffeeList);
        rvCoffeeCarousel.setAdapter(adapter);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getImagePath().observe(getViewLifecycleOwner(), image -> {
            int positionToUpdate = prefs2.getInt("spinner_position", 0) - 1;
            String imagePath = prefs2.getString("dispenser_image_path_" + positionToUpdate, null);

            if (positionToUpdate >= 0 && positionToUpdate < coffeeList.size() && imagePath != null) {
                Log.d("home_screen", "Updating dispenser at position: " + positionToUpdate);
                coffeeList.get(positionToUpdate).setImagePath(imagePath);
                adapter.notifyItemChanged(positionToUpdate);
//                Toast.makeText(getContext(), "Image updated!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("home_screen", "Invalid position or missing image path");
            }
        });

        new Thread(() -> {
            DBHandler dbHandler = new DBHandler();
            String response = dbHandler.getNameLevel();
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (response.isEmpty()) {
                    Toast.makeText(getActivity(), "Server Error, failed to load data!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    JSONArray jsonResponse = new JSONArray(response);
                    for (int i = 0; i < jsonResponse.length(); i++) {
                        JSONObject curObject = jsonResponse.getJSONObject(i);
                        if (i < coffeeList.size()) {
                            coffeeList.get(i).setName(curObject.getString("name"));
                            coffeeList.get(i).setCoffeeLevel(curObject.getInt("level"));
                        }
                    }
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("name_first", coffeeList.get(0).getName());
                    editor.putString("name_second", coffeeList.get(1).getName());
                    editor.putString("name_third", coffeeList.get(2).getName());
                    editor.apply();
                    brewConfiguration.setName(coffeeList.get(0).getName());
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "Error processing response.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();

        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvCoffeeCarousel);

        rvCoffeeCarousel.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View snappedView = snapHelper.findSnapView(layoutManager);
                    int pos = layoutManager.getPosition(snappedView);
                    Coffee currentCoffee = coffeeList.get(pos);
                    brewConfiguration.setCoffeeId(currentCoffee.getId());
                    brewConfiguration.setName(currentCoffee.getName());

                    int busyyy = prefs.getInt("machine_busy", 1);
                    MaterialButton btnStart = getView().findViewById(R.id.actionBtn);
                    btnStart.setEnabled(coffeeList.get(pos).getCoffeeLevel() >= startButtonDisableThresh);
                }
            }
        });

        ChipGroup cgShotSize = view.findViewById(R.id.cgShotSize);
        ChipGroup cgSugarSize = view.findViewById(R.id.cgSugarSize);
        Slider tempSlider = view.findViewById(R.id.tempSlider);
        TextView tvTempLabel = view.findViewById(R.id.tvTempLabel);
        MaterialButton btnTempIncrease = view.findViewById(R.id.increaseBtn);
        MaterialButton btnTempDecrease = view.findViewById(R.id.decreaseBtn);
        MaterialButton btnStart = view.findViewById(R.id.actionBtn);
        MaterialButton btnFav = view.findViewById(R.id.addfavBtn);
        CardView progressCard = getActivity().findViewById(R.id.progressCard);

        cgShotSize.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int selectedShot = 0;
            for (int checkedId : checkedIds) {
                if (checkedId == R.id.shot_small) selectedShot = 1;
                else if (checkedId == R.id.shot_med) selectedShot = 2;
                else if (checkedId == R.id.shot_large) selectedShot = 3;
            }
            brewConfiguration.setShotSize(selectedShot);
        });

        cgSugarSize.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int sugarLevel = 0;
            for (int checkedId : checkedIds) {
                if (checkedId == R.id.sugar_none) sugarLevel = 0;
                else if (checkedId == R.id.sugar_small) sugarLevel = 1;
                else if (checkedId == R.id.sugar_med) sugarLevel = 2;
                else if (checkedId == R.id.sugar_large) sugarLevel = 3;
            }
            brewConfiguration.setSugarLevel(sugarLevel);
        });

        tempSlider.addOnChangeListener((slider, value, fromUser) -> {
            int temperature = 40 + (int) value;
            tvTempLabel.setText("Infuse Temperature: " + temperature + "°C");
            brewConfiguration.setTemperature(temperature);
        });

        btnTempIncrease.setOnClickListener(v -> {
            float currentValue = tempSlider.getValue();
            if (currentValue < tempSlider.getValueTo()) {
                tempSlider.setValue(currentValue + 1);
                brewConfiguration.setTemperature((int) currentValue + 40 + 1);
            }
        });

        btnTempDecrease.setOnClickListener(v -> {
            float currentValue = tempSlider.getValue();
            if (currentValue > tempSlider.getValueFrom()) {
                tempSlider.setValue(currentValue - 1);
                brewConfiguration.setTemperature((int) currentValue + 40 - 1);
            }
        });

        btnStart.setOnClickListener(v -> {

            brewConfiguration.isMachineBusy(getActivity(), getContext());
            int busyyy = prefs.getInt("machine_busy", 1);
            Log.e("test", "busyy var: " + busyyy);
            if (busyyy == 1) {
                Toast.makeText(getActivity(), "Machine is busy! Please try again later!", Toast.LENGTH_SHORT).show();
            } else {
                brewConfiguration.sendOrder(getActivity(), getContext());
                new Handler().postDelayed(() -> brewConfiguration.saveMachineOrderId(getActivity(), getContext()), 1000);
            }
        });

        btnFav.setOnClickListener(v -> brewConfiguration.sendFavourite(getActivity(), getContext()));

        runnable = new Runnable() {
            @Override
            public void run() {
                View snappedView = snapHelper.findSnapView(layoutManager);
                int pos = layoutManager.getPosition(snappedView);
                Coffee currentCoffee = coffeeList.get(pos);
                db.isMachineBusy(getActivity(), getContext());
                int busyyy = prefs.getInt("machine_busy", 1);
                MaterialButton btnStart = getView().findViewById(R.id.actionBtn);
                btnStart.setEnabled(coffeeList.get(pos).getCoffeeLevel() >= startButtonDisableThresh);
                handler.postDelayed(this, intervalMachineBusyCheck);
            }
        };
        handler.post(runnable);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
    }
}
