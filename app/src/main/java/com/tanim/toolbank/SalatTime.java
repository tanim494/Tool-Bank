package com.tanim.toolbank;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class SalatTime extends AppCompatActivity {

    private TextView textViewFajr, textViewDuhr, textViewAsr, textViewMagrib, textViewIsha;
    double userLatitude, userLongitude;
    private MapView mapView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salat_time);

        // Initialize TextViews
        textViewFajr = findViewById(R.id.textViewFajr);
        textViewDuhr = findViewById(R.id.textViewDuhr);
        textViewAsr = findViewById(R.id.textViewAsr);
        textViewMagrib = findViewById(R.id.textViewMagrib);
        textViewIsha = findViewById(R.id.textViewIsha);


        // Set up the Refresh button
        Button buttonRefresh = findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(view -> new RefreshPrayerTimesTask().execute());

        // Initial prayer times update
        new RefreshPrayerTimesTask().execute();

        mapView = findViewById(R.id.mapView);
        mapView.getController().setCenter(new GeoPoint(userLatitude, userLongitude));
        mapView.getController().setZoom(10);
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(new GeoPoint(userLatitude, userLongitude));
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        mapView.getOverlays().add(startMarker);
    }

    private class RefreshPrayerTimesTask extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            return refreshPrayerTimes();
        }

        @Override
        protected void onPostExecute(ArrayList<String> prayerTimesList) {
            if (prayerTimesList != null) {
                // Save the fetched prayer times to SharedPreferences
                savePrayerTimesToSharedPreferences(prayerTimesList);

                // Update TextViews with the new prayer times
                textViewFajr.setText(prayerTimesList.get(0));
                textViewDuhr.setText(prayerTimesList.get(1));
                textViewAsr.setText(prayerTimesList.get(2));
                textViewMagrib.setText(prayerTimesList.get(3));
                textViewIsha.setText(prayerTimesList.get(4));
            } else {
                // Handle the case where there was an issue getting the prayer times
                // Try to load saved prayer times from SharedPreferences
                ArrayList<String> savedPrayerTimes = loadPrayerTimesFromSharedPreferences();
                if (savedPrayerTimes != null) {
                    // Use saved prayer times if available
                    textViewFajr.setText(savedPrayerTimes.get(0));
                    textViewDuhr.setText(savedPrayerTimes.get(1));
                    textViewAsr.setText(savedPrayerTimes.get(2));
                    textViewMagrib.setText(savedPrayerTimes.get(3));
                    textViewIsha.setText(savedPrayerTimes.get(4));
                } else {
                    // Display an error message or take appropriate action
                }
            }
        }

        private void savePrayerTimesToSharedPreferences(ArrayList<String> prayerTimesList) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();

            // Save each prayer time to SharedPreferences
            for (int i = 0; i < prayerTimesList.size(); i++) {
                editor.putString("prayer_time_" + i, prayerTimesList.get(i));
            }

            editor.apply();
        }

        private ArrayList<String> loadPrayerTimesFromSharedPreferences() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            ArrayList<String> savedPrayerTimes = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    String prayerTime = preferences.getString("prayer_time_" + i, null);
                    if (prayerTime != null) {
                        savedPrayerTimes.add(prayerTime);
                    } else {
                        // If any prayer time is missing, return null
                        return null;
                    }
                }
                return savedPrayerTimes;
        }
    }


    private ArrayList<String> refreshPrayerTimes() {
        try {
            // Get user's location (latitude and longitude)
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            userLatitude = Double.parseDouble(preferences.getString("last_latitude", "23.6850"));
            userLongitude = Double.parseDouble(preferences.getString("last_longitude", "90.3563"));

            // Replace with the actual logic or library for prayer time calculations
            ArrayList<String> prayerTimesList = PrayerTimeCalculator.calculatePrayerTimes(userLatitude, userLongitude);

            return prayerTimesList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class PrayerTimeCalculator {

        public static ArrayList<String> calculatePrayerTimes(double latitude, double longitude) {
            try {
                // Build the API request URL for today's prayer times
                Calendar calendar = Calendar.getInstance();
                int month = calendar.get(Calendar.MONTH) + 1;
                String apiEndpoint = "https://api.aladhan.com/v1/calendar/2024/";
                String todayApiRequest = apiEndpoint + month + "?latitude=" + latitude + "&longitude=" + longitude + "&method=1&school=1&";

                // Make the API request
                URL url = new URL(todayApiRequest);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    // Parse the API response
                    JSONObject jsonResponse = new JSONObject(stringBuilder.toString());
                    JSONArray data = jsonResponse.getJSONArray("data");
                    JSONObject todayData = data.getJSONObject(0);
                    JSONObject timings = todayData.getJSONObject("timings");

                    // Check if the expected keys exist in the API response
                    if (timings.has("Fajr") && timings.has("Dhuhr") && timings.has("Asr") && timings.has("Maghrib") && timings.has("Isha")) {
                        // Extract prayer times from the response
                        String fajr = timings.getString("Fajr");
                        String duhr = timings.getString("Dhuhr");
                        String asr = timings.getString("Asr");
                        String maghrib = timings.getString("Maghrib");
                        String isha = timings.getString("Isha");

                        // Create a list to store the formatted prayer times
                        ArrayList<String> formattedPrayerTimes = new ArrayList<>();
                        formattedPrayerTimes.add("Fajr Time: " + formatTime(timings.getString("Fajr")));
                        formattedPrayerTimes.add("Duhr Time: " + formatTime(timings.getString("Dhuhr")));
                        formattedPrayerTimes.add("Asr Time: " + formatTime(timings.getString("Asr")));
                        formattedPrayerTimes.add("Maghrib Time: " + formatTime(timings.getString("Maghrib")));
                        formattedPrayerTimes.add("Isha Time: " + formatTime(timings.getString("Isha")));


                        return formattedPrayerTimes;
                    } else {
                        // Handle the case where expected keys are missing in the API response
                        return null;
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private static String formatTime(String rawTime) {
            try {
                // Parse the raw time
                String[] timeParts = rawTime.split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1].substring(0, 2)); // Extract only the first two characters

                // Convert to 12-hour format
                String period = (hour >= 12) ? "PM" : "AM";
                hour = (hour > 12) ? hour - 12 : hour;

                // Format the time as "H:MM PM"
                return String.format("%d:%02d %s", hour, minute, period);
            } catch (Exception e) {
                e.printStackTrace();
                return rawTime; // Return the raw time if formatting fails
            }
        }
    }

}