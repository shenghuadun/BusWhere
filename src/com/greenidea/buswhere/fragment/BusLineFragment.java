package com.greenidea.buswhere.fragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlparser.util.ParserException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobads.AdView;
import com.baidu.mobads.AdViewListener;
import com.gigi.buslocation.bean.BusLine;
import com.gigi.buslocation.bean.BusPosition;
import com.gigi.buslocation.bean.BusStation;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.activity.MainActivity;
import com.greenidea.buswhere.interfaces.OnFragmentDestroyListener;
import com.greenidea.buswhere.ui.BusLineView;
import com.greenidea.buswhere.util.Util;

public class BusLineFragment extends Fragment
{
	private View contentView;
	private MainActivity parent;
	
	private ScrollView scrollView;
	private BusLineView busLineView;
	
	private TextView downAvilableTime;
	private TextView upAvilableTime;
	
	private List<BusStation> busStations;

	private OnFragmentDestroyListener onFragmentDestroyListener;
	
	/**
	 * 选中的站点
	 */
	public BusStation currentStation;
	
	/**
	 * 当前显示的线路
	 */
	private BusLine currentLine;
	
	public BusLineFragment(MainActivity activity) 
	{
		setRetainInstance(true);
		parent = activity;
	}

	@Override
	public void onDestroy()
	{
    	currentStation = null;
    	parent.invalidateOptionsMenu();
    	
    	if(null != onFragmentDestroyListener)
    	{
    		onFragmentDestroyListener.onFragmentDestroy(this);
    	}
		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		contentView = inflater.inflate(R.layout.busline, null);

		scrollView = (ScrollView) contentView.findViewById(R.id.scrollView);
		busLineView = (BusLineView) contentView.findViewById(R.id.busLineView);

		downAvilableTime = (TextView) contentView.findViewById(R.id.downAvilableTime);
		upAvilableTime = (TextView) contentView.findViewById(R.id.upAvilableTime);
		
		if(null != currentLine)
		{
			downAvilableTime.setText(currentLine.getDownAvilableTime());
			upAvilableTime.setText(currentLine.getUpAvilableTime());
		}
		
		
		busLineView.setStationClickHandler(stationClickHandler);
		
		if(null != busStations)
		{
			busLineView.setStations(busStations);
		}
		
//		addAdView();
		
		return contentView;
	}

	public void setStations(List<BusStation> busStations, BusLine line)
	{
		scrollHandler.sendEmptyMessageDelayed(0, 200);
		this.busStations = busStations;
		
		if(null != busLineView)
		{
			busLineView.setStations(busStations);
		}
		parent.getSupportActionBar().setTitle(line.getLineName());
        parent.getSupportActionBar().setSubtitle(line.getPrice());
	}

	public List<BusStation> getStations()
	{
		return busLineView.getStations();
	}

	public void clickStation(int index)
	{
		busLineView.clickStation(index);
		
		scrollHandler.sendEmptyMessageDelayed((busLineView.getRow(index + 1)) 
				* Util.dip2px(BusLineView.STATION_HEIGHT, parent.getResources()) + 20, 200);
	}

	private Handler scrollHandler = new Handler()
	{
        public void handleMessage(android.os.Message msg) 
        {  
			scrollView.smoothScrollTo(0, msg.what);
			
			parent.hideProcess();
        }
	};

	private Handler stationClickHandler = new Handler()
	{
        public void handleMessage(android.os.Message msg) 
        {  
        	BusStation stationClicked = (BusStation)msg.obj;

        	currentStation = stationClicked;
        	parent.invalidateOptionsMenu();

        	parent.showProcess();
        	
        	new BusLocatingThread(stationClicked).start();
        }
	};
	
	private class BusLocatingThread extends Thread
	{
		private BusStation stationClicked ;
		
		public BusLocatingThread(BusStation stationClicked)
		{
			this.stationClicked = stationClicked;	
			Log.d("stationClicked", stationClicked.toString());
		}

		@Override
		public void run()
		{
			Util busUtil = Util.getInstance(parent.getApplicationContext());
			List<BusPosition> positions = null;
			try
			{
				// 查询公交车位置信息
				positions = busUtil.getBusPosition(stationClicked);
			}
			catch (ParserException e)
			{
				e.printStackTrace();
			}
        	
			Map param = new HashMap();
			param.put("stationClicked", stationClicked);
			param.put("positions", positions);
			Message msg = positionHandler.obtainMessage();
			msg.obj = param;
			positionHandler.sendMessage(msg);
		}
		
	}

	private Handler positionHandler = new Handler()
	{
        public void handleMessage(android.os.Message msg) 
        {  
        	List<BusPosition> positions = (List<BusPosition>) ((Map)msg.obj).get("positions");
        	BusStation stationClicked = (BusStation) ((Map)msg.obj).get("stationClicked");
        	if(null == positions)
        	{
        		Toast.makeText(parent, "网络不给力哦亲~", Toast.LENGTH_LONG).show();
        	}
        	else if(positions.isEmpty())
        	{
        		//界面还原
        		busLineView.resetStations();
        		Toast.makeText(parent, "尚未发车", Toast.LENGTH_LONG).show();
        	}
        	else 
        	{
        		//界面还原
        		busLineView.resetStations();
        		busLineView.setBusPositions(positions, stationClicked);
			}	
        	
        	parent.hideProcess();
        }
	};
	
	public void resetStations()
	{
		if(null != busLineView)
		{
			busLineView.resetStations();
		}
	}

	public void setOnFragmentDestroyListener(OnFragmentDestroyListener onFragmentDestroyListener)
	{
		this.onFragmentDestroyListener = onFragmentDestroyListener;
	}

	private void addAdView()
	{
		AdView adView = new AdView(parent);
		adView.setVisibility(View.GONE);
		adView.setListener(adViewListener);
		((LinearLayout)contentView.findViewById(R.id.adContainer1)).addView(adView);
	}
	
	public BusLine getCurrentLine()
	{
		return currentLine;
	}

	public void setCurrentLine(BusLine currentLine)
	{
		this.currentLine = currentLine;

		if(null != downAvilableTime)
		{
			downAvilableTime.setText(currentLine.getDownAvilableTime());
		}
		if(null != upAvilableTime)
		{
			upAvilableTime.setText(currentLine.getUpAvilableTime());
		}
	}

	private AdViewListener adViewListener = new AdViewListener()
	{
		
		@Override
		public void onVideoStart()
		{
			
		}
		
		@Override
		public void onVideoFinish()
		{
			
		}
		
		@Override
		public void onVideoError()
		{
			
		}
		
		@Override
		public void onVideoClickReplay()
		{
			
		}
		
		@Override
		public void onVideoClickClose()
		{
			
		}
		
		@Override
		public void onVideoClickAd()
		{
			
		}
		
		@Override
		public void onAdSwitch()
		{
			
		}
		
		@Override
		public void onAdShow(JSONObject arg0)
		{
			
		}
		
		@Override
		public void onAdReady(AdView arg0)
		{
			arg0.setVisibility(View.VISIBLE);
		}
		
		@Override
		public void onAdFailed(String arg0)
		{
			
		}
		
		@Override
		public void onAdClick(JSONObject arg0)
		{
			
		}
	};
}
