package com.greenidea.buswhere.fragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.gigi.buslocation.bean.BusStation;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.activity.MultiLineStationActivity;
import com.greenidea.buswhere.base.BaseFragment;
import com.greenidea.buswhere.bean.OneLineStation;
import com.greenidea.buswhere.component.AlertDialogFragment;
import com.greenidea.buswhere.component.AlertDialogFragment.OnUserSelectListener;
import com.greenidea.buswhere.component.HintAdapter;
import com.greenidea.buswhere.interfaces.OnHintClickListener;
import com.greenidea.buswhere.ui.BusLineView;
import com.greenidea.buswhere.util.Util;

public class MultiLineStationAddFragment extends BaseFragment implements OnHintClickListener, OnUserSelectListener
{
	View root;
	//输入框及按钮
	private EditText lineNumInput;
	private ImageView btnSearch;
	
	//输入框输入时的自动提示
	private ListView hintList;
	private HintAdapter hintAdapter;
	
	private BusLineView busLineView;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		((MultiLineStationActivity)parent).resetTitle();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Log.d(this.getClass().getName(), "onCreateView");
		
		parent.getSupportActionBar().setTitle("添加车站");
		
		root = inflater.inflate(R.layout.stationadd, null);
		
		return root;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		findViews();
		setListeners();
	}
	private void findViews()
	{
		lineNumInput = (EditText)findViewById(R.id.lineNum);
		btnSearch = (ImageView)findViewById(R.id.btnSearch);
		
		hintList = (ListView)findViewById(R.id.hintList);

		hintAdapter = new HintAdapter(this, parent);
		hintList.setAdapter(hintAdapter);
		
		busLineView = (BusLineView) findViewById(R.id.busLineView);
		busLineView.setStationClickHandler(stationClickHandler);
	}

	private void setListeners()
	{
		lineNumInput.addTextChangedListener(new TextWatcher()
		{
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				Message msg = queryHintsHandler.obtainMessage();
				msg.obj = s.toString();
				queryHintsHandler.sendMessageDelayed(msg, 200);
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
				queryBus(lineNumInput.getText().toString());		
			}

		});
	}

	private Handler stationClickHandler = new Handler()
	{
        public void handleMessage(android.os.Message msg) 
        {  
        	BusStation stationClicked = (BusStation)msg.obj;
        	
        	AlertDialogFragment fragment = new AlertDialogFragment("添加" + stationClicked.getStationName(), 
        			MultiLineStationAddFragment.this, stationClicked);
        	fragment.show(parent.getSupportFragmentManager(), "");
        }
	};
	
	
	private Handler queryHintsHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			//此时数据还未改变，认为用户输入好了
			if(lineNumInput.getText().toString().equals(msg.obj) && !lineNumInput.getText().toString().equals(""))
			{
				showHints((String) msg.obj);
			}
		}
	};

	private void queryBus(String lineId)
	{
		parent.showProcess();
		
		new Thread(new QueryStationsRunner(lineId)).start();		
	}
	
	private void hideHints()
	{
		hintList.setVisibility(View.GONE);
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
		hintAdapter.notifyDataSetChanged();
	}

	private void showSpecialLines()
	{
		hintAdapter.refreshLines("special");
		hintList.setVisibility(View.VISIBLE);
		hintAdapter.notifyDataSetChanged();
	}

	private class QueryStationsRunner implements Runnable
	{
		private String lineId;
		
		public QueryStationsRunner(String lineId)
		{
			this.lineId = lineId;
		}
		@Override
		public void run()
		{
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			
			List<BusStation> result = Util.getInstance(parent.getApplicationContext()).getBusStations(lineId, "1");
			if(null != result)
			{
				result.addAll(Util.getInstance(parent.getApplicationContext()).getBusStations(lineId, "0"));
			}
			
			Message msg = lineStationInfoHandler.obtainMessage();
			
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("stationList", result);
			
			msg.obj = m;
			lineStationInfoHandler.sendMessageDelayed(msg, 200);
		}
	}
	
	private Handler lineStationInfoHandler = new Handler()
	{
        @SuppressWarnings("unchecked")
		public void handleMessage(Message msg) 
        {  
			Map<String, Object> m = (Map<String, Object>) msg.obj;
			
			List<BusStation> busStations = (List<BusStation>) m.get("stationList");

			if(busStations == null)
			{
        		Toast.makeText(parent, "网络不给力哦亲~", Toast.LENGTH_LONG).show();
        		parent.hideProcess();
        		return;
        	}
			else if(busStations.isEmpty())
			{
        		Toast.makeText(parent, "未查询到本路车", Toast.LENGTH_LONG).show();
        		parent.hideProcess();
        		return;
        	}
			((ScrollView)root.findViewById(R.id.scrollView)).smoothScrollTo(0, 0);
    		busLineView.setStations(busStations);
    		parent.hideProcess();
        }
	};

	@Override
	public void onHintItemClicked(String lineId)
	{
		queryBus(lineId);
		hideHints();
	}

	@Override
	public void onPositiveButtonClicked(Object obj)
	{
		BusStation station = (BusStation) obj;
		OneLineStation s = new OneLineStation();
		s.setStationId(station.getStationId());
		s.setStationName(station.getStationName());
		s.setLineId(station.getLineId());
		s.setLineName(Util.getInstance(parent.getApplicationContext()).getLineNameById(station.getLineId()));
		s.setSegmentId(station.getSegmentId());
		s.setDirection(station.getDirection());
		s.setTime("" + System.currentTimeMillis() );
		
		Util util = Util.getInstance(parent.getApplicationContext());
		if(!util.isMultiLineStationExists(s))
		{
			boolean saveResult = util.saveMultiLineStation(s);
			if(saveResult)
			{
				Toast.makeText(parent, "添加成功", Toast.LENGTH_SHORT).show();
				((MultiLineStationActivity)parent).refreshStations();
			}
			else
			{
				Toast.makeText(parent, "添加失败", Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			Toast.makeText(parent, "已经添加过了", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onNegativeButtonClicked(Object obj)
	{
		
	}
}
