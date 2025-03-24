package com.example.iot_android_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.iot_android_app.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    //progress card things
    private ProgressBar progressBar;
    private CardView progressCard;
    private TextView currentTemperature;
    private TextView currentProgress;
    private TextView summaryName;
    private TextView summaryShot;
    private TextView summarySugar;
    private TextView summaryTemperature;
    private TextView summaryHeading;
    private View myDivider;
    private boolean isExpanded = false;
    private Handler handler = new Handler();
    private int progressUpdateInterval = 10000;
    String[] progressData = new String[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new home_screen());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home_screen) {
                replaceFragment(new home_screen());
                // Handle home screen
            } else if (itemId == R.id.fav_screen) {
                replaceFragment(new fav_screen());
                // Handle favorites screen
            } else if (itemId == R.id.history_screen) {
                replaceFragment(new history_screen());
                // Handle history screen
            } else if (itemId == R.id.account_screen) {
                replaceFragment(new account_screen());
                // Handle account screen
            }

            return true;
        });

        //progress card
        progressBar = findViewById(R.id.progressBar);
        progressCard = findViewById(R.id.progressCard);
        currentTemperature = findViewById(R.id.currentTemperature);
        currentProgress = findViewById(R.id.currentProgress);
        summaryName = findViewById(R.id.summaryName);
        summaryShot = findViewById(R.id.summaryShot);
        summarySugar = findViewById(R.id.summarySugar);
        summaryTemperature = findViewById(R.id.summaryTemperature);
        summaryHeading = findViewById(R.id.summaryHeading);
        myDivider = findViewById(R.id.divider);
        // Toggle expand/collapse
        progressCard.setOnClickListener(v -> {
            isExpanded = !isExpanded;
            currentTemperature.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            currentProgress.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            summaryName.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            summaryShot.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            summarySugar.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            summaryTemperature.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            summaryHeading.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            myDivider.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        });
        startProgressUpdater();
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();

    }

    private void startProgressUpdater() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //get the data for current order
                SharedPreferences prefs = MainActivity.this.getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
                getInfo(prefs.getInt("current_order_id_m", 0));

                //dispenser,sugar,shot,temp,progress,current_temp
                Log.e("test", "progress:" + progressData[4]);
                //update things
                if (progressData[4]!=null && Integer.parseInt(progressData[4])>=0 && Integer.parseInt(progressData[4]) <100) {
                    //sugar id to text
                    int sugarid = Integer.parseInt(progressData[1]);
                    String sugarLevel = (sugarid == 0) ? "Zero" : (sugarid == 1) ? "Little" : (sugarid == 2) ? "Sweet" : (sugarid == 3) ? "Extra" : "Could not load data";
                    //sugar id to text
                    int shotid = Integer.parseInt(progressData[2]);
                    String shotSize = (shotid == 0) ? "Nuance" : (shotid == 1) ? "Refine" : (shotid == 2) ? "Amplify" : "Could not load data";

                    // set all
                    progressBar.setProgress(Integer.parseInt(progressData[4]));
                    currentProgress.setText("Progress: " + progressData[4] +"%");
                    currentTemperature.setText("Current Temperature: " + progressData[5] +"°C");
                    summaryName.setText("Name: " + progressData[0]);
                    summaryShot.setText("Shot: " + shotSize);
                    summarySugar.setText("Sugar: " + sugarLevel);
                    summaryTemperature.setText("Infusing Temperature: " + progressData[3] +"°C");

                    progressCard.setVisibility(View.VISIBLE);
                } else {
                    progressCard.setVisibility(View.GONE);
                }
                handler.postDelayed(this, progressUpdateInterval);
            }
        }, progressUpdateInterval);
    }
    private void getInfo(int orderId){
        //dispenser,sugar,shot,temp,progress,current_temp
        Log.e("test", "orderid:" + orderId);

        String[] fields = {"dispenser", "sugar", "strength", "temperature", "progress", "Temperature"};
        new Thread(() -> {
            DBHandler dbHandler = new DBHandler();
            String response = dbHandler.getCurrentOrderInfo(orderId);
            if (this == null) return;


            this.runOnUiThread(() -> {
                if (response.isEmpty()) {Toast.makeText(this, "Server Error, failed to load data!", Toast.LENGTH_SHORT).show();return;}
                try {
                    JSONArray jsonResponse = new JSONArray(response);

                    if (jsonResponse.length() == 0) {java.util.Arrays.fill(progressData, null);return;}

                    JSONObject curObject = jsonResponse.getJSONObject(0);
                    for (int i=0; i<6; i++){
                        progressData[i] = curObject.getString(fields[i]);
                    }

                } catch (JSONException e) {
                    Toast.makeText(this, "Error processing response.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}


