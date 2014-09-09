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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gigi.buslocation.bean.BusPosition;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.bean.OneLineStation;
import com.greenidea.buswhere.ui.SlideToDeleteListView.OnItemEventListener;
import com.greenidea.buswhere.util.Util;


public class MultiLineStationView extends LinearLayout implements OnItemEventListener
{
	private static final int ID_BTN_REFRESH = 10001; 
	
	private Map<String, List<OneLineStation>> stations;
	private Animation animation;
	private SlideToDeleteListView slideToDeleteListView;
	
	private Util busUtil;
	
	public void init(Context context)
	{
		this.setOrientation(LinearLayout.VERTICAL);
		animation = AnimationUtils.loadAnimation(context, R.anim.rotate);

		slideToDeleteListView = new SlideToDeleteListView(getContext());
		
		busUtil = Util.getInstance(context); 
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

		slideToDeleteListView.helper.removeAllViews();
		slideToDeleteListView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
		slideToDeleteListView.helper.setOnItemEventListener(this);
		
		for(Map.Entry<String, List<OneLineStation>> entry : stations.entrySet())
		{
			addStation(slideToDeleteListView, entry);
		}
		this.addView(slideToDeleteListView);
	}
	
	private class BusLocatingThread extends Thread
	{
		
		private Context context;
		private List<OneLineStation> stations ;
		private List<LineItemView> itemViews;
		private int stationIndex;

		public BusLocatingThread(Context context, 
				List<OneLineStation> stations,
				List<LineItemView> itemViews, 
				int stationIndex)
		{
				
			this.context = context;
			this.stations = stations;
			this.itemViews = itemViews;
			this.stationIndex = stationIndex;
		}

		@Override
		public void run()
		{
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			
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
	        		Message msg = networkFailHandler.obtainMessage();
					msg.what = stationIndex+1;
					networkFailHandler.sendMessageDelayed(msg, 500);
					
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
		}
	}


	private Handler networkFailHandler = new Handler()
	{
        public void handleMessage(android.os.Message msg) 
        {  
        	Toast.makeText(MultiLineStationView.this.getContext(), "网络不给力哦亲~", Toast.LENGTH_SHORT).show();
        	deleteLines(slideToDeleteListView, msg.what);
        }
	};

	private Handler positionHandler = new Handler()
	{
        public void handleMessage(android.os.Message msg) 
        {  
        	List<BusPosition> positions = (List<BusPosition>) ((Map)msg.obj).get("positions");
        	LineItemView view = (LineItemView) ((Map)msg.obj).get("stationView");
        	
    		view.update(positions);
        }
	};

	private void addStation(SlideToDeleteListView slideToDeleteListView, Entry<String, List<OneLineStation>> entry)
	{
		String stationName = entry.getKey();

		RelativeLayout stationNameLayout = new RelativeLayout(getContext());
		
		TextView nameView = new TextView(getContext());
		nameView.setTextSize(Util.dip2px(14, getResources()));

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		int margin1 = Util.dip2px(20, getResources());
		lp.setMargins(margin1, 0, margin1, 0);
		nameView.setLayoutParams(lp);
		nameView.setText(stationName);
		stationNameLayout.addView(nameView);
		
		ImageView refresh = new ImageView(getContext());
		refresh.setImageResource(R.drawable.ic_action_collapse);
		lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		margin1 = Util.dip2px(20, getResources());
		lp.setMargins(margin1, 0, margin1, 0);
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		lp.addRule(RelativeLayout.CENTER_VERTICAL);
		refresh.setLayoutParams(lp);
		refresh.setId(ID_BTN_REFRESH);
		stationNameLayout.addView(refresh);
		
//		ImageView loading = new ImageView(getContext());
//		loading.setId(10000);
//		loading.setImageResource(R.drawable.rotate);
//		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//		rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		rlp.addRule(RelativeLayout.CENTER_VERTICAL);
//		loading.setLayoutParams(rlp);
//		loading.setVisibility(View.INVISIBLE);
//		
//		stationNameLayout.addView(loading);
		stationNameLayout.setTag(entry.getValue());
		
		slideToDeleteListView.helper.addDeletableView(stationNameLayout);
	}

