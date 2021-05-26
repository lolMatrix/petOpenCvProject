package com.matrix.opencvproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PreviewView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder holder;
    private Camera camera;
    private MainActivity context;
    private List<Rect> rects = new ArrayList<>();
    private Paint paint;
    private CameraOverlay overlay;
    private Preview preview;


    public PreviewView(MainActivity context, Camera camera, CameraOverlay overlay) {
        super(context);
        this.context = context;
        this.camera = camera;
        this.overlay = overlay;

        holder = getHolder();
        holder.addCallback(this);
        setSecure(true);
        holder.setFormat(PixelFormat.TRANSLUCENT);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);

            Net deepDarkFantasyNet = Dnn.readNetFromDarknet(MainActivity.getPath("yolov2-tiny.cfg", context), MainActivity.getPath("yolov2-tiny.weights", context));
            NeuralPictureHandler handler = new NeuralPictureHandler(deepDarkFantasyNet);

            preview = new Preview(context, this, handler);
            camera.setPreviewCallback(preview);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (this.holder.getSurface() == null){
            return;
        }
        try {
            camera.stopPreview();
            camera.setPreviewDisplay(this.holder);
            camera.startPreview();
        } catch (Exception e){
        }

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    public void drawRect(Rect rect) {
        overlay.setDrawInStack(rect);
    }

    public NeuralPictureHandler getNeuralHandler(){
        return preview.getHandler();
    }

    public void clearOverlay() {
        overlay.clearDrawStack();
    }
}
