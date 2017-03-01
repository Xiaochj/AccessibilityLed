package com.xiaochj.accessibility.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaochj.accessibility.application.LedApplication;

import java.util.Timer;
import java.util.TimerTask;

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
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        boolean isKeyguardflag = km.isKeyguardLocked();
        boolean isPowerflag = pm.isScreenOn();
        //如果锁屏就解锁,否则不动
        if (isKeyguardflag) {
            kl.disableKeyguard();
        }
        //如果屏幕灭了,就唤醒,否则不动
        if(!isPowerflag){
            PowerManager.WakeLock pl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            pl.acquire();
            pl.release();
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
     * 自定义样式的toast且自定义时间
     * @param ctx
     * @param text
     * @param time
     */
    public static void ToastForCustomTime(Context ctx, String text, final int time){
        final Toast toast = Toast.makeText(ctx,text,Toast.LENGTH_LONG);
        LinearLayout rl = new LinearLayout(ctx);
        rl.setOrientation(LinearLayout.VERTICAL);
        TextView tv = new TextView(ctx);
        Button button = new Button(ctx);
        // Get the screen size with unit pixels.
        WindowManager wm = (WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        WindowManager.LayoutParams vlp = new WindowManager.LayoutParams(outMetrics.widthPixels,
                outMetrics.heightPixels);
        vlp.horizontalMargin = 0;
        vlp.verticalMargin = 0;
        rl.setLayoutParams(vlp);
        rl.addView(tv);
        tv.setTextSize(100);
        tv.setBackgroundColor(ctx.getResources().getColor(android.R.color.white));
        tv.setTextColor(ctx.getResources().getColor(android.R.color.black));
        tv.setText(text);
        rl.addView(button);
        button.setText("掉我关闭");
        toast.setView(rl);
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 3000);
        //全屏显示
        toast.setGravity(Gravity.FILL,0,0);
        toast.show();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        },time);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        toast.cancel();
                        timer.cancel();
                    }
                },0);
            }
        });
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
