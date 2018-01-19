package com.weidi.bluetoothchat.controller.fragmentcontroller;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.activity.BluetoothDeviceListActivity;
import com.weidi.bluetoothchat.controller.activitycontroller.BaseController;
import com.weidi.bluetoothchat.controller.bluetoothcontroller.BTController;
import com.weidi.bluetoothchat.fragment.BluetoothDeviceListPreferenceFragment;
import com.weidi.bluetoothchat.fragment.BluetoothDeviceProfilesSettingsFragment;
import com.weidi.bluetoothchat.fragment.BluetoothVisibilityTimeoutFragment;
import com.weidi.bluetoothchat.settings.BluetoothCallback;
import com.weidi.bluetoothchat.settings.BluetoothDevicePreference;
import com.weidi.bluetoothchat.settings.BluetoothEventManager;
import com.weidi.bluetoothchat.settings.BluetoothProgressCategory;
import com.weidi.bluetoothchat.settings.CachedBluetoothDevice;
import com.weidi.bluetoothchat.settings.CachedBluetoothDeviceManager;
import com.weidi.bluetoothchat.settings.LocalBluetoothAdapter;
import com.weidi.bluetoothchat.settings.LocalBluetoothManager;
import com.weidi.bluetoothchat.settings.LocalBluetoothProfileManager;
import com.weidi.bluetoothchat.settings.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static com.weidi.bluetoothchat.Constant.ACTION_REQUEST_ENABLE;

/**
 * Created by root on 16-12-16.
 */
