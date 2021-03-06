package com.greenidea.buswhere.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.greenidea.buswhere.R;
import com.greenidea.buswhere.base.BaseActivity;

public class AboutActivity extends BaseActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.about);
		
		PackageInfo pinfo;
		try
		{
			pinfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
			((TextView)findViewById(R.id.version)).setText("v " + pinfo.versionName);
		}
		catch (NameNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
