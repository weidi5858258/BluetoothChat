package com.weidi.bluetoothchat.fragment.base;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.activity.BaseActivity;
import com.weidi.injectview.InjectUtils;
import com.weidi.log.Log;

/**
 * Created by weidi on 16-5-6.
 */
public abstract class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";
    private Activity mActivity;
    private Context mContext;

    public BaseFragment() {
        super();
    }

    /*********************************
     * Created
     *********************************/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mContext = activity.getApplicationContext();
        Log.d(TAG, "onAttach(): activity = " + activity);
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
        Log.d(TAG, "onCreate(): savedInstanceState = " + savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView(): savedInstanceState = " + savedInstanceState);
        View view = inflater.inflate(provideLayout(), container);
        InjectUtils.inject(this, view);
        initData(savedInstanceState);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated(): savedInstanceState = " + savedInstanceState);
    }

    /*********************************
     * Started
     *********************************/

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    /*********************************
     * Resumed
     *********************************/

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    /*********************************
     * Paused
     *********************************/

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    /*********************************
     * Stopped
     *********************************/

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    /*********************************
     * Destroyed
     *********************************/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach()");
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

    public void enterFragment() {
        if (getAttachedActivity() != null) {
            ((BaseActivity) getAttachedActivity()).enterActivity();
        }
    }

    public void exitFragment() {
        if (getAttachedActivity() != null) {
            ((BaseActivity) getAttachedActivity()).exitActivity();
        }
    }

    protected abstract int provideLayout();

    /**
     * 供子类调用，初始化组件，统一接口
     *
     * @return
     */
//    protected abstract void initView(View view, Bundle savedInstanceState);

    /**
     * 供子类调用，初始化数据，统一接口
     *
     * @return
     */
    protected abstract void initData(Bundle savedInstanceState);

}
