package com.example.iot_android_app;

import android.util.Log;

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

    public String getDrinkById(int id) {
        return makeGETRequest("https://studev.groept.be/api/a24ib2team102/get_drink_by_id/" + id);
    }


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
        String settingsJSON = getSettings();
        try {
            JSONArray array = new JSONArray(settingsJSON);
            for (int i = 0; i < 3; i++) {
                JSONObject curObject = array.getJSONObject(i);
                String curType = curObject.getString("name");
                if (curType.equals(type) &&
                        curObject.getInt("level") > disableThr)
                    return true;
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return false;
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