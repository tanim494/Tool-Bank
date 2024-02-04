package com.tanim.toolbank;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Compass extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private ImageView compassImage;
    TextView degreeText;

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            float[] orientationValues = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientationValues);

            // Convert azimuth to degrees
            float azimuth = (float) Math.toDegrees(orientationValues[0]);

            azimuth = (azimuth + 360) % 360;

            // Rotate the compass image based on the azimuth
            compassImage.setRotation(-azimuth);

            // Convert float to integer for display
            int show = (int) azimuth;
            String direction = "";
            if (azimuth >= 0  && azimuth <=90) {
                direction = "N ";
            } else if (azimuth >= 90  && azimuth <=180) {
                direction = "E ";
            } else if (azimuth >= 271  && azimuth <=360) {
                direction = "W ";
            } else {
                direction = "S ";
            }
            degreeText.setText(direction + String.valueOf(show) + "°");
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Handle accuracy changes if needed
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        // Initialize SensorManager and get Rotation Vector Sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Initialize ImageView
        compassImage = findViewById(R.id.compassImage);
        degreeText = findViewById(R.id.degreeText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listener when the activity is resumed
        sensorManager.registerListener(sensorEventListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listener when the activity is paused
        sensorManager.unregisterListener(sensorEventListener);
    }
}
