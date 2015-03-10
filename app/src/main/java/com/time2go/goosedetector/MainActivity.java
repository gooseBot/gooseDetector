package com.time2go.goosedetector;

import android.app.Activity;
import android.os.Bundle;

import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

public class MainActivity extends Activity implements org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private Mat cameraRgbaFrame;
    private CameraBridgeViewBase mOpenCvCameraView;
    private IDetector motionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("OCVSample::Activity", "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.goose_detect_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        motionDetector = new BasicDetector(60);
    }

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Mat onCameraFrame(org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame cvcameraviewframe)
    {
        cameraRgbaFrame = cvcameraviewframe.rgba();
        Rect rect = new Rect(0, 100, cameraRgbaFrame.width(), -100 + cameraRgbaFrame.height());
        Mat mat = cameraRgbaFrame.submat(rect);
        motionDetector.detect(mat).copyTo(cameraRgbaFrame.submat(rect));
        Core.putText(cameraRgbaFrame, String.valueOf(motionDetector.getContourCount()), new Point(20D, 20D), 0, 1.0D, new Scalar(255D, 255D, 255D, 255D));
        return cameraRgbaFrame;
    }

    public void onCameraViewStarted(int i, int j)
    {
    }

    public void onCameraViewStopped()
    {
    }

    public void onDestroy()
    {
        super.onDestroy();
        if (mOpenCvCameraView != null)
        {
            mOpenCvCameraView.disableView();
        }
    }

    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
        {
            mOpenCvCameraView.disableView();
        }
    }

    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync("2.4.3", this, mLoaderCallback);
    }


}
