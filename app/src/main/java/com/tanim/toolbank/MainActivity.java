package com.tanim.toolbank;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // setupRunner();
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
       //new FetchQuoteTask().execute();
    }
    private boolean isDay() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour > 6 && hour < 18) {
            return true;
        } else {
            return false;
        }
    }

    private void updateDashboard() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateDashboard();
            }
        };
        handler.postDelayed(runnable, 1000);
        // Inside your MainActivity.java
        TextView weatherInfo = findViewById(R.id.weatherInfo);
        TextView dateInfo = findViewById(R.id.dateInfo);
        TextView timeInfo = findViewById(R.id.timeInfo);
        LinearLayout dash = findViewById(R.id.dashboardBox);
        dash.setBackgroundResource(isDay() ? R.drawable.daybg : R.drawable.nightbg);

        // Get and set weather information (replace with your logic)
        String weather = "Sunny";
        weatherInfo.setText("Weather: " + weather);

        // Get and set current date information (replace with your logic)
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        dateInfo.setText("Date: " + currentDate);

        //Set cureent time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        //int milis = calendar.get(Calendar.MILLISECOND);
        String postTime = hour > 11 && hour < 23 ? " PM" : " AM";
        String time = String.format("%d:%02d:%02d %s", hour%12, minute,second, postTime);
        timeInfo.setText("Time: " + time);
    }

    private ArrayList<ToolModel> getToolList() {
        ArrayList<ToolModel> tools = new ArrayList<>();
        tools.add(new ToolModel("Mirror", R.drawable.ic_mirror));
        tools.add(new ToolModel("Stop Watch", R.drawable.ic_stopwatch));
        tools.add(new ToolModel("Flash Light", R.drawable.ic_flash));
        // Add more tools as needed
        return tools;
    }

     /*private class FetchQuoteTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://famous-quotes4.p.rapidapi.com/random?category=all&count=2")
                    .get()
                    .addHeader("X-RapidAPI-Key", "47846a54a3msh99c804254f10f8bp1d3d12jsne7033cbe8c38")
                    .addHeader("X-RapidAPI-Host", "famous-quotes4.p.rapidapi.com")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // Use Gson to parse the JSON
                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                    JsonArray quotesArray = jsonObject.getAsJsonArray("quotes");

                    if (quotesArray.size() > 0) {
                        JsonObject firstQuote = quotesArray.get(0).getAsJsonObject();
                        String text = firstQuote.get("text").getAsString();
                        String author = firstQuote.get("author").getAsString();

                        // You can now use 'text' and 'author' as needed
                        return "Text: " + text + "\nAuthor: " + author;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

         @Override
        protected void onPostExecute(String result) {
            // Handle the result here, update UI, etc.
            if (result != null) {
                randomQuote.setText(result);
            } else {
                // Handle the case where the request failed
                randomQuote.setText("Failed to fetch quote");
            }
        }
    }*/
}
