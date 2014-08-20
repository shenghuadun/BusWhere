package com.greenidea.buswhere.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.greenidea.buswhere.R;
import com.greenidea.buswhere.activity.MainActivity;
import com.greenidea.buswhere.interfaces.OnFragmentDestroyListener;

public class StationFragment extends Fragment
{

	private View root;
	
	private MainActivity parent;
	
	private ListView stationList;
	
	private OnFragmentDestroyListener onFragmentDestroyListener;
	public StationFragment(MainActivity parent) {
		setRetainInstance(true);
		
		this.parent = parent;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		root =  inflater.inflate(R.layout.stationfragment, null);
		
		stationList = (ListView) root.findViewById(R.id.stationList);

		parent.getSupportActionBar().setTitle(R.string.menu_station);
        
		return root;
	}
	

	@Override
	public void onDestroy()
	{
    	parent.invalidateOptionsMenu();
    	
    	if(null != onFragmentDestroyListener)
    	{
    		onFragmentDestroyListener.onFragmentDestroy(this);
    	}
		super.onDestroy();
	}

	public void setOnFragmentDestroyListener(OnFragmentDestroyListener onFragmentDestroyListener)
	{
		this.onFragmentDestroyListener = onFragmentDestroyListener;
	}

	
}
