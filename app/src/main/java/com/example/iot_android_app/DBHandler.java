package com.example.iot_android_app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DBHandler {
    private int disableThr = 5;
    private JSONArray settingsCache;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private String user = "shlok";
    private String SignUpUrl = "https://studev.groept.be/api/a24ib2team102/SignUpAppChecker/";
    private String LoginUrl = "https://studev.groept.be/api/a24ib2team102/CheckLoginData/";
    private String sendOrderUrl = "https://studev.groept.be/api/a24ib2team102/send_order/";
    private String sendOrderMachineUrl = "https://studev.groept.be/api/a24ib2team102/send_order_machine/";
    private String sendFavouriteUrl = "https://studev.groept.be/api/a24ib2team102/add_to_fav/";
    private String getNameLevelUrl = "https://studev.groept.be/api/a24ib2team102/get_name_level";
    private String getHistoryUrl = "https://studev.groept.be/api/a24ib2team102/get_history_for_user/";
    private String getSettings = "https://studev.groept.be/api/a24ib2team102/get_settings";
    private String isFavorite = "https://studev.groept.be/api/a24ib2team102/in_fav/";
    private String removeFavorite = "https://studev.groept.be/api/a24ib2team102/remove_favorite/";
    private String getOrderIdFromMachineUrl = "https://studev.groept.be/api/a24ib2team102/get_latest_orderid";
    private String getCurrentOrderInfoUrl = "https://studev.groept.be/api/a24ib2team102/get_current_order_info/";
    private String checkIfMachineBusyUrl = "https://studev.groept.be/api/a24ib2team102/check_if_machine_busy";
    private String getFavsList = "https://studev.groept.be/api/a24ib2team102/get_favs/";

    public String signUpUser(String username, String email, String password) {
        String requestUrl = SignUpUrl + "?username=" + username + "&password=" + password + "&email=" + email ;
        Log.d("SignUpRequest", "Request URL: " + requestUrl);
        return makeGETRequest(requestUrl);
    }

    public String getSettings() {
        return makeGETRequest(getSettings);
    }

    public String LogInUser(String username,String password){
        String requestUrl = LoginUrl + username + "/" + password;
        return  makeGETRequest(requestUrl);
    }

    //send current data to orders and order_machine
    public String sendMyOrder(int id, String name, int sugar, int shot, int temp){
        String sqlDateTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        String requestUrl = sendOrderUrl + name + "/" + sugar + "/" + shot + "/" + temp + "/" + user + "/" + sqlDateTime;
        requestUrl = requestUrl.replaceAll(" ", "+");
        String requestUrl2 = sendOrderMachineUrl + id +"/"+ sugar +"/"+ shot +"/"+ temp;
        return  makeGETRequest(requestUrl)+makeGETRequest(requestUrl2);
    }
    //send the favourite
    public String sendMyFavourite(String name, int shot, int sugar, int temp){
        String requestUrl = sendFavouriteUrl + user + "/" + name + "/" + shot + "/" + sugar + "/" + temp;
        requestUrl = requestUrl.replaceAll(" ", "+");
        return  makeGETRequest(requestUrl);
    }
    //get names and levels of tea and coffee
    public String getNameLevel(){
        return makeGETRequest(getNameLevelUrl);
    }
    public String getOrderIdFromMachine(){
        return makeGETRequest(getOrderIdFromMachineUrl);
    }
    public String checkIfMachineBusy(){
        return makeGETRequest(checkIfMachineBusyUrl);
    }
    public String getCurrentOrderInfo(int id){
        String requestUrl = getCurrentOrderInfoUrl + id;
        return makeGETRequest(requestUrl);
    }


    public String getHistory(String user) {
        String url = getHistoryUrl + user;
        return makeGETRequest(url);
    }

    public boolean switchFavorite(String user, String type, int shots, int sugar, int temp) {
        String url = isFavorite + shots + '/' + sugar + '/' + temp + '/' + user + '/' + type;
        url = url.replaceAll(" ", "+");
        String jsonString = makeGETRequest(url);
        try {
            JSONObject obj = new JSONArray(jsonString).getJSONObject(0);
            makeGETRequest(removeFavorite + obj.getInt("id"));
            return false;
        } catch (JSONException e) {
            sendMyFavourite(type, shots, sugar, temp);
            return true;
        }
    }

    public void saveCurName() {

    }

    public boolean isFavorite(String user, String type, int shots, int sugar, int T) {
        String url = isFavorite + shots + '/' + sugar + '/' + T + '/' + user + '/' + type;
        url = url.replaceAll(" ", "+");
        String settingsJSON = makeGETRequest(url);
        try {
            new JSONArray(settingsJSON).getJSONObject(0);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public void saveMachineOrderId(Activity activity, Context context) {
        new Thread(() -> {
            String response = this.getOrderIdFromMachine();
            if (activity == null) return;

            activity.runOnUiThread(() -> {
                if (response.isEmpty()) {
                    Toast.makeText(activity, "Server Error, failed to load data!", Toast.LENGTH_SHORT).show();return;}
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

    public boolean canBeOrdered(String type) {
        if (settingsCache == null) return false;
        try {
            for (int i = 0; i < settingsCache.length(); i++) {
                JSONObject curObject = settingsCache.getJSONObject(i);
                if (curObject.getString("name").equals(type) &&
                        curObject.getInt("level") > disableThr) {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void startSettingsUpdater() {
        scheduler.scheduleWithFixedDelay(() -> {
            String settingsJSON = getSettings();
            try {
                settingsCache = new JSONArray(settingsJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS); // Runs every 5 seconds
    }

    public String makeGETRequest(String urlName){
        BufferedReader rd = null;
        StringBuilder sb = null;
        String line = null;
        try {
            URL url = new URL(urlName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            sb = new StringBuilder();
            while ((line = rd.readLine()) != null)
            {
                sb.append(line).append('\n');
            }
            conn.disconnect();
            return sb.toString();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return "";
    }

    public List<orderModel> getFavoritesList(String user) {
        List<orderModel> list = new ArrayList<>();
        String url = getFavsList + user;
        url = url.replaceAll(" ", "+");
        try {
            String jsonStr = makeGETRequest(url);
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject curObject = array.getJSONObject(i);
                list.add(new orderModel(
                        "no data",
                        curObject.getString("type"),
                        curObject.getInt("shot"),
                        curObject.getInt("sugar"),
                        curObject.getInt("temperature"),
                        canBeOrdered(curObject.getString("type")),
                        true, curObject.getString("alias")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void sendMyOrder() {

    }
}