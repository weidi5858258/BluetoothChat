package com.weidi.bluetoothchat.controller.bluetoothcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.weidi.log.Log;

import java.lang.reflect.Method;
import java.util.UUID;

/***
 <!-- 设置蓝牙的可见时间，以便被其他设备发现并连接 -->
 <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 <!--检测一个设备是否有蓝牙设备，开启蓝牙设备，获取配对的设备-->
 <uses-permission android:name="android.permission.BLUETOOTH" />
 <p>
 BluetoothAdapter代表了移动设备的本地的蓝牙适配器, 通过该蓝牙适配器可以对蓝牙进行基本操作，
 例如：
 启动设备发现(startDiscovery),
 获取已配对设备(getBoundedDevices),
 通过mac蓝牙地址获取蓝牙设备(getRemoteDevice),
 从其它设备创建一个监听连接(listenUsingRfcommWithServiceRecord);
 <p>
 开始搜索只要一行代码
 sBluetoothAdapter.startDiscovery();
 搜索蓝牙设备的过程占用资源比较多，一旦找到需要连接的设备后需要及时关闭搜索
 sBluetoothAdapter.cancelDiscovery();
 获取蓝牙设备的连接状态
 connectState = device.getBondState();
 未配对
 BluetoothDevice.BOND_NONE
 正在配对中
 BluetoothDevice.BOND_BONDING
 已配对
 BluetoothDevice.BOND_BONDED
 <p>
 基本过程:
 第一步先要判断蓝牙设备是否支持,支持才有下一步,不然就提示，此时应该有一个按钮，按下后退出应用
 第二步判断蓝牙设备是否已经打开,没有打开就进行提示是否要打开,打开了就进行下一步操作.
 第三步就是注册监听事件
 第四步开始查找设备
 第五步对查找到的设备进行配对连接
 第六步通信
 <p>
 手机连接蓝牙耳机的过程:
 1.先配对
 配对是第一步,没有配对就没有连接
 配对之前先判断一下蓝牙设备是否已经打开,
 在此之前还要判断一下设备是否有蓝牙设备,是否可用
 配对有两种方式:
 1)弹框确认
 2)自动配对
 2.再连接
 连接对方的设备时,必须要把一方作为服务端,另一方作为客户端;
 不能双方都是客户端或者服务端,这样是连接不成功的;
 作为服务端的只要配对成功后等着客户端连接就可以了,客户端要主动地去连接;
 如果有一方没有调用作为服务端的api等待着客户端的连接,那是连接不成功的.
 <p>
 连接成功后在两个设备之间才可以进行通信.
 <p>
 总结:
 1.如果手机连接的对象是像蓝牙耳机这种没有交互界面的设备时,
 调用createBond方法在手机端不会弹出选择框进行确认.
 没有问题会自动配对成功的.
 调用createBond方法后不会触发
 "android.bluetooth.device.action.PAIRING_REQUEST"
 这个广播.
 2.如果手机连接的对象是像手机这种有交互界面的设备时,
 调用createBond方法在两边手机端都会弹出选择框进行确认.
 两边都需要在选择框消失前(时间比较短)选择"确认"才能配对的上.
 调用createBond方法后会触发
 "android.bluetooth.device.action.PAIRING_REQUEST"
 这个广播.因此在接收到这个广播后还可以干一些事.
 <p>
 设计App时,可能需要有这样的按钮:
 配对,断开配对,取消配对,检查状态,连接

 BluetoothAdapter.ACTION_DISCOVERY_STARTED
 BluetoothAdapter.ACTION_DISCOVERY_FINISHED
 BluetoothDevice.ACTION_FOUND
 BluetoothDevice.ACTION_BOND_STATE_CHANGED
 BluetoothDevice.ACTION_PAIRING_REQUEST
 */
public class BTController {

    private static final String TAG = "BTController";
    public static final String BT_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private volatile static BTController sBTController;
    private volatile static BluetoothAdapter sBluetoothAdapter;
    private static String localBluetoothName = null;
    private static String localBluetoothAdress = null;

    private Context mContext;
    private IBluetoothAction mIBluetoothAction;
    //    private IRemoteConnection mIRemoteConnection;

    public interface IBluetoothAction {

        /**
         * 开始查找蓝牙设备时被触发
         */
        void actionDiscoveryStarted();

        /**
         * 结束查找蓝牙设备时被触发
         */
        void actionDiscoveryFinished();

        /**
         * 查找到一台蓝牙设备时被触发
         */
        void actionFound(BluetoothDevice device);

