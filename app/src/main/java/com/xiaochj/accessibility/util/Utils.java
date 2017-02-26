package com.xiaochj.accessibility.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.xiaochj.accessibility.application.LedApplication;

/**
 * Created by xiaochj on 16/11/5.
 */

public class Utils {

    /**
     * 监听屏幕唤醒和屏幕锁
     */
    public static void wakeAndUnlock(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isKeyguardflag = km.isKeyguardLocked();
        boolean isPowerflag = pm.isScreenOn();
        //如果屏幕灭了,就唤醒,否则不动
        if(!isPowerflag){
            PowerManager.WakeLock pl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            pl.acquire();
        }
        //如果锁屏就解锁,否则不动
        if (isKeyguardflag) {
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
            kl.disableKeyguard();
        }
    }

    /**
     * log日志
     */
    public static void LogUtil(String logType,String tag,String str){
        if(logType.equalsIgnoreCase("v")){

        }else if(logType.equalsIgnoreCase("d")){
            Log.d(tag,str);
        }else if(logType.equalsIgnoreCase("e")){

        }else if(logType.equalsIgnoreCase("i")){

        }
    }

    /**
     * toast提示
     */
    public static void ToastUtil(Context ctx, String text){
        Toast.makeText(ctx,text,Toast.LENGTH_LONG).show();
    }

    /**
     * 判断某个服务是否开启
     * @param ctx
     * @param service
     * @return
     */
    public static boolean isAccessibilitySettingsOn(Context ctx,String service) {
        int accessibilityEnabled = 0;
//        final String service = "com.test.package.name/com.test.package.name.YOURAccessibilityService";
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    ctx.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Exception e) {
            Log.e(LedApplication.TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(LedApplication.TAG, "***ACCESSIBILIY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    ctx.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();

                    Log.v(LedApplication.TAG, "-------------- > accessabilityService :: " + accessabilityService);
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        Log.v(LedApplication.TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(LedApplication.TAG, "***ACCESSIBILIY IS DISABLED***");
        }

        return accessibilityFound;
    }

}
