// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.time2go.goosedetector;

import java.util.ArrayList;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public final class BasicDetector extends BaseDetector implements IDetector
{

    public static final int N = 3;
    private static final String TAG = ":BasicDetector";
    private Mat buf[];
    private int last;
    private int threshold;
    private int drawAllcontours=-1;

    public BasicDetector(int i)
    {
        buf = null;
        last = 0;
        threshold = i;
    }

    public void saveFgMask(){

    }

    public Mat detect(Mat mat)
    {
        Size size = mat.size();
        int i = last;
        if (buf == null || (double)buf[0].width() != size.width || (double)buf[0].height() != size.height)
        {
            if (buf == null)
            {
                buf = new Mat[3];
            }
            for (int j = 0; j < 3; j++)
            {
                buf[j] = new Mat(size, CvType.CV_8UC1);
                buf[j] = Mat.zeros(size, CvType.CV_8UC1);
            }
        }
        Imgproc.cvtColor(mat, buf[last], Imgproc.COLOR_BGR2GRAY);
        int k = (1 + last) % 3;
        last = k;
        Mat mat1 = buf[k];
        Core.absdiff(buf[i], buf[k], mat1);
        Imgproc.threshold(mat1, mat1, threshold, 255D, Imgproc.THRESH_BINARY);
        Imgproc.erode(mat1, mat1, new Mat());
        Imgproc.dilate(mat1, mat1, new Mat());
        ArrayList arraylist = new ArrayList();
        Imgproc.findContours(mat1, arraylist, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        numContours = arraylist.size();
        Imgproc.drawContours(mat, arraylist, drawAllcontours, contourColor, contourThickness);
        return mat;
    }
}
