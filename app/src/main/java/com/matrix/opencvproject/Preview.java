package com.matrix.opencvproject;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Preview implements Camera.PreviewCallback {
    private PreviewView view;
    private MainActivity context;
    private NeuralPictureHandler pictureHandler;



    public Preview(MainActivity context, PreviewView view, NeuralPictureHandler pictureHandler) {
        this.context = context;
        this.view = view;
        this.pictureHandler = pictureHandler;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        view.clearOverlay();
        Camera.Parameters parameters = camera.getParameters();

        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;
        List<Rect> rects = pictureHandler.findObjectsInPhoto(data, width, height, parameters.getPreviewFormat());

        for (Rect rectangle : rects) {
            view.drawRect(rectangle);
        }
    }

    public NeuralPictureHandler getHandler() {
        return pictureHandler;
    }
}
