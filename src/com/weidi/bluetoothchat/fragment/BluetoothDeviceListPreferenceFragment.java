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

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.controller.fragmentcontroller
        .BluetoothDeviceListPreferenceFragmentController;
import com.weidi.bluetoothchat.fragment.base.BasePreferenceFragment;
import com.weidi.log.Log;

/**
 * Parent class for settings fragments that contain a list of Bluetooth
 * devices.
 */
public class BluetoothDeviceListPreferenceFragment extends BasePreferenceFragment {

    private static final String TAG = "BluetoothDeviceListPreferenceFragment";

    private BluetoothDeviceListPreferenceFragmentController
            mBluetoothDeviceListPreferenceFragmentController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(): savedInstanceState = " + savedInstanceState);
        mBluetoothDeviceListPreferenceFragmentController =
                new BluetoothDeviceListPreferenceFragmentController(this);
        mBluetoothDeviceListPreferenceFragmentController.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        mBluetoothDeviceListPreferenceFragmentController.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        mBluetoothDeviceListPreferenceFragmentController.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        mBluetoothDeviceListPreferenceFragmentController.onStop();
    }

    @Override
    public void onDestroy() {
        mBluetoothDeviceListPreferenceFragmentController.onDestroy();
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mBluetoothDeviceListPreferenceFragmentController
                .onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onPreferenceTreeClick(
            PreferenceScreen preferenceScreen,
            Preference preference) {
        if (mBluetoothDeviceListPreferenceFragmentController
                .onPreferenceTreeClick(preferenceScreen, preference)) {
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mBluetoothDeviceListPreferenceFragmentController.onCreateOptionsMenu(menu, inflater);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mBluetoothDeviceListPreferenceFragmentController.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * true表示被隐藏了,false表示被显示了
     * Fragment:
     * 被show()或者hide()时才会回调这个方法,
     * 被add()或者popBackStack()时不会回调这个方法
     * 弹窗时不会被回调(是由当前的Fragment弹出的一个DialogFragment)
     * 如果是弹出一个DialogActivity窗口,则应该会被回调,
     * 因为当前Fragment所在的Activity的生命周期发生了变化,则当前Fragment的生命周期也会发生变化
     *
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged():hidden = " + hidden);
        if (hidden) {
            //相当于Fragment的onResume
        } else {
            //相当于Fragment的onPause
            onResume();
        }
    }

}
