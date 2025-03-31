package com.example.iot_android_app;

import static android.app.PendingIntent.getActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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

    public void sendOrder(Activity activity, Context context){
        new Thread(() -> {
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String user = prefs.getString("username", "Guest");
            DBHandler dbHandler = new DBHandler();
            String response = dbHandler.sendMyOrder(coffeeId, name, sugarLevel, shotSize, temperature, user).replaceAll("\\n", "");
            Log.e("SendOrderresponse", "send_order_response: " + response);

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

    public void sendFavourite(Activity activity, Context context){
        new Thread(() -> {
            DBHandler dbHandler = new DBHandler();
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String user = prefs.getString("username", "Guest");
            String response = dbHandler.sendMyFavourite(name, shotSize, sugarLevel, temperature, user).replaceAll("\\n", "");

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
    public void saveMachineOrderId(Activity activity, Context context){
        new Thread(() -> {
            DBHandler dbHandler = new DBHandler();
            String response = dbHandler.getOrderIdFromMachine();
            if (activity == null) return;

            activity.runOnUiThread(() -> {
                if (response.isEmpty()) {Toast.makeText(activity, "Server Error, failed to load data!", Toast.LENGTH_SHORT).show();return;}
                try {
                    JSONArray jsonResponse = new JSONArray(response);
                    if (jsonResponse == null || jsonResponse.length() == 0) {Toast.makeText(activity, "Invalid server response! Please place the order again!", Toast.LENGTH_SHORT).show();return;}

                    JSONObject curObject = jsonResponse.getJSONObject(0);
                    int idTemp = (curObject.getInt("id"));
                    SharedPreferences prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("current_order_id_m", idTemp);
                    Log.e("test", "orderid: " + idTemp);
                    editor.commit(); // commit-waits until data is saved, apply-saves in the background

                } catch (JSONException e) {
                    Toast.makeText(activity, "Error processing response.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();

    }

    public void isMachineBusy(Activity activity, Context context){
        new Thread(() -> {
            DBHandler dbHandler = new DBHandler();
            String response = dbHandler.checkIfMachineBusy();
            if (activity == null) return;

            activity.runOnUiThread(() -> {
                if (response.isEmpty()) {Toast.makeText(activity, "Server Error, failed to load data!", Toast.LENGTH_SHORT).show();return;}
                try {
                    JSONArray jsonResponse = new JSONArray(response);
                    if (jsonResponse == null || jsonResponse.length() == 0) {Toast.makeText(activity, "Invalid server response!", Toast.LENGTH_SHORT).show();return;}

                    JSONObject curObject = jsonResponse.getJSONObject(0);
                    int busyyy = (curObject.getInt("has_data"));
                    SharedPreferences prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("machine_busy", busyyy);
                    Log.e("test", "machine is busy: " + busyyy);
                    editor.commit(); // commit-waits until data is saved, apply-saves in the background

                } catch (JSONException e) {
                    Toast.makeText(activity, "Error processing response.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();

    }
}
