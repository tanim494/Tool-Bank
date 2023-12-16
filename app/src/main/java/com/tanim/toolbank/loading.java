package com.tanim.toolbank;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class loading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Delay for 3 seconds and then start MainActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(loading.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}