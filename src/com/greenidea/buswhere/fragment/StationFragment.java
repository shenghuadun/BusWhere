package com.greenidea.buswhere.fragment;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.greenidea.buswhere.R;
import com.greenidea.buswhere.activity.MainActivity;
import com.greenidea.buswhere.bean.StationLinesBean;
import com.greenidea.buswhere.interfaces.OnFragmentDestroyListener;
import com.greenidea.buswhere.util.Constants;

public class StationFragment extends Fragment implements OnClickListener
{

	private LinearLayout root;
	
	private MainActivity parent;

	public SharedPreferences prefStationLines;
	
	/**
	 * 选中的车站
	 */
	public StationLinesBean selectedStation;
	
	private OnFragmentDestroyListener onFragmentDestroyListener;
	public StationFragment(MainActivity parent) {
		setRetainInstance(true);
		
		this.parent = parent;
	}

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
	public void onResume()
	{
		parent.setCurrentVisibleFragment(this);

    	parent.invalidateOptionsMenu();
		super.onResume();
	}

	@Override
	public void onDestroy()
	{
    	parent.invalidateOptionsMenu();
    	
    	if(null != onFragmentDestroyListener)
    	{
    		onFragmentDestroyListener.onFragmentDestroy(this);
    	}
    	
    	selectedStation = null;
		super.onDestroy();
	}

	public void setOnFragmentDestroyListener(OnFragmentDestroyListener onFragmentDestroyListener)
	{
		this.onFragmentDestroyListener = onFragmentDestroyListener;
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