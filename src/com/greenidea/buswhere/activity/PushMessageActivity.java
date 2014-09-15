package com.greenidea.buswhere.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.TextView;

import com.greenidea.buswhere.R;
import com.greenidea.buswhere.base.BaseActivity;

public class PushMessageActivity extends BaseActivity
{
	private TextView titleView;
	private TextView contentView;
	private TextView urlView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.pushmessage);
		
		findViews();
		
		Intent intent = getIntent();
		
		setupMessage(intent);
	}

	
	
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		
		setupMessage(intent);
	}



	private void setupMessage(Intent intent)
	{
		String title = intent.getStringExtra("title");
		String content = intent.getStringExtra("content");
		String customContentString = intent.getStringExtra("customContentString");

        String url = null;
        if (!TextUtils.isEmpty(customContentString)) 
        {
            JSONObject customJson = null;
            try 
            {
                customJson = new JSONObject(customContentString);
                if (!customJson.isNull("url")) 
                {
                	url = customJson.getString("url");
                }
            } catch (JSONException e) 
            {
                e.printStackTrace();
            }
        }
        
        titleView.setText(title);
        contentView.setText(content);
        urlView.setText(url);
	}

	private void findViews()
	{
		titleView = (TextView) findViewById(R.id.title);
		contentView = (TextView) findViewById(R.id.content);
		urlView = (TextView) findViewById(R.id.url);
	}
	

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(!getSlidingMenu().isMenuShowing())
			{
				showMenu();
			}
			else
			{
				finish();
			}
			return true;
		}
		else
		{
			return super.onKeyUp(keyCode, event);
		}
	}
}
