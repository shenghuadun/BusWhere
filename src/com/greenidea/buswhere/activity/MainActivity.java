package com.greenidea.buswhere.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.R.anim;
import android.app.Instrumentation;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.baidu.mobads.AdSettings;
import com.baidu.mobads.AdView;
import com.baidu.mobads.AdViewListener;
import com.gigi.buslocation.bean.BusLine;
import com.gigi.buslocation.bean.BusStation;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.base.BaseActivity;
import com.greenidea.buswhere.bean.FavStationBean;
import com.greenidea.buswhere.component.HintAdapter;
import com.greenidea.buswhere.fragment.BusLineFragment;
import com.greenidea.buswhere.fragment.MenuFragment;
import com.greenidea.buswhere.fragment.StationFragment;
import com.greenidea.buswhere.interfaces.OnFragmentDestroyListener;
import com.greenidea.buswhere.ui.SlideToDeleteListView;
import com.greenidea.buswhere.ui.SlideToDeleteListView.OnItemEventListener;
import com.greenidea.buswhere.util.BusDBHelper;
import com.greenidea.buswhere.util.Constants;
import com.greenidea.buswhere.util.Util;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends BaseActivity implements OnItemEventListener, OnFragmentDestroyListener
{
	private static int MAX_HIS_COUNT = 10;
	
	public SharedPreferences prefHistory;
	public SharedPreferences prefFav;
	
	//输入框及按钮
	private EditText lineNumInput;
	private ImageView btnSearch;
	
	//输入框输入时的自动提示
	private ListView hintList;
	
	//查询历史
	private LinearLayout hisLayout;
	private RelativeLayout staticContainer;
	
	//常用站点
	private SlideToDeleteListView favListView;
	private Map<String, FavStationBean> favStations = new HashMap<String, FavStationBean>();
	
	private MenuFragment menuFragment;
	private BusLineFragment busLineFragment;
	private StationFragment stationFragment;
	
	private Fragment currentVisibleFragment;

	private HintAdapter hintAdapter;

	//更新收藏站点时，画面会短暂漏出删除按钮，添加本动画来隐藏这个问题
	private AlphaAnimation faddinAnimation ;
	
	private AdView adView;
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
			findViewById(R.id.adContainer).setVisibility(View.VISIBLE);
			findViewById(R.id.adContainer).startAnimation(faddinAnimation);
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
		
		if(!getPreferences(Context.MODE_PRIVATE).getBoolean("initiallizedDB", false))
		{
			BusDBHelper.copyDatabaseFile(this.getApplicationContext(), getDatabasePath("busdb").getParent());
			getPreferences(Context.MODE_PRIVATE).edit().putBoolean("initiallizedDB", true).commit();
		}

		setContentView(R.layout.main);
		
		menuFragment = new MenuFragment(this);
		busLineFragment = new BusLineFragment(this);
		busLineFragment.setOnFragmentDestroyListener(this);
		
		stationFragment = new StationFragment(this);
		stationFragment.setOnFragmentDestroyListener(this);

		hintAdapter = new HintAdapter(this);
		
		// set the Behind View
		
		FrameLayout f = new FrameLayout(this);
		f.setId(190871026);
		setBehindContentView(f);
		getSupportFragmentManager()
		.beginTransaction()
		.setCustomAnimations(anim.slide_in_left, anim.slide_out_right)
		.replace(190871026, menuFragment)
		.commit();
		
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		initAd();
		
		findViews();
		setListeners();

		//动画
		initFaddinAnimation();
	}

	private void initAd()
	{
		//广告初始化
//		AdView.setAppSid(MainActivity.this, "f8a1fa59");
//		AdView.setAppSec(MainActivity.this, "f8a1fa59_13b50d6f");
		AdSettings.setCity("青岛");

		adView = new AdView(MainActivity.this);
		adView.setListener(adViewListener);
		final RelativeLayout adContainer = (RelativeLayout)findViewById(R.id.adContainer);
		adContainer.addView(adView);
		
		ImageView del = new ImageView(this);
		del.setImageResource(R.drawable.ic_action_remove);
//		del.setBackgroundColor(Color.parseColor("#33333333"));
		del.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				adContainer.removeAllViews();
			}
		});
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		del.setLayoutParams(params );
		adContainer.addView(del);
		findViewById(R.id.adContainer).setVisibility(View.GONE);		
	}

	@Override
	protected void onResume()
	{
		initHandler.sendEmptyMessageDelayed(0, 500);
		super.onResume();
	}
	
	private Handler initHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			//历史记录和收藏
			queryHis();
			queryFav();
		}
	};
	

	private void initFaddinAnimation()
	{
		faddinAnimation = new AlphaAnimation(0, 1);
		faddinAnimation.setDuration(200);
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
				staticContainer.setVisibility(View.VISIBLE);
			}
		});
	}
	
	private void findViews()
	{
		lineNumInput = (EditText)findViewById(R.id.lineNum);
		btnSearch = (ImageView)findViewById(R.id.btnSearch);
		hintList = (ListView)findViewById(R.id.hintList);
		hintList.setAdapter(hintAdapter);
		
		staticContainer = (RelativeLayout) findViewById(R.id.staticContainer);
		
		hisLayout = (LinearLayout)findViewById(R.id.hisLayout);
		
		favListView = (SlideToDeleteListView)findViewById(R.id.listView);
		favListView.helper.setOnItemEventListener(this);
	}

	private void hideHints()
	{
		hintList.setVisibility(View.GONE);
		staticContainer.setVisibility(View.VISIBLE);
	}
	
	private void showHints(String key)
	{
		//特殊线路
		if(key.startsWith("0"))
		{
			showSpecialLines();
		}
		else
		{
			showLinesByKey(key);
		}
	}
	
	private void showLinesByKey(String key)
	{
		hintAdapter.refreshLines(key);
		hintList.setVisibility(View.VISIBLE);
		staticContainer.setVisibility(View.GONE);
		hintAdapter.notifyDataSetChanged();
	}

	private void showSpecialLines()
	{
		hintAdapter.refreshLines("special");
		hintList.setVisibility(View.VISIBLE);
		staticContainer.setVisibility(View.GONE);
		hintAdapter.notifyDataSetChanged();
	}
	
	private void setListeners()
	{
		lineNumInput.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if(hasFocus)
				{
					lineNumInput.selectAll();
					
//					adView.setVisibility(View.GONE);
				}
				else
				{
//					adView.setVisibility(View.VISIBLE);
				}
			}
		});
		
		lineNumInput.addTextChangedListener(new TextWatcher()
		{
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				showHints(s.toString());
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
				queryBus(lineNumInput.getText().toString(), null, null);		
			}

		});
	}

	/**
	 * 查询常用车站
	 */
	private void queryFav()
	{
		staticContainer.setVisibility(View.INVISIBLE);
		
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
		staticContainer.startAnimation(faddinAnimation);

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
			
			hisLayout.removeAllViews();
			hisLayout.setGravity(Gravity.LEFT);
			
			Editor edit = prefHistory.edit();
			edit.clear();
			
			//添加到页面
			for(int i=0; i<mappingList.size(); i++)
			{
				Map.Entry<String, Long> entry = mappingList.get(i);
				
				edit.putLong(entry.getKey(), entry.getValue());

				TextView layout = getHisBlock(entry.getKey(), hisLayout.getWidth());
				
				hisLayout.addView(layout);
				
				//超过最大数量后，直接取消添加
				if(i > MAX_HIS_COUNT)
				{
					break;
				}
			}
			
			edit.commit();
		}
	}

	private TextView getHisBlock(final String text, int parentWidth)
	{
		String lineName = text.split("@")[0];
		String lineId = text.split("@")[1];
		
		TextView result = (TextView) getLayoutInflater().inflate(R.layout.block, null);
		result.setText(lineName);
		result.setTag(lineId);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(5, 5, 5, 5);
		result.setLayoutParams(params);
		result.setBackgroundResource(R.drawable.hisitembk);
		result.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				queryBus((String) v.getTag(), null, null);
			}
		});
		
		result.setTextColor(getResources().getColor(R.color.textColor));
		
		return result;
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

	public void setTitle()
	{
		getSupportActionBar().setTitle(R.string.app_name);
		getSupportActionBar().setSubtitle(null);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(hintList.getVisibility() == View.VISIBLE)
			{
				hideHints();
				
				return true;
			}
			else if(((FrameLayout)findViewById(R.id.content_frame)).getChildCount() == 0 && !getSlidingMenu().isMenuShowing())
			{
				showMenu();
				return true;
			}
			else if(((FrameLayout)findViewById(R.id.content_frame)).getChildCount() == 0 && getSlidingMenu().isMenuShowing())
			{
				finish();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
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
	/**
	 * 删除常用车站
	 */
	@Override
	public void onItemDelete(View item)
	{
		FavStationBean bean = (FavStationBean)item.getTag();
		
		deleteFavStation(bean);
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
		if(null == busLineFragment.currentStation)
		{
			menu.findItem(R.id.addFav).setVisible(false);
			menu.findItem(R.id.deleteFav).setVisible(false);
		}
		else if(isAlreadyFaved(busLineFragment.currentStation))
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
			if(null == currentVisibleFragment)
			{
				toggle();
			}
			else
			{
				new Thread(new Runnable()
				{
					
					@Override
					public void run()
					{
		                Instrumentation inst = new Instrumentation();  
		                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); 
					}
				}).start();
				
				currentVisibleFragment = null;
			}
			break;
		case R.id.addFav:
			addToFav(busLineFragment.currentStation);
			break;
			
		case R.id.deleteFav:
			deleteFavStation(busLineFragment.currentStation);
			queryFav();
			break;
			
		default: break;
		}
		
		invalidateOptionsMenu();
		
		return true;
	}

	private void showBusLine()
	{
		showFragment(1);
		
		currentVisibleFragment = busLineFragment;
		
		hideHints();
	}

	public void showFragment(int which)
	{
		switch (which)
		{
		case 1:
			getSupportFragmentManager()
			.beginTransaction()
			.setCustomAnimations(R.anim.slide_in, R.anim.slide_in)
			.replace(R.id.content_frame, busLineFragment)
			.addToBackStack(null)
			.commit();

			//立即切换fragment
			getSupportFragmentManager().executePendingTransactions();
			break;

		case 2:
			getSupportFragmentManager()
			.beginTransaction()
			.setCustomAnimations(R.anim.slide_in, R.anim.slide_in)
			.replace(R.id.content_frame, stationFragment)
			.addToBackStack(null)
			.commit();
			break;

		default:
			showContent();
			break;
		}
	}
	
	private Handler lineInfoHandler = new Handler()
	{
        @SuppressWarnings("unchecked")
		public void handleMessage(Message msg) 
        {  
			Map<String, Object> m = (Map<String, Object>) msg.obj;
			
			BusLine line = (BusLine) m.get("line");
			List<BusStation> busStations = (List<BusStation>) m.get("stationList");
			
			String stationId = (String) m.get("stationId");
			String direction = (String) m.get("direction");

			if(busStations.isEmpty())
        	{
        		Toast.makeText(getApplicationContext(), "未查询到本路车", Toast.LENGTH_SHORT).show();
        		hideProcess();
        		hideHints();
        		return;
        	}

    		showBusLine();

			//更新查询历史
    		prefHistory.edit().putLong(line.getLineName() + "@" + line.getLineId(), System.currentTimeMillis()).commit();
    		
        	//隐藏输入法
//        	lineIdInput.clearFocus();
//        	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        	imm.hideSoftInputFromWindow(lineIdInput.getWindowToken(), 0);

    		busLineFragment.setCurrentLine(line);
    		busLineFragment.setStations(busStations, line);
    		
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
    			
    			busLineFragment.clickStation(index);
    		}
        };  
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
			BusLine line = Util.getInstance(getApplicationContext()).getBusLine(lineId);
			
			List<BusStation> result = Util.getInstance(getApplicationContext()).getBusStations(lineId, "1");
			result.addAll(Util.getInstance(getApplicationContext()).getBusStations(lineId, "0"));
			
			Message msg = lineInfoHandler.obtainMessage();
			
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("stationList", result);
			m.put("stationId", stationId);
			m.put("direction", direction);
			m.put("line", line);
			
			msg.obj = m;
			lineInfoHandler.sendMessageDelayed(msg, 1000);
		}
	}
	public void queryBus(String lineId, String stationId, String direction)
	{
		showProcess();
		
		new Thread(new QueryBusRunner(lineId, stationId, direction)).start();		
		
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
		
		setTitle();
	}

}
