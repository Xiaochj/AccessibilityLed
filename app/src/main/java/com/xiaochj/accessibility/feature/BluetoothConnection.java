package com.xiaochj.accessibility.feature;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.xiaochj.accessibility.util.Utils;
import com.xiaochj.accessibility.impl.OnBtRegisterListener;
import com.xiaochj.accessibility.impl.OnLedAccessibilityListener;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by xiaochj on 16/11/24.
 */

public class BluetoothConnection implements OnBtRegisterListener {
    private static final String TAG = "accessibilityservice";

    //设备唯一标识码
    private static final UUID UUID_LED = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static BluetoothConnection INSTANCE = null;
    private OnLedAccessibilityListener onListener;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private String macStr;

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
        //如果手机不支持蓝牙,直接return
        if(mBluetoothAdapter == null){
            this.onListener.onBtNotSupportListener();
            return;
        }
        //弹出一个dialog,用户输入led的bluetooth地址
        onListener.onWriteDialogMac();

    }

//    private boolean isOurBondBlueTooth(){
//        //获得已配对的远程蓝牙设备的集合
//        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
//        if(devices.size()>0){
//            for(Iterator<BluetoothDevice> it = devices.iterator(); it.hasNext();){
//                BluetoothDevice device = (BluetoothDevice)it.next();
//                //如果远程蓝牙设备的物理地址和我们输入的匹配
//                if(device.getAddress().equalsIgnoreCase(macStr)){
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    public void bluetoothOpenAndRegister(String addr){
        this.macStr = addr;
        if(!mBluetoothAdapter.isEnabled()){
            //open bluetooth
            BluetoothConnection.this.onListener.onBtNotOpenListener();
        }
        //注册蓝牙回调广播
        onListener.registerBtReceiver(receiver,this);
        //search remote bluetooth
        searchBlueTooth();
    }

    private boolean searchBlueTooth(){
        //如果当前发现了新的设备，则停止继续扫描，当前扫描到的新设备会通过广播推向新的逻辑
        if (mBluetoothAdapter.isDiscovering()) {
            stopDiscovery();
        }
        //开始搜索
        mBluetoothAdapter.startDiscovery();
        return true;
    }

    public void stopDiscovery(){
        if(mBluetoothAdapter != null){
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    public void connectLed(String macAddr){
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

    /**
     * 蓝牙接收广播
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()){
                    case BluetoothDevice.BOND_NONE:
                        //如果没有配对,就配对
                        BluetoothConnection.this.unBondBtListener(device,macStr);
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        //如果已经配对,那进行连接
                        BluetoothConnection.this.bondedBtListener(device,macStr);
                        break;
                }
            }
        }

    };

    @Override
    public void unBondBtListener(BluetoothDevice device,String addr) {
        if (device.getAddress().equalsIgnoreCase(addr)) {
            //尝试配对
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                device.createBond();
            }
        }
    }

    @Override
    public void bondedBtListener(BluetoothDevice device,String addr) {
        if (device.getAddress().equalsIgnoreCase(macStr)) {
            connectLed(macStr);
        }
    }

}
