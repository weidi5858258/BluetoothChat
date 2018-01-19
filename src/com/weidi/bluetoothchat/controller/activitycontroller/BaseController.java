package com.weidi.bluetoothchat.controller.activitycontroller;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * Created by root on 16-12-13.
 */

public abstract class BaseController {

    protected Context mContext;

    public abstract void onCreate(Bundle savedInstanceState);

    public View onCreateView(){return null;}

    public abstract void onResume();

    public abstract void onPause();

    public abstract void onStop();

    public abstract void onDestroy();

}
