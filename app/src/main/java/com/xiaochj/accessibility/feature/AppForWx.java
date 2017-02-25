package com.xiaochj.accessibility.feature;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

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
        } /*else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {//如果没退出微信,就一直调Action_Back返回事件
            if (!event.getPackageName().toString().equalsIgnoreCase(mContext.getString(R.string.android_launcher)))
                mService.performGlobalAction(GLOBAL_ACTION_BACK);
        }*/
        else if(eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
            Utils.LogUtil("d","event","TYPE_WINDOW_CONTENT_CHANGED");
        }
        else if(eventType == AccessibilityEvent.TYPE_VIEW_CLICKED){
            Utils.LogUtil("d","event","TYPE_VIEW_CLICKED");
        }
        else if(eventType == AccessibilityEvent.TYPE_VIEW_SELECTED){
            Utils.LogUtil("d","event","TYPE_VIEW_SELECTED");
        }
        else if(eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED){
            Utils.LogUtil("d","event","TYPE_VIEW_FOCUSED");
        }
        else if(eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED){
            Utils.LogUtil("d","event","TYPE_VIEW_TEXT_CHANGED");
        }else if(eventType == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER){
            Utils.LogUtil("d","event","TYPE_VIEW_HOVER_ENTER");
        }else if(eventType == AccessibilityEvent.TYPE_VIEW_HOVER_EXIT){
            Utils.LogUtil("d","event","TYPE_VIEW_HOVER_EXIT");
        }else if(eventType == AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START){
            Utils.LogUtil("d","event","TYPE_TOUCH_EXPLORATION_GESTURE_START");
        }else if(eventType == AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END){
            Utils.LogUtil("d","event","TYPE_TOUCH_EXPLORATION_GESTURE_END");
        }else if(eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED){
            Utils.LogUtil("d","event","TYPE_VIEW_SCROLLED");
        }else if(eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
            Utils.LogUtil("d","event","TYPE_VIEW_TEXT_SELECTION_CHANGED");
        }else if(eventType == AccessibilityEvent.TYPE_ANNOUNCEMENT){
            Utils.LogUtil("d","event","TYPE_ANNOUNCEMENT");
        }else if(eventType == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED){
            Utils.LogUtil("d","event","TYPE_VIEW_ACCESSIBILITY_FOCUSED");
        }else if(eventType == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED){
            Utils.LogUtil("d","event","TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED");
        }
        else if(eventType == AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY){
            Utils.LogUtil("d","event","TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY");
        }
        else if(eventType == AccessibilityEvent.TYPE_GESTURE_DETECTION_START){
            Utils.LogUtil("d","event","TYPE_GESTURE_DETECTION_START");
        }
        else if(eventType == AccessibilityEvent.TYPE_GESTURE_DETECTION_END){
            Utils.LogUtil("d","event","TYPE_GESTURE_DETECTION_END");
        }
        else if(eventType == AccessibilityEvent.TYPE_TOUCH_INTERACTION_START){
            Utils.LogUtil("d","event","TYPE_TOUCH_INTERACTION_START");
        }
        else if(eventType == AccessibilityEvent.TYPE_TOUCH_INTERACTION_END){
            Utils.LogUtil("d","event","TYPE_TOUCH_INTERACTION_END");
        }
        else if(eventType == AccessibilityEvent.CONTENT_CHANGE_TYPE_UNDEFINED){
            Utils.LogUtil("d","event","CONTENT_CHANGE_TYPE_UNDEFINED");
        }
        else if(eventType == AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE){
            Utils.LogUtil("d","event","CONTENT_CHANGE_TYPE_SUBTREE");
        }
        else if(eventType == AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT){
            Utils.LogUtil("d","event","CONTENT_CHANGE_TYPE_TEXT");
        }
        else if(eventType == AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION){
            Utils.LogUtil("d","event","CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION");
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
        if (size > 1 && size % 2 == 0) {			//card页面由多个支付card
            performGlobal(nodeInfos,size-2);
            return;
        }
    }

    private void performGlobal(List<AccessibilityNodeInfo> nodeInfos,int index){
        String moneyStr = nodeInfos.get(index).getText().toString();
//		Utils.LogUtil("d", TAG, moneyStr);
        Utils.ToastUtil(mContext,"wx:"+moneyStr);
//        mService.performGlobalAction(GLOBAL_ACTION_BACK);
    }
}