        /**
         * 调用createBond()方法后会首先触发actionBondStateChanged()方法,
         * 此时设备的状态是在配对中(BluetoothDevice.BOND_BONDING).
         * 然后触发actionPairingRequest()方法,触发这个方法有个前提条件,
         * 就是双方设备都要有交互界面,如双方都是手机;
         * 如果一方是手机,另一方是蓝牙耳机则不会被触发.
         * 配对成功(BluetoothDevice.BOND_BONDED)或者
         * 配对失败(BluetoothDevice.BOND_NONE)或者
         * 取消配对(BluetoothDevice.BOND_NONE)
         * 再次触发actionBondStateChanged()方法.
         * 调用removeBond()方法后也会触发actionBondStateChanged()方法,
         * 状态值是BluetoothDevice.BOND_NONE
         */
        void actionBondStateChanged(BluetoothDevice device);

        void actionPairingRequest();

        // BluetoothDevice.BOND_BONDING
        // BluetoothDevice.BOND_BONDED
        // BluetoothDevice.BOND_NONE

        // 以下三个方法是actionBondStateChanged(BluetoothDevice device)方法的具体体现

        /**
         * 正在配对
         */
        void btBondBonding(BluetoothDevice device);

        /**
         * 成功配对
         */
        void btBondBonded(BluetoothDevice device);

        /**
         * 断开配对,调用removeBond()方法后
         * 配对超时
         * 取消配对,有一方在弹出框中按了"取消"按钮
         * 在配对过程中调用了cancelBondProcess()方法
         * 配对失败
         */
        void btBondNone(BluetoothDevice device);

    }

    //    public interface IRemoteConnection {
    //
    //        void onConnected(boolean isConnected);
    //
    //    }

    private BTController() {
        try {
            sBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        } catch (Exception e) {
            sBluetoothAdapter = null;
        }
    }

    public static BTController getInstance() {
        if (sBTController == null) {
            synchronized (BTController.class) {
                if (sBTController == null) {
                    sBTController = new BTController();
                }
            }
        }
        return sBTController;
    }

    /**
     * 在第一次使用这个类时先设置一下
     *
     * @param context
     */
    public void setContext(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return sBluetoothAdapter;
    }

    /***
     * 转发事件接口
     * @param iBluetoothAction
     */
    public void setIBluetoothAction(IBluetoothAction iBluetoothAction) {
        mIBluetoothAction = iBluetoothAction;
    }

    public String getLocalBluetoothName() {
        if (TextUtils.isEmpty(localBluetoothName)) {
            localBluetoothName = BTController.getInstance()
                    .getBluetoothAdapter().getName();
            /*if (TextUtils.isEmpty(localBluetoothAdress)
                    && mContext != null) {
                localBluetoothAdress =
                        ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE))
                                .getDeviceId();
            }*/
            if (TextUtils.isEmpty(localBluetoothName)) {
                localBluetoothName = getLocalBluetoothAdress();
            }
            if (TextUtils.isEmpty(localBluetoothName)) {
                throw new NullPointerException("localBluetoothName is null.");
            }
            Log.d(TAG, "localBluetoothName = " + localBluetoothName);
        }
        return localBluetoothName;
    }

    public String getLocalBluetoothAdress() {
        if (TextUtils.isEmpty(localBluetoothAdress)) {
            localBluetoothAdress = BTController.getInstance()
                    .getBluetoothAdapter().getAddress();
            /*if (TextUtils.isEmpty(localBluetoothAdress)
                    && mContext != null) {
                localBluetoothAdress =
                        ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE))
                                .getDeviceId();
            }*/
            if (TextUtils.isEmpty(localBluetoothAdress)) {
                localBluetoothAdress = UUID.randomUUID().toString();
            }
            if (TextUtils.isEmpty(localBluetoothAdress)) {
                throw new NullPointerException("localBluetoothAdress is null.");
            }
            Log.d(TAG, "localBluetoothAdress = " + localBluetoothAdress);
        }
        return localBluetoothAdress;
    }

    //    public void setIRemoteConnection(IRemoteConnection iRemoteConnection) {
    //        mIRemoteConnection = iRemoteConnection;
    //    }
    //
    //    public void setRemoteBluetoothSocket(BluetoothSocket socket) {
    //        mRemoteBluetoothSocket = socket;
    //    }
    //
    //    public BluetoothSocket getRemoteBluetoothSocket() {
    //        return mRemoteBluetoothSocket;
    //    }

