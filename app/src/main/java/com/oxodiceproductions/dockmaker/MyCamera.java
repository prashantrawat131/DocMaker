package com.oxodiceproductions.dockmaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


public class MyCamera extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    ImageCapture imageCapture;
    Executor executor;
    String DocId = "-2";
    Camera camera;
    ConstraintLayout camera_menu;
    SharedPreferences settingsSharedPreferences, sharedPreferences;
    String ImagePath = "-1", retakeImagePath = "-1";
    CameraControl cameraControl;
    ImageButton flash_button;
    File capturedImage;
    ProgressBar progressBar;
    FloatingActionButton captureImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_camera);

        previewView = findViewById(R.id.previewView);
        camera_menu = findViewById(R.id.camera_menu);
        flash_button = findViewById(R.id.imageButton8);
        progressBar = findViewById(R.id.progressBar);
        captureImageButton = findViewById(R.id.floatingActionButton);

        settingsSharedPreferences = getSharedPreferences("DocMakerSettings", MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("DocMaker", MODE_PRIVATE);

        try {
            //taking DocId
            //also taking ImagePath in case of retake image
            DocId = getIntent().getExtras().getString("DocId", "-1");
            ImagePath = getIntent().getExtras().getString("ImagePath", "-1");
            retakeImagePath = ImagePath;
        } catch (Exception ignored) {
        }


        if (sharedPreferences.getBoolean("flash", false)) {
            flash_button.setImageDrawable(getDrawable(R.drawable.flash_on));
        }

        Setup();

//		Executor executor2 = runnable -> {
//		};

        progressBar.setVisibility(View.GONE);

        previewView.setOnClickListener(view -> {
            MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(0, 0);
            MeteringPoint point = factory.createPoint(0, 0);
            FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .addPoint(point, FocusMeteringAction.FLAG_AE) // could have many
                    // auto calling cancelFocusAndMetering in 5 seconds
                    .setAutoCancelDuration(5, TimeUnit.SECONDS)
                    .build();
            cameraControl.startFocusAndMetering(action);
        });

        captureImageButton.setOnClickListener(view -> {
            camera_menu.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            try {
                FileOutputStream out = new FileOutputStream(capturedImage); //Use the stream as usual to w
                ImageCapture.OutputFileOptions outputFileOptions =
                        new ImageCapture.OutputFileOptions.Builder(out).build();
                imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        flash_button.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //if flash was on then it needs to be off on click and vice-verse
            boolean flashStateAfterClick = !sharedPreferences.getBoolean("flash", false);
            editor.putBoolean("flash", flashStateAfterClick);
            editor.apply();

            if (flashStateAfterClick) {
                imageCapture.setFlashMode(ImageCapture.FLASH_MODE_ON);
                flash_button.setImageDrawable(getDrawable(R.drawable.flash_on));
            } else {
                imageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
                flash_button.setImageDrawable(getDrawable(R.drawable.flash_off));
            }
        });
    }

    void Setup() {
        try {
            capturedImage = File.createTempFile("capturedImage", "jpeg");
        } catch (IOException ioException) {
            ioException.printStackTrace();
            GoToDocumentView();
        }

        if (capturedImage.exists())
            capturedImage.delete();

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException ignored) {
            }
        }, ContextCompat.getMainExecutor(this));
    }


    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        //selection of camera i.e. back camera
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        //attaching surface provider
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //after image capture, executor is called
        executor = runnable -> GoToCompress();

        if (sharedPreferences.getBoolean("flash", false)) {
            imageCapture = new ImageCapture.Builder()
                    .setFlashMode(ImageCapture.FLASH_MODE_ON)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(previewView.getDisplay().getRotation())
                    .build();
        } else {
            imageCapture = new ImageCapture.Builder()
                    .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(previewView.getDisplay().getRotation())
                    .build();
        }

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        cameraControl = camera.getCameraControl();
    }

    void GoToCompress() {
        //Image compression
        MyImageCompressor imageCompressor = new MyImageCompressor(getApplicationContext());
        String finalFilePath = imageCompressor.compress(capturedImage);

        //deleting old image
        capturedImage.delete();

        //setting imagePath and going for image editing
        ImagePath = finalFilePath;
        GoToCrop();
    }

    private void GoToCrop() {
        Intent in = new Intent(MyCamera.this, EditImageActivity.class);
        in.putExtra("ImagePath", ImagePath);
        in.putExtra("DocId", DocId);
        in.putExtra("fromCamera", true);
        in.putExtra("retakeImagePath", retakeImagePath);
        startActivity(in);
        finish();
    }

    private void GoToDocumentView() {
        Intent in = new Intent(MyCamera.this, document_view.class);
        in.putExtra("DocId", DocId);
        startActivity(in);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent in = new Intent(MyCamera.this, document_view.class);
        in.putExtra("DocId", DocId);
        startActivity(in);
        finish();
    }

}
