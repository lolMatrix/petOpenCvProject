package com.matrix.opencvproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.osgi.OpenCVNativeLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ConstraintLayout preview;
    private Camera mainCamera;
    private PreviewView view;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OpenCVNativeLoader loader = new OpenCVNativeLoader();
        loader.init();

        setContentView(R.layout.activity_main);
        preview = findViewById(R.id.CameraPreview);
        image = findViewById(R.id.yes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainCamera = Camera.open(0);
        mainCamera.setDisplayOrientation(90);
        CameraOverlay overlay = findViewById(R.id.overlay);
        view = new PreviewView(this, mainCamera, overlay);
        //preview.addView(overlay);
        preview.addView(view);
        overlay.bringToFront();
    }

    public void setPreview(Bitmap image){
        this.image.setImageBitmap(image);
    }

    private static String getPath(String file, Context context) {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream = null;
        try {
            // Read data from assets.
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            // Create copy file in storage.
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            // Return a path to file which may be read in common way.
            return outFile.getAbsolutePath();
        } catch (IOException ex) {
            Log.i("mmm...", "Failed to upload a file");
        }
        return "";
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mainCamera != null){
            mainCamera.stopPreview();
            mainCamera.release();
            mainCamera = null;
        }
    }
}