package com.xiaochj.accessibility.feature;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;

import com.xiaochj.accessibility.util.Utils;
import com.xiaochj.led.R;

import java.util.List;

/**
 * Created by xiaochj on 16/12/7.
 */

public class AppForZfb implements LedAccessibilityService.OnDiffAppAccEventListener {

    Context mContext;
    AccessibilityService mService;

    public AppForZfb(Context context, AccessibilityService service){
        mContext = context;
        mService = service;
    }

    @Override
    public void setAccessibilityEvent(int eventType, AccessibilityEvent event) {
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {//一个push推送过来,调通知的event
            setNotifyChanged(event);
        }
    }

    private void setNotifyChanged(AccessibilityEvent event){
        List<CharSequence> texts = event.getText();
        if(!texts.isEmpty()) {
            for (CharSequence text : texts) {
                String notifyStr = text.toString();
                //当支付宝在后台运行
                if(notifyStr.contains(mContext.getString(R.string.notify_zfb))){
                    //唤醒解锁屏幕
                    Utils.wakeAndUnlock(mContext);
                    //字符串截取,中间的金额部分
                    String[] moneyStrList1 = notifyStr.split(mContext.getString(R.string.kuan_zfb));
                    String[] moneyStrList2 = moneyStrList1[1].split(mContext.getString(R.string.yuan_zfb));
                    String moneyStr = moneyStrList2[0];
//                    Utils.ToastUtil(mContext,"zfb:"+moneyStr);
                    //显示金额
                    Utils.ToastForCustomTime(mContext,"zfb:"+moneyStr,10*1000);

                }
            }
        }
    }
}
