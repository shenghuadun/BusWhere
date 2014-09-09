package com.greenidea.share;

import java.io.File;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class WeixinShareUtil
{
	private Context context;
	private static WeixinShareUtil util;
	
	private WeixinShareUtil()
	{}
	
	public static WeixinShareUtil getInstant(Context context)
	{
		if(util == null)
		{
			util = new WeixinShareUtil();
		}
		util.context = context;
		
		return util;
	}
	
	public void shareToFriend(String content)
	{
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm",
                        "com.tencent.mm.ui.tools.ShareImgUI");
        intent.setComponent(comp);
        intent.setAction("android.intent.action.SEND");
        intent.setType("image/*");                   
        intent.putExtra(Intent.EXTRA_TEXT, content);
//        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));                
        context.startActivity(intent);
	}
	
	public void shareToTimeLine(String content, File file) 
	{
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm",
                        "com.tencent.mm.ui.tools.ShareToTimeLineUI");
        intent.setComponent(comp);
        intent.setAction("android.intent.action.SEND");
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        context.startActivity(intent);
	}
}
