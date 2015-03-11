package com.time2go.goosedetector;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

// The app at the URL was used as a guide from a code project entry
//https://code.google.com/p/make-money-apps/
//http://www.codeproject.com/Articles/791145/Motion-Detection-in-Android-Howto
//I combined info from the Howto with information from the color-blob-dectect opencv sample.

public class MainActivity extends ActionBarActivity implements org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private Mat cameraRgbaFrame;
    private CameraBridgeViewBase mOpenCvCameraView;
    private IDetector motionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("OCVSample::Activity", "called onCreate");
        //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(128, 0, 0, 0)));
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#330000ff")) );
        //getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#550000ff")));

        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.goose_detect_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        motionDetector = new BasicDetector(60);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.preferences:
                Intent intent = new Intent();
                intent.setClassName(this, "com.time2go.goosedetector.preferenceActivity");
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
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
