package com.example.iot_android_app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DBHandler {
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