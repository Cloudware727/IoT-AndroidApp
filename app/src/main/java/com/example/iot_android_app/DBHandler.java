package com.example.iot_android_app;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBHandler {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /*public void getDrinkById(int id) {
        return makeGETRequest("https://studev.groept.be/api/a24ib2team102/get_drink_by_id/" + id);
    } */
    public void getDrinkById(int id, final DBResponseCallback callback) {
        String url = "https://studev.groept.be/api/a24ib2team102/get_drink_by_id/" + id;

        makeGETRequest(url, new DBResponseCallback() {
            @Override
            public void onSuccess(String response) {
                callback.onSuccess(response);
            }

            @Override
            public void onSuccess(String[] data) {}

            @Override
            public void onError(String error) {
                // On error, pass the error message to the callback
                callback.onError(error);
            }
        });
    }

    public void makeGETRequest(final String urlName, final DBResponseCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = "";
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
                    while ((line = rd.readLine()) != null) {
                        sb.append(line).append('\n');
                    }

                    conn.disconnect();
                    response = sb.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    response = "Error: " + e.getMessage();
                }

                final String finalResponse = response;
                new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onSuccess(finalResponse);
                        }
                    }
                });
            }
        }).start();
    }
    /*public class FetchHistoryTask extends AsyncTask<String, Void, List<ItemModel>> {
        private DBHandler db;
        private ItemAdapter adapter;

        public FetchHistoryTask(DBHandler db, ItemAdapter adapter) {
            this.db = db;
            this.adapter = adapter;
        }

        @Override
        protected List<ItemModel> doInBackground(String... params) {
            String user = params[0]; // Username passed in params
            List<ItemModel> itemList = new ArrayList<>();

            // Step 1: Fetch the history for the user
            String response = db.makeGETRequest("https://studev.groept.be/api/a24ib2team102/get_history_for_user/" + user);

            try {
                JSONArray historyArray = new JSONArray(response);
                final int totalItems = historyArray.length();
                final List<String> historyList = new ArrayList<>(totalItems);

                // Step 2: Fetch data using foreign key for each history item
                for (int i = 0; i < historyArray.length(); i++) {
                    JSONObject historyItem = historyArray.getJSONObject(i);
                    int drinkId = historyItem.getInt("drink_id");

                    // Step 3: Fetch the drink information using the foreign key
                    String drinkInfoResponse = db.getDrinkById(drinkId);
                    JSONObject drinkInfo = new JSONObject(drinkInfoResponse);

                    String historyString = historyItem.getString("time") + " - " + drinkInfo.getString("Type")
                            + " x" + drinkInfo.getInt("Strength") + " (sugar: " +
                            drinkInfo.getInt("Sugar") + ") (T: " +
                            drinkInfo.getInt("Temperature") + ")";
                    historyList.add(historyString);
                }

                // Convert historyList to ItemModel objects
                for (String historyItem : historyList) {
                    itemList.add(new ItemModel(historyItem));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return itemList;
        }

        @Override
        protected void onPostExecute(List<ItemModel> result) {
            super.onPostExecute(result);
            // Update UI on the main thread
            adapter.updateItems(result);
        }
    }*/

    public interface DBResponseCallback {
        void onSuccess(String response);
        void onSuccess(String[] data);
        void onError(String error);
    }
    public void getHistory(final String jsonString, final DBResponseCallback callback) {
        try {
            JSONArray array = new JSONArray(jsonString);
            final List<String> res = new ArrayList<>();  // Use ArrayList instead of an array

            for (int i = 0; i < array.length(); i++) {
                final int id = array.getJSONObject(i).getInt("drink_id");

                // Call getDrinkById asynchronously
                int finalI = i;
                getDrinkById(id, new DBResponseCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject drink_info = new JSONObject(response);
                            res.add(array.getJSONObject(finalI).getString("time") + " - " + drink_info.getString("Type")
                                    + " x" + drink_info.getInt("Strength") + " (sugar: " +
                                    drink_info.getInt("Sugar") + ") (T: " +
                                    drink_info.getInt("Temperature") + ")");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Once all history is fetched, return the result via callback
                        if (res.size() == array.length()) {  // Check if all data is fetched
                            callback.onSuccess(res.toArray(new String[0])); // Convert to array and return
                        }
                    }

                    @Override
                    public void onSuccess(String[] data) {}

                    @Override
                    public void onError(String error) {
                        // Handle error if fetching drink info fails
                        callback.onError("Error fetching drink details: " + error);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}