package com.tanim.toolbank;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StopWatch extends AppCompatActivity {

    private TextView tvElapsedTime;
    private ImageView btnStart;

    private boolean isRunning = false;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private long seconds = 0;
    String lapText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_watch);

        tvElapsedTime = findViewById(R.id.elapsedTime);
        btnStart = findViewById(R.id.btnStart);
        ImageView btnReset = findViewById(R.id.btnReset);

        btnStart.setOnClickListener(v -> toggleTimer());
        btnReset.setOnClickListener(v -> resetTimer());

        TextView addBt = findViewById(R.id.addLap);
        TextView lapInfo = findViewById(R.id.lapInfo);
        addBt.setOnClickListener(v -> {
            lapInfo.setText(lapText);
        });
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
                seconds += 1; // Increase by 1000 milliseconds = 1 second
                updateElapsedTime();
                handler.postDelayed(this, 1000);
                lapText = seconds > 60 ? ((seconds /60) % 60) + " Minutes " + (seconds % 60) + " Second" : (seconds) + " Seconds";
            }
        };
        handler.postDelayed(runnable, 1000);
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
        seconds = 0;
        updateElapsedTime();
    }

    private void updateElapsedTime() {
        long hours = (seconds / 3600) % 60;
        long minutes = (seconds / 60) % 60;
        long seco = seconds % 60;

        String time = String.format("%01dH : %02dM : %02dS", hours, minutes, seco);
        tvElapsedTime.setText(time);
    }
}
