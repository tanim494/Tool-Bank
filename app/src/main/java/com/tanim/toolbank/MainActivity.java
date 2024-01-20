package com.tanim.toolbank;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setupRunner();
        initialSetup();
        updateDashboard();

        Button author = findViewById(R.id.authorBtn);

        author.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Author.class);
            startActivity(intent);
        });

        // Set up RecyclerView with GridLayoutManager
        RecyclerView recyclerView = findViewById(R.id.toolsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(new ToolsAdapter(this, getToolList()));

        // Execute the AsyncTask to fetch and display the quote
        // new FetchQuoteTask().execute();
    }

    private void initialSetup() {

        // Check and request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted or requested
            requestLocationUpdates();
        }
    }

    private void requestLocationUpdates() {
        // Check if the location provider is enabled
        // For simplicity, we use the fused location provider here
        // You may consider using other providers based on your requirements
        // (e.g., GPS_PROVIDER, NETWORK_PROVIDER)
        // FusedLocationProviderClient is part of the Google Play services
        FusedLocationProviderClient locationClient =
                LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            locationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            getWeatherData(latitude, longitude);
                        }
                    });
        }
    }

    private boolean isDay() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour > 6 && hour < 18;
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
        String time = String.format("%d:%02d:%02d %s", hour % 12, minute, second, postTime);
        timeInfo.setText("Time: " + time);
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

    private ArrayList<ToolModel> getToolList() {
        ArrayList<ToolModel> tools = new ArrayList<>();
        tools.add(new ToolModel("Mirror", R.drawable.ic_mirror));
        tools.add(new ToolModel("Stop Watch", R.drawable.ic_stopwatch));
        tools.add(new ToolModel("Flash Light", R.drawable.ic_flash));
        // Add more tools as needed
        return tools;
    }
}
