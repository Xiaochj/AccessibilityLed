package com.xiaochj.accessibility.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.xiaochj.accessibility.application.LedApplication;

/**
 * Created by xiaochj on 16/11/5.
 */

public class Utils {

    //log日志
    public static void LogUtil(String logType,String tag,String str){
        if(logType.equalsIgnoreCase("v")){

        }else if(logType.equalsIgnoreCase("d")){
            Log.d(tag,str);
        }else if(logType.equalsIgnoreCase("e")){

        }else if(logType.equalsIgnoreCase("i")){

        }
    }

    //toast提示
    public static void ToastUtil(Context ctx, String text){
        Toast.makeText(ctx,text,Toast.LENGTH_LONG).show();
    }

    //记录是否第一次打开app的状态
    public static void setSpFirstApp(Context ctx,boolean isFirst){
        SharedPreferences sp = ctx.getSharedPreferences(LedApplication.SP_APP,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("firstApp",isFirst);
        editor.commit();
    }

    public static boolean getSpFirstApp(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(LedApplication.SP_APP,Context.MODE_PRIVATE);
        return sp.getBoolean("firstApp",true);
    }//end

    //保存led的蓝牙地址
    public static void setSpMacAddr(Context ctx,String macAddr){
        SharedPreferences sp = ctx.getSharedPreferences(LedApplication.SP_APP,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("macAddr",macAddr);
        editor.commit();
    }

    public static String getSpMacAddr(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(LedApplication.SP_APP,Context.MODE_PRIVATE);
        return sp.getString("macAddr","");
    }//end
}
