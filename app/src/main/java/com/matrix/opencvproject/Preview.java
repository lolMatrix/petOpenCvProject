package com.matrix.opencvproject;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.icu.number.Scale;
import android.icu.text.Normalizer2;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
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

    private static final double CONFIDENCE = 0.3;
    private PreviewView view;
    private Net deepDarkFantsyNet;
    private MainActivity context;

    private final double IN_SCALE_FACTOR = 0.00392;
    private final Size SIZE = new Size(416, 416);
    private final Scalar SCALE = new Scalar(0, 0, 0);

    public Preview(MainActivity context, PreviewView view){
        this.context = context;
        this.view = view;
        deepDarkFantsyNet = Dnn.readNetFromDarknet(getPath("yolov2-tiny.cfg", this.context), getPath("yolov2-tiny.weights", this.context));
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        view.clearOverlay();
        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

        byte[] bytes = out.toByteArray();
        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        if(image != null) {
            Mat imageForDnn = new Mat();
            Utils.bitmapToMat(image, imageForDnn);
            Imgproc.cvtColor(imageForDnn, imageForDnn, Imgproc.COLOR_RGBA2RGB);

            Mat blob = Dnn.blobFromImage(imageForDnn, IN_SCALE_FACTOR, SIZE, SCALE, false, false);
            deepDarkFantsyNet.setInput(blob);
            List<Mat> detections = new ArrayList<>();

            deepDarkFantsyNet.forward(detections);

            int cols = image.getWidth();
            int rows = image.getHeight();

            for (int j = 0; j < detections.size(); j++) {
                Mat detection = detections.get(j);
                int count = detection.rows();
                int c = detection.cols();
                //detection = detection.reshape(1, (int) detection.total() / 7); //тут The total number of matrix elements is not divisible by the new number of rows in function 'cv::Mat cv::Mat::reshape(int, int) const'
                for (int i = 0; i < count; i++) {
                    Mat row = detection.row(i);
                    Mat scores = row.colRange(5, c);
                    Core.MinMaxLocResult minmax = Core.minMaxLoc(scores);
                    double confidence = minmax.maxVal;

                    if (confidence > CONFIDENCE) {
                        Point classIdPoint = minmax.maxLoc;


                        int x = (int)Math.round(row.get(0,0)[0] * cols);
                        int y = (int) Math.round(row.get(0, 1)[0] * rows);
                        int w = (int) Math.round(row.get(0, 2)[0] * cols);
                        int h = (int) Math.round(row.get(0, 3)[0] * rows);
                        int left = x - w / 2;
                        int top = y - h / 2;
                        int right = x + w / 2;
                        int bottom = y + h / 2;

                        view.drawRect(bottom, right, top, left);
                        Toast.makeText(context, "Find", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private static String getPath(String file, Context context) {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream = null;
        for(String s : assetManager.getLocales())
            Log.d("Mmm...", s);
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
}
