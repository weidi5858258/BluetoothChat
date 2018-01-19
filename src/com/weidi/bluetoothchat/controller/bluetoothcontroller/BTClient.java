package com.weidi.bluetoothchat.controller.bluetoothcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;


import com.weidi.bluetoothchat.Constant;
import com.weidi.log.Log;
import com.weidi.bluetoothchat.modle.MessageBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by root on 16-12-16.
 */

public class BTClient {

    private static final String TAG = "BTClient";

    private volatile static BTClient mBTClient = null;
    private static BluetoothDevice mRemoteBluetoothDevice = null;
    private static BluetoothSocket mRemoteBluetoothSocket = null;
    private IRemoteConnection mIRemoteConnection = null;
    private boolean isReceiveMsg = true;
    private Handler msgHandler;

    public interface IRemoteConnection {

        void onConnected(boolean isConnected);

    }

    private BTClient() {
        isReceiveMsg = true;
    }

    public static BTClient getInstance() {
        if (mBTClient == null) {
            synchronized (BTClient.class) {
                if (mBTClient == null) {
                    mBTClient = new BTClient();
                }
            }
        }
        return mBTClient;
    }

    public static void setBTClientToNull() {
        try {
            mBTClient.mRemoteBluetoothDevice = null;
            if (mBTClient.mRemoteBluetoothSocket != null) {
                mBTClient.mRemoteBluetoothSocket.close();
                mBTClient.mRemoteBluetoothSocket = null;
            }
            mBTClient.mIRemoteConnection = null;
            mBTClient.isReceiveMsg = false;
            mBTClient = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setIRemoteConnection(IRemoteConnection iRemoteConnection) {
        mIRemoteConnection = iRemoteConnection;
    }

    public IRemoteConnection getIRemoteConnection() {
        return mIRemoteConnection;
    }

    /**
     * 跟服务端是否连接着
     *
     * @return
     */
    public boolean isConnected() {
        boolean isConnected = false;
        if (getRemoteBluetoothSocket() != null) {
            isConnected = getRemoteBluetoothSocket().isConnected();
        }
        return isConnected;
    }

    public void setRemoteBluetoothDevice(BluetoothDevice device) {
        mRemoteBluetoothDevice = device;
    }

    public BluetoothDevice getRemoteBluetoothDevice() {
        return mRemoteBluetoothDevice;
    }

    public void setRemoteBluetoothSocket(BluetoothSocket socket) {
        mRemoteBluetoothSocket = socket;
    }

    public BluetoothSocket getRemoteBluetoothSocket() {
        return mRemoteBluetoothSocket;
    }

    public boolean isReceiveMsg() {
        return isReceiveMsg;
    }

    public void setReceiveMsg(boolean receiveMsg) {
        isReceiveMsg = receiveMsg;
    }

    public void setHandler(Handler handler) {
        msgHandler = handler;
    }

    public void connect(String remoteAddress) {
        if (TextUtils.isEmpty(remoteAddress)) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "对方设备的地址不可用!");
                mIRemoteConnection.onConnected(false);
            }
            return;
        }
        try {
            mRemoteBluetoothDevice = BTController.getInstance()
                    .getBluetoothAdapter()
                    .getRemoteDevice(remoteAddress);
            if (mRemoteBluetoothDevice == null) {
                if (mIRemoteConnection != null) {
                    Log.d(TAG, "连接远程设备时得到的远程设备对象为null!");
                    mIRemoteConnection.onConnected(false);
                }
                return;
            }
            UUID uuid = UUID.fromString(BTController.BT_UUID);
            mRemoteBluetoothSocket =
                    mRemoteBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            mRemoteBluetoothSocket.connect();
            if (mIRemoteConnection != null) {
                Log.d(TAG, "mRemoteBluetoothSocket = " + mRemoteBluetoothSocket);
                mIRemoteConnection.onConnected(true);
            }
        } catch (Exception e) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "connect()方法出现异常!");
                mIRemoteConnection.onConnected(false);
            }
            e.printStackTrace();
        }
    }

    public void disConnect() {
        try {
            if (mRemoteBluetoothSocket != null) {
                mRemoteBluetoothSocket.close();
                if (mIRemoteConnection != null) {
                    Log.d(TAG, "已与服务端断开连接");
                    mIRemoteConnection.onConnected(false);
                }
            }
        } catch (Exception e) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "与服务端断开连接时发生异常");
                mIRemoteConnection.onConnected(false);
            }
            e.printStackTrace();
        }
        mRemoteBluetoothSocket = null;
    }

    /**
     * 发送文本消息
     *
     * @param message
     */
    public void sendMessage(String message) {
        if (getRemoteBluetoothSocket() == null
                || !getRemoteBluetoothSocket().isConnected()
                || TextUtils.isEmpty(message)) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "客户端sendMessage()方法中出现条件不满足!");
                mIRemoteConnection.onConnected(false);
            }
            return;
        }
        try {
            OutputStream mOutputStream = getRemoteBluetoothSocket().getOutputStream();
            if (mOutputStream != null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(BTController.getInstance().getLocalBluetoothName());
                stringBuilder.append(Constant.SEPARATOR);
                stringBuilder.append(BTController.getInstance().getLocalBluetoothAdress());
                stringBuilder.append(Constant.SEPARATOR);
                stringBuilder.append(message);
                stringBuilder.append("\n");
                mOutputStream.write(stringBuilder.toString().getBytes("utf-8"));
                mOutputStream.flush();

                if (msgHandler != null) {
                    MessageBean msgBean = new MessageBean();
                    msgBean.msgContent = message;
                    msgBean.isReceiveMSg = false;
                    msgBean.msgHomeOwnership =
                            Constant.MSG_HOME_OWNERSHIP_MY;
                    Message showMsg = msgHandler.obtainMessage();
                    showMsg.what = 0;
                    showMsg.obj = msgBean;
                    msgHandler.sendMessage(showMsg);
                }
                Log.d(TAG, "sendMessage = " + message);
            }
        } catch (IOException e) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "客户端sendMessage()方法中出现异常!");
                mIRemoteConnection.onConnected(false);
            }
            e.printStackTrace();
        }
    }

    /**
     * 接收文本消息
     */
    public void receiveMessage() {
        if (getRemoteBluetoothSocket() == null || !getRemoteBluetoothSocket().isConnected()) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "客户端receiveMessage()方法中出现条件不满足!");
                mIRemoteConnection.onConnected(false);
            }
            return;
        }
        try {
            Log.d(TAG, "客户端准备接收消息");
            InputStream mInputStream = getRemoteBluetoothSocket().getInputStream();
            // 从客户端获取信息
            BufferedReader mBufferedReader = new BufferedReader(new InputStreamReader
                    (mInputStream));
            String receiveMessage = null;

            while (isReceiveMsg) {
                Log.d(TAG, "客户端进入while循环,一直监听消息的到来");
                if (mBufferedReader != null
                        && getRemoteBluetoothSocket() != null
                        && getRemoteBluetoothSocket().isConnected()
                        && isReceiveMsg) {
                    while ((receiveMessage = mBufferedReader.readLine()) != null) {
                        if (receiveMessage.contains(Constant.SEPARATOR)) {
                            String[] msg = receiveMessage.split(Constant.SEPARATOR);
                            if (msg != null && msg.length == 3) {
                                if (!BTController.getInstance().getLocalBluetoothAdress()
                                        .equals(msg[1])) {
                                    String rMsg = msg[2];
                                    Log.d(TAG, "receiveMessage():msg = " + rMsg);
                                    // do something
                                    if (msgHandler != null) {
                                        MessageBean msgBean = new MessageBean();
                                        msgBean.msgSender = msg[0];
                                        msgBean.msgBTAdress = msg[1];
                                        msgBean.msgContent = msg[2];
                                        msgBean.isReceiveMSg = true;
                                        msgBean.msgHomeOwnership =
                                                Constant.MSG_HOME_OWNERSHIP_OTHER;
                                        Message showMsg = msgHandler.obtainMessage();
                                        showMsg.what = 0;
                                        showMsg.obj = msgBean;
                                        msgHandler.sendMessage(showMsg);
                                    }
                                } else {
                                    Log.d(TAG, "receiveMessage():msg = " + receiveMessage);
                                }
                            }
                            msg = null;
                            receiveMessage = null;
                        }
                    }
                } else {
                    if (mIRemoteConnection != null) {
                        Log.d(TAG, "客户端receiveMessage()方法中接收消息时出现条件不满足!");
                        mIRemoteConnection.onConnected(false);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "客户端receiveMessage()方法中出现异常!");
                mIRemoteConnection.onConnected(false);
            }
            e.printStackTrace();
        }
    }

}
