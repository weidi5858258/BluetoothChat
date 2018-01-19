package com.weidi.bluetoothchat.controller.bluetoothcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;


import com.weidi.bluetoothchat.BTApplication;
import com.weidi.bluetoothchat.Constant;
import com.weidi.bluetoothchat.controller.activitycontroller.ChatActivityController;
import com.weidi.log.Log;
import com.weidi.bluetoothchat.modle.MessageBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import static com.weidi.bluetoothchat.controller.activitycontroller.DevicesActivityController
        .getCanSaveMyMsgType;
import static com.weidi.bluetoothchat.controller.activitycontroller.DevicesActivityController
        .getCanSaveOtherMsgType;

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
    private ChatActivityController.ChatHandler msgHandler;
    private long msgSendTime = 0;
    private long msgReceiveTime = 0;
    private BufferedReader mReceiveMsgBufferedReader;

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
            if (mBTClient.mReceiveMsgBufferedReader != null) {
                mBTClient.mReceiveMsgBufferedReader.close();
                mBTClient.mReceiveMsgBufferedReader = null;
            }
            if (mBTClient.mRemoteBluetoothSocket != null) {
                mBTClient.mRemoteBluetoothSocket.close();
                mBTClient.mRemoteBluetoothSocket = null;
            }
            mBTClient.mRemoteBluetoothDevice = null;
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

    public void setHandler(ChatActivityController.ChatHandler handler) {
        msgHandler = handler;
    }

    public void connect(BluetoothDevice bluetoothDevice){
        if(bluetoothDevice == null){
            return;
        }
//        BTController.getInstance()
//                .getBluetoothAdapter()
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

    public long getMsgSendTime() {
        return msgSendTime;
    }

    public long getMsgReceiveTime() {
        return msgReceiveTime;
    }

    /**
     * 发送文本消息
     *
     * @param message
     * @param isForward 是否转发的,true表示转发的
     */
    public void sendMessage(String message, boolean isForward) {
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
                msgSendTime = SystemClock.uptimeMillis();
                int canSaveMyMsgType = getCanSaveMyMsgType();
                int canSaveOtherMsgType = getCanSaveOtherMsgType();
                stringBuilder.append(BTController.getInstance().getLocalBluetoothName());
                stringBuilder.append(Constant.SEPARATOR);
                stringBuilder.append(BTController.getInstance().getLocalBluetoothAdress());
                stringBuilder.append(Constant.SEPARATOR);
                stringBuilder.append(msgSendTime);
                stringBuilder.append(Constant.SEPARATOR);
                stringBuilder.append(message);
                stringBuilder.append("\n");
                mOutputStream.write(stringBuilder.toString().getBytes("utf-8"));
                mOutputStream.flush();

                if (msgHandler != null && !isForward) {
                    MessageBean msgBean = new MessageBean();
                    msgBean.msgSender = BTController.getInstance().getLocalBluetoothName();
                    msgBean.msgBTAddress = BTController.getInstance().getLocalBluetoothAdress();
                    msgBean.msgSendTime = msgSendTime;
                    msgBean.msgContent = message;
                    msgBean.isReceiveMSg = false;
                    msgBean.msgHomeOwnership =
                            Constant.MSG_HOME_OWNERSHIP_MY;
                    Message showMsg = msgHandler.obtainMessage();
                    showMsg.what = 0;
                    showMsg.obj = msgBean;
                    msgHandler.sendMessage(showMsg);

                    if (((BTApplication) (msgHandler.getContext())
                            .getApplicationContext())
                            .getConnectionType() == Constant.CS) {

                        BTServer.getInstance().sendMessageAll(msgBean, true);
                    }
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
     * 发送文本消息
     *
     * @param messageBean
     * @param isForward   是否转发的,true表示转发的
     */
    public void sendMessage(MessageBean messageBean, boolean isForward) {
        if (getRemoteBluetoothSocket() == null
                || !getRemoteBluetoothSocket().isConnected()
                || messageBean == null) {
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
                stringBuilder.append(messageBean.msgSender);
                stringBuilder.append(Constant.SEPARATOR);
                stringBuilder.append(messageBean.msgBTAddress);
                stringBuilder.append(Constant.SEPARATOR);
                stringBuilder.append(messageBean.msgSendTime);
                stringBuilder.append(Constant.SEPARATOR);
                stringBuilder.append(messageBean.msgContent);
                stringBuilder.append("\n");
                mOutputStream.write(stringBuilder.toString().getBytes("utf-8"));
                mOutputStream.flush();

                if (msgHandler != null && !isForward) {
                    MessageBean msgBean = new MessageBean();
                    msgBean.msgContent = messageBean.msgContent;
                    msgBean.isReceiveMSg = false;
                    msgBean.msgHomeOwnership =
                            Constant.MSG_HOME_OWNERSHIP_MY;
                    Message showMsg = msgHandler.obtainMessage();
                    showMsg.what = 0;
                    showMsg.obj = msgBean;
                    msgHandler.sendMessage(showMsg);
                }
                Log.d(TAG, "sendMessage = " + messageBean.msgContent);
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
     * 发送文件
     */
    public void sendMessageOfFile(String filePath) {
        if (getRemoteBluetoothSocket() == null
                || !getRemoteBluetoothSocket().isConnected()
                || TextUtils.isEmpty(filePath)) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "客户端sendMessage()方法中出现条件不满足!");
                mIRemoteConnection.onConnected(false);
            }
            return;
        }
        try {
            OutputStream outputStream = getRemoteBluetoothSocket().getOutputStream();
            if (outputStream == null) {
                return;
            }
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            if (file.isDirectory()) {
                return;
            }
            if (!file.canRead()) {
                return;
            }
            //1.发送文件信息实体类
            outputStream.write("file".getBytes("utf-8"));
            // 将文件写入流
            FileInputStream fileInputStream = new FileInputStream(file);
            // 每次上传2M的内容
            byte[] bt = new byte[2048];
            int length = -1;
            int fileSize = 0;//实时监测上传进度
            while ((length = fileInputStream.read(bt)) != -1) {
                fileSize += length;
                // 2.把文件写入socket输出流
                outputStream.write(bt, 0, length);
                Log.i(TAG, "文件上传进度：" + (fileSize / file.length() * 100) + "%");
            }
            outputStream.flush();
            outputStream.close();
            // 关闭文件流
            fileInputStream.close();
            outputStream = null;
            fileInputStream = null;
            file = null;

            //该方法无效
            //outputStream.write("\n".getBytes());
        } catch (IOException e) {
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
            mReceiveMsgBufferedReader = new BufferedReader(
                    new InputStreamReader(mInputStream));
            String receiveMessage = null;

            while (isReceiveMsg) {
                Log.d(TAG, "客户端进入while循环,一直监听消息的到来");
                if (getRemoteBluetoothSocket() != null
                        && getRemoteBluetoothSocket().isConnected()
                        && isReceiveMsg) {
                    while (mReceiveMsgBufferedReader != null
                            && (receiveMessage = mReceiveMsgBufferedReader.readLine()) != null) {
                        if (receiveMessage.contains(Constant.SEPARATOR)) {
                            String[] msg = receiveMessage.split(Constant.SEPARATOR);
                            if (msg != null && msg.length == 4) {

                                String rSender = msg[0];
                                String rAddress = msg[1];
                                long rTime = Long.parseLong(msg[2]);
                                msgReceiveTime = rTime;
                                String rMsg = msg[3];

                                if (msgHandler != null
                                        && msgSendTime != rTime
                                        && !BTController.getInstance()
                                        .getLocalBluetoothAdress().equals(rAddress)) {

                                    if (BTServer.getInstance().getMsgSendTime() != rTime
                                            && !BTController.getInstance()
                                            .getLocalBluetoothAdress().equals(rAddress)) {

                                        Log.d(TAG, "receiveMessage()1:msg = " + rMsg);
                                        MessageBean msgBean = new MessageBean();
                                        msgBean.msgSender = rSender;
                                        msgBean.msgBTAddress = rAddress;
                                        msgBean.msgSendTime = rTime;
                                        msgBean.msgContent = rMsg;
                                        msgBean.isReceiveMSg = true;
                                        msgBean.msgHomeOwnership =
                                                Constant.MSG_HOME_OWNERSHIP_OTHER;
                                        Message showMsg = msgHandler.obtainMessage();
                                        showMsg.what = 0;
                                        showMsg.obj = msgBean;

                                        if (msgReceiveTime !=
                                                BTServer.getInstance().getMsgReceiveTime()) {

                                            msgHandler.sendMessage(showMsg);
                                        }

                                        if (((BTApplication) (msgHandler.getContext())
                                                .getApplicationContext())
                                                .getConnectionType() == Constant.CS) {

                                            BTServer.getInstance().sendMessageAll(msgBean, true);
                                        }
                                    }
                                } else {
                                    Log.d(TAG, "receiveMessage()2:msg = " + receiveMessage);
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
