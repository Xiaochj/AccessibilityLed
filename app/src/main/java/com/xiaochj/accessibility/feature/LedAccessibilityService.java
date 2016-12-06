package com.xiaochj.accessibility.feature;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.xiaochj.accessibility.impl.OnBtRegisterListener;
import com.xiaochj.accessibility.impl.OnLedAccessibilityListener;
import com.xiaochj.accessibility.util.Utils;
import com.xiaochj.led.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaochj on 16/10/27.
 */

public class LedAccessibilityService extends AccessibilityService implements OnLedAccessibilityListener {

	private static final String COLON = ":";
	private BluetoothConnection btc;
	private Context context;
	private BroadcastReceiver receiver = null;
	private List<LinearLayout> etList = new ArrayList<LinearLayout>();

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		context = getApplicationContext();
		btc = BluetoothConnection.getInstance();
		btc.initBluetooth(this);
	}

	//后台推送点开微信:typenotification-typestate-typescrolled-typecontent-typefocused-typecontent...
	//开着微信支付页面:typecontent...-typescrolled-typecontent...
	//开着微信其他页面:typecontent-typenotification-typecontent...-typestate-typecontent...-typescrolled-typefocused-typecontent...

	//后台推送点开支付宝:typenotification

	//注意:wx和zfb的逻辑不同,wx的notification由于不含有支付金额数,顾必须点进去详情页进行读取,稍复杂。
	//而zfb则不同,由于notificaton自带支付金额,故直接读取金额数即可,稍简单。
	@SuppressLint("NewApi")
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int eventType = event.getEventType();
		String pkgName = event.getPackageName().toString();
		//wx
		if(pkgName.equalsIgnoreCase(getString(R.string.package_wx))) {
			if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {//一个push推送过来,调通知的event
				setNotifyChanged(event);
			} else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {//调用窗口样式改变的event
				getWxSaleNumber();
			} else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {//如果没退出微信,就一直调Action_Back返回事件
				if (!event.getPackageName().toString().equalsIgnoreCase(getString(R.string.android_launcher)))
					performGlobalAction(GLOBAL_ACTION_BACK);
			}
			//zfb
		}else if(pkgName.equalsIgnoreCase(getString(R.string.package_zfb))){
			if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {//一个push推送过来,调通知的event
				setNotifyChanged(event);
			}
		}
	}

	public void setNotifyChanged(AccessibilityEvent event){
		List<CharSequence> texts = event.getText();
		if(!texts.isEmpty()) {
			for (CharSequence text : texts) {
				String notifyStr = text.toString();
				//当微信或支付宝在后台运行
				if (notifyStr.contains(getString(R.string.notify_wx))){
					notifyChanged(event);
				}else if(notifyStr.contains(getString(R.string.notify_zfb))){
					String[] moneyStrList1 = notifyStr.split(getString(R.string.kuan_zfb));
					String[] moneyStrList2 = moneyStrList1[1].split(getString(R.string.yuan_zfb));
					String moneyStr = moneyStrList2[0];
					Utils.ToastUtil(context,"zfb:"+moneyStr);
				}
			}
		}
	}

	private void notifyChanged(AccessibilityEvent event) {
		if(event.getParcelableData() != null && event.getParcelableData() instanceof Notification){
            //唤醒解锁屏幕
            Utils.wakeAndUnlock(context);
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

	public void getWxSaleNumber(){
		//打开微信
		List<AccessibilityNodeInfo> nodeInfos = getRootInActiveWindow().findAccessibilityNodeInfosByText("￥");
		int size = nodeInfos.size();
		if (size == 0)
			return;
		if (size == 1) {			//card页面是清空状态
			performGlobal(nodeInfos,0);
			return;
		}
		if (size > 1) {			//card页面由多个支付card
			performGlobal(nodeInfos,size-1);
			return;
		}
	}

	private void performGlobal(List<AccessibilityNodeInfo> nodeInfos,int index){
		String moneyStr = nodeInfos.get(index).getText().toString();
//		Utils.LogUtil("d", TAG, moneyStr);
		Utils.ToastUtil(context,"wx:"+moneyStr);
		performGlobalAction(GLOBAL_ACTION_BACK);
	}

	@Override
	public void onInterrupt() {

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//反注册广播
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
	public void onBtNotBondListener() {
		Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
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
	public void onNotBltAddress() {
		Utils.ToastUtil(context,context.getString(R.string.bluetooth_true_address));
		Utils.setSpFirstApp(context,true);
		Utils.setSpMacAddr(context,"");
	}

	@Override
	public void onWriteDialogMac() {
		//如果是第一次打开app,那就弹出输入框,键入led的蓝牙地址
		if(Utils.getSpFirstApp(context)) {
			initDialogAndEditInDialog();
		}else{
			//如果不是第一次
			if(!"".equalsIgnoreCase(Utils.getSpMacAddr(context)))
				btc.bluetoothOpenAndRegister(Utils.getSpMacAddr(context));
		}
	}

	private void initDialogAndEditInDialog() {
		View etView = LayoutInflater.from(context).inflate(R.layout.edit_addr_layout,null);
		etView.findViewById(R.id.six).findViewById(R.id.colon).setVisibility(View.GONE);
		final EditText et1 = (EditText)etView.findViewById(R.id.one).findViewById(R.id.edit);
		final EditText et2 = (EditText)etView.findViewById(R.id.two).findViewById(R.id.edit);
		final EditText et3 = (EditText)etView.findViewById(R.id.three).findViewById(R.id.edit);
		final EditText et4 = (EditText)etView.findViewById(R.id.four).findViewById(R.id.edit);
		final EditText et5 = (EditText)etView.findViewById(R.id.five).findViewById(R.id.edit);
		final EditText et6 = (EditText)etView.findViewById(R.id.six).findViewById(R.id.edit);
		etList.add((LinearLayout) etView.findViewById(R.id.one));
		etList.add((LinearLayout) etView.findViewById(R.id.two));
		etList.add((LinearLayout) etView.findViewById(R.id.three));
		etList.add((LinearLayout) etView.findViewById(R.id.four));
		etList.add((LinearLayout) etView.findViewById(R.id.five));
		etList.add((LinearLayout) etView.findViewById(R.id.six));
		//生成一个dialog,输入remote蓝牙地址
		setAlertDialog(etView, et1, et2, et3, et4, et5, et6);
	}

	private void setAlertDialog(View etView, final EditText et1, final EditText et2, final EditText et3, final EditText et4, final EditText et5, final EditText et6) {
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
		final Dialog dialog = builder.create();
		//让每一个edittext都监听字符变化,输入了2个字符,那么光标自动聚焦到下一个edittext上
		for (int i = 0; i < etList.size(); i++) {
            //监听光标动作
            setTextChangedListener(dialog, i);
        }
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}

	private void setTextChangedListener(final Dialog dialog, int i) {
		((EditText)etList.get(i).findViewById(R.id.edit)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //限定2个字符
                if (s.length() == 2) {
                    //获取当前焦点的父控件
                    ViewParent viewParent = dialog.getCurrentFocus().getParent();
                    int nextIndex = -1;
                    //遍历查找是哪个控件
                    for (int i = 0; i < etList.size(); i++) {
                        if (etList.get(i) == viewParent) {
                            //将nexIndex加1
                            nextIndex = i + 1;
                            break;
                        }
                    }
                    if(nextIndex != -1) {
                        //如果nextIndex拿到的控件是edittext
                        if(nextIndex < 6) {
                            EditText nextEdit = (EditText) etList.get(nextIndex).findViewById(R.id.edit);
                            //使下一个控件获得焦点
                            nextEdit.requestFocus();
                        }else if(nextIndex == 6){
                            //如果到了最后一个edittext,那就拿到dialog的确认button,让其获得焦点,并隐藏软键盘
                            Button positiveButton=((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                            positiveButton.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(positiveButton.getWindowToken(), 0); //强制隐藏键盘
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
	}

}
