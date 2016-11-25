package com.xiaochj.accessibility;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by xiaochj on 16/11/24.
 */

public class BluetoothConnection {
    private static final String TAG = "accessibilityservice";

    //设备唯一标识码
    private static final UUID UUID_LED = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static BluetoothConnection INSTANCE = null;
    private OnLedAccessibilityListener onListener;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;

    public BluetoothConnection(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothConnection getInstance(){
        if(INSTANCE == null) {
            synchronized (BluetoothConnection.class) {
                if(INSTANCE == null){
                    INSTANCE = new BluetoothConnection();
                }
            }
        }
        return INSTANCE;
    }

    public void connectLed(final OnLedAccessibilityListener onListener){
        this.onListener = onListener;
        if(mBluetoothAdapter == null){
            this.onListener.onBtNotSupportListener();
            return;
        }
        if(!mBluetoothAdapter.isEnabled()){
            this.onListener.onBtNotOpenListener();
            return;
        }
        //弹出一个dialog,用户输入led的mac地址,连接蓝牙
        onListener.onWriteDialogMac();
    }

    public void searchAndSendLed(String macAddr){
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddr);
        if(mBluetoothDevice != null){
            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID_LED);
                mBluetoothAdapter.cancelDiscovery();
            } catch (IOException e) {
                return;
            }
            new Thread(){
                @Override
                public void run() {
                    try {
                        mBluetoothSocket.connect();
                    } catch (IOException e) {
                        try {
                            mBluetoothSocket.close();
                        } catch (IOException e1) {
                            return;
                        }
                        return;
                    }
                }
            }.start();
        }
        Utils.LogUtil("d", TAG, String.valueOf(mBluetoothSocket.isConnected()));
    }

}
