package com.greenidea.buswhere.fragment;

import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.base.BaseFragment;
import com.greenidea.buswhere.bean.OneLineStation;
import com.greenidea.buswhere.ui.MultiLineStationView;
import com.greenidea.buswhere.util.Util;


public class StationFragment extends BaseFragment implements OnClickListener
{
	public static final int FRAGMENT_INDEX = 2;

	private LinearLayout root;
	
	/**
	 * 选中的车站
	 */
	public OneLineStation selectedStation;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		root =  (LinearLayout) inflater.inflate(R.layout.stationfragment, null);
		
		parent.getSupportActionBar().setTitle(R.string.menu_station);
		parent.getSupportActionBar().setSubtitle(null);
        
		Map<String, List<OneLineStation>> multiLineStations = Util.getInstance(parent).queryMultiLineStations();
		if(!multiLineStations.isEmpty())
		{
			MultiLineStationView view = new MultiLineStationView(parent);
			view.setStations(multiLineStations);
			root.addView(view);
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
		OneLineStation bean = (OneLineStation) v.getTag();
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
