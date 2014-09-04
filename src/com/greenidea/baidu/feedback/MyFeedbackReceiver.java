package com.greenidea.baidu.feedback;

import com.baidu.android.feedback.BDFeedbackReceiver;
import com.baidu.android.feedback.ui.FeedbackActivity;
import com.greenidea.buswhere.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class MyFeedbackReceiver extends BDFeedbackReceiver {
    private static final String TAG = "MyFeedbackReceiver";

    /**
     * 接收用户反馈消息的回调函数
     * 
     * @param context
     * @param fbMsg
     */
    @Override
    public void onFBMessage(Context context, String fbMsg) {
        Log.d(TAG, "Receive fb msg: " + fbMsg);
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        String hint = "您反馈的问题有了新的回复";
        Notification n = new Notification(R.drawable.ic_launcher,
                hint, System.currentTimeMillis());
        n.flags = Notification.FLAG_AUTO_CANCEL;
        Intent intent = new Intent(context, FeedbackActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(context.getPackageName());
        // PendingIntent
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                R.string.app_name, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        n.setLatestEventInfo(context, hint, fbMsg, contentIntent);
        nm.notify(R.string.app_name, n);
    }


}
