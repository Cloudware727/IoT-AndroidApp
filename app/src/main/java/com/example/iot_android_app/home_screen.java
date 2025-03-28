package com.example.iot_android_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.slider.Slider;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class home_screen extends Fragment{

    private RecyclerView rvCoffeeCarousel;
    private CoffeeCarouselAdapter adapter;
    private List<Coffee> coffeeList;
    private LinearSnapHelper snapHelper;
    private int startButtonDisableThresh = 5;
    //things required to run something on loop
    private Handler handler = new Handler(); private Runnable runnable;
    private int intervalMachineBusyCheck = 10000;

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

        // Initialize buttons and all
        ChipGroup cgShotSize = view.findViewById(R.id.cgShotSize);
        ChipGroup cgSugarSize = view.findViewById(R.id.cgSugarSize);
        Slider tempSlider = view.findViewById(R.id.tempSlider);
        TextView tvTempLabel = view.findViewById(R.id.tvTempLabel);
        MaterialButton btnTempIncrease = view.findViewById(R.id.increaseBtn);
        MaterialButton btnTempDecrease = view.findViewById(R.id.decreaseBtn);
        MaterialButton btnStart = view.findViewById(R.id.actionBtn);
        MaterialButton btnFav = view.findViewById(R.id.addfavBtn);
        CardView progressCard = getActivity().findViewById(R.id.progressCard);

        // Initialize coffee list with some sample coffee data
        coffeeList = new ArrayList<>();
        coffeeList.add(new Coffee(1, " ", 100, R.drawable.herbs_tea_bg));  // Background image res ID as well
        coffeeList.add(new Coffee(2, " ", 100, R.drawable.lemon_tea_bg));
        coffeeList.add(new Coffee(3, " ", 100, R.drawable.instant_coffee_bg));

        //make brewingConfiguration object
        BrewConfiguration brewConfiguration = new BrewConfiguration(1, coffeeList.get(0).getName(), 2,0,70);

        // Set up the adapter with a callback for long press
        adapter = new CoffeeCarouselAdapter(coffeeList);
        rvCoffeeCarousel.setAdapter(adapter);

        // Attach LinearSnapHelper for snapping behavior
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvCoffeeCarousel);

        //get name and level from database
        new Thread(() -> {
            DBHandler dbHandler = new DBHandler();
            String response = dbHandler.getNameLevel();
            if (getActivity() == null) return;

            getActivity().runOnUiThread(() -> {
                if (response.isEmpty()) {Toast.makeText(getActivity(), "Server Error, failed to load data!", Toast.LENGTH_SHORT).show();return;}
                try {
                    JSONArray jsonResponse = new JSONArray(response);
                    if (jsonResponse.length() == 0) {Toast.makeText(getActivity(), "Invalid server response!", Toast.LENGTH_SHORT).show();return;}
                    for (int i = 0; i < jsonResponse.length(); i++)
                    {
                        Log.e("test", "obj num: " + i);
                        JSONObject curObject = jsonResponse.getJSONObject(i);
                        coffeeList.get(i).setName(curObject.getString("name"));
                        coffeeList.get(i).setCoffeeLevel(curObject.getInt("level"));
                    }
                    //save names of tea and coffee
                    SharedPreferences prefs = getContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("name_first", coffeeList.get(0).getName());
                    editor.putString("name_second", coffeeList.get(1).getName());
                    editor.putString("name_third", coffeeList.get(2).getName());
                    editor.apply(); // commit-waits until data is saved, apply-saves in the background
                    brewConfiguration.setName(coffeeList.get(0).getName());
                    //re-initialize adapter for carousel
                    adapter = new CoffeeCarouselAdapter(coffeeList);
                    rvCoffeeCarousel.setAdapter(adapter);
                    //set start button state
                    if (coffeeList.get(0).getCoffeeLevel() < 5) {btnStart.setEnabled(false);}
                    else {btnStart.setEnabled(true);}
//                    updateStartButtonStateExternally();

                } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Error processing response.", Toast.LENGTH_SHORT).show();
                    }
            });
        }).start();


        // listen for scroll events to update your brewing config
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
                    // update start button state enable or disable
                    SharedPreferences prefs = getContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
                    int busyyy = prefs.getInt("machine_busy", 1);
                    if (coffeeList.get(pos).getCoffeeLevel() < startButtonDisableThresh) {btnStart.setEnabled(false);}
                    else {btnStart.setEnabled(true);}
                }
            }
        });


        //chip group listeners
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
        // Slider listener
        tempSlider.addOnChangeListener((slider, value, fromUser) -> {
            int temperature = 50+(int) value;
            tvTempLabel.setText("Infuse Temperature: " + temperature + "°C");
            brewConfiguration.setTemperature(temperature);
        });
        // Increase button listener
        btnTempIncrease.setOnClickListener(v -> {
            float currentValue = tempSlider.getValue();
            if (currentValue < tempSlider.getValueTo()) { // Check if within max limit
                tempSlider.setValue(currentValue + 1); // Increase by 1
                brewConfiguration.setTemperature((int)currentValue +50 + 1);
            }
        });
        // Decrease button listener
        btnTempDecrease.setOnClickListener(v -> {
            float currentValue = tempSlider.getValue();
            if (currentValue > tempSlider.getValueFrom()) { // Check if within min limit
                tempSlider.setValue(currentValue - 1); // Decrease by 1
                brewConfiguration.setTemperature((int)currentValue +50 - 1);
            }
        });
        // Start button listener
        btnStart.setOnClickListener(v -> {
            //place order only if machine if not busy otherwise give toast
            brewConfiguration.isMachineBusy(getActivity(), getContext());
            SharedPreferences prefs = getContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
            int busyyy = prefs.getInt("machine_busy", 1);
            if (busyyy == 1){
                Toast.makeText(getActivity(), "Machine is busy! Please try again later!", Toast.LENGTH_SHORT).show();
            } else {
                brewConfiguration.sendOrder(getActivity(), getContext());
                //save id of order machine table for current order
                new Handler().postDelayed(() -> {
                            brewConfiguration.saveMachineOrderId(getActivity(), getContext());
//                            btnStart.setEnabled(false);
                        }, 1000
                );
            }

        });
        // Favourites button listener
        btnFav.setOnClickListener(v -> {
            brewConfiguration.sendFavourite(getActivity(), getContext());
        });
        // keep checking if machine is busy, then enable or disable start button
        runnable = new Runnable() {
            @Override
            public void run() {
                // get current coffee
                View snappedView = snapHelper.findSnapView(layoutManager);
                int pos = layoutManager.getPosition(snappedView);
                Coffee currentCoffee = coffeeList.get(pos);
                //get if machine is busy
                brewConfiguration.isMachineBusy(getActivity(), getContext());
                SharedPreferences prefs = getContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
                int busyyy = prefs.getInt("machine_busy", 1);
                // take action
                if (coffeeList.get(pos).getCoffeeLevel() < startButtonDisableThresh) {btnStart.setEnabled(false);}
                else {btnStart.setEnabled(true);}
                Log.d("Repeating", "Start button status updated");
                handler.postDelayed(this, intervalMachineBusyCheck); // Repeat every 10 sec
            }
        };
        handler.post(runnable); // Start running


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable); // Clean up when fragment view is destroyed
    }

}
