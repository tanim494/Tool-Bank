package com.tanim.toolbank;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Collections;
import java.util.List;

public class Mirror extends AppCompatActivity implements TextureView.SurfaceTextureListener{

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private TextureView mTextureView;
    private CameraManager mCameraManager;
    private String mCameraId;
    private Surface mSurface;
    private SeekBar mBrightnessSeekBar;
    private SeekBar mZoomSeekBar;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CameraCaptureSession.CaptureCallback mCaptureCallback;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private float currentZoomLevel = 0.5f;
    private static final int MIN_BRIGHTNESS = -3;
    private static final int MAX_BRIGHTNESS = 3;
    private static final int MIN_ZOOM = 0;
    private static final int MAX_ZOOM = 3;
    ImageView mirrorFlip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mirror);

        mTextureView = findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(this);

        mBrightnessSeekBar = findViewById(R.id.brightness_slider);
        mZoomSeekBar = findViewById(R.id.zoom_slider);
        mirrorFlip = findViewById(R.id.mirrorFlip);

        mirrorFlip.setOnClickListener(v -> {
            float currentScaleX = mTextureView.getScaleX();

            if (currentScaleX == -1) {
                // If the current scale is not -1, set it to -1
                mTextureView.setScaleX(1);
                Toast.makeText(Mirror.this, "ScaleX set to 1", Toast.LENGTH_SHORT).show();
            } else if (currentScaleX == 1) {
                mTextureView.setScaleX(-1);
                // Optional: Handle the case when the current scale is already -1
                Toast.makeText(Mirror.this, "ScaleX is -1", Toast.LENGTH_SHORT).show();
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

        mZoomSeekBar.setMax(MAX_ZOOM - MIN_ZOOM);
        mZoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int zoomValue = progress + MIN_ZOOM;
                zoomCamera(zoomValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
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

            // Clamp the zoom level to the valid range
            currentZoomLevel = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, currentZoomLevel));

            Rect zoomRect = calculateZoomRect(currentZoomLevel);

            mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);

            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    // Other methods remain the same...

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
}
