package com.greenidea.buswhere.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.htmlparser.util.ParserException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gigi.buslocation.bean.BusPosition;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.bean.MultiLineStation;
import com.greenidea.buswhere.util.Util;


public class MultiLineStationView extends LinearLayout
{
	private Map<String, List<MultiLineStation>> stations;
	private Map<String, List<MultiStationItemView>> stationItems = new HashMap<String, List<MultiStationItemView>>();
	private Animation animation;
	
	public void init(Context context)
	{
		this.setOrientation(LinearLayout.VERTICAL);
		animation = AnimationUtils.loadAnimation(context, R.anim.rotate);
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
	
	private Handler loadingHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			ImageView loading = ((ImageView)msg.obj);
			switch (msg.what)
			{
			case 1:
				loading.setVisibility(View.VISIBLE);
				loading.startAnimation(animation);
				break;
			case 0:
				loading.setVisibility(View.GONE);
			default:
				break;
			}
		}
	};
	
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
				ImageView loading = (ImageView) tag.get("loading");
				
				//展开
				if(View.GONE == linesContainer.getVisibility())
				{
					linesContainer.setVisibility(View.VISIBLE);
					
					getBusPositions(items, itemViews, loading);

					Message msg = loadingHandler.obtainMessage();
					msg.what = 1;
					msg.obj = loading;
					loadingHandler.sendMessage(msg);
					
				}
				//合上
				else
				{
					Message msg = loadingHandler.obtainMessage();
					msg.what = 0;
					msg.obj = loading;
					loadingHandler.sendMessage(msg);

					linesContainer.setVisibility(View.GONE);
				}
			}
		}
	};
	

	private void getBusPositions(List<MultiLineStation> stations, List<MultiStationItemView> itemViews, ImageView loading)
	{
		BusLocatingThread thread = new BusLocatingThread(getContext(), stations, itemViews, loading);
		thread.start();
	}

	private class BusLocatingThread extends Thread
	{
		private Context context;
		private List<MultiLineStation> stations ;
		private List<MultiStationItemView> itemViews;
		private ImageView loading;
		
		public BusLocatingThread(Context context, List<MultiLineStation> stations, List<MultiStationItemView> itemViews, ImageView loading)
		{
			this.context = context;
			this.stations = stations;
			this.itemViews = itemViews;
			this.loading = loading;
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

	        	if(null == positions)
	        	{
	        		Toast.makeText(getContext(), "网络不给力哦亲~", Toast.LENGTH_SHORT).show();
	        		break;
	        	}
				Map param = new HashMap();
				param.put("stationView", itemViews.get(index));
				param.put("positions", positions);
				Message msg = positionHandler.obtainMessage();
				msg.obj = param;
				positionHandler.sendMessage(msg);
				
				index++;
			}

			Message msg = loadingHandler.obtainMessage();
			msg.what = 0;
			msg.obj = loading;
			loadingHandler.sendMessage(msg);
		}
		
	}

	private Handler positionHandler = new Handler()
	{
        public void handleMessage(android.os.Message msg) 
        {  
        	List<BusPosition> positions = (List<BusPosition>) ((Map)msg.obj).get("positions");
        	MultiStationItemView view = (MultiStationItemView) ((Map)msg.obj).get("stationView");
        	
        	if(positions.isEmpty())
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
		
		RelativeLayout relativeLayout = new RelativeLayout(getContext());
		relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		relativeLayout.setOnClickListener(onClickListener);
		this.addView(relativeLayout);
		
		//站名
		TextView nameView = new TextView(getContext());
		nameView.setTextSize(Util.dip2px(18, getResources()));
		nameView.setText(stationName);
		relativeLayout.addView(nameView);
		
		ImageView loading = new ImageView(getContext());
		loading.setId(10000);
		loading.setImageResource(R.drawable.rotate);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		lp.addRule(RelativeLayout.CENTER_VERTICAL);
		loading.setLayoutParams(lp);
		loading.setVisibility(View.INVISIBLE);
		relativeLayout.addView(loading);
	
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
		tag.put("loading", loading);
		relativeLayout.setTag(tag);
		
		stationItems.put(stationName, itemViews);
	}

	private static class MultiStationItemView extends LinearLayout
	{
		private static String t1 = "<span>index：<span style=\"color:red\">time</span>前到达<span style=\"color:red\">station</span></span>";
		private static String t2 = "<span>距您还有<span style=\"color:red\">num站</span></span>";
		
		private TextView lineName;
		private TextView text11;
		private TextView text12;
		private TextView text21;
		private TextView text22;
		
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
			LinearLayout itemRoot = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.multi_station_item_line, null);
			
			lineName = (TextView) itemRoot.findViewById(R.id.lineName);
			text11 = (TextView) itemRoot.findViewById(R.id.text11);
			text12 = (TextView) itemRoot.findViewById(R.id.text12);
			text21 = (TextView) itemRoot.findViewById(R.id.text21);
			text22 = (TextView) itemRoot.findViewById(R.id.text22);
			
			lineName.setText(station.getLineName());
			
			this.addView(itemRoot);
			return this;
		}
		
		public void update(List<BusPosition> positions)
		{
			if(positions.size() > 0)
			{
				this.text11.setText(Html.fromHtml(t1
						.replace("index", "1")
						.replace("time", positions.get(0).getWhen())
						.replace("station", positions.get(0).getStationName())));
				this.text12.setText(Html.fromHtml(t2
						.replace("num", positions.get(0).getStationNum())));
			}
			else
			{
				this.text11.setText("未发车");
			}
			if(positions.size() > 1)
			{
				this.text21.setText(Html.fromHtml(t1
						.replace("index", "2")
						.replace("time", positions.get(1).getWhen())
						.replace("station", positions.get(1).getStationName())));
				this.text22.setText(Html.fromHtml(t2
						.replace("num", positions.get(1).getStationNum())));
			}
			else
			{
				this.text21.setText("未发车");
			}
		}
		
	}
}
