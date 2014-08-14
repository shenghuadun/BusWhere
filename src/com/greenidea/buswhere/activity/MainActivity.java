package com.greenidea.buswhere.activity;

import java.util.List;

import android.R.anim;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gigi.buslocation.bean.BusStation;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.base.BaseActivity;
import com.greenidea.buswhere.fragment.BusLineFragment;
import com.greenidea.buswhere.fragment.MainFragment;
import com.greenidea.buswhere.fragment.MenuFragment;
import com.greenidea.buswhere.util.Constants;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends BaseActivity
{
	private MainFragment mainFragment;
	private MenuFragment menuFragment;
	private BusLineFragment busLineFragment;

	public BusStation currentStation;
	
	public MainActivity() 
	{
		super(R.string.app_name);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		mainFragment = new MainFragment();
		menuFragment = new MenuFragment();
		busLineFragment = new BusLineFragment();
		
		setContentView(R.layout.main);

		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mainFragment)
		.commit();
		
		// set the Behind View
		setBehindContentView(R.layout.menu);
		getSupportFragmentManager()
		.beginTransaction()
		.setCustomAnimations(anim.slide_in_left, anim.slide_out_right)
		.replace(R.id.menu_frame, menuFragment)
		.commit();
		
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
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
			mainFragment.addToFav(currentStation);
			break;
			
		case R.id.deleteFav:
			deleteFavStation(currentStation);
			refreshFav();
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

	public void clickStation(int index)
	{
		busLineFragment.clickStation(index);
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
		if(!lineId.equals(busLineFragment.getCurrentLineId()))
		{
			busLineFragment.queryBus(lineId, stationId, direction);
		}
		
		busLineFragment.resetStations();
	}

	public boolean isAlreadyFaved(BusStation station)
	{
		return mainFragment.isAlreadyFaved(station);
	}
	
//	/**
//	 * 添加到常用车站
//	 * @param station
//	 */
//	public void addToFav(BusStation station)
//	{
//		mainFragment.addToFav(station);
//	}

	public void deleteFavStation(BusStation station)
	{
		mainFragment.deleteFavStation(station);
	} 
	/**
	 * 查询收藏的车站数量
	 * @return
	 */
	public int getFavStationNum()
	{
		return mainFragment.getFavStationNum();
	}

	public void refreshFav()
	{
		mainFragment.refreshFav();
	}
}
