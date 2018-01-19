package com.weidi.bluetoothchat.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.ListView;

import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.modle.BTDevice;
import com.weidi.customadapter.CustomListViewAdapter;
import com.weidi.customadapter.CustomRecyclerViewAdapter;
import com.weidi.customadapter.CustomViewHolder;
import com.weidi.log.Log;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by root on 16-12-16.
 */

public class DevicesAdapter extends CustomRecyclerViewAdapter<BluetoothDevice> {

    private static final String TAG = "DevicesAdapter";
    private ArrayList<BTDevice> btDList;
    private int btDListSize = 0;

    public DevicesAdapter(Context context, ArrayList<BluetoothDevice> items, int layoutResId) {
        super(context, items, layoutResId);
    }

    @Override
    public void onBind(
            CustomViewHolder viewHolder,
            int viewType,
            int layoutPosition,
            BluetoothDevice device) {
        if (viewHolder == null) {
            return;
        }

        String name = device.getName();

        if (TextUtils.isEmpty(name)) {
            BTDevice tDevice = null;
            for (int i = 0; i < btDListSize; i++) {
                BTDevice tempDevice = btDList.get(i);
                if (tempDevice.remoteDeviceAddress.equals(device.getAddress())) {
                    tDevice = tempDevice;
                    break;
                }
            }
            if (tDevice != null) {
                viewHolder.setText(R.id.bt_name_tv, "蓝牙名称: " + tDevice.remoteDeviceName);
                viewHolder.setText(R.id.bt_address_tv, "蓝牙地址: " + tDevice.remoteDeviceAddress);
                // viewHolder.setText(R.id.bt_type_tv, "蓝牙类型: " + tDevice.remoteDeviceType);
                viewHolder.setText(R.id.bt_bond_state_tv, "蓝牙状态: "
                        + getBondState(tDevice.remoteDeviceBondState));
                return;
            }
        }

        viewHolder.setText(R.id.bt_name_tv, "蓝牙名称: " + device.getName());
        viewHolder.setText(R.id.bt_address_tv, "蓝牙地址: " + device.getAddress());
        //        viewHolder.setText(R.id.bt_type_tv, "蓝牙类型: " + device.getType());
        viewHolder.setText(R.id.bt_bond_state_tv,
                "蓝牙状态: " + getBondState(device.getBondState()));
    }

    public void setData(ArrayList<BTDevice> list, int size) {
        btDList = list;
        btDListSize = size;
    }

    public void addDevice(int position, BluetoothDevice device) {
        if (position == 0) {
            add(0, device);
        } else {
            add(device);
        }
    }

    public boolean isEmpty() {
        return false;
    }

    public synchronized void refresh(RecyclerView listView, int index, BluetoothDevice device) {
        int size = getItemCount();
        BluetoothDevice btDevice = null;
        for (int i = 0; i < size; i++) {
            BluetoothDevice tempDevice = getData().get(i);
            if (device.getAddress().equals(tempDevice.getAddress())) {
                btDevice = tempDevice;
                break;
            }
        }
        Iterator<BluetoothDevice> iter = getData().iterator();
        while (iter.hasNext()) {
            BluetoothDevice tempDevice = iter.next();
            if (btDevice.getAddress().equals(tempDevice.getAddress())) {
                iter.remove();
                if (index == 0) {// 配对成功的情况
                    add(0, device);
                    listView.smoothScrollToPosition(0);//
                    //                    listView.smoothScrollToPositionFromTop(0, 0);//
                    break;
                }
                size = getItemCount();
                // 取消配对后把这个设备移动到已配对的设备后面
                for (int i = 0; i < size; i++) {
                    BluetoothDevice btd = getData().get(i);
                    if (btd.getBondState() != BluetoothDevice.BOND_BONDED) {
                        if (i != 0) {
                            add(device);
                        } else {
                            add(0, device);
                        }
                        break;
                    }
                }
                add(device);
                break;
            }
        }
        iter = null;
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
