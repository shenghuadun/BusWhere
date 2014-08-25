package com.greenidea.buswhere.fragment;

import org.json.JSONObject;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobads.AdSettings;
import com.baidu.mobads.AdView;
import com.baidu.mobads.AdViewListener;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.base.BaseFragment;
import com.greenidea.buswhere.bean.FavStationBean;
import com.greenidea.buswhere.bean.HisLineBean;
import com.greenidea.buswhere.component.HintAdapter;
import com.greenidea.buswhere.ui.SlideToDeleteListView;
import com.greenidea.buswhere.ui.SlideToDeleteListView.OnItemEventListener;

public class MainFragment extends BaseFragment implements OnItemEventListener
{
	private View root;

	//输入框及按钮
	private EditText lineNumInput;
	private ImageView btnSearch;
	
	//输入框输入时的自动提示
	private ListView hintList;
	
	//查询历史
	private LinearLayout hisLayout;
	private RelativeLayout staticContainer;
	
	//常用站点
	private SlideToDeleteListView favListView;

	private HintAdapter hintAdapter;
	//更新收藏站点时，画面会短暂漏出删除按钮，添加本动画来隐藏这个问题
	private AlphaAnimation faddinAnimation ;

	private AdView adView;
	
	private AdViewListener adViewListener = new AdViewListener()
	{
		
		@Override
		public void onVideoStart()
		{
			
		}
		
		@Override
		public void onVideoFinish()
		{
			
		}
		
		@Override
		public void onVideoError()
		{
			
		}
		
		@Override
		public void onVideoClickReplay()
		{
			
		}
		
		@Override
		public void onVideoClickClose()
		{
			
		}
		
		@Override
		public void onVideoClickAd()
		{
			
		}
		
		@Override
		public void onAdSwitch()
		{
			
		}
		
		@Override
		public void onAdShow(JSONObject arg0)
		{
			
		}
		
		@Override
		public void onAdReady(AdView arg0)
		{
			findViewById(R.id.adContainer).setVisibility(View.VISIBLE);
			findViewById(R.id.adContainer).startAnimation(faddinAnimation);
		}
		
		@Override
		public void onAdFailed(String arg0)
		{
			
		}
		
		@Override
		public void onAdClick(JSONObject arg0)
		{
			
		}
	};
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		root = inflater.inflate(R.layout.main_fragment, null);
		
		hintAdapter = new HintAdapter(parent);
		
