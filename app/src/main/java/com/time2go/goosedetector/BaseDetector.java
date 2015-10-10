package com.time2go.goosedetector;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public abstract class BaseDetector
    implements IDetector
{

    protected Scalar contourColor;
    protected int contourThickness;
    protected int numContours;

    public BaseDetector()
    {
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

    public void setContourColor(Scalar scalar)
    {
        contourColor = scalar;
    }

    public void setContourThickness(int i)
    {
        contourThickness = i;
    }
}
