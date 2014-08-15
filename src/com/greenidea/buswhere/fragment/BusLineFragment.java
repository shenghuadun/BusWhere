package com.greenidea.buswhere.fragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlparser.util.ParserException;

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
import android.widget.Toast;

import com.baidu.mobads.AdView;
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
	
	private List<BusStation> busStations;

	private OnFragmentDestroyListener onFragmentDestroyListener;
	/**
	 * 当前显示的线路
	 */
	private String lineId;
	
	public BusLineFragment(MainActivity activity) 
	{
		setRetainInstance(true);
		parent = activity;
	}

	@Override
	public void onDestroy()
	{
    	parent.currentStation = null;
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

		busLineView.setStationClickHandler(stationClickHandler);
		
		if(null != busStations)
		{
			busLineView.setStations(busStations);
		}
		
		addAdHandler.sendEmptyMessageDelayed(0, 3000);
		
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
		busLineView.clickStation(index);
		
		scrollHandler.sendEmptyMessageDelayed((busLineView.getRow(index)-3) 
				* Util.dip2px(BusLineView.STATION_HEIGHT, parent.getResources()), 200);
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

        	parent.currentStation = stationClicked;
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

	private class QueryBusRunner implements Runnable
	{
		private String lineId;
		private String stationId;
		private String  direction;
		
		public QueryBusRunner(String lineId, String stationId, String direction)
		{
			this.lineId = lineId;
			this.stationId = stationId;
			this.direction = direction;
		}
		@Override
		public void run()
		{
			List<BusStation> result = Util.getInstance(parent.getApplicationContext()).getBusStations(lineId, "1");
			result.addAll(Util.getInstance(parent.getApplicationContext()).getBusStations(lineId, "0"));
			
			Message msg = lineInfoHandler.obtainMessage();
			
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("stationList", result);
			m.put("stationId", stationId);
			m.put("direction", direction);
			
			msg.obj = m;
			lineInfoHandler.sendMessageDelayed(msg, 1000);
		}
	}
	
	public void queryBus(String lineId, final String stationId, final String direction)
	{
		this.lineId = lineId;
		if(!lineId.equals(""))
		{
			parent.showProcess();
			
			new Thread(new QueryBusRunner(lineId, stationId, direction)).start();		
		}		
	}

	
	private Handler lineInfoHandler = new Handler()
	{
        @SuppressWarnings("unchecked")
		public void handleMessage(Message msg) 
        {  
			Map<String, Object> m = (Map<String, Object>) msg.obj;
			
			List<BusStation> busStations = (List<BusStation>) m.get("stationList");
			
			String stationId = (String) m.get("stationId");
			String direction = (String) m.get("direction");

			if(busStations.isEmpty())
        	{
        		Toast.makeText(parent.getApplicationContext(), "未查询到本路车", Toast.LENGTH_SHORT).show();
        		return;
        	}

        	//隐藏输入法
//        	lineIdInput.clearFocus();
//        	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        	imm.hideSoftInputFromWindow(lineIdInput.getWindowToken(), 0);

    		setStations(busStations);
    		
    		//查询常用车站
    		if(null != stationId)
    		{
    			int index = -1;
    			
    			for(int i = 0; i< busStations.size(); i++)
    			{
    				BusStation station = busStations.get(i);
    				if(station.getStationId().equals(stationId) 
    						&& station.getDirection().equals(direction))
    				{
    					index = i;
    				}
    			}
    			
    			clickStation(index);
    		}
        };  
	};

	public String getCurrentLineId()
	{
		return lineId;
	}
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
	

	private Handler addAdHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			AdView adView = new AdView(parent);
			
			((LinearLayout)parent.findViewById(R.id.adContainer1)).addView(adView);
		}
	};
}
