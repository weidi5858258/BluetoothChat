package com.weidi.bluetoothchat.modle;

import com.weidi.dbutil.ClassVersion;
import com.weidi.dbutil.Primary;

/**
 * Created by root on 16-12-23.
 */

@ClassVersion(version = 2)
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
