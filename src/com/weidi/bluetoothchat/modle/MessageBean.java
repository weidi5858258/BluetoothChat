package com.weidi.bluetoothchat.modle;

import com.weidi.bluetoothchat.dbutil.DbVersion;
import com.weidi.bluetoothchat.dbutil.Primary;

import java.io.Serializable;

@DbVersion(version = 0)
public class MessageBean implements Serializable {

    @Primary
    public int _id;

    public String msgSender;

    public String msgBTAdress;

    public int msgHomeOwnership;

    public String msgContent;

    public boolean isReceiveMSg;

}
