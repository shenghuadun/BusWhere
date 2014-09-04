package com.greenidea.buswhere.base;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;

public class BaseFragment extends SherlockFragment
{
	private final String TAG = this.getClass().getSimpleName();
	protected BaseActivity parent;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		parent = (BaseActivity)activity;

		Log.d(TAG, "onAttach");
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.d(TAG, "onResume");
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		Log.d(TAG, "onDetach");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated");
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		Log.d(TAG, "onDestroyView");
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Log.d(TAG, "onStart");
	}

	@Override
	public void onStop()
	{
		super.onStop();
		Log.d(TAG, "onStop");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}
	
	protected View findViewById(int id)
	{
		return parent.findViewById(id);
	}
}
