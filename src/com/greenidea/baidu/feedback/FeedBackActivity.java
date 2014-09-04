package com.greenidea.baidu.feedback;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;

import com.baidu.android.feedback.FeedbackManager;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.greenidea.baidu.push.Utils;
import com.greenidea.buswhere.base.BaseActivity;

public class FeedBackActivity extends BaseActivity {

    private final static String TAG = FeedBackActivity.class.getSimpleName();

    EditText mUserNameText;
    EditText mUserContactText;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFeedback();
//        initWithApiKey();
        FeedbackManager.getInstance(this).setUserInfo(
                "亲",
                "");
        FeedbackManager.getInstance(this).startFeedbackActivity();
        finish();
    }

    private void initFeedback() {
        FeedbackManager fm = FeedbackManager.getInstance(this);
        fm.register("RIafrI5enenVzIGMVH3h3yij");
    }

    private void initWithApiKey() {
        PushManager.startWork(getApplicationContext(),
                PushConstants.LOGIN_TYPE_API_KEY, Utils.getMetaValue(this, "api_key"));
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