    //    public boolean isConnected() {
    //        boolean isConnected = false;
    //        if (getRemoteBluetoothSocket() != null) {
    //            isConnected = getRemoteBluetoothSocket().isConnected();
    //        }
    //        return isConnected;
    //    }

    /**
     * 第一步
     * 判断是否支持蓝牙设备
     * 不支持的做好相应的处理,因为下面的方法都是建立在有蓝牙设备的基础之上的
     */
    public boolean isSupportedBluetooth() {
        boolean isSupportBluetooth = false;
        if (getBluetoothAdapter() != null) {
            isSupportBluetooth = true;
        }
        return isSupportBluetooth;
    }

    /**
     * 第二步
     */
    public boolean isOpenedBluetooth() {
        boolean isOpenBluetooth = false;
        if (getBluetoothAdapter().isEnabled()) {
            isOpenBluetooth = true;
        }
        return isOpenBluetooth;
    }

    /**
     * 第三步
     * 打开蓝牙
     * 第二步返回false时才需要调用
     */
    public void openBluetooth() {
        // 打开蓝牙,作用跟下面一样的
        /*if (sBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF){
            sBluetoothAdapter.enable();
        }*/
        if (!getBluetoothAdapter().isEnabled()) {
            getBluetoothAdapter().enable();
        }
    }

    /**
     * 关闭蓝牙
     * 第二步返回true时才需要调用
     */
    public void closeBluetooth() {
        if (getBluetoothAdapter().isEnabled()) {
            getBluetoothAdapter().disable();
        }
    }

    public boolean isScanning() {
        boolean isScanning = false;
        if (getBluetoothAdapter().isEnabled()) {
            isScanning = getBluetoothAdapter().isDiscovering();
        }
        return isScanning;
    }

    /**
     * 第四步
     * 扫描蓝牙设备
     */
    public void scanDevice() {
        if (getBluetoothAdapter().isEnabled()) {
            if (getBluetoothAdapter().isDiscovering()) {
                getBluetoothAdapter().cancelDiscovery();
            }
            getBluetoothAdapter().startDiscovery();
        }
    }

    /**
     * 找到一个蓝牙设备后进行连接前最好停止扫描
     */
    public void cancelScanDevice() {
        if (getBluetoothAdapter().isEnabled()) {
            if (getBluetoothAdapter().isDiscovering()) {
                getBluetoothAdapter().cancelDiscovery();
            }
        }
    }

    //    public void connect(String remoteAddress) {
    //        if (TextUtils.isEmpty(remoteAddress)) {
    //            if (mIRemoteConnection != null) {
    //                Log.d(TAG, "对方设备的地址不可用!");
    //                mIRemoteConnection.onConnected(false);
    //            }
    //            return;
    //        }
    //        try {
    //            mRemoteBluetoothDevice = BTController.getInstance()
    //                    .getBluetoothAdapter()
    //                    .getRemoteDevice(remoteAddress);
    //            if (mRemoteBluetoothDevice == null) {
    //                if (mIRemoteConnection != null) {
    //                    Log.d(TAG, "连接远程设备时得到的远程设备对象为null!");
    //                    mIRemoteConnection.onConnected(false);
    //                }
    //                return;
    //            }
    //            UUID uuid = UUID.fromString(BTController.BT_UUID);
    //            mRemoteBluetoothSocket =
    //                    mRemoteBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
    //            mRemoteBluetoothSocket.connect();
    //            if (mIRemoteConnection != null) {
    //                Log.d(TAG, "mRemoteBluetoothSocket = " + mRemoteBluetoothSocket);
    //                mIRemoteConnection.onConnected(true);
    //            }
    //        } catch (Exception e) {
    //            if (mIRemoteConnection != null) {
    //                Log.d(TAG, "connect()方法出现异常!");
    //                mIRemoteConnection.onConnected(false);
    //            }
    //            e.printStackTrace();
    //        }
    //    }

    /**
     发送文本消息
     *
     @param message
     */
    //    public void sendMessage(String message) {
    //        if (getRemoteBluetoothSocket() == null
    //                || !getRemoteBluetoothSocket().isConnected()
    //                || TextUtils.isEmpty(message)) {
    //            if (mIRemoteConnection != null) {
    //                Log.d(TAG, "客户端sendMessage()方法中出现条件不满足!");
    //                mIRemoteConnection.onConnected(false);
    //            }
    //            return;
    //        }
    //        try {
    //            message += "\n";
    //            OutputStream mOutputStream = getRemoteBluetoothSocket().getOutputStream();
    //            if (mOutputStream != null) {
    //                mOutputStream.write(message.getBytes("utf-8"));
    //                mOutputStream.flush();
    //                Log.d(TAG, "sendMessage = " + message);
    //            }
    //        } catch (IOException e) {
    //            if (mIRemoteConnection != null) {
    //                Log.d(TAG, "客户端sendMessage()方法中出现异常!");
    //                mIRemoteConnection.onConnected(false);
    //            }
    //            e.printStackTrace();
    //        }
    //    }

