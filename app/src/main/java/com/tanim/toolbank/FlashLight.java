package com.tanim.toolbank;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class FlashLight extends AppCompatActivity {

    private boolean flashlightIsOn = false;
    private CameraManager cameraManager;
    private String cameraId;
    View torchToggle;
    RelativeLayout flashSc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_light);
        flashSc = findViewById(R.id.flashSc);

        // Check if the device has a flashlight
        if (!hasFlash()) {
            // Handle the case where the device doesn't have a flashlight
            // You may want to display a message to the user or disable the toggle button
            return;
        }

        // Initialize cameraManager
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // Get the camera ID for the back camera
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Set OnClickListener for the flashlight toggle button
        torchToggle = findViewById(R.id.torchToggle);
        torchToggle.setOnClickListener(v -> toggleFlashlight());
    }

    // Method to check if the device has a flashlight
    private boolean hasFlash() {
        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    // Method to toggle the flashlight on/off
    private void toggleFlashlight() {
        try {
            if (flashlightIsOn) {
                // Turn off the flashlight
                cameraManager.setTorchMode(cameraId, false);
                torchToggle.setBackgroundResource(R.drawable.ic_off);
                flashSc.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));           } else {
                // Turn on the flashlight
                cameraManager.setTorchMode(cameraId, true);
                torchToggle.setBackgroundResource(R.drawable.ic_on);
                flashSc.setBackgroundColor(Color.WHITE);
            }
            // Update the flashlight state
            flashlightIsOn = !flashlightIsOn;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
