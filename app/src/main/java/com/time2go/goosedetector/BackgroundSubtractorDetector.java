package com.time2go.goosedetector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG;

public final class BackgroundSubtractorDetector extends BaseDetector implements IDetector {

    private static final double LEARNING_RATE = 0.1 ;
    //private static final int MIXTURES = 4;

    // ring image buffer
    //private Mat buf = null;
    private Mat buf = new Mat();
    private BackgroundSubtractorMOG bg;
    private Mat fgMask = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
    // Minimum contour area in percent for contours filtering
    //private static double mMinContourArea = 0.1;
    private static double mMinContourArea = 0.01;

    //public BackgroundSubtractorDetector(int history, float threshold){
    //public BackgroundSubtractorDetector(int history, int backgroundRatio){
    public BackgroundSubtractorDetector(){
        //history around 10, mixtures 3-5, background ratio .5 to .8?
        //bg = new BackgroundSubtractorMOG(history, MIXTURES, (backgroundRatio / 100.0));
        bg = new BackgroundSubtractorMOG();
        //history around 100, threshold 16, don't try to filter out shadows
        //bg = new BackgroundSubtractorMOG2(history, threshold, false);
    }

    public void saveFgMask(){

    }

    @Override
    public Mat detect(Mat source) {

//        Size size = source.size();
//
//        if (buf == null || buf.width() != size.width || buf.height() != size.height) {
//            if (buf == null) {
//                buf = new Mat(size, CvType.CV_8UC1);
//                buf = Mat.zeros(size, CvType.CV_8UC1);
//            }
//        }

        Imgproc.cvtColor(source, buf, Imgproc.COLOR_RGBA2GRAY);
        //Imgproc.GaussianBlur(buf,buf,new Size(3,3),0,0,Imgproc.BORDER_DEFAULT);
        bg.apply(buf, fgMask, LEARNING_RATE);
        //bg.apply(source, fgMask, LEARNING_RATE);

        //Imgproc.threshold(fgMask, fgMask, .2, 255D, Imgproc.THRESH_BINARY);
        Imgproc.erode(fgMask, fgMask, new Mat());
        Imgproc.dilate(fgMask, fgMask, new Mat());

        //ArrayList contours = new ArrayList();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(fgMask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Imgproc.drawContours(source, contours, -1, contourColor, contourThickness);
        //Imgproc.drawContours(source, mContours, -1, contourColor, contourThickness);
        numContours = contours.size();
        return source;
    }
}
