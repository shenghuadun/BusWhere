package com.greenidea.buswhere.activity;

import java.util.List;
import java.util.Map;

import android.app.Instrumentation;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.greenidea.av.GreenideaLayout;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.base.BaseActivity;
import com.greenidea.buswhere.bean.OneLineStation;
import com.greenidea.buswhere.fragment.MultiLineStationAddFragment;
import com.greenidea.buswhere.ui.MultiLineStationView;
import com.greenidea.buswhere.util.Util;


public class MultiLineStationActivity extends BaseActivity
{
	public static final int FRAGMENT_INDEX = 2;

	private LinearLayout scroll;
	
	private MultiLineStationAddFragment multiLineStationAddFragment;
	
	private boolean isFirstIn;
	
	/**
	 * 选中的车站
	 */
	public OneLineStation selectedStation;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.multi_line_station_activity);
		
		scroll = (LinearLayout) findViewById(R.id.container);
		
		Map<String, List<OneLineStation>> multiLineStations = Util.getInstance(this).queryMultiLineStations();
		if(!multiLineStations.isEmpty())
		{
			MultiLineStationView view = new MultiLineStationView(this);
			view.setStations(multiLineStations);
			scroll.addView(view);
		}
		
		isFirstIn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isFirstIn_MultiLineStationActivity", true);
		if(isFirstIn)
		{
			findViewById(R.id.guide).setVisibility(View.VISIBLE);
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isFirstIn_MultiLineStationActivity", false).commit();
		}
		

		//下载确认
		((GreenideaLayout)findViewById(R.id.adsMogoView)).downloadIsShowDialog=true;
	}

	@Override
	public void onDestroy()
	{
    	selectedStation = null;
		super.onDestroy();
	}


	public void resetTitle()
	{
		getSupportActionBar().setTitle(R.string.menu_station);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.multi_line_station_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.add:
			if(null == multiLineStationAddFragment)
			{
				multiLineStationAddFragment = new MultiLineStationAddFragment();
			}
			getSupportFragmentManager()
			.beginTransaction()
			.setCustomAnimations(R.anim.slide_in, R.anim.slide_in)
			.replace(R.id.content_frame, multiLineStationAddFragment)
			.addToBackStack(null)
			.commit();
			
			if(isFirstIn)
			{
				findViewById(R.id.guide).setVisibility(View.GONE);
			}
			return true;
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	public void refreshStations()
	{
		scroll.removeAllViews();
		Map<String, List<OneLineStation>> multiLineStations = Util.getInstance(this).queryMultiLineStations();
		if(!multiLineStations.isEmpty())
		{
			MultiLineStationView view = new MultiLineStationView(this);
			view.setStations(multiLineStations);
			scroll.addView(view);
		}
	}
	
}
