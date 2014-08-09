package com.greenidea.buswhere.base;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gigi.av.GigiLayout;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.util.Util;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class BaseActivity extends SlidingFragmentActivity {

	private int mTitleRes;
	protected ListFragment mFrag;

	public BaseActivity(int titleRes) {
		mTitleRes = titleRes;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(mTitleRes);

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
//		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
Dialog dialog;
	
	private Handler adHandler = new Handler()
	{
        public void handleMessage(Message msg) 
        {  
        	//广告设置
    		final GigiLayout adsMogoView = (GigiLayout) findViewById(R.id.adsMogoView);
    		if(null != adsMogoView)
    		{
    			adsMogoView.setVisibility(View.INVISIBLE);
    			adsMogoView.postDelayed(new Runnable()
    			{
    				
    				@Override
    				public void run()
    				{
    					adsMogoView.setVisibility(View.VISIBLE);				
    				}
    			}, 4000);
    		}
        }
	};
	
	public void showProcess()
	{
//		fragment.show(getFragmentManager(), "");
		if(null == dialog)
		{
			dialog = new Dialog(this, R.style.dialog);
			
			View view = this.getLayoutInflater().inflate(R.layout.dialog, null);
			
			dialog.setContentView(view);
		}

		ImageView img = (ImageView) dialog.findViewById(R.id.imageView1);
		img.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
		dialog.show();
	}

	public void hideProcess()
	{
		timerHandler.sendEmptyMessageDelayed(0, 500);
	}

	private Handler timerHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
//			fragment.dismiss();
			if(null != dialog)
			{
				dialog.dismiss();
			}
		}
	};

	public int dip2px(float dipValue)
	{
		return Util.dip2px(dipValue, getResources());
	}

	public int px2dip(float pxValue)
	{
		return Util.px2dip(pxValue, getResources());
	}
}
