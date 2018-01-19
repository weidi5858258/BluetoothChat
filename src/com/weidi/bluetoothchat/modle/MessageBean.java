package com.weidi.bluetoothchat.modle;

import com.weidi.dbutil.ClassVersion;
import com.weidi.dbutil.Primary;

import java.io.Serializable;

@ClassVersion(version = 0)
public class MessageBean implements Serializable {

    @Primary
    public int _id;

    public String msgSender;

    public String msgBTAddress;

    public int msgHomeOwnership;

    public String msgContent;

    public String msgFilePath;

    public long msgSendTime;

    public boolean isReceiveMSg;

    public int canSaveMyMsgType;

    public int canSaveOtherMsgType;

}
