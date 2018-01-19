package com.weidi.bluetoothchat.modle;

import com.weidi.bluetoothchat.dbutil.DbVersion;
import com.weidi.bluetoothchat.dbutil.Primary;

/**
 * Created by root on 16-12-23.
 */

@DbVersion(version = 2)
public class BTDevice {

    public int _id;

    public int remoteDeviceBondState;

    public int remoteDeviceType;

    public String remoteDeviceName;

    @Primary
    public String remoteDeviceAddress;

    public String remoteDeviceAlias;

    public String remoteDeviceBluetoothClass;

}
