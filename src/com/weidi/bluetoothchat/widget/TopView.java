package com.weidi.bluetoothchat.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.util.StyleManager;
import com.weidi.bluetoothchat.util.SystemBarTintManager;


/**
 * 顶部公共View,主要为了适配Android的沉浸式状态栏,解决部分手机状态栏高度 不同
 *
 * @author pengyang
 * @version v 1.0.0 2015/11/18 16:23  XLXZ Exp $
 */
public class TopView extends RelativeLayout {
    private LayoutInflater inflater;
    private Context mContext;
    private View layout;

    private ImageView leftImage; // 左边的图片
    private ImageView rightImage; // 右边的图片

    private LinearLayout backLayout;
    private LinearLayout rightLayout;

    private TextView midTitle; // 页面标题

    private TextView leftText; //  左边的文字TextView
    private TextView rightText; // 右边的文字TextView


    public TopView(Context context) {
        super(context);
        initview(context, null, 0);
    }

    public TopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initview(context, attrs, 0);
    }

    public TopView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initview(context, attrs, defStyle);
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    private void initview(Context context, AttributeSet attrs, int defStyle) {
        mContext = context;
        if (isInEditMode()) {
            //显示一个IDE编辑状态下标题栏
            TextView textView = new TextView(context);
            textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, getResources()
                    .getDimensionPixelSize
                            (R.dimen.layout_title_height)));
            textView.setText("标题栏");
            textView.setTextColor(getResources().getColor(R.color.red));
            textView.setBackgroundColor(getResources().getColor(R.color.app_green));
            addView(textView);
        } else {
            inflater = LayoutInflater.from(context);

            layout = inflater.inflate(R.layout.layout_title, this, true);

            compatStatusBar(context, attrs, defStyle);

            initView();
        }
    }

    /**
     * 适配沉浸式状态栏
     * 1.根据状态栏高度设置topview高度---->不同手机可能高度不一致
     * 2.如果fitsSystemWindows属性true,则布局会考虑状态栏高---->解决沉浸式状态时有些布局不响应adjustResize
     * 需要给topview设置isAdjustResize=true
     */
    private void compatStatusBar(Context context, AttributeSet attrs, int defStyle) {
        View statusBarView = layout.findViewById(R.id.view_status_bar);
        View topView = layout.findViewById(R.id.topview);
        //
   /*         topView.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, StyleManager.statusBarHeight +
                    getResources()
                    .getDimensionPixelSize
                            (R.dimen.layout_title_height)));
*/

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TopView, defStyle, 0);
        if (a.getBoolean(R.styleable.TopView_topview_isAdjustResize, false)) {

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                SystemBarTintManager tintManager = new SystemBarTintManager((Activity) mContext);
//                tintManager.setStatusBarTintEnabled(true);
//                tintManager.setStatusBarTintResource(R.color.app_title_bg);
//            }

        } else {
            statusBarView.setLayoutParams(new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, StyleManager.statusBarHeight
            ));
        }
        a.recycle();

    }


    private void initView() {
        leftImage = (ImageView) layout.findViewById(R.id.img_back);
        rightImage = (ImageView) layout.findViewById(R.id.img_right);
        rightText = (TextView) layout.findViewById(R.id.txt_right);
        midTitle = (TextView) layout.findViewById(R.id.txt_title);
        leftText = (TextView) layout.findViewById(R.id.txt_left);

        backLayout = (LinearLayout) layout.findViewById(R.id.ll_back_layout);
        rightLayout = (LinearLayout) layout.findViewById(R.id.ll_right_layout);
    }

    /**
     * 设置标题的内容
     */
    public void setAppTitle(int resourceId) {

        midTitle.setText(mContext.getResources().getString(resourceId));
    }

    /**
     * 设置标题的内容
     */
    public void setAppTitle(String title) {

        midTitle.setText(title);
    }


    /**
     * 得到标题
     */
    public TextView getAppTitle() {
        return midTitle;
    }


    /**
     * 设置返回按钮图片资源
     */
    public void setLeftImageResource(int resourseID) {
        isLeftTextViewShow(false);
        leftImage.setImageResource(resourseID);

    }

    /**
     * 设置返回按钮图片资源
     */
    public void setLeftImageDrawable(Drawable drawable) {
        isLeftTextViewShow(false);
        leftImage.setImageDrawable(drawable);
    }

    /**
     * 设置左边图片的显示状态
     */
    public void setLeftImageVisibility(int visibility) {
        backLayout.setVisibility(visibility);
    }

    /**
     * 获取左边 的控件
     *
     * @return
     */
    public ImageView getLeftImg() {
        return leftImage;
    }


    public void setLeftImgOnClickListener() {
        backLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContext instanceof Activity) {
                    ((Activity) mContext).finish();
                }
            }
        });
    }

    /**
     * 左侧文字点击效果
     */

    public void setLeftTextOnClickListener() {
        leftText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContext instanceof Activity) {
                    ((Activity) mContext).finish();
                }
            }
        });
    }

    /**
     * 得到左边的TextView
     */
    public TextView getLeftTextView() {
        isLeftTextViewShow(true);
        return rightText;
    }

    /**
     * 设置左边TextView的内容
     */
    public void setLeftTextiewText(int source) {
        isLeftTextViewShow(true);
        leftText.setText(mContext.getResources().getString(source));
    }

    /**
     * 左边是否是TextView显示
     */
    private void isLeftTextViewShow(boolean isShow) {
        if (isShow) {
            backLayout.setVisibility(View.GONE);
            leftText.setVisibility(View.VISIBLE);
        } else {
            backLayout.setVisibility(View.VISIBLE);
            leftText.setVisibility(View.GONE);
        }
    }


    /**
     * 得到右边的图片
     */
    public ImageView getRightImg() {
        isRightTextViewShow(false);
        return rightImage;
    }

    public View getRightLayout() {
        return rightLayout;
    }

    /**
     * 设置右边图片的显示状态
     */
    public void setRightImageVisibility(int visibility) {
        rightLayout.setVisibility(visibility);
    }

    /**
     * 得到右边的TextView
     */
    public TextView getRightTextView() {
        isRightTextViewShow(true);
        return rightText;
    }

    /**
     * 设置右边TextView的内容
     */
    public void setRightTextViewText(int source) {
        isRightTextViewShow(true);
        rightText.setText(mContext.getResources().getString(source));
    }

    /**
     * 设置右边TextView的内容
     */
    public void setRightTextViewText(String title) {
        isRightTextViewShow(true);
        rightText.setText(title);
    }

    /**
     * 设置右边TextView是否可以点击
     */
    public void setRightTextViewEnable(boolean isEnable) {
        rightText.setEnabled(isEnable);
    }

    /**
     * 设置右边图片的图片
     */
    public void setRightImageDrawable(int id) {
        isRightTextViewShow(false);
        rightImage.setImageResource(id);
    }

    /**
     * 设置右边图片的图片
     */
    public void setRightImageDrawable(Drawable drawable) {
        isRightTextViewShow(false);
        rightImage.setImageDrawable(drawable);
    }


    /**
     * 右边是否是TextView显示
     */
    private void isRightTextViewShow(boolean isShow) {
        if (isShow) {
            rightLayout.setVisibility(View.GONE);
            rightText.setVisibility(View.VISIBLE);
        } else {
            rightLayout.setVisibility(View.VISIBLE);
            rightText.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化标准头部
     */
    public void initCommonTop(int strRes) {
        setAppTitle(strRes);
        setLeftImgOnClickListener();
        setLeftTextOnClickListener();
        setLeftImageResource(R.drawable.icon_back);
    }
}

