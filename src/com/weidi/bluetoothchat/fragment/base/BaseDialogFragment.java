package com.weidi.bluetoothchat.fragment.base;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.weidi.bluetoothchat.Constant;
import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.listener.OnResultListener;
import com.weidi.injectview.InjectOnClick;
import com.weidi.injectview.InjectUtils;
import com.weidi.injectview.InjectView;
import com.weidi.log.Log;

import static com.weidi.bluetoothchat.R.id.bt_address_et;

/**
 * 在子类中只需要覆写这样一个周期方法就行了，其他周期方法没什么必要了：
 *
 * @Override
 *  public View onCreateView(LayoutInflater inflater,
 * ViewGroup container,
 * Bundle savedInstanceState) {
 * return super.onCreateView(inflater, container, savedInstanceState);
 * }
 */
public abstract class BaseDialogFragment extends DialogFragment {

    private static final String TAG = "BaseDialogFragment";
    private Activity mActivity;
    private Context mContext;
    private int requestCode;
    private OnResultListener mOnResultListener;

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
        Log.d(TAG, "onCreate(): savedInstanceState = " + savedInstanceState);
        setRetainInstance(true);
        setCancelable(false);
        if (provideStyle() < 0 || provideStyle() > 3) {
            setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        } else {
            setStyle(provideStyle(), 0);
        }
        requestCode =
                getArguments() != null ? getArguments().getInt(Constant.REQUESTCODE) : -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView(): savedInstanceState = " + savedInstanceState);
        View view = inflater.inflate(provideLayout(), container);
        InjectUtils.inject(this, view);
        view.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "dismiss()");
                dismiss();
            }
        });
        initData(savedInstanceState);
        return view;
    }

    /*public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(getLayout(), null);
        builder.setView(mView);
        return builder.create();
    }*/

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

    public int getRequestCode() {
        return requestCode;
    }

    public void setOnResultListener(OnResultListener listener) {
        mOnResultListener = listener;
    }

    public OnResultListener getOnResultListener() {
        return mOnResultListener;
    }

    /**
     * 样式选这么几种就行了
     * DialogFragment:
     * public static final int STYLE_NORMAL = 0;
     * public static final int STYLE_NO_TITLE = 1;
     * public static final int STYLE_NO_FRAME = 2;
     * public static final int STYLE_NO_INPUT = 3;
     * 使用: DialogFragment.STYLE_NO_TITLE(一般也是选择这个选项的)
     */
    protected abstract int provideStyle();

    protected abstract int provideLayout();

    protected abstract void initData(Bundle savedInstanceState);

}
