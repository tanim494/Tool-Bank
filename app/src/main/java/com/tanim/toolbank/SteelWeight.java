package com.tanim.toolbank;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

public class SteelWeight extends AppCompatActivity {

    private EditText heightInput, widthInput, depthInput;
    private Spinner gaugeSpinner;
    private Button calculateBtn, editBtn;
    private TextView resultText;
    private EditText steelPriceInput, laborCostInput, colorCostInput, componentCostInput, otherCostInput, extraWeightInput, profitInput; // Added new fields
    private LinearLayout editLayout; // Layout for editable costs
    private boolean isEditMode = false; // Flag to track whether we are in edit mode

    // Default values for costs (modifiable via SharedPreferences)
    double steelPrice = 133, laborCost = 3000, colorCost = 2000, componentCost = 1800, otherCost = 500, extraWeight = 35, profitMargin = 30; // Initialize with default values
    // Gauge weight per square foot (in kg)
    private final double[] gaugeWeights = {0.89, 0.687, 0.56, 0.446}; // 18, 20, 22, 24 gauges

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steel_weight);

        // Initialize views
        heightInput = findViewById(R.id.heightInput);
        widthInput = findViewById(R.id.widthInput);
        depthInput = findViewById(R.id.depthInput);
        gaugeSpinner = findViewById(R.id.gaugeSpinner);
        calculateBtn = findViewById(R.id.calculateBtn);
        resultText = findViewById(R.id.resultText);

        // Initialize the edit layout and buttons
        editLayout = findViewById(R.id.editLayout);

        steelPriceInput = findViewById(R.id.steelPriceInput);
        laborCostInput = findViewById(R.id.laborCostInput);
        colorCostInput = findViewById(R.id.colorCostInput);
        componentCostInput = findViewById(R.id.componentCostInput);
        otherCostInput = findViewById(R.id.otherCostInput);
        extraWeightInput = findViewById(R.id.extraWeightInput); // New input field
        profitInput = findViewById(R.id.profitInput); // New input field

        editBtn = findViewById(R.id.editBtn);

        // Set the input fields with the saved or default cost values
        steelPriceInput.setText(String.valueOf(steelPrice));
        laborCostInput.setText(String.valueOf(laborCost));
        colorCostInput.setText(String.valueOf(colorCost));
        componentCostInput.setText(String.valueOf(componentCost));
        otherCostInput.setText(String.valueOf(otherCost));
        extraWeightInput.setText(String.valueOf(extraWeight)); // Set default value
        profitInput.setText(String.valueOf(profitMargin)); // Set default value

        // Make cost input fields non-editable initially
        steelPriceInput.setEnabled(false);
        laborCostInput.setEnabled(false);
        colorCostInput.setEnabled(false);
        componentCostInput.setEnabled(false);
        otherCostInput.setEnabled(false);
        extraWeightInput.setEnabled(false); // Make non-editable initially
        profitInput.setEnabled(false); // Make non-editable initially

        // Set up the spinner for gauge selection
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.gauge_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gaugeSpinner.setAdapter(adapter);

        // Calculate button to compute the weight and cost
        calculateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double height = Double.parseDouble(heightInput.getText().toString());
                double width = Double.parseDouble(widthInput.getText().toString());
                double depth = Double.parseDouble(depthInput.getText().toString());

                int selectedGauge = gaugeSpinner.getSelectedItemPosition();

                int weight = (int) calculateWeight(height, width, depth, selectedGauge);
                int manufacturingCost = (int) calculateManufacturingCost(weight);
                int profitPrice = (int) calculateProfitPrice(manufacturingCost);

                resultText.setText("Estimated Weight: " + weight + " Kg\n" +
                        "Manufacturing Cost: " + manufacturingCost + " BDT\n" +
                        "Sell Price: " + profitPrice + " BDT");
            }
        });

        // Edit button to allow editing of cost fields
        // Inside onCreate method, modify the edit button logic
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    // If in edit mode, switch to view mode
                    isEditMode = false;

                    // Make the cost fields non-editable again
                    steelPriceInput.setEnabled(false);
                    laborCostInput.setEnabled(false);
                    colorCostInput.setEnabled(false);
                    componentCostInput.setEnabled(false);
                    otherCostInput.setEnabled(false);
                    extraWeightInput.setEnabled(false);
                    profitInput.setEnabled(false);

                    // Change the button text back to "Edit Costs"
                    editBtn.setText("Edit Costs");
                } else {
                    // If not in edit mode, switch to edit mode
                    isEditMode = true;

                    // Make the cost fields editable
                    steelPriceInput.setEnabled(true);
                    laborCostInput.setEnabled(true);
                    colorCostInput.setEnabled(true);
                    componentCostInput.setEnabled(true);
                    otherCostInput.setEnabled(true);
                    extraWeightInput.setEnabled(true);
                    profitInput.setEnabled(true);

                    // Change the button text to "Done"
                    editBtn.setText("Done");
                }
            }
        });


        // Done button to save new values and exit edit mode
    }

    private void saveCosts() {
        // Retrieve and store updated costs from the EditText fields
        try {
            steelPrice = Double.parseDouble(steelPriceInput.getText().toString());
            laborCost = Double.parseDouble(laborCostInput.getText().toString());
            colorCost = Double.parseDouble(colorCostInput.getText().toString());
            componentCost = Double.parseDouble(componentCostInput.getText().toString());
            otherCost = Double.parseDouble(otherCostInput.getText().toString());
            extraWeight = Double.parseDouble(extraWeightInput.getText().toString());
            profitMargin = Double.parseDouble(profitInput.getText().toString());
        } catch (NumberFormatException e) {
            // In case of invalid input, use default values
            steelPrice = 133;
            laborCost = 3000;
            colorCost = 2000;
            componentCost = 1800;
            otherCost = 500;
            extraWeight = 35;
            profitMargin = 30;
        }
    }

    private double calculateWeight(double height, double width, double depth, int gaugeIndex) {
        double fontBack = 2 * (height * width);
        double twoSide = 2 * (height * (depth / 12));
        double topBottom = 2 * (width * (depth / 12));

        double totalArea = fontBack + twoSide + topBottom;

        return totalArea * gaugeWeights[gaugeIndex] * (1 + extraWeight / 100); // Adjusted for extra weight percentage
    }

    private double calculateManufacturingCost(double weight) {
        return (steelPrice * weight) + laborCost + colorCost + componentCost + otherCost;
    }

    private double calculateProfitPrice(double manufacturingCost) {
        return manufacturingCost * (1 + profitMargin / 100);
    }
}
