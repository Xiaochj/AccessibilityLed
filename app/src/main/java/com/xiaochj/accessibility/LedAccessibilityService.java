package com.xiaochj.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.os.PowerManager;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;

import com.xiaochj.led.R;

import java.util.List;

public class LedAccessibilityService extends AccessibilityService implements OnLedAccessibilityListener{

	private static final String TAG = "accessibilityservice";
	private KeyguardManager km;
	private KeyguardManager.KeyguardLock kl;
	private PowerManager pm;
	private PowerManager.WakeLock pl;
	private BluetoothConnection btc;
	private Context context;

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		context = getApplicationContext();
		btc = BluetoothConnection.getInstance();
		btc.connectLed(this);
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
				getSaleNumber(event);
		} else if(eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){//如果没退出微信,就一直调Action_Back返回事件
			if(!event.getPackageName().toString().equalsIgnoreCase(getString(R.string.android_launcher)))
				performGlobalAction(GLOBAL_ACTION_BACK);
		}
	}

	public void setNotifyChanged(AccessibilityEvent event){
		List<CharSequence> texts = event.getText();
		if(!texts.isEmpty()) {
			for (CharSequence text : texts) {
				if (text.toString().contains(getString(R.string.notify_wx))){
					if(event.getParcelableData() != null && event.getParcelableData() instanceof Notification){
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
		String moneyStr = nodeInfos.get(index).getText().toString();
//		Utils.LogUtil("d", TAG, moneyStr);
		Utils.ToastUtil(context,moneyStr);
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


	@Override
	public void onBtNotSupportListener() {
		Utils.ToastUtil(context,context.getString(R.string.bluetooth_notsupport));
	}

	@Override
	public void onBtNotOpenListener() {
		Utils.ToastUtil(context,context.getString(R.string.bluetooth_notopen));
	}

	@Override
	public void onWriteDialogMac() {
		final EditText et = new EditText(context);
		et.setText("CC:A2:23:D6:E0:16");
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getString(R.string.bluetooth_tip)).setView(et)
				.setPositiveButton(context.getString(R.string.bluetooth_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, int which) {
				new Thread(){
					@Override
					public void run() {
						String macStr = et.getText().toString().trim();
						if(!"".equals(macStr)){
							btc.searchAndSendLed(macStr);
						}
					}
				}.start();
			}
		});
		Dialog dialog = builder.create();
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}
}
