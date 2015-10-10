package com.time2go.goosedetector;

import java.io.FileOutputStream;
import java.util.List;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.AttributeSet;
import android.util.Log;

//extending JavaCameraView so I can get access to native camera features and set focus to infinity
//  otherwise camera adjusts focus now and then creating false movement detection
public class myJavaCameraView extends JavaCameraView {

    private static final String TAG = "myJavaCameraView";


    public myJavaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFocusInfinity() {
        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        mCamera.setParameters(params);
    }

    public int getCameraWidth(){
        return mMaxWidth;
    }

    public int getCameraHeight(){
        return mMaxHeight;
    }

    public void setMaxResolution() {
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        mMaxHeight = sizes.get(0).height;    //0 seems to be the highest resolution
        mMaxWidth = sizes.get(0).width;
        List<Camera.Size> PictureSizes = params.getSupportedPictureSizes();
        //be sure to set view layout to "match parent" or image wont be scaled to fit the screen.
        //params.setPictureSize(PictureSizes.get(3).width, PictureSizes.get(3).height);
        //params.setPictureSize(mMaxWidth, mMaxHeight);
        disconnectCamera();
        connectCamera(PictureSizes.get(0).width, PictureSizes.get(0).height);
        //params.setPreviewSize(mMaxWidth, mMaxHeight);
        //mCamera.setParameters(params);
    }

    public void takePicture(final String fileName) {
        Log.i(TAG, "Tacking picture");
        PictureCallback callback = new PictureCallback() {

            private String mPictureFileName = fileName;

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i(TAG, "Saving a bitmap to file");
                Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
                try {
                    FileOutputStream out = new FileOutputStream(mPictureFileName);
                    picture.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    picture.recycle();
                    mCamera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mCamera.takePicture(null, null, callback);
    }
}