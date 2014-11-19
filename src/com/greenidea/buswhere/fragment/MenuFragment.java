package com.greenidea.buswhere.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.greenidea.buswhere.R;
import com.greenidea.buswhere.activity.AboutActivity;
import com.greenidea.buswhere.activity.MainActivity;
import com.greenidea.buswhere.activity.MultiLineStationActivity;
import com.greenidea.buswhere.base.BaseFragment;

public class MenuFragment extends BaseFragment
{

	private RelativeLayout menuView;
	
	private LinearLayout topContainer;
	private LinearLayout bottomContainer;
	
	private List<Menu> menuList = new ArrayList<MenuFragment.Menu>();
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		menuView = (RelativeLayout) inflater.inflate(R.layout.menu, null);
		topContainer = (LinearLayout) menuView.findViewById(R.id.topContainer);
		bottomContainer = (LinearLayout) menuView.findViewById(R.id.bottomContainer);

	    Menu menu = new Menu();
	    menu.intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
	    menu.iconId = R.drawable.ic_action_forward;
	    menu.nameId = R.string.menu_line;	     
	    menuList.add(menu);
	    
	    menu = new Menu();
	    menu.intent = new Intent(getActivity().getApplicationContext(), MultiLineStationActivity.class);
	    menu.iconId = R.drawable.ic_action_split;
	    menu.nameId = R.string.menu_station;	     
	    menuList.add(menu);

//	    //一键分享
//	    menu = new Menu();
//	    menu.type = Menu.TYPE_SHARE;
//	    Intent shareInt = new Intent(Intent.ACTION_SEND);
//		shareInt.setType("text/plain");
//		shareInt.putExtra(Intent.EXTRA_SUBJECT, "U社区");
//		shareInt.putExtra(Intent.EXTRA_TEXT, "我在使用【青岛公交实时查询】软件查公交车实时位置，再也不用在车站傻等了，你也快来试试吧！下载地址是：http://apk.hiapk.com/appdown/com.tgj.ju.jiji");
//		shareInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		menu.intent = shareInt;
//	    menu.iconId = R.drawable.ic_action_about;
//	    menu.nameId = R.string.share;
//	    menu.isBottomMenu = true;
//	    menuList.add(menu);
	    
	    //精品应用下载
//	    menu = new Menu();
//	    menu.type = Menu.TYPE_APP;
//	    menu.iconId = R.drawable.ic_action_download;
//	    menu.nameId = R.string.apps;
////	    menu.isBottomMenu = true;
//	    menuList.add(menu);
	    
	    //意见反馈
//	    menu = new Menu();
//	    menu.type = Menu.TYPE_FEEDBACK;
//	    menu.iconId = R.drawable.ic_action_send_now;
//	    menu.nameId = R.string.feedback;
////	    menu.isBottomMenu = true;
//	    menuList.add(menu);
	    
	    menu = new Menu();
	    menu.intent = new Intent(getActivity().getApplicationContext(), AboutActivity.class);
	    menu.iconId = R.drawable.ic_action_about;
	    menu.nameId = R.string.menu_about;
//	    menu.isBottomMenu = true;
	    menuList.add(menu);
	    
	    for(Menu m : menuList)
	    {
	    	RelativeLayout convertView = (RelativeLayout) inflater.inflate(R.layout.menuitem, null);
		    convertView.setTag(m);
		    ((ImageView)convertView.findViewById(R.id.icon)).setImageResource(m.iconId);
		    ((TextView)convertView.findViewById(R.id.menuName)).setText(m.nameId);
		    ((TextView)convertView.findViewById(R.id.menuName)).setTextColor(Color.WHITE);
		    
		    convertView.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					parent.showContent();
					Menu menu =  ((Menu) v.getTag());
					
					switch (menu.type)
					{
					case Menu.TYPE_ACTIVITY:
						Intent intent = menu.intent;
						parent.startActivity(intent);
						break;
					case Menu.TYPE_SHARE:
						intent = menu.intent;
						parent.startActivity(intent);
						break;

					default:
						break;
					}
			    }

			});
		    

		    ImageView divider = new ImageView(parent);
		    divider.setBackgroundColor(Color.parseColor("#999999"));
		    divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 1));
		    
		    if(m.isBottomMenu)
		    {
		    	bottomContainer.addView(divider);
		    	bottomContainer.addView(convertView);
		    }
		    else
		    {
		    	topContainer.addView(divider);
		    	topContainer.addView(convertView);
		    }
	    }
		
		return menuView;
	}

    class Menu
    {
    	int type = TYPE_ACTIVITY;
    	int nameId;
    	int iconId;
    	Intent intent;
    	
    	boolean isBottomMenu = false;
    	
    	static final int TYPE_ACTIVITY = -1;
    	static final int TYPE_APP = 0;
    	static final int TYPE_FEEDBACK = 1;
    	static final int TYPE_SHARE = 2;
    }
}
