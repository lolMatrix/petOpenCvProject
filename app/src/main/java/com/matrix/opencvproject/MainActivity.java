package com.matrix.opencvproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorSpace;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.osgi.OpenCVNativeLoader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

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
        CameraOverlay overlay = findViewById(R.id.overlay);

        view = new PreviewView(this, mainCamera, overlay);
        view.setDrawingCacheEnabled(true);

        //preview.addView(overlay);
        preview.addView(view);
        overlay.bringToFront();

        Button takePhoto = findViewById(R.id.TakeAPicture);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        NeuralPictureHandler handler = view.getNeuralHandler();

                        camera.stopPreview();
                        String fileString=Environment.getExternalStorageDirectory().getPath()+"/tmp.jpg";
                        BitmapFactory.Options opt=new BitmapFactory.Options();
		    /*before making an actual bitmap, check size
		    if the bitmap's size is too large,out of memory occurs.
		    */
                        opt.inJustDecodeBounds=true;
                        Bitmap tmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                        opt.inJustDecodeBounds=false;
                        opt.outColorSpace = ColorSpace.get(ColorSpace.Named.SRGB);


                        Bitmap source = BitmapFactory.decodeByteArray(data, 0, data.length,opt);

                        List<Rect> rects = handler.findObjectsInPhoto(source);

                        Bitmap image = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas overlayOnPicture = new Canvas(image);

                        Paint rectPaint = new Paint();

                        rectPaint.setStyle(Paint.Style.STROKE);
                        rectPaint.setStrokeWidth(3);
                        rectPaint.setARGB(255, 255, 0, 255);
                        overlayOnPicture.drawBitmap(source, 0, 0, rectPaint);

                        for (Rect rectangle : rects) {
                            overlayOnPicture.drawRect(rectangle, rectPaint);
                        }

                        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        if (pictureFile == null){
                            Log.d("Mmm...", "Error creating media file, check storage permissions");
                            return;
                        }

                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            Log.d("Mmm...", "onPictureTaken: saved on " + pictureFile.getAbsolutePath());
                            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);

                            fos.close();
                        } catch (FileNotFoundException e) {
                            Log.d("Mmm...", "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            Log.d("Mmm...", "Error accessing file: " + e.getMessage());
                        }

                        camera.startPreview();

                    }
                });
            }
        });
    }

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


    public static String getPath(String file, Context context) {
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