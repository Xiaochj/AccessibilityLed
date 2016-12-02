package com.xiaochj.accessibility.application;

import android.app.Application;

import java.util.UUID;

/**
 * Created by xiaochj on 16/11/25.
 */

public class LedApplication extends Application {
    public static final String TAG = "accessibilityservice";
    public static final String SP_APP = "AccssibilityLed";
    //设备唯一标识码
    public static final UUID UUID_LED = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
}
