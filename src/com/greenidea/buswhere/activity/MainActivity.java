package com.greenidea.buswhere.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.anim;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.baidu.mobads.AdView;
import com.gigi.buslocation.bean.BusStation;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.base.BaseActivity;
import com.greenidea.buswhere.bean.FavStationBean;
import com.greenidea.buswhere.fragment.BusLineFragment;
import com.greenidea.buswhere.fragment.MenuFragment;
import com.greenidea.buswhere.interfaces.OnFragmentDestroyListener;
import com.greenidea.buswhere.ui.SlideToDeleteListView;
import com.greenidea.buswhere.ui.SlideToDeleteListView.OnItemEventListener;
import com.greenidea.buswhere.util.Constants;
import com.greenidea.buswhere.util.Util;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends BaseActivity implements OnItemEventListener, OnFragmentDestroyListener
{
	public SharedPreferences prefHistory;
	public SharedPreferences prefFav;
	
	//输入框及按钮
	private EditText lineNumInput;
	private ImageView btnSearch;
	
	//输入框输入时的自动提示
	private ListView hintList;
	
	//查询历史
	private LinearLayout row0, row1;
	private RelativeLayout container;
	
	//常用站点
	private SlideToDeleteListView favListView;
	private Map<String, FavStationBean> favStations = new HashMap<String, FavStationBean>();
	
	private MenuFragment menuFragment;
	private BusLineFragment busLineFragment;

	public BusStation currentStation;

	//更新收藏站点时，画面会短暂漏出删除按钮，添加本动画来隐藏这个问题
	private AlphaAnimation faddinAnimation ;
	public MainActivity() 
	{
		super(R.string.app_name);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		prefHistory = getSharedPreferences(Constants.PREF_HISTORY, Context.MODE_PRIVATE);
		prefFav = getSharedPreferences(Constants.PREF_FAVORITE, Context.MODE_PRIVATE);
		
		menuFragment = new MenuFragment();
		busLineFragment = new BusLineFragment(this);
		busLineFragment.setOnFragmentDestroyListener(this);

		setContentView(R.layout.main);
		
		// set the Behind View
		setBehindContentView(R.layout.menu);
		getSupportFragmentManager()
		.beginTransaction()
		.setCustomAnimations(anim.slide_in_left, anim.slide_out_right)
		.replace(R.id.menu_frame, menuFragment)
		.commit();
		
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		
		findViews();
		setListeners();

		faddinAnimation = new AlphaAnimation(0, 1);
		faddinAnimation.setDuration(1000);
		faddinAnimation.setAnimationListener(new AnimationListener()
		{
			
			@Override
			public void onAnimationStart(Animation animation)
			{
			}
			
			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
			
			@Override
			public void onAnimationEnd(Animation animation)
			{
				container.setVisibility(View.VISIBLE);
			}
		});
		
		queryHis();
		queryFav();
		
		addAd();
	}
	
	private void addAd()
	{
		addAdHandler.sendEmptyMessageDelayed(0, 5000);
	}

	private Handler addAdHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			AdView adView = new AdView(MainActivity.this);
			
			((LinearLayout)findViewById(R.id.adContainer)).addView(adView);
		}
	};
	
	@Override
	protected void onResume()
	{
		super.onResume();
	}

	private void findViews()
	{
		lineNumInput = (EditText)findViewById(R.id.lineNum);
		btnSearch = (ImageView)findViewById(R.id.btnSearch);
		hintList = (ListView)findViewById(R.id.hintList);
		
		container = (RelativeLayout) findViewById(R.id.container);
		
		row0 = (LinearLayout)findViewById(R.id.tableRow0);
		row1 = (LinearLayout)findViewById(R.id.tableRow1);
		
		favListView = (SlideToDeleteListView)findViewById(R.id.listView);
		favListView.helper.setOnItemEventListener(this);
	}

	private void updateHints(String string)
	{
		
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
				
				queryBus(lineNumInput.getText().toString(), null, null);		
    			
    			//更新查询历史
	    		prefHistory.edit().putLong(lineNumInput.getText().toString(), System.currentTimeMillis()).commit();
			}

		});
	}

	/**
	 * 查询常用车站
	 */
	private void queryFav()
	{
		container.setVisibility(View.INVISIBLE);
		
		@SuppressWarnings("unchecked")
		Map<String, String> fav = (Map<String, String>)prefFav.getAll();

		favStations.clear();
		if(fav != null)
		{
			Map<String, FavStationBean> map = new HashMap<String, FavStationBean>();
			favListView.helper.removeAllViews();
			
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
		container.startAnimation(faddinAnimation);

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
				View v = new TextView(getApplicationContext());
				LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
				p.weight = 1;
				v.setLayoutParams(p);
				row0.addView(v);
			}

			while(row1.getChildCount() < 4)
			{
				View v = new TextView(getApplicationContext());
				LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
				p.weight = 1;
				v.setLayoutParams(p);
				row1.addView(v);
			}
		}
	}


	private TextView getHisBlock(final String text, int parentWidth)
	{
		TextView result = (TextView) getLayoutInflater().inflate(R.layout.block, null);
		result.setText(text + "路");
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, Util.dip2px(35, getResources()));
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
		
		if(bean != null)
		{
			queryBus(bean.getLineId(), bean.getStationId(), bean.getDirection());
		}
	}
	
	private LinearLayout getFavBlock(FavStationBean station)
	{
		LinearLayout result = new LinearLayout(getApplicationContext());
		result.setTag(station);
		result.setOrientation(LinearLayout.HORIZONTAL);
//		result.setBackgroundResource(R.drawable.hisviewblock);
		
		LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		result.setLayoutParams(p);
		
		TextView textView = (TextView)getLayoutInflater().inflate(R.layout.block, null);

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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getSupportMenuInflater().inflate(R.menu.station_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if(null == currentStation)
		{
			menu.findItem(R.id.addFav).setVisible(false);
			menu.findItem(R.id.deleteFav).setVisible(false);
		}
		else if(isAlreadyFaved(currentStation))
		{
			menu.findItem(R.id.addFav).setVisible(false);
			menu.findItem(R.id.deleteFav).setVisible(true);
		}
		else
		{
			menu.findItem(R.id.addFav).setVisible(true);
			menu.findItem(R.id.deleteFav).setVisible(false);
		}
    	
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			toggle();
			break;
		case R.id.addFav:
			addToFav(currentStation);
			break;
			
		case R.id.deleteFav:
			deleteFavStation(currentStation);
			queryFav();
			break;
			
		default: break;
		}
		
		invalidateOptionsMenu();
		
		return true;
	}

	public void setStations(List<BusStation> busStations)
	{
		busLineFragment.setStations(busStations);
	}

	private void showBusLineView()
	{
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, busLineFragment)
		.setCustomAnimations(anim.slide_in_left, anim.slide_out_right)
		.addToBackStack(null)
		.commit();
		
		//立即切换fragment
		getSupportFragmentManager().executePendingTransactions();
	}

	public void queryBus(String lineId, String stationId, String direction)
	{
		showBusLineView();
		if(!lineId.equals(busLineFragment.getCurrentLineId()) || stationId != null)
		{
			busLineFragment.queryBus(lineId, stationId, direction);
		}
		
		busLineFragment.resetStations();
	}

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
			Toast.makeText(getApplicationContext(), "常用站点过多，请删除后再收藏", Toast.LENGTH_SHORT).show();
		}
		else if(prefFav.edit().putString(s, s).commit())
		{
			Toast.makeText(getApplicationContext(), "已收藏", Toast.LENGTH_SHORT).show();
			queryFav();
		}
		else 
		{
			Toast.makeText(getApplicationContext(), "收藏失败", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean deleteFavStation(FavStationBean bean)
	{
		if(prefFav.edit().remove(bean.toString()).commit())
		{
			Toast.makeText(getApplicationContext(), "已删除", Toast.LENGTH_SHORT).show();
			return true;
		}
		else
		{
			Toast.makeText(getApplicationContext(), "删除失败", Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	public void deleteFavStation(BusStation station)
	{
		deleteFavStation(new FavStationBean(station));
	} 

	public int getFavStationNum()
	{
		return prefFav.getAll().size();
	}

	@Override
	public void onFragmentDestroy(Fragment fragment)
	{
		if(busLineFragment == fragment)
		{
			queryFav();
			queryHis();
		}
	}

}
