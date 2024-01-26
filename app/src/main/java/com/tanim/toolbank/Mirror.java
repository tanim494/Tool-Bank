package com.tanim.toolbank;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Collections;
import java.util.List;

public class Mirror extends AppCompatActivity implements TextureView.SurfaceTextureListener{

    private TextureView mTextureView;
    private CameraManager mCameraManager;
    private String mCameraId;
    private Surface mSurface;
    ImageView zoomIn , zoomOut, mirrorFlip, flashLight;
    RelativeLayout camFrame;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CameraCaptureSession.CaptureCallback mCaptureCallback;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private float currentZoomLevel = 1f;
    private static final int MIN_BRIGHTNESS = -3, MAX_BRIGHTNESS = 3, REQUEST_CAMERA_PERMISSION = 100;
    private static final float MAX_ZOOM_LEVEL = 3.5f;
    private boolean flashOn;
    private int initialBrightness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_mirror);

        mTextureView = findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(this);

        SeekBar mBrightnessSeekBar = findViewById(R.id.brightness_slider);
        zoomIn = findViewById(R.id.zoom_in_button);
        zoomOut = findViewById(R.id.zoom_out_button);
        mirrorFlip = findViewById(R.id.mirrorFlip);
        flashLight = findViewById(R.id.flashToggle);
        camFrame = findViewById(R.id.cameraFrame);

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
        complement();

        flashOn = false;
        flashLight.setOnClickListener(v -> {
            if (!flashOn) {
                // Check if the WRITE_SETTINGS permission is not granted
                if (!Settings.System.canWrite(this)) {
                    // Request the WRITE_SETTINGS permission
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
                // Set width and height for mTextureView
                ViewGroup.LayoutParams layoutParams = mTextureView.getLayoutParams();
                layoutParams.width = 900;  // Set the desired width in pixels
                layoutParams.height = 1500;  // Set the desired height in pixels
                mTextureView.setLayoutParams(layoutParams);

                // Set brightness to maximum
                ContentResolver cResolver = getContentResolver();
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 255);  // 255 is the maximum brightness

                // Update the screen brightness
                WindowManager.LayoutParams windowParams = getWindow().getAttributes();
                windowParams.screenBrightness = 0.4f;  // 1.0f means full brightness
                getWindow().setAttributes(windowParams);
                Toast.makeText(Mirror.this, "Flash turned ON", Toast.LENGTH_SHORT).show();
                flashOn = true;
            } else {

                ContentResolver contentResolver = Mirror.this.getContentResolver();
                try {
                    initialBrightness = 100;
                    Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
                } catch (Settings.SettingNotFoundException e) {
                    throw new RuntimeException(e);
                }

                ViewGroup.LayoutParams layoutParams = mTextureView.getLayoutParams();
                layoutParams.width = getScreenWidth();  // Set the desired width in pixels
                layoutParams.height = getScreenHeight() + 240;  // Set the desired height in pixels
                mTextureView.setLayoutParams(layoutParams);
                adjustScreenBrightness();
                Toast.makeText(Mirror.this, "Flash turned OFF", Toast.LENGTH_SHORT).show();
                flashOn = false;
            }
            camFrame.setBackgroundColor(flashOn ? Color.WHITE : Color.BLACK);
            flashLight.setImageTintList(ColorStateList.valueOf(flashOn ? Color.BLACK : Color.WHITE));
            mirrorFlip.setImageTintList(ColorStateList.valueOf(flashOn ? Color.BLACK : Color.WHITE));
            mBrightnessSeekBar.setProgressTintList(ColorStateList.valueOf(flashOn ? Color.BLACK : Color.WHITE));
            mBrightnessSeekBar.setThumbTintList(ColorStateList.valueOf(flashOn ? Color.BLACK : Color.WHITE));
            mBrightnessSeekBar.setProgressBackgroundTintList(ColorStateList.valueOf(flashOn ? Color.BLACK : Color.WHITE));
            zoomIn.setImageTintList(ColorStateList.valueOf(flashOn ? Color.BLACK : Color.WHITE));
            zoomOut.setImageTintList(ColorStateList.valueOf(flashOn ? Color.BLACK : Color.WHITE));

        });



        mirrorFlip.setOnClickListener(v -> {
            float currentScaleX = mTextureView.getScaleX();
            if (currentScaleX == -1) {
                mTextureView.setScaleX(1);
                Toast.makeText(Mirror.this, "This is how you look in the mirror.", Toast.LENGTH_SHORT).show();
            } else if (currentScaleX == 1) {
                mTextureView.setScaleX(-1);
                Toast.makeText(Mirror.this, "This is how other's see you.", Toast.LENGTH_SHORT).show();
            }
        });


        mBrightnessSeekBar.setMax(MAX_BRIGHTNESS - MIN_BRIGHTNESS);
        mBrightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int brightnessValue = progress + MIN_BRIGHTNESS;
                adjustBrightness(brightnessValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        zoomIn.setOnClickListener(v -> zoomCamera(0.5F));

        zoomOut.setOnClickListener(v -> zoomCamera((float) -0.5));

    }

    // Other methods remain the same...

    private void openCamera() {
        try {
            mCameraId = mCameraManager.getCameraIdList()[1];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCameraDevice = camera;
                    createPreviewSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e("Camera", "Camera error: " + error);
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e("Camera", "Error accessing camera: " + e.getMessage());
        }
    }

    // Other methods remain the same...

    private void createPreviewSession() {
        if (mSurface == null) {
            Log.e("Camera", "Error: surface is null");
            return;
        }

        try {
            List<Surface> surfaces = Collections.singletonList(mSurface);

            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureSession = session;
                    try {
                        mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        mPreviewRequestBuilder.addTarget(mSurface);

                        mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
                            // Implement any necessary callbacks
                        };

                        mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, null);
                    } catch (CameraAccessException e) {
                        Log.e("Camera", "Error creating capture request: " + e.getMessage());
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e("Camera", "Capture session configuration failed");
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e("Camera", "Error creating capture session: " + e.getMessage());
        }
    }
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        mSurface = new Surface(surfaceTexture);
        openCamera(); // Move the call to openCamera here
    }
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        // Handle surface size changes, if necessary
    }
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        // Release resources, if any
        return true;
    }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        // This function is called every time the TextureView's content is updated.
        // It's not used in this example, but you could potentially use it to perform real-time image processing on the camera preview.
    }

    // Other methods remain the same...

    private void adjustBrightness(float deltaBrightness) {
        try {
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            // Assuming mPreviewSurface is your Surface for camera preview
            builder.addTarget(mSurface);

            builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, (int) deltaBrightness);

            mCaptureSession.setRepeatingRequest(builder.build(), mCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    // Other methods remain the same...

    private void zoomCamera(float deltaZoom) {
        try {
            currentZoomLevel += deltaZoom;

            // Ensure that the zoom level is within the valid range
            currentZoomLevel = Math.max(1.0f, Math.min(MAX_ZOOM_LEVEL, currentZoomLevel));

            Log.d("Camera", "Zoom level: " + currentZoomLevel);

            Rect zoomRect = calculateZoomRect(currentZoomLevel);

            mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);

            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, null);
        } catch (CameraAccessException e) {
            Log.e("Camera", "Error accessing camera: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("Camera", "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Rect calculateZoomRect(float zoomLevel) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            Rect sensorArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

            int xCenter = sensorArraySize.width() / 2;
            int yCenter = sensorArraySize.height() / 2;

            int xDelta = (int) (0.5f * sensorArraySize.width() / zoomLevel);
            int yDelta = (int) (0.5f * sensorArraySize.height() / zoomLevel);

            return new Rect(xCenter - xDelta, yCenter - yDelta, xCenter + xDelta, yCenter + yDelta);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return new Rect(0, 0, 0, 0);
        }
    }

    private void adjustScreenBrightness() {
        ContentResolver cResolver = getContentResolver();
        // Set brightness mode to manual
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        // Set brightness back to the initial value
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 255);
        // Update the screen brightness
        WindowManager.LayoutParams windowParams = getWindow().getAttributes();
        windowParams.screenBrightness = 0.1f;  // 1.0f means full brightness
        getWindow().setAttributes(windowParams);
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        } else {
            return 0; // Return 0 or handle the error in your application
        }
    }

    private int getScreenHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        } else {
            return 0; // Return 0 or handle the error in your application
        }
    }

    private void complement() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            // Code to be executed after the delay
            // For example, update UI or perform some task
            String[] compString = {
                    "OMG! You look absolutely stunning!",
                    "It's like you've stepped out of a magazine cover.",
                    "Your presence is truly mesmerizing.",
                    "I can't help but be in awe of your effortless beauty.",
                    "You carry yourself with such grace and style.",
                    "Seriously, you're turning heads wherever you go.",
                    "Your impeccable sense of style is truly noteworthy.",
                    "You're a vision of natural, captivating beauty.",
                    "You're beautiful, the way you are.",
                    "I'm speechless by your beauty.",
                    "You have nice eyes."
            };

            int ranPos = (int) (Math.random() * compString.length);
            Toast.makeText(Mirror.this, compString[ranPos], Toast.LENGTH_LONG).show();
        }, 1000);

    }

}
