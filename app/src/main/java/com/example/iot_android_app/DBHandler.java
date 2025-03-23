package com.example.iot_android_app;

import android.util.Log;
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
    private final int UPDATE_INTERVAL = 5000; // 5 seconds

    private String user = "shlok";
    private String SignUpUrl = "https://studev.groept.be/api/a24ib2team102/SignUpAppChecker/";
    private String LoginUrl = "https://studev.groept.be/api/a24ib2team102/CheckLoginData/";
    private String sendOrderUrl = "https://studev.groept.be/api/a24ib2team102/send_order/";
    private String sendOrderMachineUrl = "https://studev.groept.be/api/a24ib2team102/send_order_machine/";
    private String sendFavouriteUrl = "https://studev.groept.be/api/a24ib2team102/add_to_fav/";
    private String getNameLevelUrl = "https://studev.groept.be/api/a24ib2team102/get_name_level";
    private String getHistoryUrl = "https://studev.groept.be/api/a24ib2team102/get_history_for_user/";
    private String getSettings = "https://studev.groept.be/api/a24ib2team102/get_settings";
    private String isFavorite = "https://studev.groept.be/api/a24ib2team102/in_favorites/";
    private String removeFavorite = "https://studev.groept.be/api/a24ib2team102/remove_favorite/";

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

    public String getHistory(String user) {
        String url = getHistoryUrl + user;
        return makeGETRequest(url);
    }

    public boolean switchFavorite(String user, String type, int shots, int sugar, int temp) {
        String url = isFavorite + shots + '/' + sugar + '/' + temp;
        String settingsJSON = makeGETRequest(url);
        boolean alreadyFavorite = false;
        int favoriteId = -1;
        try {
            JSONArray array = new JSONArray(settingsJSON);
            for (int i = 0; i < array.length(); i++) {
                JSONObject curObject = array.getJSONObject(i);
                if (curObject.getString("type").equals(type) && curObject.getString("user_id").equals(user)) {
                    alreadyFavorite = true;
                    favoriteId = curObject.getInt("id");
                    break;
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if (alreadyFavorite) {
            makeGETRequest(removeFavorite + favoriteId);
            return false;
        } else {
            sendMyFavourite(type, shots, sugar, temp);
            return true;
        }
    }

    public boolean isFavorite(String user, String type, int shots, int sugar, int T) {
        String url = isFavorite + shots + '/' + sugar + '/' + T;
        String settingsJSON = makeGETRequest(url);
        try {
            JSONArray array = new JSONArray(settingsJSON);
            for (int i = 0; i < array.length(); i++) {
                JSONObject curObject = array.getJSONObject(i);
                String curType = curObject.getString("type");
                String curUser = curObject.getString("user_id");
                if (curType.equals(type) && curUser.equals(user))
                    return true;
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return false;
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

    public String getDrinkById(int id) {
        return makeGETRequest("https://studev.groept.be/api/a24ib2team102/get_drink_by_id/" + id);
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
}