public class BluetoothDeviceListPreferenceFragmentController extends BaseController
        implements BluetoothCallback, View.OnClickListener {

    private static final String TAG = "BluetoothDeviceListPreferenceFragmentController";
    /* Private intent to show the list of received files */
    private static final String BTOPP_ACTION_OPEN_RECEIVED_FILES =
            "android.btopp.intent.action.OPEN_RECEIVED_FILES";
    private BluetoothDeviceListPreferenceFragment mBluetoothDeviceListPreferenceFragment;

    private static final int MENU_ID_SCAN = Menu.FIRST;
    private static final int MENU_ID_RENAME_DEVICE = Menu.FIRST + 1;
    private static final int MENU_ID_VISIBILITY_TIMEOUT = Menu.FIRST + 2;
    private static final int MENU_ID_SHOW_RECEIVED = Menu.FIRST + 3;
    private static final String KEY_BT_SCAN = "bt_scan";
    private static final String KEY_BT_DEVICE_LIST = "bt_device_list";// "可用设备"的容器

    private final Map<String, Handler> mHandlerMap = new HashMap<String, Handler>();
    private final IntentFilter mAdapterIntentFilter = new IntentFilter();
    private final IntentFilter mProfileIntentFilter = new IntentFilter();

    private TextView mEmptyView;

    private BluetoothDevice mSelectedDevice;

    private LocalBluetoothAdapter mLocalAdapter;
    private LocalBluetoothManager mLocalManager;
    private LocalBluetoothProfileManager mLocalProfileManager;
    private BluetoothEventManager mBluetoothEventManager;
    private CachedBluetoothDeviceManager mCachedBluetoothDeviceManager;

    // accessed from inner class (not private to avoid thunks)
    private Preference mMyDevicePreference;

    private PreferenceScreen mBTListPreferenceScreen;
    private PreferenceGroup mDeviceListGroup;
    private PreferenceGroup mPairedDevicesCategory;
    private PreferenceGroup mAvailableDevicesCategory;

    // 蓝牙关闭后,这个广播"android.bluetooth.adapter.action.DISCOVERY_FINISHED"会发送两次
    private long mPopupActivity1 = 0;
    private long mPopupActivity2 = 0;

    final WeakHashMap<CachedBluetoothDevice, BluetoothDevicePreference> mDevicePreferenceMap =
            new WeakHashMap<CachedBluetoothDevice, BluetoothDevicePreference>();

    private interface Handler {
        void onReceive(Context context, Intent intent, BluetoothDevice device);
    }

    public BluetoothDeviceListPreferenceFragmentController(Fragment fragment) {
        super(fragment);
        mBluetoothDeviceListPreferenceFragment = (BluetoothDeviceListPreferenceFragment) fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        init();
    }

    @Override
    public void onResume() {
        //        onShow(true);
        onShow(false);
    }

    @Override
    public void onPause() {
        stopDiscovery();
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
        reset();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_REQUEST_ENABLE && resultCode == -1) {
            startScanning();
        } else {
            ((BluetoothDeviceListActivity) mBluetoothDeviceListPreferenceFragment.getActivity())
                    .goBack();
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        stopDiscovery();

        if (preference instanceof BluetoothDevicePreference) {
            BluetoothDevicePreference btPreference = (BluetoothDevicePreference) preference;
            CachedBluetoothDevice device = btPreference.getCachedDevice();
            mSelectedDevice = device.getDevice();
            onDevicePreferenceClick(btPreference);
            return true;
        }
        return false;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mLocalAdapter == null) return;// 蓝牙功能不可用
        boolean bluetoothIsEnabled = mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON;
        boolean isDiscovering = mLocalAdapter.isDiscovering();
        int textId = isDiscovering ? R.string.bluetooth_searching_for_devices :
                R.string.bluetooth_search_for_devices;
        menu.add(Menu.NONE, MENU_ID_SCAN, 0, textId)
                .setEnabled(bluetoothIsEnabled && !isDiscovering)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(Menu.NONE, MENU_ID_RENAME_DEVICE, 0, R.string.bluetooth_rename_device)
                .setEnabled(bluetoothIsEnabled)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, MENU_ID_VISIBILITY_TIMEOUT, 0, R.string.bluetooth_visibility_timeout)
                .setEnabled(bluetoothIsEnabled)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, MENU_ID_SHOW_RECEIVED, 0, R.string.bluetooth_show_received_files)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        clickedmenuItem = menu.findItem(MENU_ID_SCAN);
    }

    private MenuItem clickedmenuItem;

    public boolean onOptionsItemSelected(MenuItem item) {
        clickedmenuItem = item;
        switch (item.getItemId()) {
            case android.R.id.home:// 点击返回图标事件
                ((BluetoothDeviceListActivity) mBluetoothDeviceListPreferenceFragment.getActivity())
                        .goBack();
                return true;

            case MENU_ID_SCAN:
                if (mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON) {
                    startScanning();
                }
                return true;

            case MENU_ID_RENAME_DEVICE:
                //                new BluetoothNameDialogFragment().show(
                //                        getFragmentManager(), "rename device");
                return true;

            case MENU_ID_VISIBILITY_TIMEOUT:
                new BluetoothVisibilityTimeoutFragment(mMyDevicePreference).show(
                        mBluetoothDeviceListPreferenceFragment.getActivity().getFragmentManager(),
                        "BluetoothDeviceListPreferenceFragment");
                return true;

            case MENU_ID_SHOW_RECEIVED:
                // 打开系统的界面
                Intent intent = new Intent(BTOPP_ACTION_OPEN_RECEIVED_FILES);
                mBluetoothDeviceListPreferenceFragment.getActivity().sendBroadcast(intent);
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        // User clicked on advanced options icon for a device in the list
        if (v.getTag() instanceof CachedBluetoothDevice) {
            CachedBluetoothDevice device = (CachedBluetoothDevice) v.getTag();

            //            Bundle args = new Bundle(1);
            //            args.putParcelable(
            //                    BluetoothDeviceProfilesSettingsFragment.EXTRA_DEVICE, device
            // .getDevice());
            //
            //            ((PreferenceActivity) mBluetoothDeviceListPreferenceFragment
            // .getActivity())
            //                    .startPreferencePanel(
            //                            BluetoothDeviceProfilesSettingsFragment.class.getName(),
            //                            args, R.string.bluetooth_device_advanced_title, null,
            // null, 0);

            BluetoothDeviceProfilesSettingsFragment bluetoothDeviceProfilesSettingsFragment =
                    new BluetoothDeviceProfilesSettingsFragment();
            Bundle args = new Bundle(1);
            args.putParcelable(
                    BluetoothDeviceProfilesSettingsFragmentController.EXTRA_DEVICE,
                    device.getDevice());
            bluetoothDeviceProfilesSettingsFragment.setArguments(args);
            ((BluetoothDeviceListActivity) mBluetoothDeviceListPreferenceFragment.getActivity())
                    .getFragOperManager().enter(bluetoothDeviceProfilesSettingsFragment, null);
        } else {
            Log.w(TAG, "onClick() called for other View: " + v); // TODO remove
        }
    }

    private void init() {
        mLocalAdapter = LocalBluetoothAdapter.getInstance();
        if (mLocalAdapter == null) {
            Log.e(TAG, "BluetoothAdapter is not supported on this device");
            return;
        }

        mLocalManager = LocalBluetoothManager.getInstance(mContext);
        if (mLocalAdapter == null) {
            Log.e(TAG, "BluetoothAdapter is not supported on this device");
            return;
        }

        mBluetoothDeviceListPreferenceFragment
                .addPreferencesFromResource(R.xml.bluetooth_settings);
        // 调用了并且设置为true时才会显示Menu
        mBluetoothDeviceListPreferenceFragment
                .setHasOptionsMenu(true);

        mCachedBluetoothDeviceManager = new CachedBluetoothDeviceManager(mContext);

        mBluetoothEventManager = new BluetoothEventManager(
                mLocalAdapter, mCachedBluetoothDeviceManager, mContext);

        mLocalProfileManager = new LocalBluetoothProfileManager(
                mContext, mLocalAdapter, mCachedBluetoothDeviceManager, mBluetoothEventManager);

        //        mEmptyView = (TextView) mBluetoothDeviceListPreferenceFragment
        //                .getView().findViewById(android.R.id.empty);
        //        mBluetoothDeviceListPreferenceFragment.getListView().setEmptyView(mEmptyView);

        mDeviceListGroup = (PreferenceCategory) mBluetoothDeviceListPreferenceFragment
                .findPreference(KEY_BT_DEVICE_LIST);

        Activity activity = mBluetoothDeviceListPreferenceFragment.getActivity();

        // Switch actionBarSwitch = new Switch(activity);
        // actionBarSwitch.setOnCheckedChangeListener(mSwitchOnCheckedChangeListener);

        if (activity instanceof PreferenceActivity) {
            PreferenceActivity preferenceActivity = (PreferenceActivity) activity;
            if (preferenceActivity.onIsHidingHeaders() || !preferenceActivity.onIsMultiPane()) {

                activity.getActionBar().setTitle(R.string.bluetooth);
                activity.getActionBar().setHomeButtonEnabled(true);
                activity.getActionBar().setDisplayShowTitleEnabled(true);
                // 决定左上角图标的右侧是否有向左的小箭头, true有小箭头，并且图标可以点击
                activity.getActionBar().setDisplayHomeAsUpEnabled(true);
                // 使左上角图标是否显示，如果设成false，则没有程序图标，仅仅就个标题，
                // 否则，显示应用程序图标，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME
                activity.getActionBar().setDisplayShowHomeEnabled(false);
                activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                        ActionBar.DISPLAY_SHOW_CUSTOM);
                /*final int padding = activity.getResources().getDimensionPixelSize(
                        R.dimen.action_bar_switch_padding);
                actionBarSwitch.setPaddingRelative(0, 0, padding, 0);
                activity.getActionBar().setCustomView(
                        actionBarSwitch,
                        new ActionBar.LayoutParams(
                                ActionBar.LayoutParams.WRAP_CONTENT,
                                ActionBar.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER_VERTICAL | Gravity.END));*/

            }
        }

        mBTListPreferenceScreen = mBluetoothDeviceListPreferenceFragment.getPreferenceScreen();
        mMyDevicePreference = new Preference(
                mBluetoothDeviceListPreferenceFragment.getActivity());
        mMyDevicePreference.setTitle(mLocalAdapter.getName());
        mMyDevicePreference.setSummary(R.string.bluetooth_only_visible_to_paired_devices);
        if (mBluetoothDeviceListPreferenceFragment.getResources()
                .getBoolean(com.android.internal.R.bool.config_voice_capable)) {
            mMyDevicePreference.setIcon(R.drawable.ic_bt_cellphone);    // for phones
        } else {
            mMyDevicePreference.setIcon(R.drawable.ic_bt_laptop);   // for tablets, etc.
        }
        mMyDevicePreference.setPersistent(false);
        mMyDevicePreference.setEnabled(true);
        // 把mMyDevicePreference加到PreferenceFragment中
        mBTListPreferenceScreen.addPreference(mMyDevicePreference);
        mMyDevicePreference.setOnPreferenceClickListener(mOnPreferenceClickListener);

        // Paired devices category
        if (mPairedDevicesCategory == null) {
            mPairedDevicesCategory = new PreferenceCategory(mContext);
        } else {
            mPairedDevicesCategory.removeAll();
        }
        addDeviceCategory(mPairedDevicesCategory, R.string.bluetooth_preference_paired_devices);

        // Available devices category
        if (mAvailableDevicesCategory == null) {
            mAvailableDevicesCategory = new BluetoothProgressCategory(mContext, null);
        } else {
            mAvailableDevicesCategory.removeAll();
        }
        addDeviceCategory(mAvailableDevicesCategory, R.string.bluetooth_preference_found_devices);
        mBTListPreferenceScreen.removePreference(mAvailableDevicesCategory);

        // Bluetooth on/off broadcasts
        addHandler(BluetoothAdapter.ACTION_STATE_CHANGED,
                new AdapterStateChangedHandler());

        // Discovery broadcasts
        addHandler(BluetoothAdapter.ACTION_DISCOVERY_STARTED,
                new ScanningStateChangedHandler(true));
        addHandler(BluetoothAdapter.ACTION_DISCOVERY_FINISHED,
                new ScanningStateChangedHandler(false));
        addHandler(BluetoothDevice.ACTION_FOUND,
                new BluetoothDeviceFoundHandler());
        addHandler(BluetoothDevice.ACTION_DISAPPEARED,
                new BluetoothDeviceDisappearedHandler());
        addHandler(BluetoothDevice.ACTION_NAME_CHANGED,
                new NameChangedHandler());

        // Pairing broadcasts
        addHandler(BluetoothDevice.ACTION_BOND_STATE_CHANGED,
                new BondStateChangedHandler());
        addHandler(BluetoothDevice.ACTION_PAIRING_CANCEL,
                new PairingCancelHandler());

        // Fine-grained state broadcasts
        addHandler(BluetoothDevice.ACTION_CLASS_CHANGED,
                new BluetoothClassChangedHandler());
        addHandler(BluetoothDevice.ACTION_UUID,
                new UuidChangedHandler());

        // Dock event broadcasts
        addHandler(Intent.ACTION_DOCK_EVENT,
                new DockEventHandler());

        mContext.registerReceiver(mBroadcastReceiver, mAdapterIntentFilter);

        isShowPairedDeviceAndAvailableDevice();
    }

    private void reset() {
        if (clickedmenuItem != null) {
            clickedmenuItem.setEnabled(true);
            if (clickedmenuItem.getItemId() == MENU_ID_SCAN) {
                clickedmenuItem.setTitle(R.string.bluetooth_search_for_devices);
            }
        }
        mPopupActivity1 = 0;
        mPopupActivity2 = 0;
        stopDiscovery();
        updateProgressUi(false);
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    private void stopDiscovery() {
        if (BTController.getInstance().getBluetoothAdapter().isDiscovering()) {
            BTController.getInstance().getBluetoothAdapter().cancelDiscovery();
        }
    }

    private void onShow(boolean enable) {
        Boolean isEnabled = BTController.getInstance().getBluetoothAdapter().isEnabled();
        if (enable) {
            int numberOfPairedDevices = mPairedDevicesCategory.getPreferenceCount();
            if (numberOfPairedDevices <= 0 && isEnabled) {
                startScanning();
            } else if (!isEnabled) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mBluetoothDeviceListPreferenceFragment.getActivity()
                        .startActivityForResult(enableIntent, ACTION_REQUEST_ENABLE);
            }
        } else {
            if (!isEnabled) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mBluetoothDeviceListPreferenceFragment.getActivity()
                        .startActivityForResult(enableIntent, ACTION_REQUEST_ENABLE);
            }
        }
    }

    private void isShowPairedDeviceAndAvailableDevice() {
        if (mPairedDevicesCategory.getPreferenceCount() <= 0) {
            mBTListPreferenceScreen.removePreference(mPairedDevicesCategory);
        }
        if (mAvailableDevicesCategory.getPreferenceCount() <= 0) {
            mBTListPreferenceScreen.removePreference(mAvailableDevicesCategory);
        }
    }

    private void addDeviceCategory(PreferenceGroup preferenceGroup, int titleId) {
        preferenceGroup.setTitle(titleId);
        mBTListPreferenceScreen.addPreference(preferenceGroup);
        //        setFilter(filter);
        setDeviceListGroup(preferenceGroup);
        addCachedDevices();
        preferenceGroup.setEnabled(true);
    }


    protected void setDeviceListGroup(PreferenceGroup preferenceGroup) {
        mDeviceListGroup = preferenceGroup;
    }

    protected void removeAllDevices() {
        mLocalAdapter.stopScanning();
        mDevicePreferenceMap.clear();
        mDeviceListGroup.removeAll();
    }

    protected void addCachedDevices() {
        //        Collection<CachedBluetoothDevice> cachedDevices =
        //                mLocalManager.getCachedDeviceManager().getCachedDevicesCopy();
        //        for (CachedBluetoothDevice cachedDevice : cachedDevices) {
        //            onDeviceAdded(cachedDevice);
        //        }
    }


    protected void onDevicePreferenceClick(BluetoothDevicePreference btPreference) {
        btPreference.onClicked();
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        if (mDevicePreferenceMap.get(cachedDevice) != null) {
            return;
        }

        // Prevent updates while the list shows one of the state messages
        if (mLocalAdapter.getBluetoothState() != BluetoothAdapter.STATE_ON) return;

        //        if (mFilter.matches(cachedDevice.getDevice())) {
        //        }
        createDevicePreference(cachedDevice);
    }

    private void createDevicePreference(CachedBluetoothDevice cachedDevice) {
        //        BluetoothDevicePreference preference = new BluetoothDevicePreference(
        //                getActivity(), cachedDevice);
        //
        //        initDevicePreference(preference);// 先调用子类
        //        mDeviceListGroup.addPreference(preference);
        //        mDevicePreferenceMap.put(cachedDevice, preference);
    }

    void initDevicePreference(BluetoothDevicePreference preference) {
        // Does nothing by default
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        BluetoothDevicePreference preference = mDevicePreferenceMap.remove(cachedDevice);
        if (preference != null) {
            mDeviceListGroup.removePreference(preference);
        }
    }

    //    @Override
    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {

    }

    public void onScanningStateChanged(boolean started) {
        updateProgressUi(started);
    }

    public void onBluetoothStateChanged(int bluetoothState) {
        if (bluetoothState == BluetoothAdapter.STATE_OFF) {
            updateProgressUi(false);
        }
    }

    private void addHandler(String action, Handler handler) {
        mHandlerMap.put(action, handler);
        mAdapterIntentFilter.addAction(action);
    }

    public void addProfileHandler(String action, Handler handler) {
        mHandlerMap.put(action, handler);
        mProfileIntentFilter.addAction(action);
    }

    // 搜索设备
    private void startScanning() {
        //        if (!mAvailableDevicesCategoryIsPresent) {
        //        }
        if (clickedmenuItem != null) {
            clickedmenuItem.setEnabled(false);
            clickedmenuItem.setTitle(R.string.bluetooth_searching_for_devices);
        }
        updateProgressUi(true);
        mLocalAdapter.startScanning(true);// 开始搜索后,蓝牙设备的状态改变
    }

    private void updateProgressUi(boolean start) {
        if (mDeviceListGroup instanceof BluetoothProgressCategory) {
            ((BluetoothProgressCategory) mDeviceListGroup).setProgress(start);
        }
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            Handler handler = mHandlerMap.get(action);
            if (handler != null) {
                handler.onReceive(context, intent, device);
            }
        }

    };

    private class AdapterStateChangedHandler implements Handler {

        public void onReceive(Context context, Intent intent, BluetoothDevice device) {
            Log.d(TAG, "AdapterStateChangedHandler:intent = " + intent);
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            // update local profiles and get paired devices
            mLocalAdapter.setBluetoothStateInt(state);
            // send callback to update UI and possibly start scanning
            //            synchronized (mCallbacks) {
            //                for (BluetoothCallback callback : mCallbacks) {
            //                    callback.onBluetoothStateChanged(state);
            //                }
            //            }
            //            // Inform CachedDeviceManager that the adapter state has changed
            //            mDeviceManager.onBluetoothStateChanged(state);
        }

    }

    private class ScanningStateChangedHandler implements Handler {

        private final boolean mStarted;

        private ScanningStateChangedHandler(boolean started) {
            mStarted = started;
        }

        public void onReceive(Context context, Intent intent, BluetoothDevice device) {
            Log.d(TAG, "ScanningStateChangedHandler:intent = " + intent);
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mPopupActivity1 = 0;
                mPopupActivity2 = 0;
                mBTListPreferenceScreen.addPreference(mPairedDevicesCategory);
                mBTListPreferenceScreen.addPreference(mAvailableDevicesCategory);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (clickedmenuItem != null) {
                    clickedmenuItem.setEnabled(true);
                    clickedmenuItem.setTitle(R.string.bluetooth_search_for_devices);
                }
                isShowPairedDeviceAndAvailableDevice();
                updateProgressUi(false);
                mPopupActivity1 = SystemClock.uptimeMillis();
                if (Math.abs(mPopupActivity2 - mPopupActivity1) > 1000) {
                    mPopupActivity2 = mPopupActivity1;
                    onShow(false);
                }
            }
            //            synchronized (mCallbacks) {
            //                for (BluetoothCallback callback : mCallbacks) {
            //                    callback.onScanningStateChanged(mStarted);
            //                }
            //            }
            //            mDeviceManager.onScanningStateChanged(mStarted);
            //            LocalBluetoothPreferences.persistDiscoveringTimestamp(context);
        }

    }

    private class BluetoothDeviceFoundHandler implements Handler {

        public void onReceive(Context context, Intent intent, BluetoothDevice device) {
            Log.d(TAG, "BluetoothDeviceFoundHandler:intent = " + intent);
            short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
            BluetoothClass btClass = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
            String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            // TODO Pick up UUID. They should be available for 2.1 devices.
            // Skip for now, there's a bluez problem and we are not getting uuids even for 2.1.
            CachedBluetoothDevice cachedDevice = mCachedBluetoothDeviceManager.findDevice(device);

            if (cachedDevice == null) {
                cachedDevice = mCachedBluetoothDeviceManager.addDevice(
                        mLocalAdapter, mLocalProfileManager, device);
                Log.d(TAG, "BluetoothDeviceFoundHandler created new CachedBluetoothDevice:"
                        + cachedDevice);
                // callback to UI to create Preference for new device
                //                dispatchDeviceAdded(cachedDevice);
            }
            cachedDevice.setRssi(rssi);
            cachedDevice.setBtClass(btClass);
            cachedDevice.setName(name);
            cachedDevice.setVisible(true);
            BluetoothDevicePreference preference = new BluetoothDevicePreference(
                    mContext, cachedDevice);
            preference.setOnSettingsClickListener(
                    BluetoothDeviceListPreferenceFragmentController.this);
            if (cachedDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                mPairedDevicesCategory.addPreference(preference);
            } else {
                mAvailableDevicesCategory.addPreference(preference);
            }


        }

    }

    private class BluetoothDeviceDisappearedHandler implements Handler {

        public void onReceive(Context context, Intent intent, BluetoothDevice device) {
            Log.d(TAG, "BluetoothDeviceDisappearedHandler:intent = " + intent);
            //            CachedBluetoothDevice cachedDevice = mDeviceManager.findDevice(device);
            //            if (cachedDevice == null) {
            //                Log.w(TAG, "received ACTION_DISAPPEARED for an unknown device: " +
            // device);
            //                return;
            //            }
            //            if (CachedBluetoothDeviceManager.onDeviceDisappeared(cachedDevice)) {
            //                synchronized (mCallbacks) {
            //                    for (BluetoothCallback callback : mCallbacks) {
            //                        callback.onDeviceDeleted(cachedDevice);
            //                    }
            //                }
            //            }
        }

    }

    private class NameChangedHandler implements Handler {

        public void onReceive(Context context, Intent intent, BluetoothDevice device) {
            Log.d(TAG, "NameChangedHandler:intent = " + intent);
            //            mDeviceManager.onDeviceNameUpdated(device);
        }

    }

    private class BondStateChangedHandler implements Handler {

        public void onReceive(Context context, Intent intent, BluetoothDevice device) {
            Log.d(TAG, "BondStateChangedHandler:intent = " + intent);
            if (device == null) {
                Log.e(TAG, "ACTION_BOND_STATE_CHANGED with no EXTRA_DEVICE");
                return;
            }
            int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                    BluetoothDevice.ERROR);
            //            CachedBluetoothDevice cachedDevice = mDeviceManager.findDevice(device);
            //            if (cachedDevice == null) {
            //                Log.w(TAG, "CachedBluetoothDevice for device " + device +
            //                        " not found, calling readPairedDevices().");
            //                if (!readPairedDevices()) {
            //                    Log.e(TAG, "Got bonding state changed for " + device +
            //                            ", but we have no record of that device.");
            //                    return;
            //                }
            //                cachedDevice = mDeviceManager.findDevice(device);
            //                if (cachedDevice == null) {
            //                    Log.e(TAG, "Got bonding state changed for " + device +
            //                            ", but device not added in cache.");
            //                    return;
            //                }
            //            }
            //
            //            synchronized (mCallbacks) {
            //                for (BluetoothCallback callback : mCallbacks) {
            //                    callback.onDeviceBondStateChanged(cachedDevice, bondState);
            //                }
            //            }
            //            cachedDevice.onBondingStateChanged(bondState);
            //
            //            if (bondState == BluetoothDevice.BOND_NONE) {
            //                if (device.isBluetoothDock()) {
            //                    // After a dock is unpaired, we will forget the settings
            //                    LocalBluetoothPreferences
            //                            .removeDockAutoConnectSetting(context, device
            // .getAddress());
            //
            //                    // if the device is undocked, remove it from the list as well
            //                    if (!device.getAddress().equals(getDockedDeviceAddress(context)
            // )) {
            //                        cachedDevice.setVisible(false);
            //                    }
            //                }
            //                int reason = intent.getIntExtra(BluetoothDevice.EXTRA_REASON,
            //                        BluetoothDevice.ERROR);
            //
            //                showUnbondMessage(context, cachedDevice.getName(), reason);
            //            }
        }

        /**
         * Called when we have reached the unbonded state.
         *
         * @param reason one of the error reasons from
         *               BluetoothDevice.UNBOND_REASON_*
         */
        private void showUnbondMessage(Context context, String name, int reason) {
            int errorMsg;

            switch (reason) {
                case BluetoothDevice.UNBOND_REASON_AUTH_FAILED:
                    errorMsg = R.string.bluetooth_pairing_pin_error_message;
                    break;
                case BluetoothDevice.UNBOND_REASON_AUTH_REJECTED:
                    errorMsg = R.string.bluetooth_pairing_rejected_error_message;
                    break;
                case BluetoothDevice.UNBOND_REASON_REMOTE_DEVICE_DOWN:
                    errorMsg = R.string.bluetooth_pairing_device_down_error_message;
                    break;
                case BluetoothDevice.UNBOND_REASON_DISCOVERY_IN_PROGRESS:
                case BluetoothDevice.UNBOND_REASON_AUTH_TIMEOUT:
                case BluetoothDevice.UNBOND_REASON_REPEATED_ATTEMPTS:
                case BluetoothDevice.UNBOND_REASON_REMOTE_AUTH_CANCELED:
                    errorMsg = R.string.bluetooth_pairing_error_message;
                    break;
                default:
                    Log.w(TAG,
                            "showUnbondMessage: Not displaying any message for reason: " + reason);
                    return;
            }
            Utils.showError(context, name, errorMsg);
        }

    }

    private class BluetoothClassChangedHandler implements Handler {

        public void onReceive(Context context, Intent intent, BluetoothDevice device) {
            Log.d(TAG, "BluetoothClassChangedHandler:intent = " + intent);
            //            mDeviceManager.onBtClassChanged(device);
        }

    }

    private class UuidChangedHandler implements Handler {

        public void onReceive(Context context, Intent intent, BluetoothDevice device) {
            Log.d(TAG, "UuidChangedHandler:intent = " + intent);
            //            mDeviceManager.onUuidChanged(device);
        }

    }

    private class PairingCancelHandler implements Handler {

        public void onReceive(Context context, Intent intent, BluetoothDevice device) {
            Log.d(TAG, "PairingCancelHandler:intent = " + intent);
            if (device == null) {
                Log.e(TAG, "ACTION_PAIRING_CANCEL with no EXTRA_DEVICE");
                return;
            }
            int errorMsg = R.string.bluetooth_pairing_error_message;
            //            CachedBluetoothDevice cachedDevice = mDeviceManager.findDevice(device);
            //            Utils.showError(context, cachedDevice.getName(), errorMsg);
        }

    }

    private class DockEventHandler implements Handler {

        public void onReceive(Context context, Intent intent, BluetoothDevice device) {
            Log.d(TAG, "DockEventHandler:intent = " + intent);
            // Remove if unpair device upon undocking
            int anythingButUnDocked = Intent.EXTRA_DOCK_STATE_UNDOCKED + 1;
            int state = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, anythingButUnDocked);
            if (state == Intent.EXTRA_DOCK_STATE_UNDOCKED) {
                if (device != null && device.getBondState() == BluetoothDevice.BOND_NONE) {
                    //                    CachedBluetoothDevice cachedDevice = mDeviceManager
                    // .findDevice(device);
                    //                    if (cachedDevice != null) {
                    //                        cachedDevice.setVisible(false);
                    //                    }
                }
            }
        }

    }

    private CompoundButton.OnCheckedChangeListener mSwitchOnCheckedChangeListener =
            new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    // Show toast message if Bluetooth is not allowed in airplane mode
                    //                    if (isChecked) {
                    //                        // Reset switch to off
                    //                        buttonView.setChecked(false);
                    //                    }
                    //
                    //                    if (mLocalAdapter != null) {
                    //                        mLocalAdapter.setBluetoothEnabled(isChecked);
                    //                    }
                    //                    mSwitch.setEnabled(false);
                }

            };

    private Preference.OnPreferenceClickListener mOnPreferenceClickListener =
            new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    return false;
                }

            };


}
