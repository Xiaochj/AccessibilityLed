package com.xiaochj.accessibility.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

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

}
