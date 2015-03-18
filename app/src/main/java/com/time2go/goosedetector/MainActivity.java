package com.time2go.goosedetector;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

public class MainActivity extends Activity implements org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2, SensorEventListener {

    private static final String TAG = "OCVSample::Activity";
    private Mat cameraRgbaFrame;
    private CameraBridgeViewBase mOpenCvCameraView;
    private IDetector motionDetector;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private Preferences mPreferences;
    private SharedPreferences mSharedPrefs;
    private PreferenceChangeListener mPreferenceListener = null;
    private int mROIleft;
    private int mROItop;
    private int mROIwidth;
    private int mROIheight;
    private int mCountourThreshold;
    private int mCameraMaxWidth;
    private int mCameraMaxHeight;
    private boolean mDectectEnabled;
    private static long mLastTriggerTime;

    private SensorManager mSensorManager;
    private Sensor mLight;
    private float mlux;
    private float mluxThreashold;
    private long mFrameCount;

    private Handler mHandler = new Handler();

    //private MovingAverage mAverage = new MovingAverage(5);
    private Histogram mHistogram = new Histogram(5,1,30,100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("OCVSample::Activity", "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.goose_detect_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        motionDetector = new BasicDetector(60);

        mPreferences = new Preferences();
        FragmentManager mFragmentManager = getFragmentManager();
        mFragmentManager.beginTransaction().replace(R.id.content_navigation, mPreferences).commit();
        mFragmentManager.executePendingTransactions();

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferenceListener = new PreferenceChangeListener();
        mSharedPrefs.registerOnSharedPreferenceChangeListener(mPreferenceListener);

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
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        mlux = event.values[0];
        Log.i(TAG, "Light sensor value: " + String.valueOf(mlux));
        mHandler.post(new Runnable() {
            @Override
            public void run() {
            ((TextView)findViewById(R.id.lux)).setText("Lux "+String.valueOf(mlux));
            }
        });
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
        Rect rect = new Rect(mROIleft, mROItop, mROIwidth, mROIheight);
        Mat mat = cameraRgbaFrame.submat(rect);
        motionDetector.detect(mat).copyTo(cameraRgbaFrame.submat(rect));
        final int contourCount = motionDetector.getContourCount();
        //mAverage.newNum(contourCount);
        mHistogram.fill((double)contourCount);
        //mFrameCount++;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
            ((TextView)findViewById(R.id.message)).setText("Count "+String.valueOf(contourCount));
            ((TextView)findViewById(R.id.average)).setText("Average "+String.valueOf(mHistogram.getAvg()));
            ((TextView)findViewById(R.id.histogramMean)).setText("Histogram "+String.valueOf(mHistogram.mean()));
            }
        });

        if (motionDetector.getContourCount()>mCountourThreshold && mDectectEnabled && mlux > mluxThreashold) {
            if ((System.currentTimeMillis()-mLastTriggerTime) > 1000*10) {
                Log.i(TAG, "Message send to goose gun: " + String.valueOf(motionDetector.getContourCount()));
                UDPcommunication UDPcommunicationTask = new UDPcommunication();
                UDPcommunicationTask.execute("gde", this);
                mLastTriggerTime = System.currentTimeMillis();
            }
        }
        return cameraRgbaFrame;
    }

    public void onCameraViewStarted(int i, int j)
    {
        mCameraMaxWidth=i;
        mCameraMaxHeight=j;
        Preference pref = mPreferences.findPreference("ROIwidth");
        pref.setSummary("Maximum = " + String.valueOf(mCameraMaxWidth));
        pref = mPreferences.findPreference("ROIheight");
        pref.setSummary("Maximum = " + String.valueOf(mCameraMaxHeight));
        ApplySettings();
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
        mSensorManager.unregisterListener(this);
        if (mOpenCvCameraView != null)
        {
            mOpenCvCameraView.disableView();
        }
    }

    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync("2.4.3", this, mLoaderCallback);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            ApplySettings();
        }
    }

    public void ApplySettings() {
        mROItop = Integer.parseInt(mSharedPrefs.getString("ROItop", "0"));
        if (mROItop < 0) mROItop = 0;
        if (mROItop > mCameraMaxHeight) mROItop=mCameraMaxHeight;
        mPreferences.findPreference("ROItop").setSummary(String.valueOf(mROItop));

        mROIleft = Integer.parseInt(mSharedPrefs.getString("ROIleft", "0"));
        if (mROIleft < 0) mROIleft = 0;
        if (mROIleft > mCameraMaxWidth) mROIleft=mCameraMaxWidth;
        mPreferences.findPreference("ROIleft").setSummary(String.valueOf(mROIleft));

        mROIheight = Integer.parseInt(mSharedPrefs.getString("ROIheight", String.valueOf(mCameraMaxHeight)));
        if (mROIheight <= 0) mROIheight = 1;
        if (mROIheight > mCameraMaxHeight-mROItop) mROIheight = mCameraMaxHeight-mROItop;
        mPreferences.findPreference("ROIheight").setSummary(String.valueOf(mROIheight)+ "/" + String.valueOf(mCameraMaxHeight));

        mROIwidth = Integer.parseInt(mSharedPrefs.getString("ROIwidth", String.valueOf(mCameraMaxWidth)));
        if (mROIwidth <= 0) mROIwidth = 1;
        if (mROIwidth > mCameraMaxWidth-mROIleft) mROIwidth=mCameraMaxWidth-mROIleft;
        mPreferences.findPreference("ROIwidth").setSummary(String.valueOf(mROIwidth)+ "/" + String.valueOf(mCameraMaxWidth));

        mCountourThreshold = Integer.parseInt(mSharedPrefs.getString("contoursThreshold", "10"));
        mPreferences.findPreference("contoursThreshold").setSummary(String.valueOf(mCountourThreshold));

        mluxThreashold = Integer.parseInt(mSharedPrefs.getString("luxThreshold", "5"));
        mPreferences.findPreference("luxThreshold").setSummary(String.valueOf(mluxThreashold));

        mDectectEnabled = mSharedPrefs.getBoolean("dectectEnabled", false);
    }
}
