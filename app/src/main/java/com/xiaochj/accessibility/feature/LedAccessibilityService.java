package com.xiaochj.accessibility.feature;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;

import com.xiaochj.led.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaochj on 16/10/27.
 */

public class LedAccessibilityService extends AccessibilityService {

	private static final String COLON = ":";
	private Context context;
	private BroadcastReceiver receiver = null;
	private List<LinearLayout> etList = new ArrayList<LinearLayout>();
	private AppForWx mAppForWx;
	private AppForZfb mAppForZfb;

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		context = getApplicationContext();
		mAppForWx = new AppForWx(context,this);
		mAppForZfb = new AppForZfb(context,this);
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
			mAppForWx.setAccessibilityEvent(eventType,event);
			//zfb
		}else if(pkgName.equalsIgnoreCase(getString(R.string.package_zfb))){
			mAppForZfb.setAccessibilityEvent(eventType,event);
		}
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
	}

	public interface OnDiffAppAccEventListener {

		void setAccessibilityEvent(int eventType, AccessibilityEvent event);
	}

}
