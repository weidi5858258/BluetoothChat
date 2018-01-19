/**
 * 乡邻小站
 * Copyright (c) 2011-2015 xianglin,Inc.All Rights Reserved.
 */
package com.weidi.bluetoothchat.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.weidi.bluetoothchat.Constant;
import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.controller.activitycontroller.ChatActivityController;
import com.weidi.bluetoothchat.modle.MessageBean;
import com.weidi.bluetoothchat.widget.ChatImageView;
import com.weidi.bluetoothchat.widget.XListView;

import java.util.ArrayList;

public class ChatMessageAdapter extends BaseAdapter {

    private final static String TAG = "MessageChatAdapter";
    private long XLID;
    private String toChatId;
    private LayoutInflater inflater;
    private Activity activity;
    //    MessageDBHandler messageDBHandler;
    //    ContactDBHandler contactDBHandler;

    private Context mContext;
    private int chatType;//单聊 群聊
    private String chatName;
    private String headerImgId;//对方头像ID

    private ChatActivityController.ChatHandler mChatHandler;
    //    private XListView chatList;
    private ListView chatList;
    /**
     * 消息类型  文本 语音 图片 文件等等
     **/
    private ArrayList<MessageBean> msgList;


    private int mCurrentPlayingIndex = -1;

    /**
     * @param context
     */
    public ChatMessageAdapter(Context context, ArrayList<MessageBean> list) {
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
        this.msgList = list;
    }

    @Override
    public Object getItem(int position) {
        return msgList.get(position);
    }

    /**
     * 获取item类型  多种类型item必须，不然会数据错乱
     */
    public int getItemViewType(int position) {
//        MessageBean message = msgList.get(position);
//        int msgType = message.msgType;
//        if (msgType == TEXT) {
//            return message.msgStatus > SEND_MESSAGE ? MESSAGE_TYPE_RECV_TXT
//                    : MESSAGE_TYPE_SENT_TXT;
//        }
        //        if (msgType == IMAGE) {
        //            return message.msgStatus > SEND_MESSAGE ? MESSAGE_TYPE_RECV_IMAGE
        //                    : MESSAGE_TYPE_SENT_IMAGE;
        //        }
        //        if (msgType == LOCATION) {
        //            return message.msgStatus > SEND_MESSAGE ? MESSAGE_TYPE_RECV_LOCATION
        //                    : MESSAGE_TYPE_SENT_LOCATION;
        //        }
        //        if (msgType == VOICE) {
        //            return message.msgStatus > SEND_MESSAGE ? MESSAGE_TYPE_RECV_VOICE
        //                    : MESSAGE_TYPE_SENT_VOICE;
        //        }
        //        if (msgType == VIDEO) {
        //            return message.msgStatus > SEND_MESSAGE ? MESSAGE_TYPE_RECV_VIDEO
        //                    : MESSAGE_TYPE_SENT_VIDEO;
        //        }
        //        if (msgType == FILE) {
        //            return message.msgStatus > SEND_MESSAGE ? MESSAGE_TYPE_RECV_FILE
        //                    : MESSAGE_TYPE_SENT_FILE;
        //        }
        //        if (msgType == IDCARD) {
        //            return message.msgStatus > SEND_MESSAGE ? MESSAGE_TYPE_RECV_IDCARD
        //                    : MESSAGE_TYPE_SENT_IDCARD;
        //        }
        //        if (msgType == WEBSHOPPING) {
        //            return message.msgStatus > SEND_MESSAGE ? MESSAGE_TYPE_RECV_WEBSHOPPING
        //                    : MESSAGE_TYPE_SENT_WEBSHOPPING;
        //        }
        //        if (msgType == SYS) {
        //            return message.msgStatus > SEND_MESSAGE ? MESSAGE_TYPE_RECV_SYS
        //                    : MESSAGE_TYPE_SENT_SYS;
        //        }

        return -1;// invalid
    }

    @Override
    public int getCount() {
        return msgList.size();
    }

    /**
     * 多种类型item必须，不然会数据错乱
     **/
    public int getViewTypeCount() {
        return 18;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final MessageBean bean = msgList.get(position);
        if (bean == null) {
            return convertView;
        }

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = createView(bean.msgHomeOwnership);
            initHolder(holder, convertView, bean);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        fillingData(holder, bean);

        return convertView;
    }

