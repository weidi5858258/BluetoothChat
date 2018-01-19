package com.weidi.bluetoothchat;

/**
 * Created by root on 16-7-30.
 */

public interface Constant {

    int NONE = 0;
    int CLIENT = 1;
    int SERVER = 2;
    int FIXEDTHREADPOOLCOUNT = 15;
    int ACTION_REQUEST_ENABLE = 100;
    int MSG_HOME_OWNERSHIP_MY = 0;
    int MSG_HOME_OWNERSHIP_OTHER = 1;

    int INPUT_BT_ADDRESS_REQUESTCODE = 1000;
    int INPUT_BT_ADDRESS_RESULTCODE = 1001;
    String REQUESTCODE = "requtestCode";

    String DB_NAME = "bluetoothchat.db";
    String SHAREDPREFERENCES = "record_value";
    String DBVERSION = "dbversion";
    String SEPARATOR = "@@####@@";

    int CHATTYPE_SINGLE = 0;//发送给用户
    int CHATTYPE_GROUP = 1;//发送到群主
    String CHATTYPE_ADD = "group_add";//建群
    String CHATTYPE_JOIN = "group_join";//添加群成员
    int UPDATE_GROUP_TITLE = 0X100; //修改群名称;
    int MSGSTATUS_SEND = -1; //发送中
    int MSGSTATUS_OK = 0; //发送成功
    int MSGSTATUS_FAIL = 1; //发送失败
    int MSGSTATUS_READ = 2; //已读
    int MSGSTATUS_UNREAD = 3; //未读
    int MSGSTATUS_RECEIVE = 4; //接收中

}
