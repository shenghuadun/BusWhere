package com.greenidea.buswhere.fragment;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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

public class StationAddFragment extends Fragment implements OnClickListener
{

	private LinearLayout root;
	
	private MainActivity parent;

	public SharedPreferences prefStationLines;
	
	/**
	 * 选中的车站
	 */
	public StationLinesBean selectStation;
	
	private OnFragmentDestroyListener onFragmentDestroyListener;
	public StationAddFragment(MainActivity parent) {
		setRetainInstance(true);
		
		this.parent = parent;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		root =  (LinearLayout) inflater.inflate(R.layout.stationaddfragment, null);
		
		parent.getSupportActionBar().setTitle(R.string.title_station_add);
        
		prefStationLines = parent.getSharedPreferences(Constants.PREF_STATION_LINES, Context.MODE_PRIVATE);
		
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
    	
    	selectStation = null;
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

	
}