		return root;
	}

	@Override
	public void onResume()
	{

		findViews();
		setListeners();

		initAd();
		
		//动画
		initFaddinAnimation();
		
		queryHandler.sendEmptyMessageDelayed(0, 500);
		
		super.onResume();
	}
	
	private Handler queryHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if(parent.getFavStations().isEmpty() && parent.getHisStations().isEmpty())
			{
				parent.queryHisAndFav();
			}
			
			prepareHisStations();
			prepareFavStations();
		}
		
	};
	
	
	private void initFaddinAnimation()
	{
		faddinAnimation = new AlphaAnimation(0, 1);
		faddinAnimation.setDuration(200);
		faddinAnimation.setAnimationListener(new AnimationListener()
		{
			
			@Override
			public void onAnimationStart(Animation animation)
			{
			}
			
			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
			
			@Override
			public void onAnimationEnd(Animation animation)
			{
				staticContainer.setVisibility(View.VISIBLE);
			}
		});
	}
	private void initAd()
	{
		if(null == adView)
		{
			AdSettings.setCity("青岛");
			adView = new AdView(parent);
			adView.setListener(adViewListener);
			final RelativeLayout adContainer = (RelativeLayout)findViewById(R.id.adContainer);
			adContainer.addView(adView);
			
			ImageView del = new ImageView(parent);
			del.setImageResource(R.drawable.ic_action_remove);
			del.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					adContainer.setVisibility(View.GONE);
				}
			});
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			del.setLayoutParams(params );
			adContainer.addView(del);
			findViewById(R.id.adContainer).setVisibility(View.GONE);	
		}
	}

	private void findViews()
	{
		lineNumInput = (EditText)findViewById(R.id.lineNum);
		btnSearch = (ImageView)findViewById(R.id.btnSearch);
		hintList = (ListView)findViewById(R.id.hintList);
		hintList.setAdapter(hintAdapter);
		
		staticContainer = (RelativeLayout) findViewById(R.id.staticContainer);
		
		hisLayout = (LinearLayout)findViewById(R.id.hisLayout);
		
		favListView = (SlideToDeleteListView)findViewById(R.id.listView);
		favListView.helper.setOnItemEventListener(this);
	}

	private void hideHints()
	{
		hintList.setVisibility(View.GONE);
		staticContainer.setVisibility(View.VISIBLE);
	}
	
	private void showHints(String key)
	{
		//特殊线路
		if(key.startsWith("0"))
		{
			showSpecialLines();
		}
		else
		{
			showLinesByKey(key);
		}
	}
	
	private void showLinesByKey(String key)
	{
		hintAdapter.refreshLines(key);
		hintList.setVisibility(View.VISIBLE);
		staticContainer.setVisibility(View.GONE);
		hintAdapter.notifyDataSetChanged();
	}

	private void showSpecialLines()
	{
		hintAdapter.refreshLines("special");
		hintList.setVisibility(View.VISIBLE);
		staticContainer.setVisibility(View.GONE);
		hintAdapter.notifyDataSetChanged();
	}
	
	private void setListeners()
	{
		lineNumInput.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if(hasFocus)
				{
					lineNumInput.selectAll();
					
//					adView.setVisibility(View.GONE);
				}
				else
				{
//					adView.setVisibility(View.VISIBLE);
				}
			}
		});
		
		lineNumInput.addTextChangedListener(new TextWatcher()
		{
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				Message msg = queryHintsHandler.obtainMessage();
				msg.obj = s.toString();
				queryHintsHandler.sendMessageDelayed(msg, 300);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				
			}
			
			@Override
			public void afterTextChanged(Editable s)
			{
				
			}
		});
		
		btnSearch.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				parent.queryBus(lineNumInput.getText().toString(), null, null);		
			}

		});
	}
	
	private Handler queryHintsHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			//此时数据还未改变，认为用户输入好了
			if(lineNumInput.getText().toString().equals(msg.obj))
			{
				showHints((String) msg.obj);
			}
		}
		
	};
	
	public void prepareFavStations()
	{
		staticContainer.setVisibility(View.INVISIBLE);

		favListView.helper.removeAllViews();

		for(FavStationBean  bean : parent.getFavStations())
		{
			LinearLayout layout = getFavBlock(bean);
			
			favListView.helper.addDeletableView(layout);
		}
		staticContainer.startAnimation(faddinAnimation);
	}
	
	public void prepareHisStations()
	{
		hisLayout.removeAllViews();
		hisLayout.setGravity(Gravity.LEFT);

		for(HisLineBean bean : parent.getHisStations())
		{
			TextView layout = getHisBlock(bean, hisLayout.getWidth());
			
			hisLayout.addView(layout);
		}
	}

	private TextView getHisBlock(HisLineBean hisLine, int parentWidth)
	{
		String lineName = hisLine.getLineName();
		String lineId = hisLine.getLineId();
		
		TextView result = (TextView) parent.getLayoutInflater().inflate(R.layout.block, null);
		result.setText(lineName);
		result.setTag(lineId);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(5, 5, 5, 5);
		result.setLayoutParams(params);
		result.setBackgroundResource(R.drawable.hisitembk);
		result.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				parent.queryBus((String) v.getTag(), null, null);
			}
		});
		
		result.setTextColor(getResources().getColor(R.color.textColor));
		
		return result;
	}

	private LinearLayout getFavBlock(FavStationBean station)
	{
		LinearLayout result = new LinearLayout(parent);
		result.setTag(station);
		result.setOrientation(LinearLayout.HORIZONTAL);
//		result.setBackgroundResource(R.drawable.hisviewblock);
		
		LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		result.setLayoutParams(p);
		
		TextView textView = (TextView)parent.getLayoutInflater().inflate(R.layout.block, null);

		textView.setText(station.getLineId() + "路-" + station.getStationName() 
				+ "站-" + ("0".equals(station.getDirection()) ? "上行" : "下行") );
		textView.setTextSize(16);

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.setMargins(30, 0, 30, 0);
		textView.setLayoutParams(params);
		textView.setBackgroundColor(Color.TRANSPARENT);
		textView.setTextColor(getResources().getColor(R.color.textColor));
		
		result.addView(textView);
		
		return result;
	}

	/**
	 * 选中常用车站
	 */
	@Override
	public void onItemSelectd(View item)
	{
		FavStationBean bean = (FavStationBean)item.getTag();
		
		if(bean != null)
		{
			parent.queryBus(bean.getLineId(), bean.getStationId(), bean.getDirection());
		}
	}
	/**
	 * 删除常用车站
	 */
	@Override
	public void onItemDelete(View item)
	{
		FavStationBean bean = (FavStationBean)item.getTag();
		
		parent.deleteFavStation(bean);
	}

	/**
	 * 返回按钮按下时，隐藏提示
	 * @return
	 */
	public boolean onBackPressed()
	{
		if(hintList.getVisibility() == View.VISIBLE)
		{
			hideHints();
			return true;
		}
		return false;
	}
}
