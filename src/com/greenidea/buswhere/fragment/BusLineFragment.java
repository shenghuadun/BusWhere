package com.greenidea.buswhere.fragment;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.gigi.buslocation.bean.BusStation;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.ui.BusLineView;
import com.greenidea.buswhere.util.Util;

public class BusLineFragment extends Fragment
{
	private View contentView;
	
	private ScrollView scrollView;
	private BusLineView busLineView;
	
	private List<BusStation> busStations;
	
	public BusLineFragment() 
	{
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		contentView = inflater.inflate(R.layout.busline, null);

		scrollView = (ScrollView) contentView.findViewById(R.id.scrollView);
		busLineView = (BusLineView) contentView.findViewById(R.id.busLineView);
		
		if(null != busStations)
		{
			busLineView.setStations(busStations);
		}
		return contentView;
	}

	public void setStations(List<BusStation> busStations)
	{
		scrollHandler.sendEmptyMessageDelayed(0, 200);
		this.busStations = busStations;
		
		if(null != busLineView)
		{
			busLineView.setStations(busStations);
		}
	}

	public List<BusStation> getStations()
	{
		return busLineView.getStations();
	}

	public void clickStation(int index)
	{
		if(null != busLineView)
		{
			busLineView.clickStation(index);
		}
		
		scrollHandler.sendEmptyMessageDelayed((BusLineView.getRow(index)-3) 
				* Util.dip2px(BusLineView.STATION_HEIGHT, getActivity().getResources()), 200);
		
	}

	private Handler scrollHandler = new Handler()
	{
        public void handleMessage(android.os.Message msg) 
        {  
			scrollView.smoothScrollTo(0, msg.what);
        }
	};
}
