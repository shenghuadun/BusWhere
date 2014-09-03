package com.greenidea.buswhere.activity;

import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.base.BaseActivity;
import com.greenidea.buswhere.base.BaseFragment;
import com.greenidea.buswhere.bean.OneLineStation;
import com.greenidea.buswhere.ui.MultiLineStationView;
import com.greenidea.buswhere.util.Util;


public class MultiLineStationActivity extends BaseActivity implements OnClickListener
{
	public static final int FRAGMENT_INDEX = 2;

	private LinearLayout scroll;
	
	private TextView empty;
	
	/**
	 * 选中的车站
	 */
	public OneLineStation selectedStation;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.stationfragment);
		
		scroll = (LinearLayout) findViewById(R.id.container);
		empty = (TextView) findViewById(R.id.empty);
		
		getSupportActionBar().setTitle(R.string.menu_station);
		getSupportActionBar().setSubtitle(null);
        
		Map<String, List<OneLineStation>> multiLineStations = Util.getInstance(this).queryMultiLineStations();
		if(!multiLineStations.isEmpty())
		{
			MultiLineStationView view = new MultiLineStationView(this);
			view.setStations(multiLineStations);
			scroll.addView(view);
		}
		else
		{
			empty.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void onDestroy()
	{
    	selectedStation = null;
		super.onDestroy();
	}

	@Override
	public void onClick(View v)
	{
		OneLineStation bean = (OneLineStation) v.getTag();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.station_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if(null == selectedStation)
		{
			menu.findItem(R.id.deleteStation).setVisible(false);
		}
		else
		{
			menu.findItem(R.id.deleteStation).setVisible(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.addStation:
			Intent intent = new Intent(this, StationAddActivity.class);
			startActivityForResult(intent, 1001);
			break;

		default:
			break;
		}
		
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == 1001 && 1 == resultCode)
		{
			scroll.removeAllViews();
			
			Map<String, List<OneLineStation>> multiLineStations = Util.getInstance(this).queryMultiLineStations();
			if(!multiLineStations.isEmpty())
			{
				MultiLineStationView view = new MultiLineStationView(this);
				view.setStations(multiLineStations);
				scroll.addView(view);
			}

			empty.setVisibility(View.GONE);
		}
	}

	
}
