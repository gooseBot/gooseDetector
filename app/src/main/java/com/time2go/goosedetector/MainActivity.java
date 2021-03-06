package com.time2go.goosedetector;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

// The app at the URL was used as a guide from a code project entry
//https://code.google.com/p/make-money-apps/
//http://www.codeproject.com/Articles/791145/Motion-Detection-in-Android-Howto
//I combined info from the Howto with information from the color-blob-dectect opencv sample.

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, SensorEventListener {

    private static final String TAG = "OCVSample::Activity";
    private Mat cameraRgbaFrame;
    //private Mat cameraBgrFrame = new Mat();
    private myJavaCameraView mOpenCvCameraView;
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
    private int mMinCountourThreshold;
    private int mMaxCountourThreshold;
    private int mBasicDetectorThreshold;
    private int mCameraMaxWidth;
    private int mCameraMaxHeight;
    private boolean mDectectEnabled;
    private int mHSVproximity;
    private double mFtau;
    private static long mLastTriggerTime;
    //private int mSubtractorBackgroundRatio;
    private String mMotionDetector;
    private int mSubtractorHistory;
    private int mSubtractorThreshold;
    private int mShadowDetection;

    private SensorManager mSensorManager;
    private Sensor mLight;
    private float mlux;
    private float mluxThreashold;
    private Handler mHandler = new Handler();
    //private Histogram mHistogram = new Histogram(5,1,30,100);
    private File mediaStorageDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (myJavaCameraView) findViewById(R.id.goose_detect_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

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

        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "GooseCam");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory");
            }
        }

        mOpenCvCameraView.setCvCameraViewListener(this);
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
        //mHistogram.fill((double)contourCount);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
            ((TextView)findViewById(R.id.message)).setText("Count "+String.valueOf(contourCount));
            //((TextView)findViewById(R.id.average)).setText("Average "+String.valueOf(mHistogram.getAvg()));
            //((TextView)findViewById(R.id.histogramMean)).setText("Histogram "+String.valueOf(mHistogram.mean()));
            }
        });

        if (contourCount > mMinCountourThreshold && contourCount < mMaxCountourThreshold && mDectectEnabled && mlux > mluxThreashold) {
            if ((System.currentTimeMillis()-mLastTriggerTime) > 1000*5) {
                Log.i(TAG, "Message send to goose gun: " + String.valueOf(contourCount));
                UDPcommunication UDPcommunicationTask = new UDPcommunication();
                UDPcommunicationTask.execute("gde", this);
                mLastTriggerTime = System.currentTimeMillis();

                //motionDetector.saveFgMask();

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File mediaFile = new File(mediaStorageDir.getPath() +
                        File.separator + "contour_" +
                        String.valueOf(contourCount) + "_" +
                        timeStamp + ".jpg");
                Mat cameraBgrFrame = new Mat();
                Imgproc.cvtColor(cameraRgbaFrame,cameraBgrFrame,Imgproc.COLOR_RGBA2BGR);
                Highgui.imwrite(mediaFile.toString(), cameraBgrFrame);
            }
        }
        return cameraRgbaFrame;
    }

    public void onCameraViewStarted(int i, int j)
    {
        mOpenCvCameraView.setMaxResolution();  //resets camera set focus next
        mOpenCvCameraView.setFocusInfinity();
        mCameraMaxWidth=mOpenCvCameraView.getCameraWidth();
        mCameraMaxHeight=mOpenCvCameraView.getCameraHeight();
        ApplySettings();   //this ensures ROI is within bounds in addition to getting all settings
    }

    public void onCameraViewStopped()
    {
    }

    public void onDestroy()
    {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onPause()
    {
        super.onPause();
        mSensorManager.unregisterListener(this);
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
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

        mMinCountourThreshold = Integer.parseInt(mSharedPrefs.getString("contoursMinThreshold", "10"));
        mPreferences.findPreference("contoursMinThreshold").setSummary(String.valueOf(mMinCountourThreshold));

        mMaxCountourThreshold = Integer.parseInt(mSharedPrefs.getString("contoursMaxThreshold", "150"));
        mPreferences.findPreference("contoursMaxThreshold").setSummary(String.valueOf(mMaxCountourThreshold));

        mluxThreashold = Integer.parseInt(mSharedPrefs.getString("luxThreshold", "5"));
        mPreferences.findPreference("luxThreshold").setSummary(String.valueOf(mluxThreashold));

//        mSubtractorBackgroundRatio = Integer.parseInt(mSharedPrefs.getString("subtractorBackgroundRatio", "80"));
//        mPreferences.findPreference("subtractorBackgroundRatio").setSummary(String.valueOf(mSubtractorBackgroundRatio));
//
        mSubtractorHistory = Integer.parseInt(mSharedPrefs.getString("subtractorHistory", "3"));
        mPreferences.findPreference("subtractorHistory").setSummary(String.valueOf(mSubtractorHistory));

        mSubtractorThreshold = Integer.parseInt(mSharedPrefs.getString("subtractorThreshold", "64"));
        mPreferences.findPreference("subtractorThreshold").setSummary(String.valueOf(mSubtractorThreshold));

        mShadowDetection = Integer.parseInt(mSharedPrefs.getString("shadowDetection", "40"));
        mPreferences.findPreference("shadowDetection").setSummary(String.valueOf(mShadowDetection));

        mFtau = Double.parseDouble(mSharedPrefs.getString("fTau", "5"));
        mPreferences.findPreference("fTau").setSummary(String.valueOf(mFtau));

        mHSVproximity = Integer.parseInt(mSharedPrefs.getString("HSVproximity", "5"));
        mPreferences.findPreference("HSVproximity").setSummary(String.valueOf(mHSVproximity));

        mBasicDetectorThreshold = Integer.parseInt(mSharedPrefs.getString("basicDetectorThreshold", "60"));
        mPreferences.findPreference("basicDetectorThreshold").setSummary(String.valueOf(mBasicDetectorThreshold));

        mMotionDetector = mSharedPrefs.getString("detectorMethod", "basic");
        mPreferences.findPreference("detectorMethod").setSummary(mMotionDetector);

        mDectectEnabled = mSharedPrefs.getBoolean("detectEnabled", false);

        switch (mMotionDetector) {
            case "basic":
                motionDetector = new BasicDetector(mBasicDetectorThreshold);
                break;
            case "subtractorMOG":
                //motionDetector = new BackgroundSubtractorDetector(mSubtractorHistory, mSubtractorBackgroundRatio/100);
                motionDetector = new BackgroundSubtractorDetector();
                break;
            case "subtractorMOG2":
                //motionDetector = new BackgroundSubtractorDetector2();
                motionDetector = new BackgroundSubtractorDetector2(mSubtractorHistory,
                        mSubtractorThreshold, mShadowDetection, mediaStorageDir, mHSVproximity, mFtau);
                break;
        }
    }
}
