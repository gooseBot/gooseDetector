// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.time2go.goosedetector;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

// Referenced classes of package org.opencv.samples.colorblobdetect:
//            IDetector

public abstract class BaseDetector
    implements IDetector
{

    protected Scalar contourColor;
    protected int contourThickness;
    protected int numContours;
    protected boolean targetDetected;

    public BaseDetector()
    {
        targetDetected = false;
        numContours = 0;
        contourThickness = 2;
        contourColor = new Scalar(255D, 0.0D, 0.0D);
    }

    public Mat detect(org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame cvcameraviewframe)
    {
        return detect(cvcameraviewframe.rgba());
    }

    public int getContourCount()
    {
        return numContours;
    }

    public boolean isDetected()
    {
        return targetDetected;
    }

    public void setContourColor(Scalar scalar)
    {
        contourColor = scalar;
    }

    public void setContourThickness(int i)
    {
        contourThickness = i;
    }
}
