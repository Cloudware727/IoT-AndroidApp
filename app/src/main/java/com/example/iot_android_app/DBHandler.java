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
import java.util.Date;

public class DBHandler {
    public String getDrinkById(int id) {
        return makeGETRequest("https://studev.groept.be/api/a24ib2team102/get_drink_by_id/" + id);
    }

    private String SignUpUrl = "https://studev.groept.be/api/a24ib2team102/SignUpAppChecker/";
    private String LoginUrl = "https://studev.groept.be/api/a24ib2team102/CheckLoginData/";
    private String sendOrderUrl = "https://studev.groept.be/api/a24ib2team102/send_order/";
    private String sendOrderMachineUrl = "https://studev.groept.be/api/a24ib2team102/send_order_machine/";

    public String signUpUser(String username, String email, String password) {
        String requestUrl = SignUpUrl + "?username=" + username + "&password=" + password + "&email=" + email ;
        Log.d("SignUpRequest", "Request URL: " + requestUrl);
        return makeGETRequest(requestUrl);
    }

    public String LogInUser(String username,String password){
        String requestUrl = LoginUrl + username + "/" + password;
        return  makeGETRequest(requestUrl);
    }

    public String urlSendOrder(int id, String name, int sugar, int shot, int temp, String uname){
        String sqlDateTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        String requestUrl = sendOrderUrl + name + "/" + sugar + "/" + shot + "/" + temp + "/" + uname + "/" + sqlDateTime;
        requestUrl = requestUrl.replaceAll(" ", "+");
        String requestUrl2 = sendOrderMachineUrl + id +"/"+ sugar +"/"+ shot +"/"+ temp;
        return  makeGETRequest(requestUrl)+makeGETRequest(requestUrl2);
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

    public String[] getHistory(String jsonString) {
        try {
            JSONArray array = new JSONArray(jsonString);
            String[] res = new String[array.length()];
            //for (int i = 0; i < array.length(); i++) {
            //    JSONObject curObject = array.getJSONObject(i);
            //    int id = curObject.getInt("drink_id");
            //    JSONObject drink_info = new JSONObject(getDrinkById(id));
            //    res[i] = curObject.getString("time") + " - " + drink_info.getString("Type")
            //            + " x" + drink_info.getInt("Strength") + " (sugar: " +
            //            drink_info.getInt("Sugar") + ") (T: " +
            //            drink_info.getInt("Temperature") + ")";
            //}
            return res;
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }
}