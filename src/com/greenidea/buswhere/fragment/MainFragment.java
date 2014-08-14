package com.greenidea.buswhere.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlparser.util.ParserException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gigi.buslocation.bean.BusPosition;
import com.gigi.buslocation.bean.BusStation;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.activity.MainActivity;
import com.greenidea.buswhere.bean.FavStationBean;
import com.greenidea.buswhere.ui.SlideToDeleteListView;
import com.greenidea.buswhere.ui.SlideToDeleteListView.OnItemEventListener;
import com.greenidea.buswhere.util.Constants;
import com.greenidea.buswhere.util.Util;

public class MainFragment extends Fragment implements OnItemEventListener
{
	private View contentView;
	private MainActivity parent;

	public SharedPreferences prefHistory;
	public SharedPreferences prefFav;
	
	private EditText lineNumInput;
	private ImageView btnSearch;
	private ListView hintList;
	
	//查询历史
	private LinearLayout row0, row1;
	//常用站点
	private SlideToDeleteListView favListView;
	
	//查询常用站点时的详细信息
	private FavStationBean curFavStation = null;
	
	private Map<String, FavStationBean> favStations = new HashMap<String, FavStationBean>();
	
	
	public MainFragment() 
	{
		setRetainInstance(true);
	}

	@Override
	public void onAttach(Activity activity)
	{
		parent = (MainActivity) activity;
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		contentView = inflater.inflate(R.layout.main_info, null);

		prefHistory = parent.prefHistory;
		prefFav = parent.prefFav;
		
		findViews();
		setListeners();

		return contentView;
	}
	

	@Override
	public void onResume()
	{
		queryHis();
		queryFav();
		super.onResume();
	}

	private void findViews()
	{
		lineNumInput = (EditText)findViewById(R.id.lineNum);
		btnSearch = (ImageView)findViewById(R.id.btnSearch);
		hintList = (ListView)findViewById(R.id.hintList);
		
		row0 = (LinearLayout)findViewById(R.id.tableRow0);
		row1 = (LinearLayout)findViewById(R.id.tableRow1);
		
		favListView = (SlideToDeleteListView)findViewById(R.id.listView);
		favListView.helper.setOnItemEventListener(this);
	}
	
