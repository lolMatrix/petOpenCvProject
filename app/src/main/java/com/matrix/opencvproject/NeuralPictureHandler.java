package com.matrix.opencvproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import androidx.constraintlayout.solver.widgets.Rectangle;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NeuralPictureHandler {
    private static final double CONFIDENCE = 0.3;
    private final double IN_SCALE_FACTOR = 0.00392;
    private final Size SIZE = new Size(416, 416);
    private final Scalar SCALE = new Scalar(0, 0, 0);
    private final String[] CLASSES = {
            "person",
            "bicycle",
            "car",
            "motorbike",
            "aeroplane",
            "bus",
            "train",
            "truck",
            "boat",
            "traffic light",
            "fire hydrant",
            "stop sign",
            "parking meter",
            "bench",
            "bird",
            "cat",
            "dog",
            "horse",
            "sheep",
            "cow",
            "elephant",
            "bear",
            "zebra",
            "giraffe",
            "backpack",
            "umbrella",
            "handbag",
            "tie",
            "suitcase",
            "frisbee",
            "skis",
            "snowboard",
            "sports ball",
            "kite",
            "baseball bat",
            "baseball glove",
            "skateboard",
            "surfboard",
            "tennis racket",
            "bottle",
            "wine glass",
            "fork",
            "cup",
            "knife",
            "spoon",
            "bowl",
            "banana",
            "apple",
            "sandwich",
            "orange",
            "broccoli",
            "carrot",
            "hot dog",
            "pizza",
            "donut",
            "cake",
            "chair",
            "sofa",
            "pottedplant",
            "bed",
            "diningtable",
            "toilet",
            "tvmonitor",
            "laptop",
            "mouse",
            "remote",
            "keyboard",
            "cell phone",
            "microwave",
            "oven",
            "toaster",
            "sink",
            "refrigerator",
            "book",
            "clock",
            "vase",
            "scissors",
            "teddy bear",
            "hair drier",
            "toothbrush"
    };

    private Net deepDarkFantasyNet;

    public NeuralPictureHandler(Net neuralNetwork){
        deepDarkFantasyNet = neuralNetwork;
    }

    public List<Rect> findObjectsInPhoto(Bitmap image){

        List<Rect> result = new ArrayList<>();

        if(image != null) {
            Mat imageForDnn = new Mat();
            Utils.bitmapToMat(image, imageForDnn);
            Imgproc.cvtColor(imageForDnn, imageForDnn, Imgproc.COLOR_RGBA2RGB);

            Mat blob = Dnn.blobFromImage(imageForDnn, IN_SCALE_FACTOR, SIZE, SCALE, false, false);
            deepDarkFantasyNet.setInput(blob);
            List<Mat> detections = new ArrayList<>();

            deepDarkFantasyNet.forward(detections);

            int cols = image.getWidth();
            int rows = image.getHeight();

            for (int j = 0; j < detections.size(); j++) {
                Mat detection = detections.get(j);
                int count = detection.rows();
                int c = detection.cols();
                for (int i = 0; i < count; i++) {
                    Mat row = detection.row(i);
                    Mat scores = row.colRange(5, c);
                    Core.MinMaxLocResult minmax = Core.minMaxLoc(scores);
                    double confidence = minmax.maxVal;

                    if (confidence > CONFIDENCE) {
                        Point classIdPoint = minmax.maxLoc;

                        int x = (int) Math.round(row.get(0, 0)[0] * cols);
                        int y = (int) Math.round(row.get(0, 1)[0] * rows);
                        int w = (int) Math.round(row.get(0, 2)[0] * cols);
                        int h = (int) Math.round(row.get(0, 3)[0] * rows);
                        int left = x - w / 2;
                        int top = y - h / 2;
                        int right = x + w / 2;
                        int bottom = y + h / 2;

                        String score = CLASSES[(int) classIdPoint.x];
                        Log.d("Mmm...", "Class: " + score);

                        Rect rect = new Rect(left, top, right, bottom);
                        result.add(rect);
                    }
                }
            }
        }

        return result;
    }

    public List<Rect> findObjectsInPhoto(byte[] data, int width, int height, int format){

        List<Rect> result = new ArrayList<>();

        YuvImage yuv = new YuvImage(data, format, width, height, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

        byte[] bytes = out.toByteArray();
        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        if(image != null) {
            Mat imageForDnn = new Mat();
            Utils.bitmapToMat(image, imageForDnn);
            Imgproc.cvtColor(imageForDnn, imageForDnn, Imgproc.COLOR_RGBA2RGB);

            Mat blob = Dnn.blobFromImage(imageForDnn, IN_SCALE_FACTOR, SIZE, SCALE, false, false);
            deepDarkFantasyNet.setInput(blob);
            List<Mat> detections = new ArrayList<>();

            deepDarkFantasyNet.forward(detections);

            int cols = image.getWidth();
            int rows = image.getHeight();

            for (int j = 0; j < detections.size(); j++) {
                Mat detection = detections.get(j);
                int count = detection.rows();
                int c = detection.cols();
                for (int i = 0; i < count; i++) {
                    Mat row = detection.row(i);
                    Mat scores = row.colRange(5, c);
                    Core.MinMaxLocResult minmax = Core.minMaxLoc(scores);
                    double confidence = minmax.maxVal;

                    if (confidence > CONFIDENCE) {
                        Point classIdPoint = minmax.maxLoc;

                        int x = (int) Math.round(row.get(0, 0)[0] * cols);
                        int y = (int) Math.round(row.get(0, 1)[0] * rows);
                        int w = (int) Math.round(row.get(0, 2)[0] * cols);
                        int h = (int) Math.round(row.get(0, 3)[0] * rows);
                        int left = x - w / 2;
                        int top = y - h / 2;
                        int right = x + w / 2;
                        int bottom = y + h / 2;

                        String score = CLASSES[(int) classIdPoint.x];
                        Log.d("Mmm...", "Class: " + score);

                        Rect rect = new Rect(left, top, right, bottom);
                        result.add(rect);
                    }
                }
            }
        }

        return result;
    }

}
