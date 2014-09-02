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
        	
    		view.update(positions);
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
			this.setBackgroundColor(Color.parseColor("#f6f6f6"));

			int p = Util.dip2px(10, getResources());
			//站名整行
			stationNameLayout = new RelativeLayout(getContext());
			stationNameLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			stationNameLayout.setOnClickListener(onClickListener);
			stationNameLayout.setPadding(p, p, p, p);
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

			ImageView separator1 = new ImageView(getContext());
			separator1.setBackgroundColor(R.color.separator);
			LayoutParams lparam = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
			lparam.setMargins(0, 0, 0, -1);
			separator1.setLayoutParams(lparam);
			this.addView(separator1);
			
			//本站点所有路线容器，方便总体控制
			lineItemLayout = new LinearLayout(getContext());
			lineItemLayout.setOrientation(LinearLayout.VERTICAL);
			lineItemLayout.setVisibility(View.GONE);
			lineItemLayout.setPadding(p, 0, p, 0);
			lineItemLayout.setBackgroundColor(Color.WHITE);
			this.addView(lineItemLayout);
			
			ImageView separator2 = new ImageView(getContext());
			separator2.setBackgroundColor(R.color.separator);
			LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
			param.setMargins(0, -1, 0, 0);
			separator2.setLayoutParams(param);
			this.addView(separator2);
			
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
		
		private TextView nobus1;
		private TextView nobus2;
		
		public LineItemView(Context context)
		{
			super(context);
//			this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, Util.dip2px(100, getResources())));
		}
		
		public void showNoBus(int index)
		{
			switch (index)
			{
			case 1:
				nobus1.setVisibility(VISIBLE);
				break;
			case 2:
				nobus2.setVisibility(VISIBLE);
				break;
			default:
				break;
			}
		}
		public void hideNoBus(int index)
		{
			switch (index)
			{
			case 1:
				nobus1.setVisibility(GONE);
				break;
			case 2:
				nobus2.setVisibility(GONE);
				break;
			default:
				break;
			}
		}
		
		private void resetInfo(int index)
		{
			switch (index)
			{
			case 1:
				time1.setText("");
				station1.setText("");
				num1.setText("");
				break;
			case 2:
				time2.setText("");
				station2.setText("");
				num2.setText("");
			default:
				break;
			}
		}

		public LineItemView init(String stationName)
		{
			RelativeLayout itemRoot = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.multi_station_item_line, null);

			itemRoot.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			lineName = (TextView) itemRoot.findViewById(R.id.lineName);
			time1 = (TextView) itemRoot.findViewById(R.id.time1);
			time2 = (TextView) itemRoot.findViewById(R.id.time2);
			station1 = (TextView) itemRoot.findViewById(R.id.station1);
			station2 = (TextView) itemRoot.findViewById(R.id.station2);
			num1 = (TextView) itemRoot.findViewById(R.id.num1);
			num2 = (TextView) itemRoot.findViewById(R.id.num2);
			nobus1 = (TextView) itemRoot.findViewById(R.id.nobus1);
			nobus2 = (TextView) itemRoot.findViewById(R.id.nobus2);
			
			lineName.setText(stationName);
			
			this.addView(itemRoot);
			return this;
		}
		
		public void update(List<BusPosition> positions)
		{
			hideNoBus(1);
			hideNoBus(2);
			if(null == positions)
			{
        		showNoBus(1);
        		showNoBus(2);
			}
			else if(positions.size() == 1)
			{
				time1.setText(positions.get(0).getWhen());
				station1.setText(positions.get(0).getStationName());
				num1.setText(positions.get(0).getStationNum() + "站");
        		showNoBus(2);
			}
			else if(positions.size() == 2)
			{
				time1.setText(positions.get(0).getWhen());
				station1.setText(positions.get(0).getStationName());
				num1.setText(positions.get(0).getStationNum() + "站");
				
				time2.setText(positions.get(1).getWhen());
				station2.setText(positions.get(1).getStationName());
				num2.setText(positions.get(1).getStationNum() + "站");
			}
		}
		
	}
}