    private void fillingData(ViewHolder holder, MessageBean bean) {
        holder.chatcontent_tv.setText(bean.msgContent);
    }

    public void setListView(ListView listView) {
        this.chatList = listView;
    }

    //消息类型
    public static final int SYS = 0;
    public static final int TEXT = 1;
    public static final int IMAGE = 2;
    public static final int VOICE = 3;
    public static final int VIDEO = 4;
    public static final int IDCARD = 5;
    public static final int REDBUNDLE = 6;
    public static final int WEBSHOPPING = 7;

    public static final int FILE = 111;
    public static final int LOCATION = 1112;

    //消息状态
    public static final int SEND_MESSAGE = Constant.MSGSTATUS_FAIL;//0成功 1失败
    public static final int RECEIVE_MESSAGE = 2;//2,3

    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
    private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
    private static final int MESSAGE_TYPE_SENT_VOICE = 6;
    private static final int MESSAGE_TYPE_RECV_VOICE = 7;
    private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
    private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
    private static final int MESSAGE_TYPE_SENT_FILE = 10;
    private static final int MESSAGE_TYPE_RECV_FILE = 11;

    private static final int MESSAGE_TYPE_SENT_IDCARD = 12;
    private static final int MESSAGE_TYPE_RECV_IDCARD = 13;
    private static final int MESSAGE_TYPE_SENT_WEBSHOPPING = 14;
    private static final int MESSAGE_TYPE_RECV_WEBSHOPPING = 15;
    private static final int MESSAGE_TYPE_SENT_SYS = 16;
    private static final int MESSAGE_TYPE_RECV_SYS = 17;


    public void refresh() {
        notifyDataSetChanged();
    }

    public void addChatMsg(MessageBean msg) {
        this.msgList.add(msg);
        refresh();

        if (this.chatList != null) {
            int listViewCount = getCount();
            int lastVisiblePosition = this.chatList.getLastVisiblePosition();
            if (listViewCount - 2 != lastVisiblePosition) {
                if (!mIsLastItemCompleteVisible(listViewCount, lastVisiblePosition)) {
                    if (mChatHandler != null && msg.isReceiveMSg) {
                        mChatHandler.sendEmptyMessage(1);
                    }
                }
            }
        }
    }

    public void setChatData(ArrayList<MessageBean> dataList) {
        this.msgList = dataList;
    }

    public void setHandler(ChatActivityController.ChatHandler handler) {
        this.mChatHandler = handler;
    }

    private boolean mIsLastItemCompleteVisible(int listViewCount, int lastVisiblePosition) {
        if (lastVisiblePosition >= listViewCount - 1) {
            final int childIndex = lastVisiblePosition -
                    this.chatList.getFirstVisiblePosition();
            final int childCount = this.chatList.getChildCount();
            final int index = Math.min(childIndex, childCount - 1);
            final View lastVisibleChild = this.chatList.getChildAt(index);
            if (lastVisibleChild != null) {
                return lastVisibleChild.getBottom() <= this.chatList.getBottom();
            }
        }
        return false;
    }

    private View createView(int msgHomeOwnership) {
        switch (msgHomeOwnership) {
            case Constant.MSG_HOME_OWNERSHIP_MY:
                return inflater.inflate(R.layout.row_sent_message, null);
            case Constant.MSG_HOME_OWNERSHIP_OTHER:
                return inflater.inflate(R.layout.row_received_message, null);
            default:
                return null;
        }
    }