	@Override
	public void onItemDelete(SlideToDeleteListView slideToDeleteListView, int index, View view)
	{
		int size = slideToDeleteListView.helper.getChildCount();
		//本项已经展开
		if(index < size -1 && !slideToDeleteListView.helper.isItemDeletable(index + 1))
		{
			deleteLines(slideToDeleteListView, index+1);
		}
		
		List<OneLineStation> stations = (List<OneLineStation>) view.getTag();
		Util util = Util.getInstance(getContext());
		for(OneLineStation station : stations)
		{
			util.deleteMultiLineStation(station);
		}
	}
	
	@Override
	public void onItemSelected(final SlideToDeleteListView slideToDeleteListView, int index, View view)
	{
		List<OneLineStation> stations = (List<OneLineStation>) view.getTag();
		
		int size = slideToDeleteListView.helper.getChildCount();
		//下方还有其他项（包括可删的或不可删的）
		if(index < size -1)
		{
			//下一项是可删的，表示本项还未展开
			if(slideToDeleteListView.helper.isItemDeletable(index + 1))
			{
				List<LineItemView> itemViews = showLines(slideToDeleteListView, index, stations);
				
				((ImageView)view.findViewById(ID_BTN_REFRESH)).setImageResource(R.drawable.ic_action_expand);
			}
			else
			{
				deleteLines(slideToDeleteListView, index+1);
				((ImageView)view.findViewById(ID_BTN_REFRESH)).setImageResource(R.drawable.ic_action_collapse);
			}
		}
		else
		{
			List<LineItemView> itemViews = showLines(slideToDeleteListView, index, stations);

			((ImageView)view.findViewById(ID_BTN_REFRESH)).setImageResource(R.drawable.ic_action_expand);
		}

		//清除选中样式
		slideToDeleteListView.postDelayed(new Runnable()
		{
			
			@Override
			public void run()
			{
				MultiLineStationView.this.resetPressState(slideToDeleteListView);
			}
		}, 500);
	}
	
	private void resetPressState(SlideToDeleteListView slideToDeleteListView)
	{
		slideToDeleteListView.helper.resetPressState();
	}

	/**
	 * 添加点击的这站对应的路线
	 * @param slideToDeleteListView
	 * @param index
	 * @param stations
	 */
	private List<LineItemView> showLines(SlideToDeleteListView slideToDeleteListView, int index, List<OneLineStation> stations)
	{
		LinearLayout lineItemLayout = new LinearLayout(getContext());
		lineItemLayout.setOrientation(LinearLayout.VERTICAL);
		List<LineItemView> itemViews = new ArrayList<MultiLineStationView.LineItemView>();
		
		for(OneLineStation station : stations)
		{
			LineItemView item = new LineItemView(getContext());
			item.init(station.getLineName());
			itemViews.add(item);
			lineItemLayout.addView(item);
		}
		
		slideToDeleteListView.helper.addNormalView(lineItemLayout, index+1);
		
		BusLocatingThread thread = new BusLocatingThread(getContext(),stations, itemViews, index);
		thread.start();
		
		return itemViews;
	}
	
	/**
	 * 删除index项
	 * @param slideToDeleteListView
	 * @param index
	 */
	private synchronized void deleteLines(SlideToDeleteListView slideToDeleteListView, int index)
	{
		//TODO 是路线而不是车站名，规避没有网络时快速点击多个站点index不再准确的问题
		if(slideToDeleteListView.helper.getChildCount() > index
				&& !slideToDeleteListView.helper.isItemDeletable(index))
		{
			slideToDeleteListView.helper.removeViewAt(index);
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
			resetInfo(index);
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
			resetInfo(index);
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
			if(positions.isEmpty())
			{
        		showNoBus(1);
        		showNoBus(2);
			}
			else if(positions.size() == 1)
			{
				time1.setText(positions.get(0).getWhen() + "前");
				station1.setText(positions.get(0).getStationName());
				num1.setText(positions.get(0).getStationNum() + "站");
        		showNoBus(2);
			}
			else if(positions.size() == 2)
			{
				time1.setText(positions.get(0).getWhen() + "前");
				station1.setText(positions.get(0).getStationName());
				num1.setText(positions.get(0).getStationNum() + "站");
				
				time2.setText(positions.get(1).getWhen() + "前");
				station2.setText(positions.get(1).getStationName());
				num2.setText(positions.get(1).getStationNum() + "站");
			}
		}
		
	}

}
