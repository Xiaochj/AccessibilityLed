package com.xiaochj.accessibility.impl;

import android.bluetooth.BluetoothDevice;

/**
 * Created by xiaochj on 16/11/29.
 */

public interface OnBtRegisterListener {
    void unBondBtListener(BluetoothDevice device,String addr);
    void bondedBtListener(BluetoothDevice device,String addr);
}
