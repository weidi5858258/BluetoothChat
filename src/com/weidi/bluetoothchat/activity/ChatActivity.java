package com.weidi.bluetoothchat.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.controller.activitycontroller.ChatActivityController;
import com.weidi.injectview.InjectLayout;
import com.weidi.injectview.InjectOnClick;
import com.weidi.injectview.InjectView;
import com.weidi.bluetoothchat.widget.PasteEditText;

@InjectLayout(R.layout.activity_chat)
public class ChatActivity extends BaseActivity {

    public static final int PIC_FRAGMENT_INDEX = 0x000;
    public static final int VOICE_FRAGMENT_INDEX = 0x001;
    public static final int FACE_FRAGMENT_INDEX = 0x002;
    public static final int CARD_FRAGMENT_INDEX = 0x003;

    private static final String SMILE_UTILS_CLASS
            = "com.xianglin.fellowvillager.app.chat.utils.SmileUtils";

    @InjectView(R.id.root_layout)
    public RelativeLayout rootLayout;

    @InjectView(R.id.xlistview_rl)
    public RelativeLayout xlistview_rl;

    @InjectView(R.id.list)
//    public XListView chatList;
    public ListView chatList;

    @InjectView(R.id.sys_msg_tv)
    public TextView sysMsgTv;

    @InjectView(R.id.send_btn)
    public Button btnSend;

    @InjectView(R.id.sendmessage_pet)
    public PasteEditText mPasteEditText;

    @InjectView(R.id.prompt_iv)
    public ImageView prompt_iv;

    /************************************************************/


    @InjectView(R.id.bar_bottom)
    public LinearLayout barBottom;

    @InjectView(R.id.rl_bottom)
    public LinearLayout rlBottom;

    @InjectView(R.id.btn_set_mode_voice)
    public Button btnSetModeVoice;

    @InjectView(R.id.btn_set_mode_keyboard)
    public Button btnSetModeKeyboard;

    @InjectView(R.id.btn_press_to_speak)
    public LinearLayout btnPressToSpeak;

    @InjectView(R.id.edittext_layout)
    public RelativeLayout edittextLayout;


    @InjectView(R.id.iv_emoticons_normal)
    public ImageView ivEmoticonsNormal;

    @InjectView(R.id.iv_emoticons_checked)
    public ImageView ivEmoticonsChecked;

    @InjectView(R.id.btn_more)
    public Button btnMore;


    @InjectView(R.id.ll_bottom_menu)
    public LinearLayout ll_bottom_menu;

    @InjectView(R.id.pb_load_more)
    public ProgressBar pbLoadMore;


    @InjectView(R.id.btn_send_voice)
    public Button btnSendVoice;

    //    @InjectView(R.id.top_bar)
    //    TopView topView;

    @InjectView(R.id.ll_chat_pic)
    public LinearLayout ll_chat_pic;
    @InjectView(R.id.ll_chat_voice)
    public LinearLayout ll_chat_voice;
    @InjectView(R.id.ll_chat_face)
    public LinearLayout ll_chat_face;
    @InjectView(R.id.ll_chat_card)
    public LinearLayout ll_chat_card;
    @InjectView(R.id.ll_menu_container)
    public LinearLayout ll_menu_container;

    @InjectView(R.id.iv_pic_icon)
    public ImageView mPicIconIv;

    @InjectView(R.id.iv_voice_icon)
    public ImageView mVoiceIconIv;

    @InjectView(R.id.iv_face_icon)
    public ImageView mFaceIconIv;

    @InjectView(R.id.iv_card_icon)
    public ImageView mCardIconIv;

    public static final String COPY_IMAGE = "EASEMOBIMG";
    public static final int REQUEST_CODE_COPY_AND_PASTE = 11;

    private ChatActivityController mChatActivityController = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChatActivityController = new ChatActivityController(this);
        mChatActivityController.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @InjectOnClick({R.id.xlistview_rl, R.id.sendmessage_pet, R.id.send_btn})
    public void onClick(View view) {
        mChatActivityController.onClick(view);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        exitActivity();
    }

}