    /**
     接收文本消息
     */
    //    public void receiveMessage() {
    //        if (getRemoteBluetoothSocket() == null || !getRemoteBluetoothSocket().isConnected()) {
    //            if (mIRemoteConnection != null) {
    //                Log.d(TAG, "客户端receiveMessage()方法中出现条件不满足!");
    //                mIRemoteConnection.onConnected(false);
    //            }
    //            return;
    //        }
    //        try {
    //            Log.d(TAG, "客户端准备接收消息");
    //            InputStream mInputStream = getRemoteBluetoothSocket().getInputStream();
    //            // 从客户端获取信息
    //            BufferedReader mBufferedReader = new BufferedReader(new InputStreamReader
    //                    (mInputStream));
    //            String receiveMessage = null;
    //
    //            while (true) {
    //                Log.d(TAG, "客户端进入while循环,一直监听消息的到来");
    //                if (mBufferedReader != null
    //                        && getRemoteBluetoothSocket() != null
    //                        && getRemoteBluetoothSocket().isConnected()) {
    //                    while ((receiveMessage = mBufferedReader.readLine()) != null) {
    //                        Log.d(TAG, "receiveMessage():msg = " + receiveMessage);
    //                    }
    //                } else {
    //                    if (mIRemoteConnection != null) {
    //                        Log.d(TAG, "客户端receiveMessage()方法中接收消息时出现条件不满足!");
    //                        mIRemoteConnection.onConnected(false);
    //                    }
    //                    break;
    //                }
    //            }
    //        } catch (IOException e) {
    //            if (mIRemoteConnection != null) {
    //                Log.d(TAG, "客户端receiveMessage()方法中出现异常!");
    //                mIRemoteConnection.onConnected(false);
    //            }
    //            e.printStackTrace();
    //        }
    //    }

