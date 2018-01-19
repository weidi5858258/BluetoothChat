package com.weidi.bluetoothchat.controller.bluetoothcontroller;

import android.bluetooth.BluetoothServerSocket;
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
import com.weidi.threadpool.ThreadPool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import static android.media.CamcorderProfile.get;
import static com.weidi.bluetoothchat.Constant.FIXEDTHREADPOOLCOUNT;

/**
 * Created by root on 16-12-16.
 */

/**
 * 现在只考虑某个设备作为服务端后,就不能再作为客户端去连接其他设备了
 */
public class BTServer {

    private static final String TAG = "BTServer";

    /* 一些常量，代表服务器的名称 */
    public static final String PROTOCOL_SCHEME_L2CAP = "btl2cap";
    public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
    public static final String PROTOCOL_SCHEME_BT_OBEX = "btgoep";
    public static final String PROTOCOL_SCHEME_TCP_OBEX = "tcpobex";

    private volatile static BTServer mBTServer = null;
    private BluetoothServerSocket mBluetoothServerSocket = null;
    private ArrayList<BluetoothSocket> btSocketList = null;
    private IRemoteConnection mIRemoteConnection = null;
    private boolean isAccept = true;
    private boolean isReceiveMsg = true;
    private ChatActivityController.ChatHandler msgHandler;
    private long msgSendTime = 0;
    private long msgReceiveTime = 0;
    private BufferedReader mReceiveMsgBufferedReader;

    public interface IRemoteConnection {

        void onConnected(BluetoothSocket socket, boolean isConnected);

    }

    private BTServer() {
        btSocketList = new ArrayList<BluetoothSocket>();
        isAccept = true;
        isReceiveMsg = true;
    }

    public static BTServer getInstance() {
        if (mBTServer == null) {
            synchronized (BTServer.class) {
                if (mBTServer == null) {
                    mBTServer = new BTServer();
                }
            }
        }
        return mBTServer;
    }

    public static void setBTServerToNull() {
        try {
            if (mBTServer.mReceiveMsgBufferedReader != null) {
                mBTServer.mReceiveMsgBufferedReader.close();
                mBTServer.mReceiveMsgBufferedReader = null;
            }
            if (mBTServer.btSocketList != null && mBTServer.btSocketList.size() > 0) {
                for (BluetoothSocket socket : mBTServer.btSocketList) {
                    socket.close();
                }
                mBTServer.btSocketList.clear();
                mBTServer.btSocketList = null;
            }
            if (mBTServer.mBluetoothServerSocket != null) {
                mBTServer.mBluetoothServerSocket.close();
                mBTServer.mBluetoothServerSocket = null;
            }
            mBTServer.mIRemoteConnection = null;
            mBTServer.isAccept = false;
            mBTServer.isReceiveMsg = false;
            mBTServer = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setIRemoteConnection(IRemoteConnection iRemoteConnection) {
        mIRemoteConnection = iRemoteConnection;
    }

    public ArrayList<BluetoothSocket> getBtSocketList() {
        return btSocketList;
    }

    public void removeClientBluetoothSocket(BluetoothSocket socket) {
        if (btSocketList != null) {
            if (btSocketList.contains(socket)) {
                btSocketList.remove(socket);
            }
        }
    }

    public boolean isAccept() {
        return isAccept;
    }

    public void setAccept(boolean accept) {
        isAccept = accept;
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

    public void accept() {
        Log.d(TAG, "服务端进入accept()方法");
        BluetoothSocket mBluetoothSocketWithClient = null;
        try {
            UUID uuid = UUID.fromString(BTController.BT_UUID);
            mBluetoothServerSocket = BTController.getInstance()
                    .getBluetoothAdapter()
                    .listenUsingRfcommWithServiceRecord(
                            PROTOCOL_SCHEME_RFCOMM,
                            uuid);
            if (mBluetoothServerSocket == null) {
                if (mIRemoteConnection != null) {
                    Log.d(TAG, "连接客户端时得到的BluetoothServerSocket对象为null!");
                    mIRemoteConnection.onConnected(mBluetoothSocketWithClient, false);
                }
                return;
            }
            while (isAccept) {
                Log.d(TAG, "本机已作为服务端正在等待客户端的连接...");
                mBluetoothSocketWithClient = mBluetoothServerSocket.accept();
                if (mBluetoothSocketWithClient != null) {
                    btSocketList.add(mBluetoothSocketWithClient);
                }
                if (mIRemoteConnection != null) {
                    Log.d(TAG, "mBluetoothSocketWithClient = " + mBluetoothSocketWithClient);
                    mIRemoteConnection.onConnected(mBluetoothSocketWithClient, true);
                }
            }
        } catch (Exception e) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "服务端accept()方法中出现异常!");
                mIRemoteConnection.onConnected(mBluetoothSocketWithClient, false);
            }
            e.printStackTrace();
        }
    }

