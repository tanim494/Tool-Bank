package com.tanim.toolbank;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StopWatch extends AppCompatActivity {

    private TextView tvElapsedTime;
    private Button btnStart;
    private Button btnStop;
    private Button btnReset;

    private boolean isRunning = false;
    private Handler handler = new Handler();
    private Runnable runnable;
    private long milliseconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_watch);

        tvElapsedTime = findViewById(R.id.tvElapsedTime);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnReset = findViewById(R.id.btnReset);

        btnStart.setOnClickListener(v -> toggleTimer());

        btnStop.setOnClickListener(v -> stopTimer());

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
        btnStart.setText("Pause");
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
        btnStart.setText("Start");
        handler.removeCallbacks(runnable);
    }

    private void stopTimer() {
        isRunning = false;
        btnStart.setText("Start");
        handler.removeCallbacks(runnable);
        // Add any additional stop functionality if needed
    }

    private void resetTimer() {
        isRunning = false;
        btnStart.setText("Start");
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
