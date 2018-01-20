/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.weidi.bluetoothchat.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import com.weidi.bluetoothchat.fragment.base.BasePreferenceFragment;
import com.weidi.log.Log;

/**
 * This preference fragment presents the user with all of the profiles
 * for a particular device, and allows them to be individually connected
 * (or disconnected). "已配对的蓝牙设备"的设置界面
 */
public final class BluetoothDeviceProfilesSettingsFragment extends BasePreferenceFragment {

    private static final String TAG = "BluetoothDeviceProfilesSettingsFragment";

//    private BluetoothDeviceProfilesSettingsFragmentController
//            mBluetoothDeviceProfilesSettingsFragmentController;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(): savedInstanceState = " + savedInstanceState);
//        mBluetoothDeviceProfilesSettingsFragmentController =
//                new BluetoothDeviceProfilesSettingsFragmentController(this);
//        mBluetoothDeviceProfilesSettingsFragmentController.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
//        mBluetoothDeviceProfilesSettingsFragmentController.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
//        mBluetoothDeviceProfilesSettingsFragmentController.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
//        mBluetoothDeviceProfilesSettingsFragmentController.onStop();
    }

    @Override
    public void onDestroy() {
//        mBluetoothDeviceProfilesSettingsFragmentController.onDestroy();
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        mBluetoothDeviceProfilesSettingsFragmentController.onSaveInstanceState(outState);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
//        if (mBluetoothDeviceProfilesSettingsFragmentController
//                .onPreferenceTreeClick(screen, preference)) {
//            return true;
//        }
        return super.onPreferenceTreeClick(screen, preference);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (mBluetoothDeviceProfilesSettingsFragmentController.onOptionsItemSelected(item)) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged():hidden = "+hidden);
        if (hidden) {
            //相当于Fragment的onResume
        } else {
            //相当于Fragment的onPause
            onResume();
        }
    }

}
