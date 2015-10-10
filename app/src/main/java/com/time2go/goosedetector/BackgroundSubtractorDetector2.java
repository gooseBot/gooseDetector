package com.time2go.goosedetector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.drawContours;

public final class BackgroundSubtractorDetector2 extends BaseDetector implements IDetector {

    private static final double LEARNING_RATE = 0.1 ;

    private BackgroundSubtractorMOG2 bg;
    private Mat buf[];
    private Mat fgMask = new Mat();
    private Mat sourceHSV = new Mat();
    private Mat sourceRGB = new Mat();
    private Mat myMask;
    private int sunShadowHistory=3;
    private static final String TAG = "BackgroundSubtractorDetector2";
    private int current;
    private File mMediaStorageDir;
    private int mHSVproximity=5;

    public BackgroundSubtractorDetector2(int history, float threshold, boolean shadowDetect, File mediaDir, int HSVproximity, double fTau) {
        //To reduce noise artifacts, we increase varThreshold from the default 16 to 64
        mMediaStorageDir=mediaDir;
        mHSVproximity=HSVproximity;
        bg = Video.createBackgroundSubtractorMOG2(history, threshold, shadowDetect);
        //mBgMog2.setInt("nmixtures" , 3);
        //mBgMog2.setDouble("fTau" , 0.2);

        buf = new Mat[sunShadowHistory];
        for (int i=0;i<sunShadowHistory;i++) {
            buf[i] = new Mat();
        }
    }

    public void saveFgMask(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mMediaStorageDir.getPath() +
                File.separator + timeStamp + "_fgMask.png");
        Imgcodecs.imwrite(mediaFile.toString(), fgMask);
    }

    @Override
    public Mat detect(Mat source) {

        int allContours=-1;
        int filled=-1;

        current++;
        int indexCurrent = current % sunShadowHistory;
        int indexOldest = (current-(sunShadowHistory-1)) % sunShadowHistory;
        Imgproc.cvtColor(source, sourceHSV, Imgproc.COLOR_RGB2HSV_FULL);
        sourceHSV.copyTo(buf[indexCurrent]);

        //bg.apply(source, fgMask, LEARNING_RATE);
        bg.apply(source, fgMask);

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
