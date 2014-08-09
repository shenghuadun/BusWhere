package com.greenidea.buswhere.activity;

import java.util.List;

import android.R.anim;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gigi.buslocation.bean.BusStation;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.base.BaseActivity;
import com.greenidea.buswhere.bean.FavStationBean;
import com.greenidea.buswhere.fragment.BusLineFragment;
import com.greenidea.buswhere.fragment.MainInfoFragment;
import com.greenidea.buswhere.fragment.MenuFragment;
import com.greenidea.buswhere.util.Constants;
import com.greenidea.buswhere.util.Util;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends BaseActivity 
{
	private MainInfoFragment mainFragment;
	private MenuFragment menuFragment;
	private BusLineFragment busLineFragment;
	
	private SearchView searchView;
	
	public SharedPreferences prefHistory;
	public SharedPreferences prefFav;

	//查询常用站点时的详细信息
	private FavStationBean curFavStation = null;
	
	public MainActivity() 
	{
		super(R.string.app_name);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// set the Above View
		if (savedInstanceState != null)
		{
			mainFragment = (MainInfoFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mainFragment");
			menuFragment = (MenuFragment) getSupportFragmentManager().getFragment(savedInstanceState, "menuFragment");
			busLineFragment = (BusLineFragment) getSupportFragmentManager().getFragment(savedInstanceState, "busLineFragment");
		}
		if (mainFragment == null)
		{
			mainFragment = new MainInfoFragment();
		}
		if (menuFragment == null)
		{
			menuFragment = new MenuFragment();
		}
		if (busLineFragment == null)
		{
			busLineFragment = new BusLineFragment();
		}

		prefHistory = getSharedPreferences(Constants.PREF_HISTORY, Context.MODE_PRIVATE);
		prefFav = getSharedPreferences(Constants.PREF_FAVORITE, Context.MODE_PRIVATE);
		
		// set the Above View
		setContentView(R.layout.main);

		getSupportFragmentManager()
		.beginTransaction()
		.setCustomAnimations(anim.slide_in_left, anim.slide_out_right)
		.replace(R.id.content_frame, mainFragment)
		.addToBackStack(null)
		.commit();
		
		// set the Behind View
		setBehindContentView(R.layout.menu);
		getSupportFragmentManager()
		.beginTransaction()
		.setCustomAnimations(anim.slide_in_left, anim.slide_out_right)
		.replace(R.id.menu_frame, menuFragment)
		.commit();
		
		// customize the SlidingMenu
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "mainFragment", mainFragment);
		getSupportFragmentManager().putFragment(outState, "menuFragment", menuFragment);
		getSupportFragmentManager().putFragment(outState, "busLineFragment", busLineFragment);
	}
	
//	public void switchContent(Fragment fragment)
//	{
//		mainFragment = (MainInfoFragment) fragment;
//		getSupportFragmentManager()
//		.beginTransaction()
//		.replace(R.id.content_frame, fragment)
//		.commit();
//		
//		getSlidingMenu().showContent();
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.menu, menu);

		searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setOnQueryTextListener(new OnQueryTextListener()
		{
			
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				final String lineNum = searchView.getQuery().toString();
				if(!lineNum.equals(""))
				{
					showProcess();
					
					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							String lineId = lineNum;
							
							List<BusStation> result = Util.getInstance(MainActivity.this.getApplicationContext()).getBusStations(lineId, "1");
							result.addAll(Util.getInstance(MainActivity.this.getApplicationContext()).getBusStations(lineId, "0"));
							
							Message msg = lineInfoHandler.obtainMessage();
							msg.obj = result;
							lineInfoHandler.sendMessageDelayed(msg, 1000);
						}
					}).start();		
				}
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText)
			{
				// TODO Auto-generated method stub
				return false;
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.search:
			break;

		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	
	private Handler lineInfoHandler = new Handler()
	{
        public void handleMessage(Message msg) 
        {  
        	@SuppressWarnings("unchecked")
			List<BusStation> busStations = (List<BusStation>) msg.obj;
        	
        	hideProcess();
        	
        	if(busStations.isEmpty())
        	{
        		Toast.makeText(MainActivity.this, "未查询到本路车", Toast.LENGTH_SHORT).show();
        		return;
        	}
        	
        	searchView.clearFocus();

    		//显示路线
    		getSupportFragmentManager()
    		.beginTransaction()
    		.setCustomAnimations(anim.slide_in_left, anim.slide_out_right)
    		.replace(R.id.content_frame, busLineFragment)
    		.addToBackStack(null)
    		.commit();
    		
        	//隐藏输入法
//        	lineIdInput.clearFocus();
//        	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        	imm.hideSoftInputFromWindow(lineIdInput.getWindowToken(), 0);

    		prefHistory.edit().putLong(busStations.get(0).getLineId(), System.currentTimeMillis()).commit();
    		
        	busLineFragment.setStations(busStations);
    		
    		//查询常用车站
    		if(null != curFavStation)
    		{
    			int index = -1;
    			for(int i = 0; i< busLineFragment.getStations().size(); i++)
    			{
    				BusStation station = busLineFragment.getStations().get(i);
    				if(station.getStationId().equals(curFavStation.getStationId()) 
    						&& station.getDirection().equals(curFavStation.getDirection()))
    				{
    					index = i;
    				}
    			}
    			
    			busLineFragment.clickStation(index);
    			
    			//需要清空，以免下次查询线路时，会查询站点
    			curFavStation = null;
    		}
    		
    		//更新查询记录
    		mainFragment.queryHis();
        };  
	};

}
