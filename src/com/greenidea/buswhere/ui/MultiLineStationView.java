package com.greenidea.buswhere.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.htmlparser.util.ParserException;

import com.gigi.buslocation.bean.BusPosition;
import com.gigi.buslocation.bean.BusStation;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.bean.MultiLineStation;
import com.greenidea.buswhere.util.Util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MultiLineStationView extends LinearLayout
{
	private Map<String, List<MultiLineStation>> stations;
	private Map<String, List<MultiStationItemView>> stationItems = new HashMap<String, List<MultiStationItemView>>();
	
	public void init(Context context)
	{
		this.setOrientation(LinearLayout.VERTICAL);
	}

	public MultiLineStationView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public MultiLineStationView(Context context)
	{
		super(context);
		init(context);
	}

	public Map<String, List<MultiLineStation>> getStations()
	{
		return stations;
	}

	public void setStations(Map<String, List<MultiLineStation>> stations)
	{
		this.stations = stations;
		
		this.removeAllViews();
		
		for(Map.Entry<String, List<MultiLineStation>> entry : stations.entrySet())
		{
			addStation(entry);
		}
	}
	
	private OnClickListener onClickListener = new OnClickListener()
	{
		@SuppressWarnings("unchecked")
		@Override
		public void onClick(View v)
		{
			Map<String, Object> tag = (Map<String, Object>) v.getTag();
			
			if(tag != null)
			{
				List<MultiLineStation> items = (List<MultiLineStation>) tag.get("stations");
				List<MultiStationItemView> itemViews = (List<MultiStationItemView>) tag.get("stationViews");
				LinearLayout linesContainer = (LinearLayout) tag.get("container");
				
				//展开
				if(View.GONE == linesContainer.getVisibility())
				{
//					linesContainer.startAnimation();
					linesContainer.setVisibility(View.VISIBLE);
					
					getBusPositions(items, itemViews);
				}
				//合上
				else
				{
//					linesContainer.startAnimation();
					linesContainer.setVisibility(View.GONE);
				}
			}
		}
	};
	

	private void getBusPositions(List<MultiLineStation> stations, List<MultiStationItemView> itemViews)
	{
		BusLocatingThread thread = new BusLocatingThread(getContext(), stations, itemViews);
		thread.start();
	}

	private class BusLocatingThread extends Thread
	{
		private Context context;
		private List<MultiLineStation> stations ;
		private List<MultiStationItemView> itemViews;
		
		public BusLocatingThread(Context context, List<MultiLineStation> stations, List<MultiStationItemView> itemViews)
		{
			this.context = context;
			this.stations = stations;
			this.itemViews = itemViews;
		}

		@Override
		public void run()
		{
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			
			Util busUtil = Util.getInstance(context);
			
			int index = 0;
			for(MultiLineStation station : stations)
			{
				List<BusPosition> positions = null;
				try
				{
					// 查询公交车位置信息
					positions = busUtil.getBusPosition(station.getLineId(), station.getStationId(), station.getSegmentId());
				}
				catch (ParserException e)
				{
					e.printStackTrace();
				}
	        	
				Map param = new HashMap();
				param.put("stationView", itemViews.get(index));
				param.put("positions", positions);
				Message msg = positionHandler.obtainMessage();
				msg.obj = param;
				positionHandler.sendMessage(msg);
				
				index++;
			}
		}
		
	}

	private Handler positionHandler = new Handler()
	{
        public void handleMessage(android.os.Message msg) 
        {  
        	List<BusPosition> positions = (List<BusPosition>) ((Map)msg.obj).get("positions");
        	MultiStationItemView view = (MultiStationItemView) ((Map)msg.obj).get("stationView");
        	
        	if(null == positions)
        	{
        		Toast.makeText(getContext(), "网络不给力哦亲~", Toast.LENGTH_SHORT).show();
        	}
        	else if(positions.isEmpty())
        	{
        		view.setNoBus();
        	}
        	else 
        	{
        		view.update(positions);
			}	
        }
	};

	private void addStation(Entry<String, List<MultiLineStation>> entry)
	{
		String stationName = entry.getKey();
		
		//站名
		TextView nameView = new TextView(getContext());
		nameView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		nameView.setTextSize(Util.dip2px(18, getResources()));
		nameView.setText(stationName);
		nameView.setOnClickListener(onClickListener);
		this.addView(nameView);
	
		List<MultiStationItemView> itemViews = new ArrayList<MultiLineStationView.MultiStationItemView>();

		//本站点所有路线容器，方便总体控制
		LinearLayout linesContainer = new LinearLayout(getContext());
		linesContainer.setOrientation(LinearLayout.VERTICAL);
		//默认不显示，点击后才显示
		linesContainer.setVisibility(View.GONE);
		this.addView(linesContainer);
		
		List<MultiLineStation> items = entry.getValue();
		for(MultiLineStation station : items)
		{
			MultiStationItemView item = new MultiStationItemView(getContext());
			item.init(station);
			itemViews.add(item);
			linesContainer.addView(item);
		}

		//添加到TextView上，点击时用来更新
		Map<String, Object> tag = new HashMap<String, Object>();
		tag.put("stations", items);
		tag.put("stationViews", itemViews);
		tag.put("container", linesContainer);
		nameView.setTag(tag);
		
		stationItems.put(stationName, itemViews);
	}

	private static class MultiStationItemView extends LinearLayout
	{
		private TextView lineName;
		private TextView stationNum;
		private TextView curPos;
		private TextView posTime;
		
		public MultiStationItemView(Context context)
		{
			super(context);
		}
		
		public void setNoBus()
		{
			// TODO Auto-generated method stub
			
		}

		public MultiStationItemView init(MultiLineStation station)
		{
			RelativeLayout itemRoot = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.multi_station_item_line, null);
			
			lineName = (TextView) itemRoot.findViewById(R.id.lineName);
			stationNum = (TextView) itemRoot.findViewById(R.id.stationNum);
			curPos = (TextView) itemRoot.findViewById(R.id.curPos);
			posTime = (TextView) itemRoot.findViewById(R.id.posTime);
			
			lineName.setText(station.getLineName());
			
			this.addView(itemRoot);
			return this;
		}
		
		public void update(List<BusPosition> positions)
		{
			this.stationNum.setText(position.getStationNum());
			this.curPos.setText(position.getStationName());
			this.posTime.setText(position.getWhen());
		}
		
	}
}
