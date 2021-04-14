package com.matrix.opencvproject;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Camera;
import android.os.Bundle;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    private FrameLayout preview;
    private Camera mainCamera;
    private PreviewView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preview = findViewById(R.id.CameraPreview);
        preview.addView(view = new PreviewView(this, mainCamera));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainCamera = Camera.open(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mainCamera != null){
            mainCamera.release();
            mainCamera = null;
        }
    }
}