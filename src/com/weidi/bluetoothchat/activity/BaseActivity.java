package com.weidi.bluetoothchat.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.controller.activitycontroller.BaseController;
import com.weidi.injectview.InjectUtils;

public class BaseActivity extends Activity {

    protected Context mContext = null;
    protected BaseController mController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        InjectUtils.inject(this, null);
        mContext = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 在2秒之间连续按两次“后退”键，才能退出应用。
     * 放到IndexActivity中去
     */
//    protected void exit() {
//        // 因为第一次按的时候“PRESS_TIME”为“0”，所以肯定大于2000
//        if (SystemClock.uptimeMillis() - PRESS_TIME > TIME) {
//            Toast.makeText(this,
//                    "再按一次 退出" + getResources().getString(R.string.app_name),
//                    Toast.LENGTH_SHORT).show();
//            PRESS_TIME = SystemClock.uptimeMillis();
//        } else {
//            // 按第二次的时候如果距离前一次的时候在2秒之内，那么就走下面的路线
//            //			APPManager.getInstance().exit();
//
//        }
//    }

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


    protected void animFromBottomToTop() {
        try {
            // overridePendingTransition(R.anim.roll_up, R.anim.roll);
        } catch (Exception e) {
        }
    }

}