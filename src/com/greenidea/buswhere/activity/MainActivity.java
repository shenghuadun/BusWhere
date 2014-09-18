package com.greenidea.buswhere.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.adsmogo.offers.MogoOffer;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.gigi.buslocation.bean.BusLine;
import com.gigi.buslocation.bean.BusStation;
import com.greenidea.baidu.push.Utils;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.base.BaseActivity;
import com.greenidea.buswhere.bean.FavStationBean;
import com.greenidea.buswhere.bean.HisLineBean;
import com.greenidea.buswhere.fragment.BusLineFragment;
import com.greenidea.buswhere.fragment.MainFragment;
import com.greenidea.buswhere.interfaces.OnHintClickListener;
import com.greenidea.buswhere.util.BusDBHelper;
import com.greenidea.buswhere.util.Constants;
import com.greenidea.buswhere.util.Util;

public class MainActivity extends BaseActivity implements OnHintClickListener
{
	private boolean isFirstIn;
	private MainFragment mainFragment;
	private BusLineFragment busLineFragment;

	private List<FavStationBean> favStations = new ArrayList<FavStationBean>();
	
	private List<HisLineBean> hisStations = new ArrayList<HisLineBean>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		if(!getPreferences(Context.MODE_PRIVATE).getBoolean("initiallizedDB", false))
		{
			showProcess();
			BusDBHelper.copyDatabaseFile(this.getApplicationContext(), getDatabasePath("busdb").getParent());
			getPreferences(Context.MODE_PRIVATE).edit().putBoolean("initiallizedDB", true).commit();
			hideProcess();
		}

		setContentView(R.layout.main);

