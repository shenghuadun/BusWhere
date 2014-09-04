package com.greenidea.buswhere.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
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
			((TextView)findViewById(R.id.version)).setText("version " + pinfo.versionName);
		}
		catch (NameNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
