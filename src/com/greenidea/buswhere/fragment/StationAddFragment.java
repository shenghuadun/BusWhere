package com.greenidea.buswhere.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.greenidea.buswhere.R;
import com.greenidea.buswhere.activity.MainActivity;
import com.greenidea.buswhere.bean.MultiLineStation;
import com.greenidea.buswhere.util.Constants;

public class StationAddFragment extends Fragment implements OnClickListener
{

	private LinearLayout root;
	
	private MainActivity parent;

	/**
	 * 选中的车站
	 */
	public MultiLineStation selectStation;
	
	public StationAddFragment(MainActivity parent) {
		setRetainInstance(true);
		
		this.parent = parent;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		root =  (LinearLayout) inflater.inflate(R.layout.stationaddfragment, null);
		
		parent.getSupportActionBar().setTitle(R.string.title_station_add);
        
		return root;
	}


	@Override
	public void onDestroy()
	{
    	selectStation = null;
		super.onDestroy();
	}

	@Override
	public void onClick(View v)
	{
		MultiLineStation bean = (MultiLineStation) v.getTag();
	}

	
}