    private View createViewByMessage(int MsgType, int direct, int position) {
        switch (MsgType) {
            case LOCATION:
                //                return direct > SEND_MESSAGE ? inflater
                //                        .inflate(R.layout.row_received_location, null) : inflater
                //                        .inflate(R.layout.row_sent_location, null);
            case IMAGE:
                //                return direct > SEND_MESSAGE ? inflater
                //                        .inflate(R.layout.row_received_picture, null) : inflater
                //                        .inflate(R.layout.row_sent_picture, null);

            case VOICE:
                //                return direct > SEND_MESSAGE ? inflater
                //                        .inflate(R.layout.row_received_voice, null) : inflater
                //                        .inflate(R.layout.row_sent_voice, null);
            case VIDEO:
                //                return direct > SEND_MESSAGE ? inflater
                //                        .inflate(R.layout.row_received_video, null) : inflater
                //                        .inflate(R.layout.row_sent_video, null);
            case FILE:
                //                return direct > SEND_MESSAGE ? inflater
                //                        .inflate(R.layout.row_received_file, null) : inflater
                //                        .inflate(R.layout.row_sent_file, null);
            case IDCARD:
                //                return direct > SEND_MESSAGE ? inflater
                //                        .inflate(R.layout.row_received_id_card, null) : inflater
                //                        .inflate(R.layout.row_sent_id_card, null);
            case WEBSHOPPING:
                //                return direct > SEND_MESSAGE ? inflater
                //                        .inflate(R.layout.row_received_web_shopping, null) :
                // inflater
                //                        .inflate(R.layout.row_sent_web_shopping, null);
            case SYS:
                //                return  direct > SEND_MESSAGE ? inflater
                //                        .inflate(R.layout.row_received_sysmsg, null) : inflater
                //                        .inflate(R.layout.row_received_sysmsg, null);
            default:
                return direct > SEND_MESSAGE ? inflater
                        .inflate(R.layout.row_received_message, null) : inflater
                        .inflate(R.layout.row_sent_message, null);
        }
    }


    private void handlerItemByType(final ChatMessageAdapter.ViewHolder holder, final MessageBean
            messageBean, final int position) {
        //LogCatLog.e(TAG, "MsgType=" + messageBean.msgType + ",position=" + position + ",
        // content=" + messageBean.msgContent + ",msgStatus=" + messageBean.msgStatus);
        // 设置内容
//        final int msgStatus = messageBean.msgStatus;
//
//        switch (messageBean.msgType) {
//            case TEXT:
//                //                handlerTextMsg(holder, messageBean);
//                break;
//
//            case IMAGE:
//                //                handlerImageMsg(holder, messageBean);
//                break;
//
//            case VOICE:// 播放音频
//                //                handlerVoiceMsg(holder, messageBean, position);
//                break;
//            case IDCARD:
//                //                handlerIDCARD(holder, messageBean);
//                break;
//            case WEBSHOPPING:
//                //                handlerWEBSHOPPING(holder, messageBean);
//                break;
//            case FILE:
//                break;
//            case SYS:
//                //                handlerSysMsg(holder, messageBean);
//                break;
//
//        }
    }

    private void initHolder(ViewHolder holder, View convertView, MessageBean bean) {
        holder.chatcontent_tv = (TextView) convertView.findViewById(R.id.chatcontent_tv);
    }

    private void initHolder(MessageBean bean, ViewHolder holder, View convertView) {
//        try {
//            int MsgType = bean.msgType;
//
//            if (MsgType == TEXT) {
//            } else if (MsgType == LOCATION) {
//            } else if (MsgType == IMAGE) {
//            } else if (MsgType == VOICE) {
//            } else if (MsgType == VIDEO) {
//            } else if (MsgType == FILE) {
//            } else if (MsgType == IDCARD) {
//            } else if (MsgType == WEBSHOPPING) {
//            } else if (MsgType == SYS) {
//            }
//        } catch (Exception e) {
//
//        }

    }

    private static class ViewHolder {
        TextView chatcontent_tv;

        ImageView iv;
        RelativeLayout rl_voice;//语音长度控制
        ChatImageView chatImageView;
        TextView tvSubTitle;
        TextView webRealPrice;//真实价格
        TextView webOldPrice;//原始价格
        ImageView cardImage;//卡片图片
        ProgressBar pb;
        ImageView staus_iv;
        ImageView head_iv;
        TextView tv_userId;
        ImageView playBtn;
        TextView timeLength;
        TextView size;
        LinearLayout container_status_btn;
        LinearLayout ll_container;
        ImageView iv_read_status;
        // 显示已读回执状态
        TextView tv_ack;
        // 显示送达回执状态
        TextView tv_delivered;

        TextView tv_file_name;
        TextView tv_file_size;
        TextView tv_file_download_state;

        TextView tv_sys_msg;
    }

}