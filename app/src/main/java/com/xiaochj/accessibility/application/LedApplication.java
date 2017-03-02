package com.xiaochj.accessibility.application;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * 程序的入口
 * Created by xiaochj on 16/11/25.
 */

public class LedApplication extends Application {

    public static final String TAG = "accessibilityservice";
    public static final String SP_APP = "AccssibilityLed";
    public static final String SERVICE =
            "com.xiaochj.led/com.xiaochj.accessibility.feature.LedAccessibilityService";

    public static final int WEIXIN = 0;
    public static final int ZHIFUBAO = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化tts服务
        SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID +"=58b639a3");
    }
}
