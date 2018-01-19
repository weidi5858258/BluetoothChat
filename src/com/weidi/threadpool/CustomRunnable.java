package com.weidi.threadpool;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class CustomRunnable implements java.lang.Runnable {

    private static final String TAG = "CustomRunnable";
    /**
     * 取消任务：false表示没有取消任务，任务在不断地进行；设为true时表示要取消任务了，不想再进行任务
     */
    //    private boolean cancleTask = false;
    /**  */
    //    private boolean cancleException = false;
    /**  */
    private MHandler mHandler = null;

    private static final int RUNBEFORE = 0;
    private static final int RUNAFTER = 1;
    private static final int RUNERROR = 2;
    private CallBack mCallBack = null;

    private int runBeforeSleepTime = 0;
    private int runAfterSleepTime = 0;

    public static interface CallBack {
        /**
         * 主线程
         */
        void runBefore();

        /**
         * 子线程
         */
        Object running() throws IOException;

        /**
         * 主线程
         */
        void runAfter(Object object);

        /**
         * 主线程
         */
        void runError();
    }

    public CustomRunnable() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("CustomRunnable对象的创建不是在主线程中进行!!!");
        }
        if (mHandler == null) {
            mHandler = new MHandler(this);
        }
    }

    @Override
    public void run() {
        if (mCallBack != null) {
            try {
                mHandler.sendEmptyMessage(RUNBEFORE);
                if (runBeforeSleepTime != 0) {
                    SystemClock.sleep(runBeforeSleepTime);
                    runBeforeSleepTime = 0;
                }
                Object object = mCallBack.running();
                Message msg = mHandler.obtainMessage();
                msg.what = RUNAFTER;
                msg.obj = object;
                mHandler.sendMessageDelayed(msg, runAfterSleepTime);
            } catch (Exception e) {
                mHandler.sendEmptyMessage(RUNERROR);
                Log.e(TAG, "CustomRunnable:run()方法出现异常: " + e);
            } finally {
//                if (runBeforeSleepTime == 0 && runAfterSleepTime == 0) {
//                    release();
//                }
            }
        }
    }

    /**
     * 设为false时取消任务正常进行；设为true时取消任务
     */
    //    public CustomRunnable setCancleTaskUnit(boolean cancleTask) {
    //        this.cancleTask = cancleTask;
    //        return this;
    //    }
    public CustomRunnable setCallBack(CallBack callBack) {
        mCallBack = callBack;
        return this;
    }

    public CustomRunnable setRunBeforeSleepTime(int seconds) {
        if (seconds < 0) {
            runBeforeSleepTime = 0;
        } else {
            runBeforeSleepTime = seconds;
        }
        return this;
    }

    public CustomRunnable setRunAfterSleepTime(int seconds) {
        if (seconds < 0) {
            runAfterSleepTime = 0;
        } else {
            runAfterSleepTime = seconds;
        }
        return this;
    }

    private static class MHandler extends Handler {

        private WeakReference<CustomRunnable> mCustomRunnable;

        private MHandler(CustomRunnable customRunnable) {
            mCustomRunnable = new WeakReference<CustomRunnable>(customRunnable);
        }

        @Override
        public void handleMessage(Message msg) {
            CustomRunnable customRunnable = mCustomRunnable.get();
            if (customRunnable == null || customRunnable.mCallBack == null) {
                return;
            }
            switch (msg.what) {
                case RUNBEFORE:
                    customRunnable.mCallBack.runBefore();
                    break;
                case RUNAFTER:
                    customRunnable.mCallBack.runAfter(msg.obj);
                    customRunnable.runAfterSleepTime = 0;
                    break;
                case RUNERROR:
                    customRunnable.runBeforeSleepTime = 0;
                    customRunnable.runAfterSleepTime = 0;
                    customRunnable.mCallBack.runError();
                    break;
            }
            super.handleMessage(msg);
        }

    }

    private void release() {

    }


    /**
     直接复制使用
     ThreadPool.getCachedThreadPool().execute(
     new CustomRunnable().setCallBack(new CustomRunnable.CallBack() {
    @Override public void runBefore() {

    }

    @Override public Object running() throws IOException {

    }

    @Override public void runAfter(Object object) {

    }

    @Override public void runError() {

    }

    }));
     */

}
