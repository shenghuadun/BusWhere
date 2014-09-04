package com.baidu.feedback.demo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.android.feedback.FeedbackManager;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

public class DemoActivity extends Activity implements OnClickListener,
        OnCheckedChangeListener {

    private final static String TAG = DemoActivity.class.getSimpleName();

    EditText mUserNameText;
    EditText mUserContactText;

    public static final String API_KEY = "hOZZMCV0CErOO9KlX4TFqsZR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnSubmitInfo = (Button) findViewById(R.id.submit_info);
        btnSubmitInfo.setOnClickListener(this);

        Button btnStartActivity = (Button) findViewById(R.id.start_fb_activity);
        btnStartActivity.setOnClickListener(this);

        mUserNameText = (EditText) findViewById(R.id.user_name);
        mUserContactText = (EditText) findViewById(R.id.user_contact);

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox_onoff_push);
        checkBox.setOnClickListener(this);
        checkBox.setOnCheckedChangeListener(this);
        checkBox.setChecked(hasBind(getApplicationContext()));
        // 初始化feedback
        initFeedback();
    }

    private void initFeedback() {
        FeedbackManager fm = FeedbackManager.getInstance(this);
        fm.register(API_KEY);
    }

    private void initWithApiKey() {
        PushManager.startWork(getApplicationContext(),
                PushConstants.LOGIN_TYPE_API_KEY, API_KEY);
    }

    @Override
    public void onClick(View v) {
        // 输入结束隐藏输入法
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(this.getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        switch (v.getId()) {
        case R.id.submit_info:
            FeedbackManager.getInstance(this).setUserInfo(
                    mUserNameText.getText().toString(),
                    mUserContactText.getText().toString());
            Toast.makeText(getApplicationContext(), R.string.user_info_set_ok,
                    Toast.LENGTH_SHORT).show();
            break;
        case R.id.start_fb_activity:
            FeedbackManager.getInstance(this).startFeedbackActivity();
            break;

        default:
            break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.checkbox_onoff_push) {
            if (!isChecked) {
                FeedbackManager.getInstance(this).disablePush();
                DemoActivity.setBind(this, false);
            } else {
                DemoActivity.setBind(this, true);
                initWithApiKey();
            }
        }
    }

    // 用share preference来实现是否绑定的开关。在ionBind且成功时设置true，unBind且成功时设置false
    public static boolean hasBind(Context context) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        String flag = sp.getString("bind_flag", "");
        if ("ok".equalsIgnoreCase(flag)) {
            return true;
        }
        return false;
    }

    public static void setBind(Context context, boolean flag) {
        String flagStr = "not";
        if (flag) {
            flagStr = "ok";
        }
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        editor.putString("bind_flag", flagStr);
        editor.commit();
    }

}
