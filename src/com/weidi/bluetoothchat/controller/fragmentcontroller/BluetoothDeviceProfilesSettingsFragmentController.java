package com.weidi.bluetoothchat.controller.fragmentcontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.activity.BluetoothDeviceListActivity;
import com.weidi.bluetoothchat.controller.activitycontroller.BaseController;
import com.weidi.bluetoothchat.controller.bluetoothcontroller.BTController;
import com.weidi.bluetoothchat.fragment.BluetoothDeviceProfilesSettingsFragment;
import com.weidi.bluetoothchat.settings.CachedBluetoothDevice;
import com.weidi.bluetoothchat.settings.CachedBluetoothDeviceManager;
import com.weidi.bluetoothchat.settings.LocalBluetoothManager;
import com.weidi.bluetoothchat.settings.LocalBluetoothProfileManager;
import com.weidi.bluetoothchat.settings.Utils;
import com.weidi.bluetoothchat.settings.profile.LocalBluetoothProfile;

import java.util.HashMap;

/**
 * Created by root on 16-12-16.
 */
public class BluetoothDeviceProfilesSettingsFragmentController extends BaseController
        implements CachedBluetoothDevice.Callback, Preference.OnPreferenceChangeListener {

    private static final String TAG = "BluetoothDeviceListPreferenceFragmentController";
    private BluetoothDeviceProfilesSettingsFragment mBluetoothDeviceProfilesSettingsFragment;

    private static final String KEY_RENAME_DEVICE = "rename_device";
    private static final String KEY_UNPAIR = "unpair";
    private static final String KEY_PROFILE_CONTAINER = "profile_container";

    public static final String EXTRA_DEVICE = "device";
    private RenameEditTextPreference mRenameDeviceNamePref;
    private LocalBluetoothManager mManager;
    private CachedBluetoothDevice mCachedDevice;
    private LocalBluetoothProfileManager mProfileManager;

    private PreferenceGroup mProfileContainer;// 相当于一个容器,用来装蓝牙设备
    private EditTextPreference mDeviceNamePref;

    private final HashMap<LocalBluetoothProfile, CheckBoxPreference> mAutoConnectPrefs
            = new HashMap<LocalBluetoothProfile, CheckBoxPreference>();

    private AlertDialog mDisconnectDialog;
    private boolean mProfileGroupIsRemoved;

    public BluetoothDeviceProfilesSettingsFragmentController(Fragment fragment) {
        super(fragment);
        mBluetoothDeviceProfilesSettingsFragment =
                (BluetoothDeviceProfilesSettingsFragment) fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        init(savedInstanceState);
    }

    @Override
    public void onResume() {
        mManager.setForegroundActivity(mBluetoothDeviceProfilesSettingsFragment.getActivity());
        mCachedDevice.registerCallback(this);
        if (mCachedDevice.getBondState() == BluetoothDevice.BOND_NONE)
            //            finish();
            refresh();
        EditText et = mDeviceNamePref.getEditText();
        if (et != null) {
            et.addTextChangedListener(mRenameDeviceNamePref);
            Dialog d = mDeviceNamePref.getDialog();
            if (d instanceof AlertDialog) {
                Button b = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
                b.setEnabled(et.getText().length() > 0);
            }
        }
    }

    @Override
    public void onPause() {
        mCachedDevice.unregisterCallback(this);
        mManager.setForegroundActivity(null);
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
        reset();
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRA_DEVICE, mCachedDevice.getDevice());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //        if (requestCode == ACTION_REQUEST_ENABLE && resultCode == -1) {
        //            startScanning();
        //        } else {
        //            ((BluetoothDeviceListActivity) mBluetoothDeviceListPreferenceFragment
        // .getActivity())
        //                    .goBack();
        //        }
    }


    public void onClick(View v) {
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// 点击返回图标事件
                ((BluetoothDeviceListActivity) mBluetoothDeviceProfilesSettingsFragment.getActivity())
                        .getFragmentManager().popBackStack();
                return true;
        }
        return false;
    }

    private void init(Bundle savedInstanceState) {
        BluetoothDevice device;
        if (savedInstanceState != null) {
            device = savedInstanceState.getParcelable(EXTRA_DEVICE);
        } else {
            Bundle args = mBluetoothDeviceProfilesSettingsFragment.getArguments();
            device = args.getParcelable(EXTRA_DEVICE);
        }

        Activity activity = mBluetoothDeviceProfilesSettingsFragment.getActivity();
        if (activity instanceof PreferenceActivity) {
            PreferenceActivity preferenceActivity = (PreferenceActivity) activity;
            if (preferenceActivity.onIsHidingHeaders() || !preferenceActivity.onIsMultiPane()) {
                activity.getActionBar().setTitle(R.string.bluetooth_device_advanced_title);
                activity.getActionBar().setHomeButtonEnabled(true);
                activity.getActionBar().setHomeButtonEnabled(true);
                activity.getActionBar().setDisplayShowTitleEnabled(true);
                // 决定左上角图标的右侧是否有向左的小箭头, true有小箭头，并且图标可以点击
                activity.getActionBar().setDisplayHomeAsUpEnabled(true);
                // 使左上角图标是否显示，如果设成false，则没有程序图标，仅仅就个标题，
                // 否则，显示应用程序图标，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME
                activity.getActionBar().setDisplayShowHomeEnabled(false);
            }
        }

        mBluetoothDeviceProfilesSettingsFragment
                .addPreferencesFromResource(R.xml.bluetooth_device_advanced);
        // 调用了并且设置为true时才会显示Menu
        mBluetoothDeviceProfilesSettingsFragment
                .setHasOptionsMenu(true);
        mBluetoothDeviceProfilesSettingsFragment.getPreferenceScreen().setOrderingAsAdded(false);
        mDeviceNamePref = (EditTextPreference) mBluetoothDeviceProfilesSettingsFragment
                .findPreference(KEY_RENAME_DEVICE);
        mProfileContainer = (PreferenceGroup) mBluetoothDeviceProfilesSettingsFragment
                .findPreference(KEY_PROFILE_CONTAINER);

        if (device == null) {
            Log.w(TAG, "Activity started without a remote Bluetooth device");
            //            finish();
            return;  // TODO: test this failure path
        }
        mRenameDeviceNamePref = new RenameEditTextPreference();
        mManager = LocalBluetoothManager.getInstance(mBluetoothDeviceProfilesSettingsFragment
                .getActivity());
        CachedBluetoothDeviceManager deviceManager =
                mManager.getCachedDeviceManager();
        mProfileManager = mManager.getProfileManager();
        mCachedDevice = deviceManager.findDevice(device);
        if (mCachedDevice == null) {
            Log.w(TAG, "Device not found, cannot connect to it");
            //            finish();
            return;  // TODO: test this failure path
        }

        String deviceName = mCachedDevice.getName();
        mDeviceNamePref.setSummary(deviceName);
        mDeviceNamePref.setText(deviceName);
        mDeviceNamePref.setOnPreferenceChangeListener(this);

        // Add a preference for each profile
        addPreferencesForProfiles();
    }

    private void reset() {
        if (mDisconnectDialog != null) {
            mDisconnectDialog.dismiss();
            mDisconnectDialog = null;
        }
    }

    private void addPreferencesForProfiles() {
        for (LocalBluetoothProfile profile : mCachedDevice.getConnectableProfiles()) {
            Preference pref = createProfilePreference(profile);
            mProfileContainer.addPreference(pref);
        }
        showOrHideProfileGroup();
    }

    // 显示或隐藏配置文件
    private void showOrHideProfileGroup() {
        int numProfiles = mProfileContainer.getPreferenceCount();
        if (!mProfileGroupIsRemoved && numProfiles == 0) {
            mBluetoothDeviceProfilesSettingsFragment.getPreferenceScreen().removePreference
                    (mProfileContainer);
            mProfileGroupIsRemoved = true;
        } else if (mProfileGroupIsRemoved && numProfiles != 0) {
            mBluetoothDeviceProfilesSettingsFragment.getPreferenceScreen().addPreference
                    (mProfileContainer);
            mProfileGroupIsRemoved = false;
        }
    }

    /**
     * Creates a checkbox preference for the particular profile. The key will be
     * the profile's name.
     *
     * @param profile The profile for which the preference controls.
     * @return A preference that allows the user to choose whether this profile
     * will be connected to.
     */
    private CheckBoxPreference createProfilePreference(LocalBluetoothProfile profile) {
        CheckBoxPreference pref = new CheckBoxPreference(mBluetoothDeviceProfilesSettingsFragment
                .getActivity());
        pref.setKey(profile.toString());
        pref.setTitle(profile.getNameResource(mCachedDevice.getDevice()));
        pref.setPersistent(false);
        pref.setOrder(getProfilePreferenceIndex(profile.getOrdinal()));
        pref.setOnPreferenceChangeListener(this);

        int iconResource = profile.getDrawableResource(mCachedDevice.getBtClass());
        if (iconResource != 0) {
            pref.setIcon(mBluetoothDeviceProfilesSettingsFragment.getResources().getDrawable
                    (iconResource));
        }

        /**
         * Gray out profile while connecting and disconnecting
         */
        pref.setEnabled(!mCachedDevice.isBusy());

        refreshProfilePreference(pref, profile);

        return pref;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        String key = preference.getKey();
        if (key.equals(KEY_UNPAIR)) {
            unpairDevice();
            //            finish();
            return true;
        }

        return false;
    }

    @Override// OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDeviceNamePref) {// 重命名,按下"确定"后
            mCachedDevice.setName((String) newValue);
        } else if (preference instanceof CheckBoxPreference) {// 相当于"配置文件"中的item单击事件
            LocalBluetoothProfile prof = getProfileOf(preference);
            onProfileClicked(prof, (CheckBoxPreference) preference);
            return false;   // checkbox will update from onDeviceAttributesChanged() callback
        } else {
            return false;
        }

        return true;
    }

    private void onProfileClicked(LocalBluetoothProfile profile, CheckBoxPreference profilePref) {
        BluetoothDevice device = mCachedDevice.getDevice();

        int status = profile.getConnectionStatus(device);
        boolean isConnected =
                status == BluetoothProfile.STATE_CONNECTED;

        if (isConnected) {
            askDisconnect(mBluetoothDeviceProfilesSettingsFragment.getActivity(), profile);
        } else {
            if (profile.isPreferred(device)) {
                // profile is preferred but not connected: disable auto-connect
                profile.setPreferred(device, false);
                refreshProfilePreference(profilePref, profile);
            } else {
                profile.setPreferred(device, true);
                mCachedDevice.connectProfile(profile);
            }
        }
    }

    private void askDisconnect(Context context,
                               final LocalBluetoothProfile profile) {
        // local reference for callback
        final CachedBluetoothDevice device = mCachedDevice;
        String name = device.getName();
        if (TextUtils.isEmpty(name)) {
            name = context.getString(R.string.bluetooth_device);
        }

        String profileName = context.getString(profile.getNameResource(device.getDevice()));

        String title = context.getString(R.string.bluetooth_disable_profile_title);
        String message = context.getString(R.string.bluetooth_disable_profile_message,
                profileName, name);

        DialogInterface.OnClickListener disconnectListener =
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        device.disconnect(profile);
                        profile.setPreferred(device.getDevice(), false);
                    }
                };

        mDisconnectDialog = Utils.showDisconnectDialog(context,
                mDisconnectDialog, disconnectListener, title, Html.fromHtml(message));
    }

    public void onDeviceAttributesChanged() {
        refresh();
    }

    private void refresh() {
        String deviceName = mCachedDevice.getName();
        mDeviceNamePref.setSummary(deviceName);
        mDeviceNamePref.setText(deviceName);

        refreshProfiles();
    }

    private void refreshProfiles() {
        for (LocalBluetoothProfile profile : mCachedDevice.getConnectableProfiles()) {
            CheckBoxPreference profilePref = (CheckBoxPreference)
                    mBluetoothDeviceProfilesSettingsFragment.findPreference(profile.toString());
            if (profilePref == null) {
                profilePref = createProfilePreference(profile);
                mProfileContainer.addPreference(profilePref);
            } else {
                refreshProfilePreference(profilePref, profile);
            }
        }
        for (LocalBluetoothProfile profile : mCachedDevice.getRemovedProfiles()) {
            Preference profilePref = mBluetoothDeviceProfilesSettingsFragment.findPreference
                    (profile.toString());
            if (profilePref != null) {
                Log.d(TAG, "Removing " + profile.toString() + " from profile list");
                mProfileContainer.removePreference(profilePref);
            }
        }
        showOrHideProfileGroup();
    }

    private void refreshProfilePreference(CheckBoxPreference profilePref,
                                          LocalBluetoothProfile profile) {
        BluetoothDevice device = mCachedDevice.getDevice();

        /*
         * Gray out checkbox while connecting and disconnecting
         */
        profilePref.setEnabled(!mCachedDevice.isBusy());
        profilePref.setChecked(profile.isPreferred(device));
        profilePref.setSummary(profile.getSummaryResourceForDevice(device));
    }

    private LocalBluetoothProfile getProfileOf(Preference pref) {
        if (!(pref instanceof CheckBoxPreference)) {
            return null;
        }
        String key = pref.getKey();
        if (TextUtils.isEmpty(key)) return null;

        try {
            return mProfileManager.getProfileByName(pref.getKey());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private int getProfilePreferenceIndex(int profIndex) {
        return mProfileContainer.getOrder() + profIndex * 10;
    }

    // 取消配对
    private void unpairDevice() {
        mCachedDevice.unpair();
    }

    private void stopDiscovery() {
        if (BTController.getInstance().getBluetoothAdapter().isDiscovering()) {
            BTController.getInstance().getBluetoothAdapter().cancelDiscovery();
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

    private class RenameEditTextPreference implements TextWatcher {
        public void afterTextChanged(Editable s) {
            Dialog d = mDeviceNamePref.getDialog();
            if (d instanceof AlertDialog) {
                ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s.length() > 0);
            }
        }

        // TextWatcher interface
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // not used
        }

        // TextWatcher interface
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // not used
        }
    }

}
