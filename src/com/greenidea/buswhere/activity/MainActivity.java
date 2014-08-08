package com.greenidea.buswhere.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.base.BaseActivity;
import com.greenidea.buswhere.fragment.MainInfoFragment;
import com.greenidea.buswhere.fragment.MenuFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends BaseActivity 
{
	
	private Fragment mainFragment;
	private Fragment menuFragment;
	
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
			mainFragment = getSupportFragmentManager().getFragment(savedInstanceState, "mainFragment");
			menuFragment = getSupportFragmentManager().getFragment(savedInstanceState, "menuFragment");
		}
		if (mainFragment == null)
		{
			mainFragment = new MainInfoFragment();
		}
		if (menuFragment == null)
		{
			menuFragment = new MenuFragment();
		}
		
		// set the Above View
		setContentView(R.layout.main);
		
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mainFragment)
		.commit();
		
		// set the Behind View
		setBehindContentView(R.layout.menu);
		getSupportFragmentManager()
		.beginTransaction()
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
	}
	
	public void switchContent(Fragment fragment)
	{
		mainFragment = fragment;
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, fragment)
		.commit();
		
		getSlidingMenu().showContent();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.menu, menu);

		return true;
	}

}
