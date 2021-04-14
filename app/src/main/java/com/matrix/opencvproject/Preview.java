package com.matrix.opencvproject;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.icu.number.Scale;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Preview implements Camera.PreviewCallback {

    private static final double CONFIDENCE = 0.3;
    private Net deepDarkFantsyNet;
    private Context context;

    private final double IN_SCALE_FACTOR = 0.007843;
    private final Size SIZE = new Size(420, 420);
    private final Scalar SCALE = new Scalar(0, 0, 0);

    public Preview(Context context){
        this.context = context;
        deepDarkFantsyNet = Dnn.readNetFromDarknet(getPath("yolov2-tiny.cfg", this.context), getPath("yolov2-tiny.weights", this.context));
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
        Mat imageForDnn = new Mat();
        Utils.bitmapToMat(image, imageForDnn);
        Imgproc.cvtColor(imageForDnn, imageForDnn, Imgproc.COLOR_RGBA2RGB);

        Mat blob = Dnn.blobFromImage(imageForDnn, IN_SCALE_FACTOR, SIZE, SCALE, false, false);
        deepDarkFantsyNet.setInput(blob);
        List<Mat> detections = new ArrayList<>();

        deepDarkFantsyNet.forward(detections);

        int cols = image.getWidth();
        int rows = image.getHeight();

        for (Mat detection : detections){
            detection = detection.reshape(1, (int)detection.total() / 7);
            for (int i = 0; i < detection.rows(); i++){
                double confidence = (int) detection.get(i, 2)[0];

                if(confidence > CONFIDENCE){
                    int classId = (int)detection.get(i, 1)[0];

                    int left   = (int)(detection.get(i, 3)[0] * cols);
                    int top    = (int)(detection.get(i, 4)[0] * rows);
                    int right  = (int)(detection.get(i, 5)[0] * cols);
                    int bottom = (int)(detection.get(i, 6)[0] * rows);

                    Imgproc.rectangle(imageForDnn, new Point(left, top), new Point(right, bottom),
                            new Scalar(0, 255, 0));
                }
            }
        }

        data = Utils.matToBitmap(imageForDnn, image);
        data = image.getNinePatchChunk();
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
}
