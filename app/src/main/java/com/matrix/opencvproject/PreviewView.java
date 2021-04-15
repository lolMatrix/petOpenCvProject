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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PreviewView extends SurfaceView implements SurfaceHolder.Callback {

    private boolean isReady = false;
    private SurfaceHolder holder;
    private Camera camera;
    private MainActivity context;
    private List<Rect> rects = new ArrayList<>();
    private Paint paint;
    private CameraOverlay overlay;


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
            camera.setPreviewCallback(new Preview(context, this));
            camera.startPreview();
            isReady = true;
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

    public void drawRect(int left, int top, int right, int bottom) {
        int scaleX = getWidth();
        int scaleY = getHeight();
        Rect rect = new Rect(left, top, right, bottom);


        overlay.setDrawInStack(rect);
    }

    public void clearOverlay() {
        overlay.clearDrawStack();
    }
}
