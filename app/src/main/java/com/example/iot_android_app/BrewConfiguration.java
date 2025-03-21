package com.example.iot_android_app;

import static android.app.PendingIntent.getActivity;

import android.app.Activity;
import android.util.Log;

import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BrewConfiguration {
    private int coffeeId; //1to3
    private int shotSize; // 1 to 3
    private int sugarLevel; //0 to 3
    private int temperature; // in °C 60 to 90
    private String name; //name of tea or coffee

    // Constructor, getters and setters
    public BrewConfiguration(int coffeeId, String name, int shotSize, int sugarLevel, int temperature) {
        this.coffeeId = coffeeId;
        this.shotSize = shotSize;
        this.sugarLevel = sugarLevel;
        this.temperature = temperature;
        this.name=name;
    }

    // Getters and setters...
    public void setCoffeeId(int coffeeId) {
        this.coffeeId = coffeeId;
    }

    public void setShotSize(int shotSize) {
        this.shotSize = shotSize;
    }

    public void setSugarLevel(int sugarLevel) {
        this.sugarLevel = sugarLevel;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void sendOrder(Activity activity){
        new Thread(() -> {
            DBHandler dbHandler = new DBHandler();
            String response = dbHandler.sendMyOrder(coffeeId, name, sugarLevel, shotSize, temperature).replaceAll("\\n", "");

            if (activity == null) return;

            activity.runOnUiThread(() -> {
                if (response.equals("[][]")) {
                    Toast.makeText(activity, "Order Placed!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("SendOrder", "response: " + response);
                    Toast.makeText(activity, "Failed to place order!", Toast.LENGTH_SHORT).show();
                }


            });
        }).start();
    }

    public void sendFavourite(Activity activity){
        new Thread(() -> {
            DBHandler dbHandler = new DBHandler();
            String response = dbHandler.sendMyFavourite(name, shotSize, sugarLevel, temperature).replaceAll("\\n", "");

            if (activity == null) return;

            activity.runOnUiThread(() -> {
                if (response.equals("[]")) {
                    Toast.makeText(activity, "Added to favourites!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("SendOrder", "response: " + response);
                    Toast.makeText(activity, "Failed to add favourite!", Toast.LENGTH_SHORT).show();
                }


            });
        }).start();
    }
}
