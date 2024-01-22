package com.tanim.toolbank;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ToolsAdapter extends RecyclerView.Adapter<ToolsAdapter.ToolViewHolder> {

    private final Context context;
    private final ArrayList<ToolModel> toolList;

    public ToolsAdapter(Context context, ArrayList<ToolModel> toolList) {
        this.context = context;
        this.toolList = toolList;
    }

    @NonNull
    @Override
    public ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tool_item, parent, false);
        return new ToolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolViewHolder holder, final int position) {
        final ToolModel game = toolList.get(position);

        // Set game icon/image
        holder.toolIcon.setImageResource(game.getIconResourceId());

        // Set game name
        holder.toolName.setText(game.getName());

        // Handle item click
        holder.itemView.setOnClickListener(view -> openGameActivity(game.getName()));

    }

    private void openGameActivity(String gameName) {
        switch (gameName) {
           case "Mirror" :
                Intent in1 = new Intent(context, Mirror.class);
                context.startActivity(in1);
                break;
            case "Stop Watch" :
                Intent in2 = new Intent(context, StopWatch.class);
                context.startActivity(in2);
                break;
            case "Flash Light" :
                Intent in3 = new Intent(context, FlashLight.class);
                context.startActivity(in3);
                break;
            case "BMI":
                Intent in4 = new Intent(context, Bmi.class);
                context.startActivity(in4);
        }
    }



    @Override
    public int getItemCount() {
        return toolList.size();
    }

    static class ToolViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final ImageView toolIcon;
        final TextView toolName;

        ToolViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            toolIcon = itemView.findViewById(R.id.toolIcon);
            toolName = itemView.findViewById(R.id.toolName);
        }
    }
}