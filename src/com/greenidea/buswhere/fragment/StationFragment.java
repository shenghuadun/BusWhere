package com.greenidea.buswhere.fragment;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.base.BaseFragment;
import com.greenidea.buswhere.bean.MultiLineStation;
import com.greenidea.buswhere.util.Constants;
import com.greenidea.buswhere.util.Util;


public class StationFragment extends BaseFragment implements OnClickListener
{
	public static final int FRAGMENT_INDEX = 2;

	private LinearLayout root;
	
	/**
	 * 选中的车站
	 */
	public MultiLineStation selectedStation;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		root =  (LinearLayout) inflater.inflate(R.layout.stationfragment, null);
		
		parent.getSupportActionBar().setTitle(R.string.menu_station);
		parent.getSupportActionBar().setSubtitle(null);
        
		Map<String, List<MultiLineStation>> multiLineStations = Util.getInstance(parent).queryMultiLineStations();
		if(!multiLineStations.isEmpty())
		{
			for(Map.Entry<String, List<MultiLineStation>> entry : multiLineStations.entrySet())
			{
				String stationName = entry.getKey();
				List<MultiLineStation> list = entry.getValue();
				
				LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.multi_line_station_item, null);
				TextView stationNameView = (TextView) layout.findViewById(R.id.stationName);
				stationNameView.setText(stationName);
				
				for(MultiLineStation item : list)
				{
					TextView line = new TextView(parent);
//					LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
					line.setPadding(parent.dip2px(5), parent.dip2px(7), parent.dip2px(5), parent.dip2px(8));
					line.setTag(item);
					line.setOnClickListener(this);
					line.setText(item.getLineName());
					line.setBackgroundColor(Color.parseColor("#BBBBBB"));
					layout.addView(line);
				}
				root.addView(layout);
			}
		}

		return root;
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
		MultiLineStation bean = (MultiLineStation) v.getTag();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.station_menu, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		if(null == selectedStation)
		{
			menu.findItem(R.id.deleteStation).setVisible(false);
		}
		else
		{
			menu.findItem(R.id.deleteStation).setVisible(true);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return super.onOptionsItemSelected(item);
	}

	
}
