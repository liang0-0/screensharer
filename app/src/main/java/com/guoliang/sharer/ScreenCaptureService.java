package com.guoliang.sharer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.RequiresApi;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author by talon, Date on 19/6/23.
 * note:
 */
public class ScreenCaptureService extends Service {

    private final String TAG = "ScreenCaptureService";

    /**
     * 是否为标清视频
     */
    private boolean isVideoSd = false;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;

    private int mResultCode;
    private Intent mResultData;

    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;
    private ImageReader mImageReader;

    public ScreenCaptureService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        System.out.println("ScreenRecordService.onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mResultCode = intent.getIntExtra("resultCode", 1);
        mResultData = intent.getParcelableExtra("data");

        getScreenBaseInfo();

        mMediaProjection = createMediaProjection();
        mMediaRecorder = createMediaRecorder();
        mVirtualDisplay = createVirtualDisplay(); // 必须在mediaRecorder.prepare() 之后调用，否则报错"fail to get surface"
        mImageReader = createImageReader();
        mMediaRecorder.start();
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if(mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if(mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            mMediaProjection.stop();
            mMediaRecorder.reset();
        }
        if(mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }


    /**
     * 获取屏幕相关数据
     */
    private void getScreenBaseInfo() {
        mScreenWidth = ScreenUtils.getScreenWidth(this);
        mScreenHeight = ScreenUtils.getScreenHeight(this);
        mScreenDensity = ScreenUtils.getScreenDensityDpi(this);
        System.out.printf("ScreenRecordService.getScreenBaseInfo:%d, %d, %d%n", mScreenWidth, mScreenHeight, mScreenDensity);
    }

    private ImageReader createImageReader() {
        return ImageReader.newInstance(mScreenWidth, mScreenHeight, ImageFormat.FLEX_RGBA_8888, 2);
    }

    private MediaProjection createMediaProjection() {
        Log.i(TAG, "Create MediaProjection");
        return ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE)).getMediaProjection(mResultCode, mResultData);
    }


    private MediaRecorder createMediaRecorder() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());
        String curTime = formatter.format(curDate).replace(" ", "");
        String videoQuality = "HD";
        if (isVideoSd) videoQuality = "SD";

        Log.i(TAG, "Create MediaRecorder");
        MediaRecorder mediaRecorder = new MediaRecorder();
//        if(isAudio) mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        String outputFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + videoQuality + curTime + ".mp4";
        System.out.println("ScreenRecordService.createMediaRecorder: outputFile = " + outputFile);
        mediaRecorder.setOutputFile(outputFile);
        mediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);  //after setVideoSource(), setOutFormat()
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);  //after setOutputFormat()
//        if(isAudio) mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);  //after setOutputFormat()
        int bitRate;
        if (isVideoSd) {
            mediaRecorder.setVideoEncodingBitRate(mScreenWidth * mScreenHeight);
            mediaRecorder.setVideoFrameRate(30);
            bitRate = mScreenWidth * mScreenHeight / 1000;
        } else {
            mediaRecorder.setVideoEncodingBitRate(5 * mScreenWidth * mScreenHeight);
            mediaRecorder.setVideoFrameRate(60); //after setVideoSource(), setOutFormat()
            bitRate = 5 * mScreenWidth * mScreenHeight / 1000;
        }
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return mediaRecorder;
    }

    private VirtualDisplay createVirtualDisplay() {
        Log.i(TAG, "Create VirtualDisplay");
        return mMediaProjection.createVirtualDisplay(TAG, mScreenWidth, mScreenHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);
    }
}