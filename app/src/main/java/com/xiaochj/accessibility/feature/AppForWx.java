package com.xiaochj.accessibility.feature;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.xiaochj.accessibility.application.LedApplication;
import com.xiaochj.accessibility.util.TtsUtils;
import com.xiaochj.accessibility.util.Utils;
import com.xiaochj.led.R;

import java.util.List;

/**
 * Created by xiaochj on 16/12/7.
 */

public class AppForWx implements LedAccessibilityService.OnDiffAppAccEventListener {

    Context mContext;
    AccessibilityService mService;

    public AppForWx(Context context, AccessibilityService service){
        mContext = context;
        mService = service;
    }

    @Override
    public void setAccessibilityEvent(int eventType, AccessibilityEvent event) {
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {//一个push推送过来,调通知的event
            Utils.LogUtil("d","event","TYPE_NOTIFICATION_STATE_CHANGED");
            setNotifyChanged(event);
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {//调用窗口样式改变的event
            Utils.LogUtil("d","event","TYPE_WINDOW_STATE_CHANGED");
            getWxSaleNumber();
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {//如果没退出微信,就一直调Action_Back返回事件
            if (!event.getPackageName().toString().equalsIgnoreCase(mContext.getString(R.string.android_launcher)))
                mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        }
    }

    private void setNotifyChanged(AccessibilityEvent event){
        List<CharSequence> texts = event.getText();
        if(!texts.isEmpty()) {
            for (CharSequence text : texts) {
                String notifyStr = text.toString();
                //当微信或支付宝在后台运行
                if (notifyStr.contains(mContext.getString(R.string.notify_wx))){
                    if(event.getParcelableData() != null && event.getParcelableData() instanceof Notification){
                        //唤醒解锁屏幕
                        Utils.wakeAndUnlock(mContext);
                        //获取notification序列化数据
                        Notification notify = (Notification)event.getParcelableData();
                        //拿到其中的contentIntent
                        PendingIntent pendingIntent = notify.contentIntent;
                        try{
                            //打开notification
                            pendingIntent.send();
                        }catch (Exception e){}
                    }
                }
            }
        }
    }

    private void getWxSaleNumber(){
        //打开微信
        List<AccessibilityNodeInfo> nodeInfos = mService.getRootInActiveWindow().findAccessibilityNodeInfosByText("￥");
        int size = nodeInfos.size();
        if (size == 0)
            return;
        if (size == 1) {			//card页面是清空状态
            performGlobal(nodeInfos,0);
            return;
        }
        if (size > 1 && size % 2 == 0) {			//card页面有多个支付card
            performGlobal(nodeInfos,size-2);
            return;
        }
    }

    private void performGlobal(List<AccessibilityNodeInfo> nodeInfos,int index){
        String moneyStr = nodeInfos.get(index).getText().toString();
//		Utils.LogUtil("d", TAG, moneyStr);
//        Utils.ToastUtil(mContext,"wx:"+moneyStr);
        //显示金额
        Utils.ToastForCustomTime(mContext, LedApplication.WEIXIN,""+moneyStr,10000);
        //读取金额
        TtsUtils.getInstance(mContext).ttsSales("微信到账"+moneyStr+"元");
        mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }
}
