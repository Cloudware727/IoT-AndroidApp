package com.example.iot_android_app;

import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.slider.Slider;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class home_screen extends Fragment{

    private RecyclerView rvCoffeeCarousel;
    private CoffeeCarouselAdapter adapter;
    private List<Coffee> coffeeList;

    private LinearSnapHelper snapHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //database init
        DBHandler db = new DBHandler();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_screen, container, false);

        // Initialize RecyclerView
        rvCoffeeCarousel = view.findViewById(R.id.rvCoffeeCarousel);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCoffeeCarousel.setLayoutManager(layoutManager);

        // Initialize coffee list with some sample coffee data
        coffeeList = new ArrayList<>();
        coffeeList.add(new Coffee(1, "Herbs Tea", 80, R.drawable.herbs_tea_bg));  // Background image res ID as well
        coffeeList.add(new Coffee(2, "Lemon Tea", 50, R.drawable.lemon_tea_bg));
        coffeeList.add(new Coffee(3, "Instant Coffee", 60, R.drawable.instant_coffee_bg));

        //make brewingConfiguration object
        BrewConfiguration brewConfiguration = new BrewConfiguration(1, coffeeList.get(0).getName(), 2,0,70);

        // Set up the adapter with a callback for long press
        adapter = new CoffeeCarouselAdapter(coffeeList);
        rvCoffeeCarousel.setAdapter(adapter);

        // Attach LinearSnapHelper for snapping behavior
        snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(rvCoffeeCarousel);

        // Optionally, listen for scroll events to update your brewing config
        rvCoffeeCarousel.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Get the current snapped view (hero card)
                    View snappedView = snapHelper.findSnapView(layoutManager);
                    int pos = layoutManager.getPosition(snappedView);
                    Coffee currentCoffee = coffeeList.get(pos);
                    // Now update your brewing configuration with the current hero card info
                    brewConfiguration.setCoffeeId(currentCoffee.getId());
                    brewConfiguration.setName(currentCoffee.getName());
                }
            }
        });


        //chip group listeners
        ChipGroup cgShotSize = view.findViewById(R.id.cgShotSize);
        ChipGroup cgSugarSize = view.findViewById(R.id.cgSugarSize);

        // Set OnCheckedStateChangeListener for the Shot Size ChipGroup
        cgShotSize.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int selectedShot = 2; // Default value
            for (int checkedId : checkedIds) {
                if (checkedId == R.id.shot_small) {
                    selectedShot = 1; // Small shot selected
                } else if (checkedId == R.id.shot_med) {
                    selectedShot = 2; // Medium shot selected
                } else if (checkedId == R.id.shot_large) {
                    selectedShot = 3; // Large shot selected
                }
            }
            // Update the BrewConfiguration with selected shot size
            brewConfiguration.setShotSize(selectedShot);
            // Optionally, update UI feedback here
        });

        //sugar
        cgSugarSize.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int sugarLevel = 0; // Default value
            for (int checkedId : checkedIds) {
                if (checkedId == R.id.sugar_none) {
                    sugarLevel = 0; // Small shot selected
                } else if (checkedId == R.id.sugar_small) {
                    sugarLevel = 1; // Small shot selected
                } else if (checkedId == R.id.sugar_med) {
                    sugarLevel = 2; // Medium shot selected
                } else if (checkedId == R.id.sugar_large) {
                    sugarLevel = 3; // Large shot selected
                }
            }
            // Update the BrewConfiguration with selected shot size
            brewConfiguration.setSugarLevel(sugarLevel);
            // Optionally, update UI feedback here
        });


        //temperature update
        Slider tempSlider = view.findViewById(R.id.tempSlider);
        TextView tvTempLabel = view.findViewById(R.id.tvTempLabel);
        MaterialButton btnTempIncrease = view.findViewById(R.id.increaseBtn);
        MaterialButton btnTempDecrease = view.findViewById(R.id.decreaseBtn);
        // Slider listener
        tempSlider.addOnChangeListener((slider, value, fromUser) -> {
            int temperature = 60 + (int) value;
            tvTempLabel.setText("Infuse Temperature: " + temperature + "°C");
            brewConfiguration.setTemperature(temperature);
        });
        // Increase button listener
        btnTempIncrease.setOnClickListener(v -> {
            float currentValue = tempSlider.getValue();
            if (currentValue < tempSlider.getValueTo()) { // Check if within max limit
                tempSlider.setValue(currentValue + 1); // Increase by 1
                brewConfiguration.setTemperature((int)currentValue + 1);
            }
        });
        // Decrease button listener
        btnTempDecrease.setOnClickListener(v -> {
            float currentValue = tempSlider.getValue();
            if (currentValue > tempSlider.getValueFrom()) { // Check if within min limit
                tempSlider.setValue(currentValue - 1); // Decrease by 1
                brewConfiguration.setTemperature((int)currentValue - 1);
            }
        });
        // Start button listener
        MaterialButton btnStart = view.findViewById(R.id.actionBtn);
        btnStart.setOnClickListener(v -> {
            brewConfiguration.sendOrder(getActivity());
        });

        return view;
    }

}
