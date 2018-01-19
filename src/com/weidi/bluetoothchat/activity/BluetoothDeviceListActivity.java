package com.weidi.bluetoothchat.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.view.View;

import com.weidi.bluetoothchat.Constant;
import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.fragment.BluetoothDeviceListPreferenceFragment;
import com.weidi.bluetoothchat.fragment.FragOperManager;
import com.weidi.bluetoothchat.fragment.base.BaseFragment;
import com.weidi.bluetoothchat.fragment.base.BasePreferenceFragment;
import com.weidi.eventbus.EventBus;
import com.weidi.log.Log;

import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 17-2-8.
 */

public class BluetoothDeviceListActivity extends PreferenceActivity
        implements BasePreferenceFragment.BackHandlerInterface {

    private BluetoothDeviceListPreferenceFragment mBluetoothDeviceListPreferenceFragment;
    private FragOperManager mFragOperManager;
    private BasePreferenceFragment mBasePreferenceFragment;
    private Bundle savedInstanceState;
    private String mFragmentTag;

    private static HashMap<String, Integer> mFragmentBackTypeSMap;

    static {
        mFragmentBackTypeSMap = new HashMap<String, Integer>();
        mFragmentBackTypeSMap.put("BluetoothDeviceProfilesSettingsFragment", Constant.POPBACKSTACK);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetoothdevicelist);

        if (savedInstanceState != null) {
            this.savedInstanceState = savedInstanceState;
        } else {
            mBluetoothDeviceListPreferenceFragment = new BluetoothDeviceListPreferenceFragment();
            getFragOperManager().enter(mBluetoothDeviceListPreferenceFragment, null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         做这个事的原因:
         如果有多个Fragment开启着,并且相互之间是显示和隐藏,不是弹出,
         那么页面在MainFragment时关闭屏幕,然后在点亮屏幕后,
         MainFragment的onResume()方法比其他Fragment的onResume()方法要先执行,
         这样要后退时导致MainFragment的onBackPressed()方法执行不了.
         */
        if (savedInstanceState != null) {
            final String fragmentTag = savedInstanceState.getString("FragmentTag");
            List<Fragment> fragmentsList = mFragOperManager.getFragmentsList();
            int count = fragmentsList.size();
            for (int i = 0; i < count; i++) {
                final Fragment fragment = fragmentsList.get(i);
                if (fragment != null && fragment.getClass().getSimpleName().equals(fragmentTag)) {
                    if (fragment instanceof BaseFragment) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (((BasePreferenceFragment) fragment)
                                        .getBackHandlerInterface() != null) {
                                    ((BasePreferenceFragment) fragment).getBackHandlerInterface()
                                            .setSelectedFragment(
                                                    (BasePreferenceFragment) fragment, fragmentTag);
                                }
                            }
                        }, 500);
                    }
                    break;
                }
            }
            savedInstanceState = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        savedInstanceState = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mBluetoothDeviceListPreferenceFragment.onActivityResult(requestCode, resultCode, data);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString("FragmentTag", mFragmentTag);
        this.savedInstanceState = outState;
    }

    public void onClick(View view) {
    }

    @Override
    public void onBackPressed() {
        if (mBasePreferenceFragment == null) {
            goBack();
        }

        String fragmentName = mBasePreferenceFragment.getClass().getSimpleName();
        for (String key : mFragmentBackTypeSMap.keySet()) {
            if (key.equals(fragmentName)) {
                int type = mFragmentBackTypeSMap.get(key);
                EventBus.getDefault().post(type, mBasePreferenceFragment);
                break;
            }
        }

        if (mBasePreferenceFragment.onBackPressed()) {
            goBack();
        }

        //        if (mBasePreferenceFragment == null || !mBasePreferenceFragment.onBackPressed()) {
        //            getFragmentManager().popBackStack();
        //            //            if(getFragmentManager().getBackStackEntryCount() == 0){
        //            //                goBack();
        //            //            }else{
        //            //                getFragmentManager().popBackStack();
        //            //            }
        //        } else {
        //            goBack();
        //        }
    }

    @Override
    public void setSelectedFragment(BasePreferenceFragment selectedFragment, String fragmentTag) {
        mBasePreferenceFragment = selectedFragment;
        mFragmentTag = fragmentTag;
    }

    public FragOperManager getFragOperManager(){
        if(mFragOperManager == null){
            mFragOperManager = new FragOperManager(this, R.id.container);
        }
        return mFragOperManager;
    }

    public void goBack() {
        finish();
        exitActivity();
    }

    /**
     * 打开页面时，页面从右往左滑入
     * 底下的页面不需要有动画
     */
    public void enterActivity() {
        try {
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭页面时，页面从左往右滑出
     */
    public void exitActivity() {
        try {
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
