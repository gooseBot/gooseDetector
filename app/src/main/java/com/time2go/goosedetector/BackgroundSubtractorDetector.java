package com.time2go.goosedetector;

import java.util.ArrayList;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;

public final class BackgroundSubtractorDetector extends BaseDetector implements IDetector {

    private static final double LEARNING_RATE = 0.1 ;
    private static final int MIXTURES = 4;
    private static final int HISTORY = 3;
    private static final double BACKGROUND_RATIO = 0.8;

    // ring image buffer
    private Mat buf = null;
    private BackgroundSubtractorMOG2 bg;

    public BackgroundSubtractorDetector(int history, float threshold){
        //bg = new BackgroundSubtractorMOG2(HISTORY, MIXTURES, (backgroundRatio / 100.0));
        bg = new BackgroundSubtractorMOG2(history, threshold, false);
    }

    @Override
    public Mat detect(Mat source) {

        Size size = source.size();

        if (buf == null || buf.width() != size.width || buf.height() != size.height) {
            if (buf == null) {
                buf = new Mat(size, CvType.CV_8UC1);
                buf = Mat.zeros(size, CvType.CV_8UC1);
            }
        }

        Imgproc.cvtColor(source, buf, Imgproc.COLOR_RGBA2RGB);
        //Imgproc.cvtColor(source, buf, Imgproc.COLOR_RGBA2GRAY);
        Mat fgMask = new Mat();
        bg.apply(buf, fgMask, LEARNING_RATE);

        Imgproc.erode(fgMask, fgMask, new Mat());
        Imgproc.dilate(fgMask, fgMask, new Mat());

        ArrayList contours = new ArrayList();
        Mat hierarchy = new Mat();
        //Imgproc.findContours(fgMask, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.findContours(fgMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        numContours = contours.size();
        Imgproc.drawContours(source, contours, -1, contourColor, contourThickness);
        return source;
    }

}
