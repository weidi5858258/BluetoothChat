package com.weidi.bluetoothchat.controller.activitycontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.weidi.bluetoothchat.BTApplication;
import com.weidi.bluetoothchat.Constant;
import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.activity.ChatActivity;
import com.weidi.bluetoothchat.adapter.ChatMessageAdapter;
import com.weidi.bluetoothchat.controller.bluetoothcontroller.BTClient;
import com.weidi.bluetoothchat.controller.bluetoothcontroller.BTController;
import com.weidi.bluetoothchat.controller.bluetoothcontroller.BTServer;
import com.weidi.bluetoothchat.modle.MessageBean;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by root on 16-12-16.
 */

public class ChatActivityController extends BaseController {

    private static final String TAG = "ChatActivityController";
    private ChatActivity mChatActivity;
    private ArrayList<MessageBean> msgList = new ArrayList<MessageBean>();
    private ChatMessageAdapter mChatMessageAdapter;
    private ChatHandler mChatHandler;
    private InputMethodManager mInputMethodManager;

    public ChatActivityController(ChatActivity activity) {
        super(activity);
        mChatActivity = activity;
        mChatMessageAdapter = new ChatMessageAdapter(mContext, msgList);
        activity.chatList.setAdapter(mChatMessageAdapter);
        mChatHandler = new ChatHandler(this, Looper.getMainLooper());
        BTController.getInstance().setContext(mContext);
        if (((BTApplication) mContext.getApplicationContext())
                .getConnectionType() == Constant.CLIENT
                && BTClient.getInstance().isConnected()) {

            BTClient.getInstance().setHandler(mChatHandler);
        } else if (((BTApplication) mContext.getApplicationContext())
                .getConnectionType() == Constant.SERVER
                && BTServer.getInstance().getBtSocketList() != null
                && BTServer.getInstance().getBtSocketList().size() > 0) {

            BTServer.getInstance().setHandler(mChatHandler);
        } else if (((BTApplication) mContext.getApplicationContext())
                .getConnectionType() == Constant.CS) {

            if (BTClient.getInstance().isConnected()
                    && BTServer.getInstance().getBtSocketList() != null
                    && BTServer.getInstance().getBtSocketList().size() > 0) {

                BTClient.getInstance().setHandler(mChatHandler);
                BTServer.getInstance().setHandler(mChatHandler);
            } else if (BTClient.getInstance().isConnected()) {

                BTClient.getInstance().setHandler(mChatHandler);
            } else if (BTServer.getInstance().getBtSocketList() != null
                    && BTServer.getInstance().getBtSocketList().size() > 0) {

                BTServer.getInstance().setHandler(mChatHandler);
            }
        }
        mInputMethodManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        //        AndroidBug5497Workaround.assistActivity(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        init(savedInstanceState);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.xlistview_rl:
                break;
            case R.id.sendmessage_pet:

                break;
            case R.id.send_btn:// 发送
                String content = mChatActivity.mPasteEditText.getText().toString().trim();
                if (content.length() > 0) {
                    if (((BTApplication) mContext.getApplicationContext())
                            .getConnectionType() == Constant.CLIENT
                            && BTClient.getInstance().isConnected()) {

                        BTClient.getInstance().sendMessage(content, false);
                        mChatActivity.mPasteEditText.setText("");
                    } else if (((BTApplication) mContext.getApplicationContext())
                            .getConnectionType() == Constant.SERVER
                            && BTServer.getInstance().getBtSocketList() != null
                            && BTServer.getInstance().getBtSocketList().size() > 0) {

                        BTServer.getInstance().sendMessageAll(content, false);
                        mChatActivity.mPasteEditText.setText("");
                    } else if (((BTApplication) mContext.getApplicationContext())
                            .getConnectionType() == Constant.CS) {

                        if (BTClient.getInstance().isConnected()
                                && BTServer.getInstance().getBtSocketList() != null
                                && BTServer.getInstance().getBtSocketList().size() > 0) {

                            BTClient.getInstance().sendMessage(content, false);
                            // BTServer.getInstance().sendMessageAll(content, true);
                        } else if (BTClient.getInstance().isConnected()) {

                            BTClient.getInstance().sendMessage(content, false);
                        } else if (BTServer.getInstance().getBtSocketList() != null
                                && BTServer.getInstance().getBtSocketList().size() > 0) {

                            BTServer.getInstance().sendMessageAll(content, false);
                        }
                        mChatActivity.mPasteEditText.setText("");
                    } else {
                        showSysMsg("与远程设备已断开连接");
                    }
                } else {

                }
                break;
            case R.id.reset_btn:
                break;
            case R.id.disconnect_btn:
                break;
            case R.id.pair_btn:
                break;
            case R.id.disconnect_pair_btn:
                break;
            case R.id.cancel_pair_btn:
                break;
            case R.id.check_state_btn:
                break;
            // "连接"的话肯定是作为客户端的
            case R.id.connect_btn:
                break;
            case R.id.chat_btn:
                break;
            default:
                break;
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public Context getContext() {
        return mContext;
    }

    //屏幕高度
    private int screenHeight = 0;
    //软件盘弹起后所占高度阀值
    private int keyHeight = 0;

    private void init(Bundle savedInstanceState) {
        //获取屏幕高度
        screenHeight = mChatActivity.getWindowManager().getDefaultDisplay().getHeight();
        //阀值设置为屏幕高度的1/3
        keyHeight = screenHeight / 3;

        mChatActivity.rootLayout.addOnLayoutChangeListener(mOnLayoutChangeListener);

        mChatMessageAdapter.setListView(mChatActivity.chatList);
        mChatMessageAdapter.setHandler(mChatHandler);

        mChatActivity.chatList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        mChatActivity.chatList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_FLING) {

                } else if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {

                } else if (scrollState == SCROLL_STATE_IDLE) {//停止滚动
                    int listViewCount = mChatMessageAdapter.getCount();
                    int lastVisiblePosition = mChatActivity.chatList.getLastVisiblePosition();
                    if (mIsLastItemCompleteVisible(listViewCount, lastVisiblePosition)) {
                        if (mChatHandler != null) {
                            mChatHandler.sendEmptyMessage(2);
                        }
                    }
                }
            }

