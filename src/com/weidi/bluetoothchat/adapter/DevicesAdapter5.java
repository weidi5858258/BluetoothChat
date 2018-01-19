package com.weidi.bluetoothchat.adapter;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.modle.BTDevice;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by root on 16-12-16.
 */

public class DevicesAdapter5 extends BaseAdapter {

    private static final String TAG = "DevicesAdapter5";
    private Context mContext;
    private ArrayList<BluetoothDevice> btList;
    private ArrayList<BTDevice> btDList;
    private int btDListSize = 0;

    public DevicesAdapter5(Context context, ArrayList<BluetoothDevice> list) {
        if (context == null || list == null) {
            throw new NullPointerException("DevicesAdapter中有空指针");
        }
        mContext = context;
        btList = list;
    }

    @Override
    public int getCount() {
        return btList.size();
    }

    @Override
    public Object getItem(int position) {
        return btList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;
        BluetoothDevice device = btList.get(position);
        if (convertView == null) {
            view = View.inflate(mContext, R.layout.item_bluetooth_device, null);
            viewHolder = new ViewHolder();
            viewHolder.bt_name_tv = (TextView) view.findViewById(R.id.bt_name_tv);
            viewHolder.bt_address_tv = (TextView) view.findViewById(R.id.bt_address_tv);
            viewHolder.bt_type_tv = (TextView) view.findViewById(R.id.bt_type_tv);
            viewHolder.bt_bond_state_tv = (TextView) view.findViewById(R.id.bt_bond_state_tv);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) convertView.getTag();
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
                viewHolder.bt_name_tv.setText("蓝牙名称: " + tDevice.remoteDeviceName);
                viewHolder.bt_address_tv.setText("蓝牙地址: " + tDevice.remoteDeviceAddress);
                viewHolder.bt_type_tv.setText("蓝牙类型: " + tDevice.remoteDeviceType);
                viewHolder.bt_bond_state_tv.setText("蓝牙状态: "
                        + getBondState(tDevice.remoteDeviceBondState));
                return view;
            }
        }

        viewHolder.bt_name_tv.setText("蓝牙名称: " + device.getName());
        viewHolder.bt_address_tv.setText("蓝牙地址: " + device.getAddress());
//        viewHolder.bt_type_tv.setText("蓝牙类型: " + device.getType());
        viewHolder.bt_bond_state_tv.setText("蓝牙状态: " + getBondState(device.getBondState()));

        return view;
    }

    public void setData(ArrayList<BTDevice> list, int size) {
        btDList = list;
        btDListSize = size;
    }

    public void addDevice(int position, BluetoothDevice device) {
        if (position == 0) {
            btList.add(0, device);
        } else {
            btList.add(device);
        }
        notifyDataSetChanged();
    }

    public synchronized void refresh(ListView listView, int index, BluetoothDevice device) {
        int size = btList.size();
        BluetoothDevice btDevice = null;
        for (int i = 0; i < size; i++) {
            BluetoothDevice tempDevice = btList.get(i);
            if (device.getAddress().equals(tempDevice.getAddress())) {
                btDevice = tempDevice;
                break;
            }
        }
        Iterator<BluetoothDevice> iter = this.btList.iterator();
        while (iter.hasNext()) {
            BluetoothDevice tempDevice = iter.next();
            if (btDevice.getAddress().equals(tempDevice.getAddress())) {
                iter.remove();
                if (index == 0) {// 配对成功的情况
                    btList.add(0, device);
                    listView.smoothScrollToPositionFromTop (0, 0);//
                    break;
                }
                size = btList.size();
                // 取消配对后把这个设备移动到已配对的设备后面
                for (int i = 0; i < size; i++) {
                    BluetoothDevice btd = btList.get(i);
                    if (btd.getBondState() != BluetoothDevice.BOND_BONDED) {
                        if (i != 0) {
                            btList.add(i, device);
                        } else {
                            btList.add(0, device);
                        }
                        break;
                    }
                }
                btList.add(device);
                break;
            }
        }
        iter = null;
        notifyDataSetChanged();
    }

    public void clear() {
        btList.clear();
        notifyDataSetChanged();
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

    private static class ViewHolder {
        private TextView bt_name_tv, bt_address_tv, bt_type_tv, bt_bond_state_tv;
    }


}
