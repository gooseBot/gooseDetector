package com.time2go.goosedetector;

import android.media.Image;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG;
import org.opencv.video.BackgroundSubtractorMOG2;

import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.drawContours;

public final class BackgroundSubtractorDetector2 extends BaseDetector implements IDetector {

    private static final double LEARNING_RATE = 0.1 ;

    private BackgroundSubtractorMOG2 bg;
    //private Mat buf = new Mat();
    //private Mat buf[] = new Mat[sunShadowHistory];
    private Mat buf[];
    //private Mat buf[];
    private Mat fgMask = new Mat();
    private Mat sourceHSV = new Mat();
    private Mat sourceRGB = new Mat();
    private Mat myMask;
    private int sunShadowHistory=3;
    //private Mat fgMask;
    private static final String TAG = "OCVSample::Activity";
    private double mShadowDetect;
    private double mFtau;
    //private int indexOldest;
    private int current;
    private File mMediaStorageDir;
    private int mHSVproximity=5;
    private static int allContours=-1;
    private static int filled=-1;

    public BackgroundSubtractorDetector2(int history, float threshold, double shadowDetect, File mediaDir, int HSVproximity, double fTau) {
        //To reduce noise artifacts, we increase varThreshold from the default 16 to 64
        mShadowDetect=shadowDetect;
        mMediaStorageDir=mediaDir;
        mHSVproximity=HSVproximity;
        mFtau=fTau;
        bg = new BackgroundSubtractorMOG2(history, threshold, true);
        //bg = new BackgroundSubtractorMOG2();
        //bg.setInt("nmixtures", 3);
        bg.setDouble("fTau",mFtau);
        //bg.setInt("nShadowDetection", 40);
        bg.setDouble("nShadowDetection", mShadowDetect);

        //mBgMog2.setInt("nmixtures" , 3);
        //bg.setDouble("fVarInit" , 80.0);
        //mBgMog2.setDouble("fTau" , 0.2);
        //bg.setDouble("fVarMin" , 200.0);
        //bg.setDouble("fVarMax" , 80.0);
        bg.setBool("detectShadows", true);

        buf = new Mat[sunShadowHistory];
        for (int i=0;i<sunShadowHistory;i++) {
            buf[i] = new Mat();
        }

        Log.e(TAG,"nmixtures = " + bg.getInt("nmixtures"));
        Log.e(TAG,"fTau = " + bg.getDouble("fTau"));
        Log.e(TAG, "nShadowDetection = " + bg.getInt("nShadowDetection"));
    }

    public void saveFgMask(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mMediaStorageDir.getPath() +
                File.separator + "contour_" +
                timeStamp + "_fgMask.png");
        Highgui.imwrite(mediaFile.toString(), fgMask);
    }

    @Override
    public Mat detect(Mat source) {

        //Imgproc.cvtColor(source,sourceRGB,Imgproc.COLOR_RGBA2BGR);

        current++;
        int indexCurrent = current % sunShadowHistory;
        int indexOldest = (current-(sunShadowHistory-1)) % sunShadowHistory;
        Imgproc.cvtColor(source, sourceHSV, Imgproc.COLOR_RGB2HSV_FULL);
        //Imgproc.cvtColor(sourceRGB, sourceHSV, Imgproc.COLOR_RGB2HSV_FULL);
        sourceHSV.copyTo(buf[indexCurrent]);

        //bg.apply(sourceRGB, fgMask, LEARNING_RATE);
        bg.apply(source, fgMask, LEARNING_RATE);
        //save the file for debugging

        //Imgproc.threshold(fgMask, fgMask, 1, 255, Imgproc.THRESH_BINARY);
        Imgproc.erode(fgMask, fgMask, new Mat());
        Imgproc.dilate(fgMask, fgMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(fgMask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (current > (sunShadowHistory-1)) {
            if (myMask==null) {
                myMask = new Mat(source.size(), CvType.CV_8UC1);
            }
            Imgproc.drawContours(myMask, contours, allContours, contourColor, filled);

            for (int contourIdx = contours.size()-1; contourIdx >= 0; contourIdx--) {
                Rect roi = Imgproc.boundingRect(contours.get(contourIdx));
                Scalar meanHSVcurrent = Core.mean(buf[indexCurrent].submat(roi), myMask.submat(roi));
                Scalar meanHSVoldest = Core.mean(buf[indexOldest].submat(roi), myMask.submat(roi));

                if (Math.abs(meanHSVcurrent.val[0] - meanHSVoldest.val[0]) < mHSVproximity){
                    contours.remove(contourIdx);
                } else {
                    Scalar meanRGB = Core.mean(source.submat(roi), myMask.submat(roi) );
                    //Log.e(TAG, "contourOld = " + meanHSVoldest.toString());
                    //Log.e(TAG, "contourCurrent = " + meanHSVcurrent.toString());
                    drawContours(source, contours, contourIdx, meanRGB, -1);
                }
            }
        }

        drawContours(source, contours, allContours, contourColor, contourThickness);
        numContours = contours.size();
        return source;
    }
}
