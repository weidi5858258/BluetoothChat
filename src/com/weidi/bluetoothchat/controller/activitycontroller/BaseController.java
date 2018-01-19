package com.weidi.bluetoothchat.controller.activitycontroller;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * Created by root on 16-12-13.
 */

public abstract class BaseController {

    protected Context mContext;

    public BaseController(Object object) {
        if (object == null) {
            throw new NullPointerException("BaseController's object is null!");
        }
        if (object instanceof Activity) {
            mContext = ((Activity)object).getApplicationContext();
        } else if (object instanceof Fragment) {
            mContext = ((Fragment)object).getActivity().getApplicationContext();
        }
    }

    public abstract void onCreate(Bundle savedInstanceState);

    public View onCreateView() {
        return null;
    }

    public abstract void onResume();

    public abstract void onPause();

    public abstract void onStop();

    public abstract void onDestroy();

}
