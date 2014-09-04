package com.greenidea.buswhere.fragment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlparser.util.ParserException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.gigi.buslocation.bean.BusLine;
import com.gigi.buslocation.bean.BusPosition;
import com.gigi.buslocation.bean.BusStation;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.activity.MainActivity;
import com.greenidea.buswhere.base.BaseFragment;
import com.greenidea.buswhere.ui.BusLineView;
import com.greenidea.buswhere.util.Util;

public class BusLineFragment extends BaseFragment
{
	private static final String TAG = "BusLineFragment";
	public static final int FRAGMENT_INDEX = 1;
	
	private MainActivity parent;
	private View contentView;
	
	private ScrollView scrollView;
	private BusLineView busLineView;
	
	private TextView downAvilableTime;
	private TextView upAvilableTime;
	
	private List<BusStation> busStations;

	/**
	 * 选中的站点
	 */
	public BusStation currentStation;
	
	/**
	 * 当前显示的线路
	 */
	private BusLine currentLine;
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
    	currentStation = null;
    	parent.resetTitle();
		Log.d(TAG, "onDestroy");
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		parent = (MainActivity) super.parent;
		
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

		Log.d(TAG, "onCreateView");
		return contentView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		Log.d(TAG, "onActivityCreated");
		if(null != savedInstanceState)
		{
			BusLine line = (BusLine) savedInstanceState.get("currentLine");
			if(null != line)
			{
	    		setCurrentLine(line);
	    		setStations((List<BusStation>) savedInstanceState.get("busStations"), line);
			}

			currentStation = (BusStation) savedInstanceState.get("currentStation");
			if(null != currentStation)
			{
				clickStation(currentStation);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		if(null != busStations)
		{
			outState.putSerializable("busStations", (Serializable) busStations);
		}
		if(null != currentStation)
		{
			outState.putSerializable("currentStation", currentStation);
		}
		if(null != currentLine)
		{
			outState.putSerializable("currentLine", currentLine);
		}
		
		Log.d(TAG, "onSaveInstanceState");
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.busline_menu, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		if(null == currentStation)
		{
			menu.findItem(R.id.addFav).setVisible(false);
			menu.findItem(R.id.deleteFav).setVisible(false);
		}
		else if(parent.isAlreadyFaved(currentStation))
		{
			menu.findItem(R.id.addFav).setVisible(false);
			menu.findItem(R.id.deleteFav).setVisible(true);
		}
		else
		{
			menu.findItem(R.id.addFav).setVisible(true);
			menu.findItem(R.id.deleteFav).setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.addFav:
				parent.addToFav(currentStation);
				break;
				
			case R.id.deleteFav:
				parent.deleteFavStation(currentStation);
				break;
		}

		parent.invalidateOptionsMenu();
		return super.onOptionsItemSelected(item);
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

	public void clickStation(BusStation station)
	{
		int index = -1;
		
		for(int i = 0; i< busStations.size(); i++)
		{
			BusStation bs = busStations.get(i);
			if(station.getStationId().equals(bs.getStationId()) 
					&& station.getDirection().equals(bs.getDirection()))
			{
				index = i;
			}
		}
		
		clickStation(index);
	}
	
	public void clickStation(int index)
	{
		busLineView.clickStation(index);
		
		scrollHandler.sendEmptyMessageDelayed((busLineView.getRow(index + 1)) 
				* Util.dip2px(BusLineView.STATION_HEIGHT, parent.getResources()) + 20, 200);
		
		Log.d(TAG, "" + (busLineView.getRow(index + 1)) 
				* Util.dip2px(BusLineView.STATION_HEIGHT, parent.getResources()));
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
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			
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
}
