package com.greenidea.buswhere.base;

import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;
import com.greenidea.buswhere.activity.MainActivity;

public class BaseFragment extends SherlockFragment
{
	protected MainActivity parent;
	
	public BaseFragment(MainActivity activity) 
	{
		setRetainInstance(true);
		parent = activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	protected View findViewById(int id)
	{
		return parent.findViewById(id);
	}
}