	private void setListeners()
	{
		lineNumInput.addTextChangedListener(new TextWatcher()
		{
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				updateHints(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				
			}
			
			@Override
			public void afterTextChanged(Editable s)
			{
				
			}
		});
		
		btnSearch.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				String stationId = null;
				String direction = null;
				
				if(curFavStation != null)
				{
					stationId = curFavStation.getStationId();
					direction = curFavStation.getDirection();
				}
				parent.queryBus(lineNumInput.getText().toString(), stationId, direction);		

    			//需要清空，以免下次查询线路时，会查询站点
    			curFavStation = null;
    			
    			//更新查询历史
	    		prefHistory.edit().putLong(lineNumInput.getText().toString(), System.currentTimeMillis()).commit();
	    		
	    		//更新查询记录
	    		queryHis();
			}

		});
	}

	private void updateHints(String string)
	{
		
	}
	
	private int getScreenX(Point downPoint)
	{
		return downPoint.x;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	/**
	 * 查询常用车站
	 */
	private void queryFav()
	{
		@SuppressWarnings("unchecked")
		Map<String, String> fav = (Map<String, String>)prefFav.getAll();

		favStations.clear();
		
		if(fav != null && !fav.isEmpty())
		{
			Map<String, FavStationBean> map = new HashMap<String, FavStationBean>();
			
			favListView.helper.removeAllView();
			
			//添加到页面
			for(Map.Entry<String, String> entry : fav.entrySet())
			{
				FavStationBean bean = FavStationBean.fromString(entry.getValue());
				map.put(bean.toString(), bean);
				
				LinearLayout layout = getFavBlock(bean);
				favListView.helper.addDeletableView(layout);
			}

			favStations = map;
		}
	}

	private void queryHis()
	{
		@SuppressWarnings("unchecked")
		Map<String, Long> his = (Map<String, Long>)prefHistory.getAll();
		
		if(his != null && !his.isEmpty())
		{
			//按时间排序
			ArrayList<Map.Entry<String, Long>> mappingList = new ArrayList<Map.Entry<String, Long>>(his.entrySet()); 
			Collections.sort(mappingList, new Comparator<Map.Entry<String, Long>>()
			{ 
				public int compare(Map.Entry<String, Long> mapping1,Map.Entry<String, Long> mapping2)
				{ 
					return mapping2.getValue().compareTo(mapping1.getValue()); 
				} 
			});
			
			row0.removeAllViews();
			row1.removeAllViews();

			row0.setGravity(Gravity.LEFT);
			
			Editor edit = prefHistory.edit();
			edit.clear();
			
			//添加到页面
			for(int i=0; i<mappingList.size(); i++)
			{
				Map.Entry<String, Long> entry = mappingList.get(i);
				
				edit.putLong(entry.getKey(), entry.getValue());

				TextView layout = getHisBlock(entry.getKey(), row0.getWidth());
				switch (i/4)
				{
				case 0:
					row0.addView(layout);
					break;
				case 1:
					row1.addView(layout);
					break;

				default:
					break;
				}
			}
			
			edit.commit();
			
			//有可能一行不满四个，需要添加占位view
			while(row0.getChildCount() < 4)
			{
				View v = new TextView(parent.getApplicationContext());
				LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
				p.weight = 1;
				v.setLayoutParams(p);
				row0.addView(v);
			}

			while(row1.getChildCount() < 4)
			{
				View v = new TextView(parent.getApplicationContext());
				LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
				p.weight = 1;
				v.setLayoutParams(p);
				row1.addView(v);
			}
		}
	}


	private TextView getHisBlock(final String text, int parentWidth)
	{
		TextView result = (TextView) parent.getLayoutInflater().inflate(R.layout.block, null);
		result.setText(text + "路");
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, Util.dip2px(35, parent.getResources()));
		params.setMargins(5, 5, 5, 5);
		params.weight = 1;
		result.setLayoutParams(params);
		result.setGravity(Gravity.CENTER);
		
		result.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				lineNumInput.setText(text);
				btnSearch.performClick();
			}
		});
		
		result.setTextColor(getResources().getColor(R.color.textColor));
		
		return result;
	}

	/**
	 * 删除常用车站
	 */
	@Override
	public void onItemDelete(View item)
	{
		FavStationBean bean = (FavStationBean)item.getTag();
		
		deleteFavStation(bean);
	}

	/**
	 * 选中常用车站
	 */
	@Override
	public void onItemSelectd(View item)
	{
		FavStationBean bean = (FavStationBean)item.getTag();
		
		curFavStation = bean;
	}
	
	private LinearLayout getFavBlock(FavStationBean station)
	{
		LinearLayout result = new LinearLayout(parent.getApplicationContext());
		result.setTag(station);
		result.setOrientation(LinearLayout.HORIZONTAL);
//		result.setBackgroundResource(R.drawable.hisviewblock);
		
		LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		result.setLayoutParams(p);
		
		TextView textView = (TextView)parent.getLayoutInflater().inflate(R.layout.block, null);

		textView.setText(station.getLineId() + "路-" + station.getStationName() 
				+ "站-" + ("0".equals(station.getDirection()) ? "上行" : "下行") );
		textView.setTextSize(16);

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.setMargins(30, 0, 30, 0);
		textView.setLayoutParams(params);
		textView.setBackgroundColor(Color.TRANSPARENT);
		textView.setTextColor(getResources().getColor(R.color.textColor));
		
		result.addView(textView);
		
		return result;
	}

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
		}
	}
	
	private View findViewById(int id)
	{
		return contentView.findViewById(id);
	}
	/**
	 * 当前站点是否已经加入收藏了
	 * @param favStationKey
	 * @return
	 */
	public boolean isAlreadyFaved(BusStation station)
	{
		return favStations.containsKey(new FavStationBean(station).toString());
	}

	/**
	 * 添加到常用车站
	 * @param station
	 */
	public void addToFav(BusStation station)
	{
		String s = new FavStationBean(station).toString();
		if(getFavStationNum() >= Constants.MAXFAVNUM)
		{
			Toast.makeText(parent.getApplicationContext(), "常用站点过多，请删除后再添加", Toast.LENGTH_SHORT).show();
		}
		else if(prefFav.edit().putString(s, s).commit())
		{
			Toast.makeText(parent.getApplicationContext(), "已添加", Toast.LENGTH_SHORT).show();
			refreshFav();
		}
		else 
		{
			Toast.makeText(parent.getApplicationContext(), "添加失败", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean deleteFavStation(FavStationBean bean)
	{
		if(prefFav.edit().remove(bean.toString()).commit())
		{
			Toast.makeText(parent.getApplicationContext(), "已删除", Toast.LENGTH_SHORT).show();
			return true;
		}
		else
		{
			Toast.makeText(parent.getApplicationContext(), "删除失败", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	public boolean deleteFavStation(BusStation station)
	{
		return deleteFavStation(new FavStationBean(station));
	}

	public void refreshFav()
	{
		queryFav();
	}
	
	/**
	 * 查询收藏的车站数量
	 * @return
	 */
	public int getFavStationNum()
	{
		return prefFav.getAll().size();
	}
}