            @Override
            public void onScroll(
                    AbsListView view,
                    int firstVisibleItem,
                    int visibleItemCount,
                    int totalItemCount) {
            }
        });
    }

    //    private boolean hideKeyboard() {
    //        if (mInputMethodManager.isActive(searchEditText)) {
    //            //因为是在fragment下，所以用了getView()获取view，也可以用findViewById（）来获取父控件
    //            getView().requestFocus();
    //            //使其它view获取焦点.这里因为是在fragment下,所以便用了getView(),可以指定任意其它view
    //            mInputMethodManager.hideSoftInputFromWindow(mChatActivity.getCurrentFocus()
    //                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    //            return true;
    //        }
    //        return false;
    //    }

    private boolean mIsLastItemCompleteVisible(int listViewCount, int lastVisiblePosition) {
        if (lastVisiblePosition >= listViewCount - 1) {
            final int childIndex = lastVisiblePosition -
                    mChatActivity.chatList.getFirstVisiblePosition();
            final int childCount = mChatActivity.chatList.getChildCount();
            final int index = Math.min(childIndex, childCount - 1);
            final View lastVisibleChild = mChatActivity.chatList.getChildAt(index);
            if (lastVisibleChild != null) {
                return lastVisibleChild.getBottom() <= mChatActivity.chatList.getBottom();
            }
        }
        return false;
    }


    private void showSysPrompt() {
        mChatActivity.prompt_iv.setVisibility(View.VISIBLE);
    }

    private void hideSysPrompt() {
        mChatActivity.prompt_iv.setVisibility(View.GONE);
    }

    private void showSysMsg(String info) {
        mChatActivity.sysMsgTv.setVisibility(View.VISIBLE);
        mChatActivity.sysMsgTv.setText(info);
    }

    private void hideSysMsg() {
        mChatActivity.sysMsgTv.setVisibility(View.GONE);
    }

    public static class ChatHandler extends Handler {

        private WeakReference<ChatActivityController> mWeakReference;
        private Context mContext;

        private ChatHandler(ChatActivityController controller, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<ChatActivityController>(controller);
            mContext = controller.getContext();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ChatActivityController controller = mWeakReference.get();
            if (controller == null) {
                return;
            }
            switch (msg.what) {
                case 0:
                    controller.mChatMessageAdapter.addChatMsg((MessageBean) msg.obj);
                    break;
                case 1:
                    controller.showSysPrompt();
                    break;
                case 2:
                    controller.hideSysPrompt();
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
            }
        }

        public Context getContext() {
            return mContext;
        }

    }

    private View.OnLayoutChangeListener mOnLayoutChangeListener =
            new View.OnLayoutChangeListener() {

                @Override
                public void onLayoutChange(View v,
                                           int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    // old是改变前的左上右下坐标点值，没有old的是改变后的左上右下坐标点值

                    System.out.println("---------------------------------------------------------");
                    System.out.println(v);
                    System.out.println(oldLeft + " " + oldTop + " " + oldRight + " " + oldBottom);
                    System.out.println(left + " " + top + " " + right + " " + bottom);


                    // 现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起
                    if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {

                        System.out.println("监听到软键盘弹起");

                    } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {

                        System.out.println("监听到软件盘关闭");

                    }
                }

            };

    /**
     * 当ListView下方软键盘弹出时，ListView无法压缩显示的问题解决方案
     * 模式一，压缩模式
     * windowSoftInputMode的值如果设置为adjustResize，
     * 那么该Activity主窗口总是被调整大小以便留出软键盘的空间。
     * 主窗口布局重新进行measure和layout。大家会发现测量数据会发生变化。
     * 模式二，平移模式
     * windowSoftInputMode的值如果设置为adjustPan，
     * 那么该Activity主窗口并不调整屏幕的大小，只是做了平移。
     * 这里可能就是屏幕上移效果。
     * <p>
     * 这两种模式在开发中会倾向于第一种，因为效果要好，
     * 但是不一定设置了就有效啊。
     * 解决方法如下
     * 在你的Activity的oncreate()方法里调用
     * AndroidBug5497Workaround.assistActivity(this);
     * 即可。注意：在setContentView(R.layout.xxx)之后调用。
     */
    private static class AndroidBug5497Workaround {

        // For more information, see https://code.google.com/p/android/issues/detail?id=5497
        // To use this class, simply invoke assistActivity() on an Activity that already has its
        // content view set.

        private static void assistActivity(Activity activity) {
            new AndroidBug5497Workaround(activity);
        }

        private View mChildOfContent;
        private int usableHeightPrevious;
        private FrameLayout.LayoutParams frameLayoutParams;

        private AndroidBug5497Workaround(Activity activity) {
            FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
            mChildOfContent = content.getChildAt(0);
            mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                    .OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    possiblyResizeChildOfContent();
                }
            });
            frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
        }

        private void possiblyResizeChildOfContent() {
            int usableHeightNow = computeUsableHeight();
            if (usableHeightNow != usableHeightPrevious) {
                int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
                int heightDifference = usableHeightSansKeyboard - usableHeightNow;
                if (heightDifference > (usableHeightSansKeyboard / 4)) {
                    // keyboard probably just became visible
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                } else {
                    // keyboard probably just became hidden
                    frameLayoutParams.height = usableHeightSansKeyboard;
                }
                mChildOfContent.requestLayout();
                usableHeightPrevious = usableHeightNow;
            }
        }

        private int computeUsableHeight() {
            Rect r = new Rect();
            mChildOfContent.getWindowVisibleDisplayFrame(r);
            return (r.bottom - r.top);
        }

    }

}
