package com.tanim.toolbank;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setupRunner();
        locationSetup();
        Toast.makeText(this, isLocationEnabled() ? "Using GPS for Live Weather" : "Using Previously Saved Location for Weather", Toast.LENGTH_SHORT).show();
        updateDashboard();

        Button author = findViewById(R.id.authorBtn);
        author.setOnClickListener(v -> {
            Intent intent = new Intent(this, Author.class);
            startActivity(intent);
        });

        // Set up RecyclerView with GridLayoutManager
        RecyclerView recyclerView = findViewById(R.id.toolsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(new ToolsAdapter(this, getToolList()));

        // Execute the AsyncTask to fetch and display the quote
        // new FetchQuoteTask().execute();
    }

    private void updateDashboard() {
        Handler handler = new Handler();
        Runnable runnable = this::updateDashboard;
        handler.postDelayed(runnable, 1000);
        // Inside your MainActivity.java
        TextView dateInfo = findViewById(R.id.dateInfo);
        TextView timeInfo = findViewById(R.id.timeInfo);
        LinearLayout dash = findViewById(R.id.dashboardBox);
        dash.setBackgroundResource(isDay() ? R.drawable.daybg : R.drawable.nightbg);

        // Get and set current date information (replace with your logic)
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        dateInfo.setText("Date: " + currentDate);

        // Set current time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String postTime = hour >= 12 && hour < 24 ? " PM" : " AM";
        String time = hour == 0 || hour == 12 ? String.format("%d:%02d:%02d %s", 12, minute, second, postTime) : String.format("%d:%02d:%02d %s", hour % 12, minute, second, postTime);
        timeInfo.setText("Time: " + time);
    }

    private boolean isDay() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour > 6 && hour < 18;
    }

    private ArrayList<ToolModel> getToolList() {
        ArrayList<ToolModel> tools = new ArrayList<>();
        tools.add(new ToolModel("Mirror", R.drawable.ic_mirror));
        tools.add(new ToolModel("Stop Watch", R.drawable.ic_stopwatch));
        tools.add(new ToolModel("Flash Light", R.drawable.ic_flash));
        tools.add(new ToolModel("BMI", R.drawable.ic_bmi));
        // Add more tools as needed
        return tools;
    }

    private void locationSetup() {
        Handler handler = new Handler();
        Runnable runnable = this::locationSetup;
        handler.postDelayed(runnable, 10000);
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(this, new String [] {Manifest.permission.ACCESS_FINE_LOCATION},1001);
        } else {
            // Check if location is enabled
            if (isLocationEnabled()) {
                // Request location updates if enabled
                requestLocationUpdates();
            } else {
                // Use last known location from SharedPreferences if location is not enabled
                useLastKnownLocation();
            }
        }
    }

    private void useLastKnownLocation() {
        // Retrieve last known location from SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        latitude = Double.parseDouble(preferences.getString("last_latitude", "23.6850"));
        longitude = Double.parseDouble(preferences.getString("last_longitude", "90.3563"));
        getWeatherData(latitude, longitude);
    }

    private boolean isLocationEnabled() {
        try {
            int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            return mode != Settings.Secure.LOCATION_MODE_OFF;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void requestLocationUpdates() {
        // Check if the location provider is enabled
        // For simplicity, we use the fused location provider here
        // You may consider using other providers based on your requirements
        // (e.g., GPS_PROVIDER, NETWORK_PROVIDER)
        // FusedLocationProviderClient is part of the Google Play services
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            locationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            getWeatherData(latitude, longitude);
                            saveLastKnownLocation(latitude, longitude);
                        }
                    });
        }
    }

    private void saveLastKnownLocation(double latitude, double longitude) {
        // Save last known location in SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("last_latitude", String.valueOf(latitude));
        editor.putString("last_longitude", String.valueOf(longitude));
        editor.apply();
    }

    private void getWeatherData(double latitude, double longitude) {
        // Replace "YOUR_API_KEY" with your OpenWeatherMap API key
        String apiKey = "d21f1b1c4ebef5b63c03d2c8a219beea";
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude +
                "&lon=" + longitude + "&appid=" + apiKey + "&units=metric"; // Request temperature in Celsius

        // Use a library like Retrofit, Volley, or OkHttp to make the API request
        // (Example using AsyncTask for simplicity)
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        return readStream(in);
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    // Parse the JSON response and update your UI with weather information
                    // Example: Update TextView with weather temperature
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject.has("main")) {
                            JSONObject mainObject = jsonObject.getJSONObject("main");
                            if (mainObject.has("temp")) {
                                double temperatureCelsius = mainObject.getDouble("temp");
                                TextView weatherInfo = findViewById(R.id.weatherInfo);
                                weatherInfo.setText("Temperature: " + temperatureCelsius + "°C");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private String readStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();
        return stringBuilder.toString();
    }
}
