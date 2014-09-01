package com.greenidea.buswhere.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.htmlparser.util.ParserException;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
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
import com.greenidea.buswhere.bean.OneLineStation;
import com.greenidea.buswhere.util.Util;


public class MultiLineStationView extends LinearLayout
{
	private Map<String, List<OneLineStation>> stations;
	private Map<String, StationItemView> stationItems = new HashMap<String, StationItemView>();
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

	public Map<String, List<OneLineStation>> getStations()
	{
		return stations;
	}

	public void setStations(Map<String, List<OneLineStation>> stations)
	{
		this.stations = stations;
		
		this.removeAllViews();
		
		for(Map.Entry<String, List<OneLineStation>> entry : stations.entrySet())
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
	

	private class BusLocatingThread extends Thread
	{
		private Context context;
		private List<OneLineStation> stations ;
		private List<LineItemView> itemViews;
		private ImageView loading;
		
		public BusLocatingThread(Context context, List<OneLineStation> stations, List<LineItemView> itemViews, ImageView loading)
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
		
			Looper.prepare();
			Util busUtil = Util.getInstance(context);
			
			int index = 0;
			for(OneLineStation station : stations)
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

			Looper.loop();
		}
		
	}

	private Handler positionHandler = new Handler()
	{
        public void handleMessage(android.os.Message msg) 
        {  
        	List<BusPosition> positions = (List<BusPosition>) ((Map)msg.obj).get("positions");
        	LineItemView view = (LineItemView) ((Map)msg.obj).get("stationView");
        	
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

	private void addStation(Entry<String, List<OneLineStation>> entry)
	{
		String stationName = entry.getKey();
		
		StationItemView item = new StationItemView(getContext());
		item.init(stationName, entry.getValue());
		stationItems.put(stationName, item);
		this.addView(item);
	}

	private class StationItemView extends LinearLayout
	{
		RelativeLayout stationNameLayout;
		TextView nameView;
		ImageView loading;
		List<LineItemView> itemViews;
		
		LinearLayout lineItemLayout;
		
		public StationItemView(Context context)
		{
			super(context);
			this.setOrientation(LinearLayout.VERTICAL);
			int p = Util.dip2px(10, getResources());
			this.setPadding(p, 0, p, 0);
			
			//站名整行
			stationNameLayout = new RelativeLayout(getContext());
			stationNameLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			stationNameLayout.setOnClickListener(onClickListener);
			this.addView(stationNameLayout);

			//站名
			nameView = new TextView(getContext());
			nameView.setTextSize(Util.dip2px(14, getResources()));

			stationNameLayout.addView(nameView);
			
			//加载图片
			loading = new ImageView(getContext());
			loading.setId(10000);
			loading.setImageResource(R.drawable.rotate);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			lp.addRule(RelativeLayout.CENTER_VERTICAL);
			loading.setLayoutParams(lp);
			loading.setVisibility(View.INVISIBLE);
			
			stationNameLayout.addView(loading);

			//本站点所有路线容器，方便总体控制
			lineItemLayout = new LinearLayout(getContext());
			lineItemLayout.setOrientation(LinearLayout.VERTICAL);
			lineItemLayout.setVisibility(View.GONE);
			this.addView(lineItemLayout);
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
					List<OneLineStation> items = (List<OneLineStation>) tag.get("stations");
					
					//展开
					if(View.GONE == lineItemLayout.getVisibility())
					{
						lineItemLayout.setVisibility(View.VISIBLE);
						
						BusLocatingThread thread = new BusLocatingThread(getContext(), items, itemViews, loading);
						thread.start();

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

						lineItemLayout.setVisibility(View.GONE);
					}
				}
			}
		};
		
		
		public void init(String stationName, List<OneLineStation> lineItems)
		{
			nameView.setText(stationName);
		
			itemViews = new ArrayList<MultiLineStationView.LineItemView>();
			
			for(OneLineStation station : lineItems)
			{
				LineItemView item = new LineItemView(getContext());
				item.init(station.getLineName());
				itemViews.add(item);
				lineItemLayout.addView(item);
			}

			//添加到TextView上，点击时用来更新
			Map<String, Object> tag = new HashMap<String, Object>();
			tag.put("stations", lineItems);
			stationNameLayout.setTag(tag);
		}
		
	}
	
	private static class LineItemView extends LinearLayout
	{
		private TextView lineName;
		private TextView time1;
		private TextView time2;
		private TextView station1;
		private TextView station2;
		private TextView num1;
		private TextView num2;
		
		public LineItemView(Context context)
		{
			super(context);
			this.setBackgroundColor(Color.RED);
		}
		
		public void setNoBus()
		{
			// TODO Auto-generated method stub
			
		}

		public LineItemView init(String stationName)
		{
			RelativeLayout itemRoot = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.multi_station_item_line, null);
			
			lineName = (TextView) itemRoot.findViewById(R.id.lineName);
			time1 = (TextView) itemRoot.findViewById(R.id.time1);
			time2 = (TextView) itemRoot.findViewById(R.id.time2);
			station1 = (TextView) itemRoot.findViewById(R.id.station1);
			station2 = (TextView) itemRoot.findViewById(R.id.station2);
			num1 = (TextView) itemRoot.findViewById(R.id.num1);
			num2 = (TextView) itemRoot.findViewById(R.id.num2);
			
			lineName.setText(stationName);
			
			this.addView(itemRoot);
			return this;
		}
		
		public void update(List<BusPosition> positions)
		{
			if(positions.size() > 0)
			{
				time1.setText(positions.get(0).getWhen());
				station1.setText(positions.get(0).getStationName());
				num1.setText(positions.get(0).getStationNum());
			}
			else
			{
				this.time1.setText("未发车");
			}
			if(positions.size() > 1)
			{
				time2.setText(positions.get(1).getWhen());
				station2.setText(positions.get(1).getStationName());
				num2.setText(positions.get(1).getStationNum());
			}
			else
			{
				this.time2.setText("未发车");
			}
		}
		
	}
}
