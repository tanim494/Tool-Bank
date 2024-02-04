package com.tanim.toolbank;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Bmi extends AppCompatActivity {
    TextView currentheight, currentweight;
    SeekBar mseekbarforheight, mseekbarforweight;
    Button mcalculatebmi;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        currentweight = findViewById(R.id.currentweight);
        currentheight = findViewById(R.id.currentHeight);
        mseekbarforheight = findViewById(R.id.seekbarforheight);
        mseekbarforweight = findViewById(R.id.seekbarforweight);
        mcalculatebmi = findViewById(R.id.calculatebmi);

        mseekbarforheight.setMax(300);
        mseekbarforheight.setProgress(173);
        updateHeightText();
        mseekbarforheight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateHeightText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        updateWeightText();
        mseekbarforweight.setMax(150); // Set the maximum weight value as per your requirement
        mseekbarforweight.setProgress(60);
        updateWeightText();
        mseekbarforweight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateWeightText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mcalculatebmi.setOnClickListener(v -> {
            // Add your BMI calculation logic here
            double heightInMeters = mseekbarforheight.getProgress() / 100.0;
            double bmi = mseekbarforweight.getProgress() / (heightInMeters * heightInMeters);

            // Now you can use the 'bmi' value as needed
            showResultDialog(bmi);
        });
    }

    private String getAdditionalInfo(double bmi, LinearLayout resultView) {
        String additionalInfo;
        if (bmi < 18.5) {
            resultView.setBackgroundColor(Color.parseColor("#3498db")); // Replace with your desired color code
            additionalInfo = "Underweight";
        } else if (bmi >= 18.5 && bmi < 24.9) {
            additionalInfo = "Normal weight";
            resultView.setBackgroundColor(Color.parseColor("#2ecc71")); // Replace with your desired color code
        } else if (bmi >= 25 && bmi < 29.9) {
            additionalInfo = "Overweight";
            resultView.setBackgroundColor(Color.parseColor("#f1c40f")); // Replace with your desired color code
        } else {
            additionalInfo = "Obese";
            resultView.setBackgroundColor(Color.parseColor("#e74c3c")); // Replace with your desired color code
        }

        return additionalInfo;
    }

    private void showResultDialog(double bmi) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_bmi_result);
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = 0;

        LinearLayout resultView = dialog.findViewById(R.id.resultView);

        TextView resultTextView = dialog.findViewById(R.id.resultTextView);
        resultTextView.setText(String.format("Your BMI is: %.2f", bmi));

        TextView additionalInfoTextView = dialog.findViewById(R.id.additionalInfoTextView);
        String additionalInfo = getAdditionalInfo(bmi, resultView);
        additionalInfoTextView.setText(additionalInfo);

        Button okButton = dialog.findViewById(R.id.okButton);
        okButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void updateHeightText() {
        int totalInches = (int) (mseekbarforheight.getProgress() / 2.54);
        int feet = totalInches / 12;
        int inches = totalInches % 12;
        currentheight.setText(String.format("%d' %d\"", feet, inches));
    }

    private void updateWeightText() {
        currentweight.setText(mseekbarforweight.getProgress() + " KG");
    }
}
