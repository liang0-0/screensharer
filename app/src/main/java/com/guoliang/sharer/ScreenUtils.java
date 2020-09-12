package com.guoliang.sharer;

import android.app.Service;
import android.util.DisplayMetrics;

public class ScreenUtils {
    public static int getScreenWidth(Service screenRecordService) {
        DisplayMetrics displayMetrics = screenRecordService.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Service screenRecordService) {
        return screenRecordService.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getScreenDensityDpi(Service screenRecordService) {
        return screenRecordService.getResources().getDisplayMetrics().densityDpi;
    }
}
