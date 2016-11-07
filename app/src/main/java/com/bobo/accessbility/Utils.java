package com.bobo.accessbility;

import android.util.Log;

/**
 * Created by xiaochj on 16/11/5.
 */

public class Utils {

    public static void LogUtil(String logType,String tag,String str){
        if(logType.equalsIgnoreCase("v")){

        }else if(logType.equalsIgnoreCase("d")){
            Log.d(tag,str);
        }else if(logType.equalsIgnoreCase("e")){

        }else if(logType.equalsIgnoreCase("i")){

        }
    }
}
