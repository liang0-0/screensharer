package com.guoliang.sharer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_VIDEO = 1;
    private static final int REQUEST_CODE_CAPTURE = 2;
    private MediaProjectionManager mMediaProjectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission(this); //检查权限

        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    public void screenshot(View view) {
        Intent service = new Intent(this, ScreenCaptureService.class);
        service.putExtra("data", "data");
        startService(service);
    }

    public void StartRecorder(View view) {
        createScreenCapture();
    }

    public void StopRecorder(View view) {
        Intent service = new Intent(this, ScreenRecordService.class);
        stopService(service);
    }


    public static void checkPermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkPermission =
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                            + ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)
                            + ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            + ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                //动态申请
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
            }
        }
    }


    private void createScreenCapture() {
        Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_CODE_VIDEO);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_VIDEO) {
                try {
                    Toast.makeText(this, "允许录屏", Toast.LENGTH_SHORT).show();

                    Intent service = new Intent(this, ScreenRecordService.class);
                    service.putExtra("resultCode", resultCode);
                    service.putExtra("data", data);
                    startService(service);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_CODE_CAPTURE) {
                try {
                    Toast.makeText(this, "允许截屏", Toast.LENGTH_SHORT).show();

                    Intent service = new Intent(this, ScreenCaptureService.class);
                    service.putExtra("resultCode", resultCode);
                    service.putExtra("data", data);
                    startService(service);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "拒绝录屏/截屏", Toast.LENGTH_SHORT).show();
        }
    }
}