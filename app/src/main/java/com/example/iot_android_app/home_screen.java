package com.example.iot_android_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_screen, container, false);

        // Initialize RecyclerView
        rvCoffeeCarousel = view.findViewById(R.id.rvCoffeeCarousel);
        rvCoffeeCarousel.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Initialize coffee list with some sample coffee data
        coffeeList = new ArrayList<>();
        coffeeList.add(new Coffee(1, "Herbs Tea", 80, R.drawable.herbs_tea_bg));  // Background image res ID as well
        coffeeList.add(new Coffee(2, "Lemon Tea", 50, R.drawable.lemon_tea_bg));
        coffeeList.add(new Coffee(3, "Instant Coffee", 60, R.drawable.instant_coffee_bg));

        // Set up the adapter with a callback for long press
        adapter = new CoffeeCarouselAdapter(coffeeList);
        rvCoffeeCarousel.setAdapter(adapter);

        // Attach LinearSnapHelper for snapping behavior
        snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(rvCoffeeCarousel);

        

        return view;
    }

}
