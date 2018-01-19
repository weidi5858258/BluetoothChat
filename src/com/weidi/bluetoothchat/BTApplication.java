package com.weidi.bluetoothchat;

import android.app.Application;

import com.weidi.dbutil.DbUtils;
import com.weidi.dbutil.SimpleDao;
import com.weidi.log.Log;
import com.weidi.bluetoothchat.modle.BTDevice;
import com.weidi.threadpool.CustomRunnable;
import com.weidi.threadpool.ThreadPool;

/**
 * Created by root on 16-12-16.
 */

public class BTApplication extends Application {

    private static int CONNECTIONTYPE = Constant.NONE;


    @Override
    public void onCreate() {
        super.onCreate();
        SimpleDao.setContext(getApplicationContext());
        ThreadPool.getCachedThreadPool().execute(new CustomRunnable() {
            @Override
            public void run() {
                DbUtils.getInstance().createOrUpdateDBWithVersion(
                        getApplicationContext(),
                        new Class[]{BTDevice.class});
                Runtime rt = Runtime.getRuntime();
                long maxMemory = rt.maxMemory();
                Log.d("maxMemory:", Long.toString(maxMemory / (1024 * 1024)));
            }
        });
    }

    public void setConnectionType(int type) {
        CONNECTIONTYPE = type;
    }

    public int getConnectionType() {
        return CONNECTIONTYPE;
    }

}