    /**
     * 与设备配对
     * 配对成功则返回true
     * 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    public boolean createBond(BluetoothDevice btDevice) {
        Boolean returnValue = false;
        if (btDevice == null) {
            return returnValue;
        }
        try {
            Class bdClass = BluetoothDevice.class;
            Method createBond = bdClass.getDeclaredMethod("createBond");
            createBond.setAccessible(true);
            returnValue = (Boolean) createBond.invoke(btDevice);
            Log.d("createBond() = " + returnValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue.booleanValue();
    }

    /**
     * 与设备断开配对
     * 断开成功返回true
     * 在配对过程中调用此方法是没有作用的,只有配对成功后调用此方法就能够断开配对好的设备
     * 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    public boolean removeBond(BluetoothDevice btDevice) {
        Boolean returnValue = false;
        if (btDevice == null) {
            return returnValue;
        }
        try {
            Class bdClass = BluetoothDevice.class;
            Method removeBond = bdClass.getDeclaredMethod("removeBond");
            removeBond.setAccessible(true);
            returnValue = (Boolean) removeBond.invoke(btDevice);
            if (returnValue) {
                if (BTClient.getInstance().getIRemoteConnection() != null) {
                    Log.d(TAG, "客户端removeBond()方法中断开配对!");
                    BTClient.getInstance().getIRemoteConnection().onConnected(false);
                }
            }
            Log.d("removeBond() = " + returnValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue.booleanValue();
    }

    /**
     * 取消配对
     * 在配对过程中调用此方法能够取消配对,配对成功后再调用此方法就没有作用了
     */
    public boolean cancelBondProcess(BluetoothDevice btDevice) {
        Boolean returnValue = false;
        if (btDevice == null) {
            return returnValue;
        }
        try {
            Class bdClass = BluetoothDevice.class;
            Method cancelBondProcess = bdClass.getDeclaredMethod("cancelBondProcess");
            cancelBondProcess.setAccessible(true);
            returnValue = (Boolean) cancelBondProcess.invoke(btDevice);
            Log.d("cancelBondProcess() = " + returnValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue.booleanValue();
    }

    /**
     * 实际使用没有效果
     *
     * @param btDevice
     * @param pwd
     * @return
     * @throws Exception
     */
    private boolean setPin(BluetoothDevice btDevice, String pwd) {
        Boolean returnValue = false;
        if (btDevice == null || TextUtils.isEmpty(pwd)) {
            return returnValue;
        }
        try {
            Class bdClass = BluetoothDevice.class;
            Method setPin = bdClass.getDeclaredMethod("setPin", new Class[]{byte[].class});
            Method convertPinToBytes = bdClass.getDeclaredMethod("convertPinToBytes", String.class);
            byte[] pinBytes = (byte[]) convertPinToBytes.invoke(null, pwd);
            returnValue = (Boolean) setPin.invoke(btDevice, pinBytes);
            Log.d("setPin() = " + returnValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    private boolean cancelPairingUserInput(BluetoothDevice btDevice) {
        Boolean returnValue = false;
        if (btDevice == null) {
            return returnValue;
        }
        try {
            Class bdClass = BluetoothDevice.class;
            Method cancelPairingUserInput = bdClass.getMethod("cancelPairingUserInput");
            //            cancelBondProcess(btDevice);
            returnValue = (Boolean) cancelPairingUserInput.invoke(btDevice);
            Log.d("cancelPairingUserInput() = " + returnValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue.booleanValue();
    }

    // 检测是否连接蓝牙耳机
    private void handleHeadsetStateChange() {
        Intent intent = new Intent(Intent.ACTION_HEADSET_PLUG);
        if (BluetoothProfile.STATE_CONNECTED ==
                getBluetoothAdapter().getProfileConnectionState(BluetoothProfile.HEADSET)) {
            intent.putExtra("state", 1);
            intent.putExtra("microphone", 1);
            //            mContext.sendBroadcast(intent);
        } else if (BluetoothProfile.STATE_DISCONNECTED ==
                getBluetoothAdapter().getProfileConnectionState(BluetoothProfile.HEADSET)) {
            intent.putExtra("state", -1);
            //            mContext.sendBroadcast(intent);
        }
    }

    private boolean hasRegister = false;

    public void registerBluetoothReceiver(Context mContext) {
        IntentFilter mIntentFilter = new IntentFilter();
        // Register for broadcasts when start bluetooth search
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        // Register for broadcasts when discovery has finished
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // Register for broadcasts when a device is discovered
        mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        // 指明一个远程设备的连接状态的改变
        mIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mIntentFilter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
        mContext.registerReceiver(mBluetoothReceiver, mIntentFilter);
        hasRegister = true;
    }

    public void unRegisterBluetoothReceiver(Context mContext) {
        if (hasRegister) {
            mContext.unregisterReceiver(mBluetoothReceiver);
            hasRegister = false;
        }
    }

    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(TAG, "intent == null");
                return;
            }
            String action = intent.getAction();
            if (mIBluetoothAction == null) {
                Log.e(TAG, "mIBluetoothAction == null");
                return;
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.e(TAG, "接收到 action=BluetoothAdapter.ACTION_DISCOVERY_STARTED");
                mIBluetoothAction.actionDiscoveryStarted();
                return;
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e(TAG, "接收到 action=BluetoothAdapter.ACTION_DISCOVERY_FINISHED");
                mIBluetoothAction.actionDiscoveryFinished();
                return;
            }
            BluetoothDevice mBluetoothDevice = intent.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE);
            if (mBluetoothDevice == null) {
                Log.e(TAG, "mBluetoothDevice == null");
                return;
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //                Log.e(TAG, "接收到action=BluetoothDevice.ACTION_FOUND");
                mIBluetoothAction.actionFound(mBluetoothDevice);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                Log.e(TAG, "接收到 action=BluetoothDevice.ACTION_BOND_STATE_CHANGED");
                mIBluetoothAction.actionBondStateChanged(mBluetoothDevice);
                if (mBluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    mIBluetoothAction.btBondBonding(mBluetoothDevice);
                } else if (mBluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    mIBluetoothAction.btBondBonded(mBluetoothDevice);
                } else if (mBluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    if (BTClient.getInstance().getIRemoteConnection() != null) {
                        Log.d(TAG, "接收到\"BluetoothDevice.BOND_NONE\"的广播");
                        BTClient.getInstance().getIRemoteConnection().onConnected(false);
                    }
                    mIBluetoothAction.btBondNone(mBluetoothDevice);
                }
            } else if ("android.bluetooth.device.action.PAIRING_REQUEST".equals(action)) {
                Log.e(TAG, "接收到 action=BluetoothDevice.ACTION_PAIRING_REQUEST");
                mIBluetoothAction.actionPairingRequest();
            }
        }
    };

}
