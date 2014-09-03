package com.greenidea.buswhere.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;

public class BaseFragment extends SherlockFragment
{
	protected BaseActivity parent;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
	}

	@Override
	public void onAttach(Activity activity)
	{
		parent = (BaseActivity)activity;
		super.onAttach(activity);
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
