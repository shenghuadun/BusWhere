package com.greenidea.buswhere.base;

import android.R.anim;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.fragment.MenuFragment;
import com.greenidea.buswhere.util.Util;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class BaseActivity extends SlidingFragmentActivity {

	private MenuFragment menuFragment;

	protected boolean exitOnBackPressed = true;
	
	public BaseActivity() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setBehindScrollScale(0.0f);
//		sm.setBehindCanvasTransformer(new CanvasTransformer() {
//			@Override
//			public void transformCanvas(Canvas canvas, float percentOpen) {
//				float scale = (float) (percentOpen*0.25 + 0.75);
//				canvas.scale(scale, scale, canvas.getWidth()/2, canvas.getHeight()/2);
//			}
//		});
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		menuFragment = new MenuFragment();

		// set the Behind View
		FrameLayout f = new FrameLayout(this);
		f.setId(190871026);
		setBehindContentView(f);
		
		getSupportFragmentManager()
		.beginTransaction()
		.setCustomAnimations(anim.slide_in_left, anim.slide_out_right)
		.replace(190871026, menuFragment)
		.commit();
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

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(exitOnBackPressed)
			{
				if(!getSlidingMenu().isMenuShowing())
				{
					showMenu();
				}
				else
				{
					//退出应用
					Intent intent = new Intent(Intent.ACTION_MAIN);  
	                intent.addCategory(Intent.CATEGORY_HOME);  
	                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
	                startActivity(intent);  
	                android.os.Process.killProcess(android.os.Process.myPid());
				}
				return true;
			}
			else
			{
				exitOnBackPressed = true;
				return super.onKeyUp(keyCode, event);
			}
		}
		else
		{
			return super.onKeyUp(keyCode, event);
		}
	}
	
	public int dip2px(float dipValue)
	{
		return Util.dip2px(dipValue, getResources());
	}

	public int px2dip(float pxValue)
	{
		return Util.px2dip(pxValue, getResources());
	}
}
