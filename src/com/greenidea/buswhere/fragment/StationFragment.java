package com.greenidea.buswhere.fragment;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.greenidea.buswhere.bean.StationLinesBean;
import com.greenidea.buswhere.util.Constants;


public class StationFragment extends BaseFragment implements OnClickListener
{
	public static final int FRAGMENT_INDEX = 2;

	private LinearLayout root;
	
	public SharedPreferences prefStationLines;
	
	/**
	 * 选中的车站
	 */
	public StationLinesBean selectedStation;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		root =  (LinearLayout) inflater.inflate(R.layout.stationfragment, null);
		
		parent.getSupportActionBar().setTitle(R.string.menu_station);
        
		prefStationLines = parent.getSharedPreferences(Constants.PREF_STATION_LINES, Context.MODE_PRIVATE);
		
		@SuppressWarnings("unchecked")
		Map<String, String> stations = (Map<String, String>)prefStationLines.getAll();
		
		if(stations != null && !stations.isEmpty())
		{
			for(Map.Entry<String, String> entry : stations.entrySet())
			{
				String stationName = entry.getKey();
				StationLinesBean bean = StationLinesBean.from(entry.getValue());
				
				TextView stationView = new TextView(parent);
				
//				LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				
				stationView.setPadding(parent.dip2px(5), parent.dip2px(7), parent.dip2px(5), parent.dip2px(8));
				stationView.setTag(bean);
				stationView.setOnClickListener(this);
				
				root.addView(stationView);
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
		StationLinesBean bean = (StationLinesBean) v.getTag();
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
