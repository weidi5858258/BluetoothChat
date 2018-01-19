package com.weidi.bluetoothchat.fragment.base;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weidi.bluetoothchat.activity.BaseActivity;
import com.weidi.bluetoothchat.activity.BluetoothDeviceListActivity;
import com.weidi.injectview.InjectUtils;
import com.weidi.log.Log;

/**
 * Created by weidi on 16-5-6.
 */
public abstract class BasePreferenceFragment extends PreferenceFragment {

    private static final String TAG = "BasePreferenceFragment";
    private static final boolean DEBUG = false;
    private Activity mActivity;
    private Context mContext;
    private BackHandlerInterface mBackHandlerInterface;

    public interface BackHandlerInterface {

        void setSelectedFragment(BasePreferenceFragment selectedFragment, String fragmentTag);

    }

    public BasePreferenceFragment() {
        super();
    }

    /*********************************
     * Created
     *********************************/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (DEBUG) Log.d(TAG, "onAttach(): activity = " + activity);
        mActivity = activity;
        mContext = activity.getApplicationContext();
        if (!(activity instanceof BackHandlerInterface)) {
            throw new ClassCastException("Hosting Activity must implement BackHandlerInterface");
        } else {
            mBackHandlerInterface = (BackHandlerInterface) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 一旦我们设置 setRetainInstance(true)，意味着在 Activity 重绘时，
         * 我们的 Fragment 不会被重复绘制，也就是它会被“保留”。为了验证
         * 其作用，我们发现在设置为 true 状态时，旋转屏幕，Fragment 依然是
         * 之前的 Fragment。而如果将它设置为默认的 false，那么旋转屏幕时
         * Fragment 会被销毁，然后重新创建出另外一个 fragment 实例。并且
         * 如官方所说，如果 Fragment 不重复创建，意味着 Fragment 的
         * onCreate 和 onDestroy 方法不会被重复调用。所以在旋转屏
         * Fragment 中，我们经常会设置 setRetainInstance(true)，
         * 这样旋转时 Fragment 不需要重新创建。
         */
        setRetainInstance(true);
        if (DEBUG) Log.d(TAG, "onCreate(): savedInstanceState = " + savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onActivityCreated(): savedInstanceState = " + savedInstanceState);
    }

    /*********************************
     * Started
     *********************************/

    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) Log.d(TAG, "onStart()");
    }

    /*********************************
     * Resumed
     *********************************/

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume()");
        if (mBackHandlerInterface != null) {
            mBackHandlerInterface.setSelectedFragment(this, this.getClass().getName());
        }
    }

    /*********************************
     * Paused
     *********************************/

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.d(TAG, "onPause()");
    }

    /*********************************
     * Stopped
     *********************************/

    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG) Log.d(TAG, "onStop()");
    }

    /*********************************
     * Destroyed
     *********************************/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (DEBUG) Log.d(TAG, "onDestroyView()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (DEBUG) Log.d(TAG, "onDetach()");
    }

    public Activity getAttachedActivity() {
        return mActivity;
    }

    public Context getMyContext() {
        if (mContext == null) {
            if (getAttachedActivity() != null) {
                mContext = getAttachedActivity().getApplicationContext();
            } else if (getActivity() != null) {
                mContext = getActivity().getApplicationContext();
            }
        }
        return mContext;
    }

    public BackHandlerInterface getBackHandlerInterface() {
        return mBackHandlerInterface;
    }

    public void enterFragment() {
        if (getAttachedActivity() != null) {
            ((BluetoothDeviceListActivity) getAttachedActivity()).enterActivity();
        }
    }

    public void exitFragment() {
        if (getAttachedActivity() != null) {
            ((BluetoothDeviceListActivity) getAttachedActivity()).exitActivity();
        }
    }

    /**
     * 所有继承BackHandledFragment的子类都将在这个方法中实现物理Back键按下后的逻辑
     * FragmentActivity捕捉到物理返回键点击事件后会首先询问Fragment是否消费该事件
     * 如果没有Fragment消息时FragmentActivity自己才会消费该事件
     */
    public abstract boolean onBackPressed();

}
