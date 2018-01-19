/**
 * 乡邻小站
 * Copyright (c) 2011-2015 Xianglin,Inc.All Rights Reserved.
 */
package com.weidi.bluetoothchat.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;


/**
 * 样式设置工具类
 *
 * @author songdiyuan
 * @version $Id: StyleManager.java, v 1.0.0 2015-11-13 下午7:59:15 songdy Exp $
 */
public class StyleManager {
    private static final String TAG = "StyleManager";

    /**
     * 高度大于0时 表明在4.4中开启沉浸式状态栏
     */
    public static int statusBarHeight = 0;

    private static StyleManager mInstance;
    private Typeface mTypefaceCN, mTypefaceEN;
    private static Context mContext;
    private int adjustFontSize;

    private StyleManager() {
        init();
    }


    /**
     * 获取StyleManager实例
     *
     * @return StyleManager
     */
    public static synchronized StyleManager getInstance(Context context) {
        mContext = context;
        if (mInstance == null) {
            mInstance = new StyleManager();
        }
        return mInstance;

    }

    /**
     * 初始化
     */
    private void init() {
        //		mTypefaceCN = Typeface.createFromAsset(mContext.getAssets(),
        // "paone/assets/css/FZZYJ.ttf");
        //		mTypefaceEN = Typeface.createFromAsset(mContext.getAssets(),
        // "paone/assets/css/Gotham-Book.ttf");
    }


    /**
     * 判断是否中文字符开头
     *
     * @param str
     * @return
     */
    private boolean isChinese(String str) {
        if (TextUtils.isEmpty(str)) {
            return true;
        }

        if (String.valueOf(str.charAt(0)).getBytes().length == 2) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置当前布局控件字体
     *
     * @param root
     */
    public void setViewFonts(ViewGroup root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View v = root.getChildAt(i);
            if (v instanceof Button) {
                //				if(isChinese(((Button) v).getText().toString())){
                //					((Button) v).setTypeface(mTypefaceCN);
                //				}else{
                //					((Button) v).setTypeface(mTypefaceEN);
                //				}
                //			}  else if (v instanceof ClearEditText) {
                //				if(isChinese(((ClearEditText) v).getText().toString())){
                //					((ClearEditText) v).setTypeface(mTypefaceCN);
                //				}else{
                //					((ClearEditText) v).setTypeface(mTypefaceEN);
                //				}

            } else if (v instanceof EditText) {
                //				if(isChinese(((EditText) v).getText().toString())){
                //					((EditText) v).setTypeface(mTypefaceCN);
                //				}else{
                //					((EditText) v).setTypeface(mTypefaceEN);
                //				}
                //				((EditText) v).setHintTextColor(mContext.getResources().getColor(R
                // .color.textgrey));
                //				((EditText) v).setTextColor(mContext.getResources().getColor(R
                // .color.uedgrey));
            } else if (v instanceof TextView) {
                //				if(isChinese(((TextView) v).getText().toString())){
                //					((TextView) v).setTypeface(mTypefaceCN);
                //				}else{
                //					((TextView) v).setTypeface(mTypefaceEN);
                //				}
            } else if (v instanceof ViewGroup) {
                setViewFonts((ViewGroup) v);
            }
        }
    }

    /**
     * 获取顶层layout
     *
     * @param act
     * @return
     */
    public ViewGroup getContentView(Activity act) {
        ViewGroup systemContent = (ViewGroup) act.getWindow().getDecorView()
                .findViewById(android.R.id.content);
        ViewGroup content = null;
        if (systemContent.getChildCount() > 0
                && systemContent.getChildAt(0) instanceof ViewGroup) {
            content = (ViewGroup) systemContent.getChildAt(0);
        }
        return content;
    }

