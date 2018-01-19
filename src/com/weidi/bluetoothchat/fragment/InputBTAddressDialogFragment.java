package com.weidi.bluetoothchat.fragment;

import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.weidi.bluetoothchat.Constant;
import com.weidi.bluetoothchat.R;
import com.weidi.bluetoothchat.fragment.base.BaseDialogFragment;
import com.weidi.injectview.InjectOnClick;
import com.weidi.injectview.InjectView;
import com.weidi.log.Log;

/**
 * Created by weidi on 16-5-11.
 */
public class InputBTAddressDialogFragment extends BaseDialogFragment {

    @InjectView(R.id.bt_address_et)
    public EditText bt_address_et;
    @InjectView(R.id.input_address_btn)
    public Button input_address_btn;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @InjectOnClick({R.id.input_address_btn})
    public void onClick(View view) {
        // 40:49:0F:15:61:2E
        String temp = bt_address_et.getText().toString().trim();
        if (TextUtils.isEmpty(temp)) {
            bt_address_et.setHint("请输入远程设备蓝牙地址");
            return;
        }
        String reg = "[0-9a-zA-Z]{2}:[0-9a-zA-Z]{2}:[0-9a-zA-Z]{2}" +
                ":[0-9a-zA-Z]{2}:[0-9a-zA-Z]{2}:[0-9a-zA-Z]{2}";
        if (temp.length() == 17 && temp.matches(reg)) {
            if (getOnResultListener() != null) {
                getOnResultListener().onResult(
                        getRequestCode(),
                        Constant.INPUT_BT_ADDRESS_RESULTCODE,
                        temp.toUpperCase());
            }
            dismiss();
        }
//        else{
//            bt_address_et.setText("");
//            bt_address_et.setHint("如:00:0A:11:2F:34:67");
//        }
    }

    @Override
    protected int provideStyle() {
        return DialogFragment.STYLE_NO_TITLE;
    }

    @Override
    protected int provideLayout() {
        return R.layout.input_address;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

}
