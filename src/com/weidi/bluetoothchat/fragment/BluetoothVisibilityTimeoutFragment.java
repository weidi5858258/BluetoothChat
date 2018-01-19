package com.weidi.bluetoothchat.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;

import com.weidi.bluetoothchat.R;

/**
 * Created by root on 17-2-8.
 */

public class BluetoothVisibilityTimeoutFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    public static final int DISCOVERABLE_TIMEOUT_NEVER = 0;
    private static final int DISCOVERABLE_TIMEOUT_TWO_MINUTES = 120;
    private static final int DISCOVERABLE_TIMEOUT_FIVE_MINUTES = 300;
    private static final int DISCOVERABLE_TIMEOUT_ONE_HOUR = 3600;

    private static final String SYSTEM_PROPERTY_DISCOVERABLE_TIMEOUT =
            "debug.bt.discoverable_time";
    // Bluetooth advanced settings screen was replaced with action bar items.
    // Use the same preference key for discoverable timeout as the old ListPreference.
    private static final String KEY_DISCOVERABLE_TIMEOUT = "bt_discoverable_timeout";

    private static final String VALUE_DISCOVERABLE_TIMEOUT_TWO_MINUTES = "twomin";
    private static final String VALUE_DISCOVERABLE_TIMEOUT_FIVE_MINUTES = "fivemin";
    private static final String VALUE_DISCOVERABLE_TIMEOUT_ONE_HOUR = "onehour";
    private static final String VALUE_DISCOVERABLE_TIMEOUT_NEVER = "never";

    private SharedPreferences mSharedPreferences;
    private int mTimeoutSecs = -1;

    public BluetoothVisibilityTimeoutFragment(Preference discoveryPreference) {
        mSharedPreferences = discoveryPreference.getSharedPreferences();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.bluetooth_visibility_timeout)
                // 单选
                .setSingleChoiceItems(
                        R.array.bluetooth_visibility_timeout_entries,
                        getDiscoverableTimeoutIndex(),
                        this)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    public void onClick(DialogInterface dialog, int which) {
        setDiscoverableTimeout(which);
        dismiss();
    }

    private int getDiscoverableTimeoutIndex() {
        int timeout = getDiscoverableTimeout();
        switch (timeout) {
            case DISCOVERABLE_TIMEOUT_TWO_MINUTES:
            default:
                return 0;

            case DISCOVERABLE_TIMEOUT_FIVE_MINUTES:
                return 1;

            case DISCOVERABLE_TIMEOUT_ONE_HOUR:
                return 2;

            case DISCOVERABLE_TIMEOUT_NEVER:
                return 3;
        }
    }

    private int getDiscoverableTimeout() {
        if (mTimeoutSecs != -1) {
            return mTimeoutSecs;
        }

        int timeout = SystemProperties.getInt(SYSTEM_PROPERTY_DISCOVERABLE_TIMEOUT, -1);
        if (timeout < 0) {
            String timeoutValue = mSharedPreferences.getString(KEY_DISCOVERABLE_TIMEOUT,
                    VALUE_DISCOVERABLE_TIMEOUT_TWO_MINUTES);

            if (timeoutValue.equals(VALUE_DISCOVERABLE_TIMEOUT_NEVER)) {
                timeout = DISCOVERABLE_TIMEOUT_NEVER;
            } else if (timeoutValue.equals(VALUE_DISCOVERABLE_TIMEOUT_ONE_HOUR)) {
                timeout = DISCOVERABLE_TIMEOUT_ONE_HOUR;
            } else if (timeoutValue.equals(VALUE_DISCOVERABLE_TIMEOUT_FIVE_MINUTES)) {
                timeout = DISCOVERABLE_TIMEOUT_FIVE_MINUTES;
            } else {
                timeout = DISCOVERABLE_TIMEOUT_TWO_MINUTES;
            }
        }
        mTimeoutSecs = timeout;
        return timeout;
    }

    private void setDiscoverableTimeout(int index) {
        String timeoutValue;
        switch (index) {
            case 0:
            default:
                mTimeoutSecs = DISCOVERABLE_TIMEOUT_TWO_MINUTES;
                timeoutValue = VALUE_DISCOVERABLE_TIMEOUT_TWO_MINUTES;
                break;

            case 1:
                mTimeoutSecs = DISCOVERABLE_TIMEOUT_FIVE_MINUTES;
                timeoutValue = VALUE_DISCOVERABLE_TIMEOUT_FIVE_MINUTES;
                break;

            case 2:
                mTimeoutSecs = DISCOVERABLE_TIMEOUT_ONE_HOUR;
                timeoutValue = VALUE_DISCOVERABLE_TIMEOUT_ONE_HOUR;
                break;

            case 3:
                mTimeoutSecs = DISCOVERABLE_TIMEOUT_NEVER;
                timeoutValue = VALUE_DISCOVERABLE_TIMEOUT_NEVER;
                break;
        }
        mSharedPreferences.edit().putString(KEY_DISCOVERABLE_TIMEOUT, timeoutValue).apply();
//        setEnabled(true);   // enable discovery and reset timer
    }

}
