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
