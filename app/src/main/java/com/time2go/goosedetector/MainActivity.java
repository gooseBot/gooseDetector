package com.time2go.goosedetector;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

// The app at the URL was used as a guide from a code project entry
//https://code.google.com/p/make-money-apps/
//http://www.codeproject.com/Articles/791145/Motion-Detection-in-Android-Howto
//I combined info from the Howto with information from the color-blob-dectect opencv sample.

public class MainActivity extends Activity implements org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private Mat cameraRgbaFrame;
    private CameraBridgeViewBase mOpenCvCameraView;
    private IDetector motionDetector;
    private DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("OCVSample::Activity", "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getFragmentManager().beginTransaction().replace(R.id.content_navigation,
                new Preferences()).commit();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                Toast.makeText(getApplicationContext(), "closed", Toast.LENGTH_SHORT).show();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Toast.makeText(getApplicationContext(), "open", Toast.LENGTH_SHORT).show();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

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
