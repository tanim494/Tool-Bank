package com.tanim.toolbank;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button author = findViewById(R.id.authorBtn);

        author.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Author.class);
            startActivity(intent);
        });

        // Set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.gamesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ToolsAdapter(this, getToolList()));
    }

    private ArrayList<ToolModel> getToolList() {
        ArrayList<ToolModel> games = new ArrayList<>();
        games.add(new ToolModel("Mirror", R.drawable.ic_mirror));
        games.add(new ToolModel("Stop Watch", R.drawable.ic_stopwatch));
        // Add more games as needed
        return games;
    }
}