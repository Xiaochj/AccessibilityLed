package com.xiaochj.accessibility.impl;

import android.content.BroadcastReceiver;

/**
 * Created by xiaochj on 16/11/24.
 */

public interface OnLedAccessibilityListener {

    void onBtNotSupportListener();
    void onBtNotOpenListener();
    void onWriteDialogMac();
    void registerBtReceiver(BroadcastReceiver receiver, OnBtRegisterListener btRegisterListener);
}