    public void disConnect(BluetoothSocket socket) {
        try {
            if (socket == null) {
                return;
            }
            if (btSocketList == null || btSocketList.size() == 0
                    || !btSocketList.contains(socket)) {
                return;
            }
            socket.close();
            if (mIRemoteConnection != null) {
                Log.d(TAG, "已与客户端断开连接");
                mIRemoteConnection.onConnected(socket, false);
            }
        } catch (Exception e) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "与客户端断开连接时发生异常");
                mIRemoteConnection.onConnected(socket, false);
            }
            e.printStackTrace();
        }
    }

    public void disConnectAll() {
        BluetoothSocket socket = null;
        try {
            if (btSocketList != null && btSocketList.size() > 0) {
                int count = btSocketList.size();
                for (int i = 0; i < count; ++i) {
                    socket = btSocketList.get(i);
                    if (socket == null) {
                        continue;
                    }
                    socket.close();
                    if (mIRemoteConnection != null) {
                        Log.d(TAG, "已与客户端断开连接");
                        mIRemoteConnection.onConnected(socket, false);
                    }
                }
            }
        } catch (Exception e) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "与客户端断开连接时发生异常");
                mIRemoteConnection.onConnected(socket, false);
            }
            e.printStackTrace();
        }
    }

    public long getMsgSendTime() {
        return msgSendTime;
    }

    public long getMsgReceiveTime() {
        return msgReceiveTime;
    }

    /**
     * 发送文本消息
     * 对单个客户端发送消息
     * 在子线程中进行
     *
     * @param message
     */
    public void sendMessage(
            BluetoothSocket socket,
            String message,
            boolean isFirist,
            boolean isCS) {
        if (socket == null || !socket.isConnected() || TextUtils.isEmpty(message)) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "服务端sendMessage()方法中出现条件不满足!");
                mIRemoteConnection.onConnected(socket, false);
            }
            return;
        }
        try {
            OutputStream mOutputStream = socket.getOutputStream();
            if (mOutputStream != null) {
                StringBuilder stringBuilder = new StringBuilder();
                msgSendTime = SystemClock.uptimeMillis();
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

                if (msgHandler != null && isFirist && !isCS) {
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
                }
                Log.d(TAG, "sendMessage = " + message);
            }
        } catch (IOException e) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "服务端sendMessage()方法中出现异常!");
                mIRemoteConnection.onConnected(socket, false);
            }
            e.printStackTrace();
        }
    }

    /**
     * 发送文本消息
     * 对单个客户端发送消息
     * 在子线程中进行
     *
     * @param messageBean
     */
    public void sendMessage(
            BluetoothSocket socket,
            MessageBean messageBean,
            boolean isFirist,
            boolean isCS) {
        if (socket == null || !socket.isConnected() || messageBean == null) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "服务端sendMessage()方法中出现条件不满足!");
                mIRemoteConnection.onConnected(socket, false);
            }
            return;
        }
        try {
            OutputStream mOutputStream = socket.getOutputStream();
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

                if (msgHandler != null && isFirist && !isCS) {
                    MessageBean msgBean = new MessageBean();
                    msgBean.msgSender = messageBean.msgSender;
                    msgBean.msgBTAddress = messageBean.msgBTAddress;
                    msgBean.msgSendTime = msgSendTime;
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
                Log.d(TAG, "服务端sendMessage()方法中出现异常!");
                mIRemoteConnection.onConnected(socket, false);
            }
            e.printStackTrace();
        }
    }

    /**
     * 发送文本消息
     * 对单个客户端发送消息
     * 在子线程中进行
     *
     * @param message
     */
    public void sendMessage2(BluetoothSocket socket, String message) {
        if (socket == null || !socket.isConnected() || TextUtils.isEmpty(message)) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "服务端sendMessage2()方法中出现条件不满足!");
                mIRemoteConnection.onConnected(socket, false);
            }
            return;
        }
        try {
            OutputStream mOutputStream = socket.getOutputStream();
            if (mOutputStream != null) {
                message += "\n";
                mOutputStream.write(message.getBytes("utf-8"));
                mOutputStream.flush();
                Log.d(TAG, "sendMessage2 = " + message);
            }
        } catch (IOException e) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "服务端sendMessage2()方法中出现异常!");
                mIRemoteConnection.onConnected(socket, false);
            }
            e.printStackTrace();
        }
    }

    /**
     * 在子线程中进行
     *
     * @param message
     */
    public void sendMessageAll(String message, boolean isCS) {
        if (btSocketList == null || btSocketList.size() <= 0) {
            return;
        }
        int count = btSocketList.size();
        for (int i = 0; i < count; ++i) {
            BluetoothSocket socket = btSocketList.get(i);
            if (i != 0) {
                sendMessage(socket, message, false, isCS);
            } else {
                sendMessage(socket, message, true, isCS);
            }
        }
    }

    /**
     * 在子线程中进行
     *
     * @param msgBean
     */
    public void sendMessageAll(MessageBean msgBean, boolean isCS) {
        if (btSocketList == null || btSocketList.size() <= 0) {
            return;
        }
        int count = btSocketList.size();
        for (int i = 0; i < count; ++i) {
            BluetoothSocket socket = btSocketList.get(i);
            if (i != 0) {
                sendMessage(socket, msgBean, false, isCS);
            } else {
                sendMessage(socket, msgBean, true, isCS);
            }
        }
    }

    /**
     * 在子线程中进行
     *
     * @param message
     */
    public void sendMessageAll2(String message) {
        if (btSocketList == null || btSocketList.size() <= 0) {
            return;
        }
        int count = btSocketList.size();
        for (int i = 0; i < count; ++i) {
            BluetoothSocket socket = btSocketList.get(i);
            sendMessage2(socket, message);
        }
    }

    /**
     * 接收文本消息
     * 服务端跟客户端建立好连接后,应该开一个线程去不断接收这个客户端发来的消息
     * 每个客户端都要有一个线程
     */
    public void receiveMessage(BluetoothSocket socket) {
        if (socket == null || !socket.isConnected()) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "服务端receiveMessage()方法中出现条件不满足!");
                mIRemoteConnection.onConnected(socket, false);
            }
            return;
        }
        try {
            Log.d(TAG, "服务端准备接收消息");
            InputStream mInputStream = socket.getInputStream();
            // 从客户端获取信息
            mReceiveMsgBufferedReader = new BufferedReader(
                    new InputStreamReader(mInputStream));
            String receiveMessage = null;

            while (isReceiveMsg) {
                Log.d(TAG, "服务端进入while循环,一直监听消息的到来");
                if (socket != null
                        && socket.isConnected()
                        && isReceiveMsg) {
                    while (mReceiveMsgBufferedReader != null
                            && (receiveMessage = mReceiveMsgBufferedReader.readLine()) != null) {
                        if (receiveMessage.contains(Constant.SEPARATOR)) {
                            String[] msg = receiveMessage.split(Constant.SEPARATOR);
                            // receiveMessage():msg = GT-N7100@@####@@BC:20:A4:35:F0:A8@@####@@炸弹��
                            // 自己在聊天界面显示msg[1]消息
                            if (msg != null && msg.length == 4) {
                                String rSender = msg[0];
                                String rAddress = msg[1];
                                long rTime = Long.parseLong(msg[2]);
                                msgReceiveTime = rTime;
                                String rMsg = msg[3];

                                if (msgHandler != null
                                        && BTClient.getInstance().getMsgSendTime() != rTime
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
                                            BTClient.getInstance().getMsgReceiveTime()) {

                                        msgHandler.sendMessage(showMsg);
                                    }

                                    if (((BTApplication) (msgHandler.getContext())
                                            .getApplicationContext())
                                            .getConnectionType() == Constant.CS) {
                                        // 自己本身作为客户端
                                        BTClient.getInstance().sendMessage(msgBean, true);
                                    }
                                } else {
                                    Log.d(TAG, "receiveMessage()2:msg = " + receiveMessage);
                                }
                            }
                        }

                        // 这个服务端所控制的所有客户端
                        sendMessageAll2(receiveMessage);

                        receiveMessage = null;
                    }
                } else {
                    if (mIRemoteConnection != null) {
                        Log.d(TAG, "服务端receiveMessage()方法中接收消息时出现条件不满足!");
                        mIRemoteConnection.onConnected(socket, false);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "服务端receiveMessage()方法中出现异常!");
                mIRemoteConnection.onConnected(socket, false);
            }
            e.printStackTrace();
        }
    }

    public void receiveMessageAll() {
        if (btSocketList == null || btSocketList.size() <= 0) {
            return;
        }
        int count = btSocketList.size();
        for (int i = 0; i < count; ++i) {
            final BluetoothSocket socket = btSocketList.get(i);
            ThreadPool.getFixedThreadPool(FIXEDTHREADPOOLCOUNT).execute(new Runnable() {
                @Override
                public void run() {
                    receiveMessage(socket);
                }
            });
        }
    }


}
