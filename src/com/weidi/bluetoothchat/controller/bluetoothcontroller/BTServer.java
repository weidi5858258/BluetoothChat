package com.weidi.bluetoothchat.controller.bluetoothcontroller;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.weidi.bluetoothchat.Constant;
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
    private Handler msgHandler;

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
            if (mBTServer.mBluetoothServerSocket != null) {
                mBTServer.mBluetoothServerSocket.close();
                mBTServer.mBluetoothServerSocket = null;
            }
            if (mBTServer.btSocketList != null && mBTServer.btSocketList.size() > 0) {
                for (BluetoothSocket socket : mBTServer.btSocketList) {
                    socket.close();
                }
                mBTServer.btSocketList.clear();
                mBTServer.btSocketList = null;
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

    public void setHandler(Handler handler) {
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

    /**
     * 发送文本消息
     * 对单个客户端发送消息
     * 在子线程中进行
     *
     * @param message
     */
    public void sendMessage(BluetoothSocket socket, String message) {
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
                Log.d(TAG, "sendMessage = " + stringBuilder.toString());
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
    public void sendMessageAll(String message) {
        if (btSocketList == null || btSocketList.size() <= 0) {
            return;
        }
        int count = btSocketList.size();
        for (int i = 0; i < count; ++i) {
            BluetoothSocket socket = btSocketList.get(i);
            sendMessage(socket, message);
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
            BufferedReader mBufferedReader = new BufferedReader(
                    new InputStreamReader(mInputStream));
            String receiveMessage = null;

            while (isReceiveMsg) {
                Log.d(TAG, "服务端进入while循环,一直监听消息的到来");
                if (mBufferedReader != null
                        && socket != null
                        && socket.isConnected()
                        && isReceiveMsg) {
                    while ((receiveMessage = mBufferedReader.readLine()) != null) {
                        if (receiveMessage.contains(Constant.SEPARATOR)) {
                            Log.d(TAG, "receiveMessage():msg = " + receiveMessage);
                            String[] msg = receiveMessage.split(Constant.SEPARATOR);

                            // 自己在聊天界面显示msg[1]消息
                            if (msgHandler != null && msg != null && msg.length == 3) {
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
                        }

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
