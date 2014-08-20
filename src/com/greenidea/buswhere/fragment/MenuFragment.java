package com.greenidea.buswhere.fragment;

import java.util.ArrayList;
import java.util.List;

import android.R.menu;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.greenidea.buswhere.R;
import com.greenidea.buswhere.activity.MainActivity;

public class MenuFragment extends Fragment
{

	private RelativeLayout menuView;
	
	private LinearLayout topContainer;
	private LinearLayout bottomContainer;
	
	private MainActivity parent;
	
	private List<Menu> menuList = new ArrayList<MenuFragment.Menu>();
	
	public MenuFragment(MainActivity parent) {
		setRetainInstance(true);
		
		this.parent = parent;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		menuView = (RelativeLayout) inflater.inflate(R.layout.menu, null);
		topContainer = (LinearLayout) menuView.findViewById(R.id.topContainer);
		bottomContainer = (LinearLayout) menuView.findViewById(R.id.bottomContainer);

	    Menu menu = new Menu();
	    menu.fragId = 0;
	    menu.iconId = R.drawable.line;
	    menu.nameId = R.string.menu_line;	     
	    menuList.add(menu);
	    
	    menu = new Menu();
	    menu.fragId = 2;
	    menu.iconId = R.drawable.station;
	    menu.nameId = R.string.menu_station;	     
	    menuList.add(menu);
	    
	    menu = new Menu();
	    menu.fragId = 3;
	    menu.iconId = R.drawable.ic_action_about;
	    menu.nameId = R.string.menu_about;
	    menu.isBottomMenu = true;
	    menuList.add(menu);
	    
	    for(Menu m : menuList)
	    {
	    	RelativeLayout convertView = (RelativeLayout) inflater.inflate(R.layout.menuitem, null);
	
		    convertView.setTag(m);
		    ((ImageView)convertView.findViewById(R.id.icon)).setImageResource(m.iconId);
		    ((TextView)convertView.findViewById(R.id.menuName)).setText(m.nameId);
		    
		    convertView.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					parent.showContent();
	
					parent.showFragment(((Menu) v.getTag()).fragId);
				}
			});
		    

		    ImageView divider = new ImageView(parent);
		    divider.setBackgroundColor(Color.parseColor("#ebebeb"));
		    divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, parent.dip2px(1)));
		    
		    if(m.isBottomMenu)
		    {
		    	bottomContainer.addView(divider);
		    	bottomContainer.addView(convertView);
		    }
		    else
		    {
		    	topContainer.addView(convertView);
		    	topContainer.addView(divider);
		    }
	    }
		
		return menuView;
	}
	
    class Menu
    {
    	int nameId;
    	int iconId;
    	int fragId;
    	
    	boolean isBottomMenu = false;
    }
}
