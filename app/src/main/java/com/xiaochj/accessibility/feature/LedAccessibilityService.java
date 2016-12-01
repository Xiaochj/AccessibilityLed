package com.xiaochj.accessibility.feature;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;

import com.xiaochj.accessibility.impl.OnBtRegisterListener;
import com.xiaochj.accessibility.impl.OnLedAccessibilityListener;
import com.xiaochj.accessibility.util.Utils;
import com.xiaochj.led.R;

import java.util.List;

public class LedAccessibilityService extends AccessibilityService implements OnLedAccessibilityListener {

	private static final String TAG = "accessibilityservice";
	private static final String COLON = ":";
	private KeyguardManager km;
	private KeyguardManager.KeyguardLock kl;
	private PowerManager pm;
	private PowerManager.WakeLock pl;
	private BluetoothConnection btc;
	private Context context;
	private BroadcastReceiver receiver = null;

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
	public void onDestroy() {
		super.onDestroy();
		//凡注册广播
		if(receiver != null){
			context.unregisterReceiver(receiver);
		}
		//蓝牙没关,停止搜索
		btc.stopDiscovery();
	}

	@Override
	public void onBtNotSupportListener() {
		Utils.ToastUtil(context,context.getString(R.string.bluetooth_not_support));
	}

	@Override
	public void onBtNotOpenListener() {
		//open bluetooth
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(enableBtIntent);
	}

	@Override
	public void registerBtReceiver(BroadcastReceiver receiver,OnBtRegisterListener listener) {
		this.receiver = receiver;
		IntentFilter intent = new IntentFilter();
		intent.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备
		intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
		intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行动扫描模式改变了
		intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//动作状态发生了变化
		context.registerReceiver(receiver, intent);
	}

	@Override
	public void onWriteDialogMac() {
		//如果是第一次打开app,那就弹出输入框,键入led的蓝牙地址
		if(Utils.getSpFirstApp(context)) {
			View etView = LayoutInflater.from(context).inflate(R.layout.edit_addr_layout,null);
			etView.findViewById(R.id.six).findViewById(R.id.colon).setVisibility(View.GONE);
			final EditText et1 = (EditText)etView.findViewById(R.id.one).findViewById(R.id.edit);
			final EditText et2 = (EditText)etView.findViewById(R.id.two).findViewById(R.id.edit);
			final EditText et3 = (EditText)etView.findViewById(R.id.three).findViewById(R.id.edit);
			final EditText et4 = (EditText)etView.findViewById(R.id.four).findViewById(R.id.edit);
			final EditText et5 = (EditText)etView.findViewById(R.id.five).findViewById(R.id.edit);
			final EditText et6 = (EditText)etView.findViewById(R.id.six).findViewById(R.id.edit);
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(context.getString(R.string.bluetooth_tip)).setView(etView)
					.setPositiveButton(context.getString(R.string.bluetooth_ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, int which) {
							StringBuffer sb = new StringBuffer();
							sb.append(et1.getText().toString().trim()).append(COLON)
									.append(et2.getText().toString().trim()).append(COLON)
									.append(et3.getText().toString().trim()).append(COLON)
									.append(et4.getText().toString().trim()).append(COLON)
									.append(et5.getText().toString().trim()).append(COLON)
									.append(et6.getText().toString().trim());
							String macStr = sb.toString().trim();
							if (!"".equals(macStr)) {
								Utils.setSpFirstApp(context,false);//记录第一次app
								Utils.setSpMacAddr(context,macStr);//保存macAddr
								btc.bluetoothOpenAndRegister(macStr);//打开和注册蓝牙
							}

						}
					});
			Dialog dialog = builder.create();
			dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			dialog.show();
		}else{
			//如果不是第一次
			if(!"".equalsIgnoreCase(Utils.getSpMacAddr(context)))
				btc.bluetoothOpenAndRegister(Utils.getSpMacAddr(context));
		}
	}

	@Override
	public void onNotBltAddress() {
		Utils.ToastUtil(context,context.getString(R.string.bluetooth_true_address));
		Utils.setSpFirstApp(context,true);
		Utils.setSpMacAddr(context,"");
	}
}
