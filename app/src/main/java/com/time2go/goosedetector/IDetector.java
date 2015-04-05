// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.time2go.goosedetector;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public interface IDetector
{

    public abstract Mat detect(org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame cvcameraviewframe);

    public abstract Mat detect(Mat mat);

    public abstract int getContourCount();

    public abstract void setContourColor(Scalar scalar);

    public abstract void setContourThickness(int i);

    public abstract void saveFgMask();
}
