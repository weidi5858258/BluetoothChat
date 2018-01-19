package com.weidi.bluetoothchat.adapter;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.weidi.bluetoothchat.R;
import com.weidi.customadapter.CustomAdapter;
import com.weidi.customadapter.CustomViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 16-12-16.
 */

public class DevicesAdapter2 extends CustomAdapter<BluetoothDevice> {

    private static final String TAG = "DevicesAdapter2";

    public DevicesAdapter2(Context context, List<BluetoothDevice> items, int layoutResId) {
        super(context, items, layoutResId);
    }

    @Override
    public CustomViewHolder onCreate(final View convertView, final ViewGroup parent, int viewType) {
        // These code show how to add click listener to item view of ViewHolder.
        final CustomViewHolder holder = super.onCreate(convertView, parent, viewType);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "position = " + holder.getAdapterPosition());
                Log.d(TAG, "convertView = " + convertView);
                Log.d(TAG, "parent = " + parent);
            }
        });
        return holder;
    }

    @Override
    public void onBind(
            CustomViewHolder holder,
            int viewType,
            int layoutPosition,
            BluetoothDevice device) {
        if(holder == null){
            return;
        }
        holder.setText(R.id.bt_name_tv, "蓝牙名称:" + device.getName());
        holder.setText(R.id.bt_address_tv, "蓝牙地址:" + device.getAddress());
        holder.setText(R.id.bt_type_tv, "蓝牙类型:" + device.getType());
        holder.setText(R.id.bt_bond_state_tv, "蓝牙状态:" + getBondState(device.getBondState()));
    }

    private String getBondState(int state) {
        String st = "未知状态";
        switch (state) {
            case BluetoothDevice.BOND_BONDING:
                st = "配对中";
                break;
            case BluetoothDevice.BOND_BONDED:
                st = "已配对";
                break;
            case BluetoothDevice.BOND_NONE:
                st = "未配对";
                break;
        }
        return st;
    }

}
