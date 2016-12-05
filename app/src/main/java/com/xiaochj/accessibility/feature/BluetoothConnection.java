package com.xiaochj.accessibility.feature;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.xiaochj.accessibility.application.LedApplication;
import com.xiaochj.accessibility.impl.OnBtRegisterListener;
import com.xiaochj.accessibility.impl.OnLedAccessibilityListener;
import com.xiaochj.accessibility.util.Utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by xiaochj on 16/11/24.
 */

public class BluetoothConnection implements OnBtRegisterListener {

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

    public void initBluetooth(final OnLedAccessibilityListener onListener){
        this.onListener = onListener;
        //如果手机不支持蓝牙,直接return
        if(mBluetoothAdapter == null){
            this.onListener.onBtNotSupportListener();
            return;
        }
        //弹出一个dialog,用户输入led的bluetooth地址
        onListener.onWriteDialogMac();

    }

    private boolean hasOurBondBlueTooth(){
        //获得已配对的远程蓝牙设备的集合
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if(devices.size()>0){
            for(Iterator<BluetoothDevice> it = devices.iterator(); it.hasNext();){
                BluetoothDevice device = (BluetoothDevice)it.next();
                //如果远程蓝牙设备的物理地址和我们输入的匹配
                if(device.getAddress().equalsIgnoreCase(macStr)){
                    return true;
                }
            }
        }
        return false;
    }

    public void bluetoothOpenAndRegister(String addr){
        //检测是不是标准的蓝牙地址
        if(!mBluetoothAdapter.checkBluetoothAddress(addr)){
            //不是的话,就恢复初始化状态(主要sp存储的数据)
            onListener.onNotBltAddress();
            //重新走一遍,输入地址
            onListener.onWriteDialogMac();
            return;
        }
        this.macStr = addr;
        //注册蓝牙回调广播
        onListener.registerBtReceiver(receiver,this);
        if(!mBluetoothAdapter.isEnabled()){
            //open bluetooth
            BluetoothConnection.this.onListener.onBtNotOpenListener();
        }else {
            //search remote bluetooth
            searchBlueTooth();
        }
    }

    private boolean searchBlueTooth(){
        //如果配对列表中有了,那么直接连接
        if(hasOurBondBlueTooth()){
            connectLed(macStr);
            return false;
        }
        //配对列表没有,如果当前发现了新的设备，则停止继续扫描，当前扫描到的新设备会通过广播推向新的逻辑
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

    public void connectLed(String macAddr) {
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddr);
        if (mBluetoothDevice != null) {
            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(LedApplication.UUID_LED);
                mBluetoothAdapter.cancelDiscovery();
            } catch (IOException e) {
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    try {
                        mBluetoothSocket.connect();
                    } catch (IOException e) {
                        try {
                            Class<?> clazz = mBluetoothDevice.getClass();
                            Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
                            Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                            Object[] params = new Object[] {Integer.valueOf(1)};
                            mBluetoothSocket = (BluetoothSocket) m.invoke(mBluetoothDevice, params);
                            mBluetoothSocket.connect();
                        } catch (Exception e1) {
                            Utils.LogUtil("d",LedApplication.TAG,"连接失败:"+e1.getMessage().toString());
                        }
                        Utils.LogUtil("d",LedApplication.TAG,"连接失败:"+e.getMessage().toString());
                    }
                }
            }.start();
        }
//        Utils.LogUtil("d", TAG, String.valueOf(mBluetoothSocket.isConnected()));
    }

    /**
     * 蓝牙接收广播
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //启动蓝牙,action会是action.changed那么直接搜索blt
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                searchBlueTooth();
            }else if (BluetoothDevice.ACTION_FOUND.equals(action)) {//如果是action.found,那么打开remote蓝牙列表
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
            }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){//如果配对,那连接
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDED:
                        //连接
                        BluetoothConnection.this.bondedBtListener(device, macStr);
                        break;
                }
            }
        }

    };

    @Override
    public void unBondBtListener(BluetoothDevice device,String addr) {
        if (device.getAddress().equalsIgnoreCase(addr)) {
            // 搜索蓝牙设备的过程占用资源比较多，一旦找到需要连接的设备后需要及时关闭搜索
            stopDiscovery();
            //尝试配对(Android api > 19 Android 4.4以上)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                device.createBond();
            }else{
                //跳到系统的蓝牙页面,手动配对
                onListener.onBtNotBondListener();
//                Utils.LogUtil("d", TAG, String.valueOf(Build.VERSION.SDK_INT));
            }
        }
    }

    @Override
    public void bondedBtListener(BluetoothDevice device,String addr) {
        if (device.getAddress().equalsIgnoreCase(macStr)) {
            // 搜索蓝牙设备的过程占用资源比较多，一旦找到需要连接的设备后需要及时关闭搜索
            stopDiscovery();
            //连接蓝牙
            connectLed(macStr);
        }
    }

}