    /**
     * 遍历设置字体(注:传入Activity顶层Layout)
     *
     * @param viewGroup
     */
    public void setViewSize(ViewGroup viewGroup) {
        if (adjustFontSize == 0) {
            adjustFontSize = adjustFontSize();
        }
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof ViewGroup) {
                setViewSize((ViewGroup) v);
            } else if (v instanceof Button) {// 按钮加大这个一定要放在TextView上面，因为Button也继承了TextView
                // ( (Button)v ).setTextSize(adjustFontSize+2);
            } else if (v instanceof TextView) {
                //				if (v.getId() == R.id.tv_title_text) {// 顶部标题
                //					((TextView) v).setTextSize(adjustFontSize + 4);
                //				} else {
                // ( (TextView)v ).setTextSize(adjustFontSize);
                //				}
            }
        }
    }

    /**
     * 获取字体大小
     *
     * @return
     */
    private int adjustFontSize() {
        int screenWidth = DeviceInfo.getInstance().getScreenWidth();
        int screenHeight = DeviceInfo.getInstance().getScreenHeight();
        screenWidth = screenWidth > screenHeight ? screenWidth : screenHeight;
        /**
         * 1. 在视图的 onsizechanged里获取视图宽度，一般情况下默认宽度是480，所以计算一个缩放比率 rate = (float)
         * w/480 w是实际宽度
         * 2.然后在设置字体尺寸时 paint.setTextSize((int)(5*rate));
         * 5是在分辨率宽为480 下需要设置的字体大小 实际字体大小 = 默认字体大小 x rate
         */
        int rate = (int) (5 * (float) screenWidth / 480); // 我自己测试这个倍数比较适合，当然你可以测试后再修改
        return rate < 15 ? 15 : rate; // 字体太小也不好看的
    }

    /**
     * 设置状态栏样式
     */
    @TargetApi(19)
    public void setStatusBarStyle(Activity activity) {

					/*针对引导界面全屏特殊处理*/
        String activityName = getClassName(activity);

        View contentView =
                activity.findViewById(android.R.id.content);
        if (contentView != null) {
            ViewGroup viewGroup = (ViewGroup) contentView;
            View childView = viewGroup.getChildAt(0);
            if (childView != null) {
                childView.setFitsSystemWindows(true);

                //					if("FundWithYZTBEarningsDisplayActivity".equals(activityName)){
                //						tintManager.setStatusBarTintResource(R.color
                // .fund_yztb_bg_showcase_color);
                //					}else if("PrivateInvestmentIntroduceActivity".equals
                // (activityName)){
                //						tintManager.setStatusBarTintResource(R.color
                // .privateInvestmentIntroduceActivity_mainlayout_start);
                //					}else{

                //					}

            }
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//
//            setTranslucentStatus(true, activity);
//
// /*		    SystemBarTintManager tintManager = new SystemBarTintManager(activity);
//            tintManager.setStatusBarTintEnabled(true);
//			tintManager.setStatusBarTintResource(R.color.app_title_bg);*/
//
//            statusBarHeight = getStatusBarHeight(activity);
//        }
    }

    /**
     * 获取状态栏高度-
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {

        if (statusBarHeight > 0) {
            return statusBarHeight;
        }

        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (statusBarHeight <= 0) {
                statusBarHeight = dip2px(context, 25);//默认高度
            }
        }

        return statusBarHeight;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public String getClassName(Object o) {

        Class<? extends Object> c = o.getClass();
        String cName = c.getName();
        String[] tmp = cName.split("\\.");
        cName = tmp[tmp.length - 1];

        return cName;
    }

    /**
     * 设置半透明状态
     *
     * @param on
     * @param activity
     */
    @TargetApi(19)
    private void setTranslucentStatus(boolean on, Activity activity) {

//        Window win = activity.getWindow();
//        WindowManager.LayoutParams winParams = win.getAttributes();
//        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
//        if (on) {
//            winParams.flags |= bits;
//        } else {
//            winParams.flags &= ~bits;
//        }
//        win.setAttributes(winParams);
    }

    /**
     * Getter method for property <tt>mTypefaceCN</tt>.
     *
     * @return property value of mTypefaceCN
     */
    public Typeface getmTypefaceCN() {
        return mTypefaceCN;
    }

    /**
     * Getter method for property <tt>mTypefaceEN</tt>.
     *
     * @return property value of mTypefaceEN
     */
    public Typeface getmTypefaceEN() {
        return mTypefaceEN;
    }

}
