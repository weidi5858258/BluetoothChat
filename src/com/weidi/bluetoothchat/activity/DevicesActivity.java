package com.weidi.bluetoothchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.controller.activitycontroller.DevicesActivityController;
import com.weidi.injectview.InjectLayout;
import com.weidi.injectview.InjectOnClick;
import com.weidi.injectview.InjectOnItemClick;
import com.weidi.injectview.InjectView;

@InjectLayout(R.layout.activity_devices)
public class DevicesActivity extends BaseActivity {

    @InjectView(R.id.bt_switch)
    public Switch bt_switch;

    @InjectView(R.id.bt_address_tv)
    public TextView bt_address_tv;

    @InjectView(R.id.as_service_btn)
    public Button as_service_btn;
    @InjectView(R.id.search_device_btn)
    public Button searth_device_btn;
    @InjectView(R.id.be_searched_btn)
    public Button be_searched_btn;
    @InjectView(R.id.cancel_search_btn)
    public Button cancel_search_btn;
    @InjectView(R.id.reset_btn)
    public Button reset_btn;
    @InjectView(R.id.disconnect_btn)
    public Button disconnect_btn;
    @InjectView(R.id.pair_btn)
    public Button pair_btn;
    @InjectView(R.id.disconnect_pair_btn)
    public Button disconnect_pair_btn;
    @InjectView(R.id.cancel_pair_btn)
    public Button cancel_pair_btn;
    @InjectView(R.id.check_state_btn)
    public Button check_state_btn;
    @InjectView(R.id.connect_btn)
    public Button connect_btn;
    @InjectView(R.id.chat_btn)
    public Button chat_btn;
    @InjectView(R.id.input_btn)
    public Button input_btn;
    @InjectView(R.id.cs_btn)
    public Button cs_btn;

    // test
    @InjectView(R.id.test1_btn)
    public Button test1_btn;
    @InjectView(R.id.test2_btn)
    public Button test2_btn;
    @InjectView(R.id.test3_btn)
    public Button test3_btn;
    @InjectView(R.id.test4_btn)
    public Button test4_btn;

    @InjectView(R.id.process_bar)
    public ProgressBar process_bar;
    @InjectView(R.id.bt_status_text)
    public TextView bt_status_text;
    @InjectView(R.id.bluetooth_device_recyclerview)
    public RecyclerView bluetooth_device_recyclerview;

    private DevicesActivityController mDevicesActivityController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevicesActivityController = new DevicesActivityController(this);
        mDevicesActivityController.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDevicesActivityController.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDevicesActivityController.onActivityResult(requestCode, resultCode, data);
    }

    @InjectOnClick({R.id.as_service_btn, R.id.search_device_btn, R.id.be_searched_btn,
            R.id.cancel_search_btn, R.id.reset_btn, R.id.disconnect_btn,
            R.id.pair_btn, R.id.disconnect_pair_btn, R.id.cancel_pair_btn,
            R.id.check_state_btn, R.id.connect_btn, R.id.chat_btn,
            R.id.input_btn, R.id.cs_btn,
            R.id.test1_btn, R.id.test2_btn, R.id.test3_btn, R.id.test4_btn})
    public void onClick(View view) {
        mDevicesActivityController.onClick(view);
    }

    //    @InjectOnItemClick(R.id.bluetooth_device_recyclerview)
    //    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    //        mDevicesActivityController.onItemClick(parent, view, position, id);
    //    }

    @Override
    public void onBackPressed() {
        exit();
    }

    /**
     * 连续双击back退出
     */
    private long PRESS_TIME;
    private static final int TIME = 2000;

    /**
     * 在2秒之间连续按两次“后退”键，才能退出应用。
     */
    private void exit() {
        // 因为第一次按的时候“PRESS_TIME”为“0”，所以肯定大于2000
        if (SystemClock.uptimeMillis() - PRESS_TIME > TIME) {
            Toast.makeText(this,
                    "再按一次 退出: " + getResources().getString(R.string.app_name),
                    Toast.LENGTH_SHORT).show();
            PRESS_TIME = SystemClock.uptimeMillis();
        } else {
            // 按第二次的时候如果距离前一次的时候在2秒之内，那么就走下面的路线
            super.onBackPressed();
            this.finish();
            exitActivity();
        }
    }

}