		isFirstIn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isFirstIn_MainActivity", true);
		if(isFirstIn)
		{
			findViewById(R.id.guide).setVisibility(View.VISIBLE);
			findViewById(R.id.guide).setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					findViewById(R.id.guide).setVisibility(View.GONE);
				}
			});
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isFirstIn_MainActivity", false).commit();
		}

		mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment);
		
		setupBaiduPush();
		
		MogoOffer.init(this, "95645d068efe4d55854960e0d10f3978");
		//设置顺序展示模式下，选择积分墙入口的弹出框标题；
		MogoOffer.setOfferListTitle("精品应用下载");
		//设置顺序展示模式下，选择积分墙入口的弹出框入口前缀；
		MogoOffer.setOfferEntranceMsg("精品应用下载");
		//设置是否显示芒果积分墙积分显示；
		//（此处只能够设置芒果积分墙， 其他单一积分墙需要到各个平台网站设置）
		MogoOffer.setMogoOfferScoreVisible(false);
	}
	
	private void setupBaiduPush()
	{
		// Push: 以apikey的方式登录，一般放在主Activity的onCreate中。
        // 这里把apikey存放于manifest文件中，只是一种存放方式，
        // 您可以用自定义常量等其它方式实现，来替换参数中的Utils.getMetaValue(PushDemoActivity.this,
        // "api_key")
        // 通过share preference实现的绑定标志开关，如果已经成功绑定，就取消这次绑定
        if (!Utils.hasBind(getApplicationContext())) {
            PushManager.startWork(getApplicationContext(),
                    PushConstants.LOGIN_TYPE_API_KEY,
                    Utils.getMetaValue(this, "api_key"));
            // Push: 如果想基于地理位置推送，可以打开支持地理位置的推送的开关
            // PushManager.enableLbs(getApplicationContext());
        }
	}

	public void queryHisAndFav()
	{
		//历史记录和收藏
		hisStations = queryHisStations();
		favStations = queryFavStations();
	}
	
	/**
	 * 查询常用车站
	 */
	private List<FavStationBean> queryFavStations()
	{
		List<FavStationBean> result = Util.getInstance(this).queryFav();

		return result;
	}

	private List<HisLineBean> queryHisStations()
	{
		List<HisLineBean> result = Util.getInstance(this).queryHis();

		return result;
	}

	public void resetTitle()
	{
		getSupportActionBar().setTitle(R.string.app_name);
		getSupportActionBar().setSubtitle(null);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			//删除当前fragment
			if(((FrameLayout)findViewById(R.id.content_frame)).getChildCount() != 0 )
			{
				//如果露出main的话，需要重新加载收藏和历史
				if(getSupportFragmentManager().getBackStackEntryCount() == 1)
				{
					mainFragment.prepareFavStations();
					mainFragment.prepareHisStations();
				}
				exitOnBackPressed = false;
				return super.onKeyUp(keyCode, event);
			}
			//当前显示的已经是mainFragment
			else
			{
				boolean consumed = mainFragment.onBackPressed();

				if(!consumed)
				{
					return super.onKeyUp(keyCode, event);
				}
				return true;
			}
		}
		else
		{
			return super.onKeyUp(keyCode, event);
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			new Thread(new Runnable()
			{
				
				@Override
				public void run()
				{
	                Instrumentation inst = new Instrumentation();  
	                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); 
				}
			}).start();
			
			return true;
		default: break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	public void showFragment(int which)
	{
		switch (which)
		{
		case BusLineFragment.FRAGMENT_INDEX:
			if(null == busLineFragment)
			{
				busLineFragment = new BusLineFragment();
			}
			getSupportFragmentManager()
			.beginTransaction()
			.setCustomAnimations(R.anim.slide_in, R.anim.slide_in)
			.replace(R.id.content_frame, busLineFragment)
			.addToBackStack(null)
			.commit();

			//立即切换fragment
			getSupportFragmentManager().executePendingTransactions();
			break;

		default:
			break;
		}
	}
	
	private Handler lineStationInfoHandler = new Handler()
	{
        @SuppressWarnings("unchecked")
		public void handleMessage(Message msg) 
        {  
			Map<String, Object> m = (Map<String, Object>) msg.obj;
			
			BusLine line = (BusLine) m.get("line");
			List<BusStation> busStations = (List<BusStation>) m.get("stationList");
			
			String stationId = (String) m.get("stationId");
			String direction = (String) m.get("direction");

			if(busStations == null)
			{
        		Toast.makeText(MainActivity.this, "网络不给力哦亲~", Toast.LENGTH_LONG).show();
        		hideProcess();
        		return;
        	}
			else if(busStations.isEmpty())
			{
        		Toast.makeText(MainActivity.this, "未查询到本路车", Toast.LENGTH_LONG).show();
        		hideProcess();
        		return;
        	}

			showFragment(BusLineFragment.FRAGMENT_INDEX);

			//更新查询历史
    		HisLineBean bean = new HisLineBean();
    		bean.setLineId(line.getLineId());
    		bean.setLineName(line.getLineName());
    		bean.setGroup(line.getGroupName());
    		bean.setTime(String.valueOf(System.currentTimeMillis()));

    		addNewHis(bean);
    		
        	//隐藏输入法
//        	lineIdInput.clearFocus();
//        	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        	imm.hideSoftInputFromWindow(lineIdInput.getWindowToken(), 0);

    		busLineFragment.setCurrentLine(line);
    		busLineFragment.setStations(busStations, line);
    		
    		//查询的是收藏车站
    		if(null != stationId)
    		{
    			int index = -1;
    			
    			for(int i = 0; i< busStations.size(); i++)
    			{
    				BusStation station = busStations.get(i);
    				if(station.getStationId().equals(stationId) 
    						&& station.getDirection().equals(direction))
    				{
    					index = i;
    				}
    			}
    			
    			busLineFragment.clickStation(index);
    		}
        }

		private void addNewHis(HisLineBean bean)
		{
			//如果本线路已经有了，先删除它
			for(HisLineBean his : hisStations)
			{
				if(his.getLineId().equals(bean.getLineId()))
				{
					hisStations.remove(his);
					break;
				}
			}
			//放在第一位
			hisStations.add(0, bean);
    		Util.getInstance(MainActivity.this).saveHis(bean);
		};  
	};

	public void queryBus(String lineId, String stationId, String direction)
	{
		showProcess();
		
		new Thread(new QueryStationsRunner(lineId, stationId, direction)).start();		
		
		//第一次查询时，busLineFragment还未生成
		if(null != busLineFragment)
		{
			busLineFragment.resetStations();
		}
	}
	
	/**
	 * 判断车站是否已经收藏过
	 * @param station
	 * @return
	 */
	public boolean isAlreadyFaved(BusStation station)
	{
		for(FavStationBean bean : favStations)
		{
			if(bean.getLineId().equals(station.getLineId()) && bean.getStationId().equals(station.getStationId()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 添加到常用车站
	 * @param station
	 */
	public void addToFav(BusStation station)
	{
		FavStationBean favBean = new FavStationBean(station);
		addToFav(favBean);
	}
	
	public void addToFav(FavStationBean favBean)
	{
		if(getFavStationNum() >= Constants.MAX_FAV_NUM)
		{
			Toast.makeText(getApplicationContext(), "常用站点过多，请删除后再收藏", Toast.LENGTH_SHORT).show();
		}
		else if(Util.getInstance(this).saveFav(favBean))
		{
			favStations.add(0, favBean);
			Toast.makeText(getApplicationContext(), "已收藏", Toast.LENGTH_SHORT).show();
		}
		else 
		{
			Toast.makeText(getApplicationContext(), "收藏失败", Toast.LENGTH_SHORT).show();
		}
	}

	public boolean deleteFavStation(FavStationBean bean)
	{
		if(Util.getInstance(this).deleteFav(bean))
		{
			for(FavStationBean fsb : favStations)
			{
				if(fsb.getLineId().equals(bean.getLineId()) 
						&& fsb.getStationId().equals(bean.getStationId()) 
						&& fsb.getDirection().equals(bean.getDirection()))
				{
					favStations.remove(fsb);
					break;
				}
			}
			
			Toast.makeText(getApplicationContext(), "已取消", Toast.LENGTH_SHORT).show();
			return true;
		}
		else
		{
			Toast.makeText(getApplicationContext(), "取消失败", Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	
	public void deleteFavStation(BusStation station)
	{
		deleteFavStation(new FavStationBean(station));
	} 

	public int getFavStationNum()
	{
		return favStations.size();
	}

	public List<FavStationBean> getFavStations()
	{
		return favStations;
	}

	public void setFavStations(List<FavStationBean> favStations)
	{
		this.favStations = favStations;
	}

	public List<HisLineBean> getHisStations()
	{
		return hisStations;
	}

	public void setHisStations(List<HisLineBean> hisStations)
	{
		this.hisStations = hisStations;
	}
	
	private class QueryStationsRunner implements Runnable
	{
		private String lineId;
		private String stationId;
		private String  direction;
		
		public QueryStationsRunner(String lineId, String stationId, String direction)
		{
			this.lineId = lineId;
			this.stationId = stationId;
			this.direction = direction;
		}
		@Override
		public void run()
		{
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			
			BusLine line = Util.getInstance(getApplicationContext()).getBusLine(lineId);
			
			List<BusStation> downList = Util.getInstance(getApplicationContext()).getBusStations(lineId, "1");
			List<BusStation> upList = Util.getInstance(getApplicationContext()).getBusStations(lineId, "0");

			if(null != downList)
			{
				downList.addAll(upList);
			}
			

			//数据库中没有的情况
			if(null == line)
			{
				String startStation = "";
				String endStation = "";
				if(!downList.isEmpty())
				{
					startStation = downList.get(0).getStationName();
				}
				if(!upList.isEmpty())
				{
					endStation = upList.get(0).getStationName();
				}
				
				line = new BusLine();
				line.setLineId(lineId);
				line.setLineName(lineId + "路");
				line.setMainStationsDesc("");
				line.setPrice("");
				line.setSearchKey(lineId);
				line.setTotalLength("");
				line.setUpAvilableTime(endStation + ":未知服务时间");
				line.setUpDesc(endStation);
				line.setDownAvilableTime(startStation + ":未知服务时间");
				line.setDownDesc(startStation);
				line.setGroupName("gj");
			}
			
			Message msg = lineStationInfoHandler.obtainMessage();
			
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("stationList", downList);
			m.put("stationId", stationId); 
			m.put("direction", direction);
			m.put("line", line);
			
			msg.obj = m;
			lineStationInfoHandler.sendMessageDelayed(msg, 300);
		}
	}

	@Override
	public void onHintItemClicked(String lineId)
	{
		queryBus(lineId, null, null);	
	}

	@Override
	protected void onDestroy()
	{
		MogoOffer.clear(this);
		super.onDestroy();
	}
	
}
