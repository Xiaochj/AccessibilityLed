package com.bobo.accessbility;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.accessibilitydemo.R;

import java.util.List;

public class WindowAccessibilitySeivice extends AccessibilityService {

	private static final String TAG = "accessibilityservice";
	private KeyguardManager km;
	private KeyguardManager.KeyguardLock kl;
	private PowerManager pm;
	private PowerManager.WakeLock pl;
	private boolean isNotify = false;

	public boolean isNotify() {
		return isNotify;
	}

	public void setNotify(boolean notify) {
		isNotify = notify;
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
	}

	//后台推送点开微信:typenotification-typestate-typescrolled-typecontent-typefocused-typecontent...
	//开着微信支付页面:typecontent...-typescrolled-typecontent...
	//开着微信其他页面:typecontent-typenotification-typecontent...-typestate-typecontent...-typescrolled-typefocused-typecontent...
	@SuppressLint("NewApi")
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int eventType = event.getEventType();
		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {//当微信在后台运行,一个push推送过来,调通知的event
			setNotifyChanged(event);
		} else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {//打开微信之后,调用窗口样式改变的event
//			if(isNotify()) {
				getSaleNumber(event);
//			}
		}/*else if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {//在支付card页面停留,页面滑动,出现了新的支付card
			if(!isNotify()) {
				getSaleNumber(event);
			}
		}else if(eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED){//从后台或其他页面通过push打开微信,会走这个event,card页面不会
			if(isNotify()){
				setNotify(false);
			}
		}*/
		else if(eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
//			if(!isNotify()){
				performGlobalAction(GLOBAL_ACTION_BACK);
//				setNotify(false);
//			}
		}
	}

	public void setNotifyChanged(AccessibilityEvent event){
		List<CharSequence> texts = event.getText();
		if(!texts.isEmpty()) {
			for (CharSequence text : texts) {
				if (text.toString().contains(getString(R.string.notify_wx))){
					if(event.getParcelableData() != null && event.getParcelableData() instanceof Notification){
						//是从notify进来的
						if(!isNotify()) {
							setNotify(true);
						}
						//唤醒解锁屏幕
						wakeAndUnlock();
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

	public void getSaleNumber(AccessibilityEvent event){
		//如果是微信的launcher页面且是打开微信
		if(event.getPackageName().toString().equalsIgnoreCase(getString(R.string.package_wx))) {
			List<AccessibilityNodeInfo> nodeInfos = getRootInActiveWindow().findAccessibilityNodeInfosByText("￥");
			int size = nodeInfos.size();
			if (size == 0)
				return;
			if (size == 1) {			//card页面是清空状态
				performGlobal(nodeInfos,0);
				return;
			}if (size > 1) {			//card页面由多个支付card
				performGlobal(nodeInfos,size-1);
				return;
			}
		}
	}

	private void performGlobal(List<AccessibilityNodeInfo> nodeInfos,int index){
		Utils.LogUtil("d", TAG, nodeInfos.get(index).getText().toString());
		performGlobalAction(GLOBAL_ACTION_BACK);
	}

	/**
	 * 监听屏幕唤醒和屏幕锁
	 */
	public void wakeAndUnlock(){
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		boolean isKeyguardflag = km.isKeyguardLocked();
		boolean isPowerflag = pm.isScreenOn();
		//如果屏幕灭了,就唤醒,否则不动
		if(!isPowerflag){
			pl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
			pl.acquire();
		}
		//如果锁屏就解锁,否则不动
		if (isKeyguardflag) {
			kl = km.newKeyguardLock("unLock");
			kl.disableKeyguard();
		}
	}

	@Override
	public void onInterrupt() {

	}



}
