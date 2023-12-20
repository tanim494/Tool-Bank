package com.tanim.toolbank;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StopWatch extends AppCompatActivity {

    private TextView tvElapsedTime;
    private ImageView btnStart;

    private boolean isRunning = false;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private long milliseconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_watch);

        tvElapsedTime = findViewById(R.id.tvElapsedTime);
        btnStart = findViewById(R.id.btnStart);
        ImageView btnReset = findViewById(R.id.btnReset);

        btnStart.setOnClickListener(v -> toggleTimer());

        btnReset.setOnClickListener(v -> resetTimer());
    }

    private void toggleTimer() {
        if (isRunning) {
            pauseTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        isRunning = true;
        btnStart.setImageResource(R.drawable.ic_pause);
        runnable = new Runnable() {
            @Override
            public void run() {
                milliseconds += 100; // Increase by 10 milliseconds
                updateElapsedTime();
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(runnable, 100);
    }

    private void pauseTimer() {
        isRunning = false;
        btnStart.setImageResource(R.drawable.ic_play);
        handler.removeCallbacks(runnable);
    }

    private void resetTimer() {
        isRunning = false;
        btnStart.setImageResource(R.drawable.ic_play);
        handler.removeCallbacks(runnable);
        milliseconds = 0;
        updateElapsedTime();
    }

    private void updateElapsedTime() {
        long hours = milliseconds / 3600000;
        long minutes = (milliseconds % 3600000) / 60000;
        long seconds = (milliseconds % 60000) / 1000;
        long millis = milliseconds % 1000;

        String time = String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, millis);
        tvElapsedTime.setText(time);
    }
}
