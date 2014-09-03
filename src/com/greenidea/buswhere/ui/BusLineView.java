package com.greenidea.buswhere.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.gigi.buslocation.bean.BusPosition;
import com.gigi.buslocation.bean.BusStation;
import com.greenidea.buswhere.R;


public class BusLineView extends View implements OnTouchListener
{
	// ***********************************************************************//
	//
	// 静态常量区
	//
	// ***********************************************************************//
	/**
	 * 界面中每行车站数
	 */
	private static int STATIONS_PER_LINE = 4;

	/**
	 * 车站高度
	 */
	public static final int STATION_HEIGHT = 80;
	/**
	 * 车站宽度
	 */
	private static int STATION_WIDTH = 90;

	/**
	 * 线路宽度
	 */
	private static final int LINE_HEIGHT = 8;
	/**
	 * 线与边界的距离
	 */
	private static final int LINE_MARGIN_BOTTOM = 40;
	/**
	 * 小人与下边界的距离
	 */
	private static final int MAN_MARGIN_BOTTOM = 5;
	/**
	 * 待经过线路宽度
	 */
	private static final int STROKE_Unpassed_HEIGHT = 4;

	private static final int BLOCK_PADDING = 2;

	/**
	 * 路线拐角半径
	 */
	private static final int CORNER_RADIUS = 5;

	/**
	 * 箭头间距
	 */
	private static final int SPACE = 20;

	/**
	 * 下行背景色
	 */
	private static final int COLOR_BLOCK_DOWN = Color.parseColor("#c5eaf8");
	/**
	 * 上行背景色
	 */
	private static final int COLOR_BLOCK_UP = Color.parseColor("#e2f0b6");
	/**
	 * 选中颜色
	 */
	private static final int COLOR_BLOCK_SELECTED = Color.parseColor("#ffc641");
	/**
	 * 线路颜色
	 */
	private static final int COLOR_LINE = Color.parseColor("#52AA1C");
	/**
	 * 点路线颜色
	 */
	private static final int COLOR_LINE_DASH = Color.parseColor("#291EEF");

	/**
	 * 待经过线路颜色
	 */
	private static final int COLOR_LINE_UNPASSED = Color.WHITE;
	/**
	 * 站名颜色
	 */
	private static final int COLOR_STATION_NAME = Color.parseColor("#157392");
	/**
	 * 时间颜色
	 */
	private static final int COLOR_STATION_TIME = Color.parseColor("#669900");

	/**
	 * 车站圆圈半径
	 */
	private static final int STATION_RADIUS = 5;
	private static final int STATION_RADIUS_UNPASSED = 5;

	// ***********************************************************************//
	//
	// 成员变量区
	//
	// ***********************************************************************//

	/**
	 * 线路画笔
	 */
	private Paint paintLine;

	private PathEffect pathLineEffect;
	/**
	 * 待经过线路画笔
	 */
	private Paint paintLineUnpassed;

	/**
	 * 站名画笔
	 */
	private Paint paintStationName;
	private Paint paintStationTime;

	private Paint paintLineUnpassedDynamic;
	private Path arrowPath;

	private Path pathLine;
	private Path pathLineUnpassed;

	private int phase;

	/**
	 * 路线中的站点
	 */
	private List<BusStation> stations = new ArrayList<BusStation>();

	/**
	 * 界面元素
	 */
	private List<BusStationViewItem> stationViewItems = new ArrayList<BusLineView.BusStationViewItem>();

	/**
	 * 处理站点点击事件的handler
	 */
	private Handler stationClickHandler = null;

	private Bitmap bitMapVehicleLeft;
	private Bitmap bitMapVehicleRight;
	private Bitmap bitMapManLeft;
	private Bitmap bitMapManRight;
	
	private Bitmap back_up;
	private Bitmap back_down;
	
	/**
	 * 按下、抬起点
	 */
	private Point downPoint = new Point(-1, -1);
	private Point upPoint = new Point(-1, -1);
	
	private boolean measured = false;
	private int pendingClickStationIndex = -1;
	
//	private OnStationLongClickListener onStationLongClickListener;
	
	public void setStationClickHandler(Handler stationClickHandler)
	{
		this.stationClickHandler = stationClickHandler;
	}
//	
//	public void setOnStationLongClickListener(OnStationLongClickListener onStationLongClickListener)
//	{
//		this.onStationLongClickListener = onStationLongClickListener;
//	}


	public void init(Context context)
	{
		bitMapVehicleLeft = BitmapFactory.decodeResource(getResources(), R.drawable.bus_left);
		bitMapVehicleRight = BitmapFactory.decodeResource(getResources(), R.drawable.bus_right);
		bitMapManLeft = BitmapFactory.decodeResource(getResources(), R.drawable.man_left);
		bitMapManRight = BitmapFactory.decodeResource(getResources(), R.drawable.man_right);

		back_up = BitmapFactory.decodeResource(getResources(), R.drawable.back_up);
		back_down = BitmapFactory.decodeResource(getResources(), R.drawable.back_down);
		
		paintLine = new Paint();
		paintLine.setStyle(Paint.Style.STROKE);
		paintLine.setStrokeWidth(d2p(LINE_HEIGHT));
		paintLine.setAntiAlias(true);
		paintLine.setColor(COLOR_LINE);

		paintLineUnpassed = new Paint();
		paintLineUnpassed.setStyle(Paint.Style.STROKE);
		paintLineUnpassed.setStrokeWidth(d2p(STROKE_Unpassed_HEIGHT));
		paintLineUnpassed.setAntiAlias(true);
		paintLineUnpassed.setColor(COLOR_LINE_UNPASSED);

		paintLineUnpassedDynamic = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintLineUnpassedDynamic.setStyle(Paint.Style.STROKE);
		paintLineUnpassedDynamic.setStrokeWidth(d2p(STROKE_Unpassed_HEIGHT));
		paintLineUnpassedDynamic.setColor(COLOR_LINE_DASH);

		pathLineEffect = new CornerPathEffect(CORNER_RADIUS);

		paintStationName = new Paint();
		paintStationName.setAntiAlias(true);
		paintStationName.setTextSize(d2p(14));
		paintStationName.setColor(COLOR_STATION_NAME);
		
		paintStationTime = new Paint();
		paintStationTime.setAntiAlias(true);
		paintStationTime.setTextSize(d2p(12));
		paintStationTime.setColor(COLOR_STATION_TIME);

		// 线路路径
		pathLine = new Path();
		pathLineUnpassed = new Path();

		// 箭头路径
		arrowPath = new Path();
		arrowPath.moveTo(d2p(3), 0);
		arrowPath.lineTo(0, d2p(-3));
		arrowPath.lineTo(d2p(6), d2p(-3));
		arrowPath.lineTo(d2p(9), 0);
		arrowPath.lineTo(d2p(6), d2p(3));
		arrowPath.lineTo(0, d2p(3));

		this.setLongClickable(true);
		this.setOnTouchListener(this);
//		this.setOnLongClickListener(this);
	}

	public BusLineView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public BusLineView(Context context)
	{
		super(context);
		init(context);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (MotionEvent.ACTION_UP == event.getAction())
		{
			upPoint.x = (int) event.getX() - (v.getWidth() - 2 * d2p(BLOCK_PADDING) - STATIONS_PER_LINE * d2p(STATION_WIDTH)) / 2;
			upPoint.y = (int) event.getY() - 10; 

			// 找到点击的站点
			BusStation station = null;
			BusStationViewItem selectedItem = null;
			for (int i = 0; i < stationViewItems.size(); i++)
			{
				BusStationViewItem item = stationViewItems.get(i);

				//按下、抬起是同一个视图
				if (item.rect.contains(upPoint.x , upPoint.y ) && item.rect.contains(downPoint.x , downPoint.y ) )
				{
					selectedItem = item;
					station = stations.get(i);
					break;
				}
			}
			if(null != station)
			{
				clickStation(station, selectedItem);
				invalidate();
			}
			
			//清掉按下状态
			downPoint.x = -1;
			downPoint.y = -1;
			
			return true;
		}
		else if(MotionEvent.ACTION_DOWN == event.getAction())
		{
			downPoint.x = (int) event.getX() - (v.getWidth() - 2 * d2p(BLOCK_PADDING) - STATIONS_PER_LINE * d2p(STATION_WIDTH)) / 2;
			downPoint.y = (int) event.getY() - 10;
			
		}
		
		return false;
	}

	private void clickStation(BusStation station, BusStationViewItem viewItem)
	{
		resetStations();
		viewItem.isSelected = true;
		
		if(null != station && null != stationClickHandler)
		{
			Message msg = stationClickHandler.obtainMessage();
			msg.obj = station;
			stationClickHandler.sendMessageDelayed(msg, 300);
		}
	}

	public void clickStation(int index)
	{
		if(measured)
		{
			clickStation(this.stationViewItems.get(index).station, this.stationViewItems.get(index));
		}
		else
		{
			pendingClickStationIndex = index;
		}
	}
	
//	@Override
//	public boolean onLongClick(View v)
//	{
//		for (int i = 0; i < stationViewItems.size(); i++)
//		{
//			BusStationViewItem item = stationViewItems.get(i);
//
//			if (item.rect.contains(downPoint.x, downPoint.y) && null != onStationLongClickListener)
//			{
//				onStationLongClickListener.onStationLongClick(item.station, v, downPoint);
//			}
//		}
//		downPoint.x = -1;
//		downPoint.y = -1;
//		return true;
//	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		canvas.save();

		canvas.translate((canvas.getWidth() - 2 * d2p(BLOCK_PADDING) - STATIONS_PER_LINE * d2p(STATION_WIDTH)) / 2, 10);

		boolean showDynamicPath = false;
		
		if (stationViewItems.size() > 0)
		{
			// 1.车站信息
			for (BusStationViewItem item : stationViewItems)
			{
				canvas.save();

				Rect rect = item.rect;
				canvas.translate(rect.left, rect.top);

				// 画背景
				int color = 0;
				Bitmap back;
				if ("1".equals(item.station.getDirection()))
				{
					back = back_down;
					color = COLOR_BLOCK_DOWN;
				}
				else
				{
					back = back_up;
					color = COLOR_BLOCK_UP;
				}

				// 增加背景间距
				canvas.clipRect(d2p(BLOCK_PADDING), d2p(BLOCK_PADDING), rect.right - rect.left - d2p(BLOCK_PADDING), rect.bottom - rect.top
						- d2p(BLOCK_PADDING));
				
				//按下效果
				if(item.isSelected)
				{
					canvas.drawColor(COLOR_BLOCK_SELECTED);
				}
				else
				{
//					canvas.drawBitmap(back, null, backRect, null);
					canvas.drawColor(color);
				}
				

				canvas.drawText(item.station.getStationName(), d2p(3), d2p(30), paintStationName);
				if(!item.positionInfoList.isEmpty())
				{
					for(int i=0; i<item.positionInfoList.size(); i++)
					{
						BusPosition pos = item.positionInfoList.get(i);
						canvas.drawText(pos.getWhen(), d2p(STATION_WIDTH + 10) / 2,
								stationViewItems.get(i).rect.bottom	- d2p(STATION_HEIGHT/2 -25 - i*12),
								paintStationTime);
					}
				}

				canvas.drawCircle(d2p(STATION_WIDTH) / 2, d2p(STATION_HEIGHT - LINE_MARGIN_BOTTOM), d2p(STATION_RADIUS), paintLine);
				canvas.restore();
			}

			// 2.线路
			drawLinePath(canvas, paintLine);

			// 3.待经过站点
//					paintLineUnpassed.setStyle(Paint.Style.FILL);
//			for (int i = 0; i < stationViewItems.size(); i++)
//			{
//				if (stationViewItems.get(i).isWattingForPassing)
//				{
//					Rect rect = stationViewItems.get(i).rect;
//					canvas.drawCircle((rect.left + rect.right) / 2, rect.bottom - d2p(LINE_MARGIN_BOTTOM), d2p(STATION_RADIUS_Unpassed), paintLineUnpassed);
//					showDynamicPath = true;
//				}
//			}
//					paintLineUnpassed.setStyle(Paint.Style.STROKE);
			paintLineUnpassed.setStyle(Paint.Style.FILL);
			for (int i = 0; i < stationViewItems.size(); i++)
			{
				Rect rect = stationViewItems.get(i).rect;
				canvas.drawCircle((rect.left + rect.right) / 2, 
						rect.bottom - d2p(LINE_MARGIN_BOTTOM), 
						d2p(STATION_RADIUS_UNPASSED), 
						paintLineUnpassed);
			}
			paintLineUnpassed.setStyle(Paint.Style.STROKE);
			
			// 4.待经过路线
			getUnpassedLinePath();
			canvas.drawPath(pathLineUnpassed, paintLineUnpassed);
			paintLineUnpassedDynamic.setPathEffect(new PathDashPathEffect(arrowPath, d2p(10), phase, PathDashPathEffect.Style.MORPH));
			canvas.drawPath(pathLineUnpassed, paintLineUnpassedDynamic);

			//phase-=1;

			// 5.车辆当前位置和目标站点
			for (int i = 0; i < stationViewItems.size(); i++)
			{
				// 车
				if (stationViewItems.get(i).isArrivingStation)
				{
					if (isItemInLTRRow(i))
					{
						canvas.drawBitmap(bitMapVehicleLeft, stationViewItems.get(i).rect.left + d2p(STATION_WIDTH) / 2 - bitMapVehicleLeft.getWidth()/2, stationViewItems.get(i).rect.bottom
								- d2p(LINE_MARGIN_BOTTOM) - bitMapVehicleLeft.getHeight() / 2, null);
					}
					else
					{
						canvas.drawBitmap(bitMapVehicleRight, stationViewItems.get(i).rect.left + d2p(STATION_WIDTH) / 2 - bitMapVehicleRight.getWidth()/2,
								stationViewItems.get(i).rect.bottom - d2p(LINE_MARGIN_BOTTOM) - bitMapVehicleLeft.getHeight() / 2, null);
					}
				}
				// 小人
				else if (stationViewItems.get(i).isTargetStation)
				{
					if (isItemInLTRRow(i))
					{
						canvas.drawBitmap(bitMapManRight, stationViewItems.get(i).rect.left + d2p(STATION_WIDTH) / 2 + d2p(STATION_RADIUS), 
								stationViewItems.get(i).rect.bottom	- d2p(MAN_MARGIN_BOTTOM) - bitMapManRight.getHeight(), null);
					}
					else
					{
						canvas.drawBitmap(bitMapManLeft, stationViewItems.get(i).rect.left + d2p(STATION_WIDTH) / 2 - bitMapManLeft.getWidth() - d2p(STATION_RADIUS),
								stationViewItems.get(i).rect.bottom - d2p(MAN_MARGIN_BOTTOM) - bitMapManLeft.getHeight(), null);
					}
				}
			}

		}

		canvas.restore();

		if(showDynamicPath)
		{
			postDelayed(new Runnable()
			{
				
				@Override
				public void run()
				{
					invalidate();
				}
			}, 200);
		}
		 Log.d("", "刷新");
	}

	/**
	 * 根据当前已有的站点信息生成线路路径
	 * @param paintLine2 
	 * @param canvas 
	 */
	private void drawLinePath(Canvas canvas, Paint paintLine)
	{
		int rows = (int) Math.ceil(stationViewItems.size() * 1.0 / STATIONS_PER_LINE);
		for (int i = 0; i < rows; i++)
		{
			pathLine.reset();
			
			//第一行，移动到起点
			if(i==0)
			{
				pathLine.moveTo(getRowHead(stationViewItems, i).rect.left - d2p(BLOCK_PADDING), d2p((i + 1) * STATION_HEIGHT - LINE_MARGIN_BOTTOM));
			}
			else
			{
				// 从左到右
				if (0 == i % 2)
				{
					pathLine.moveTo(getRowHead(stationViewItems, i).rect.left - d2p(2*BLOCK_PADDING), d2p(i * STATION_HEIGHT));
					pathLine.lineTo(getRowHead(stationViewItems, i).rect.left - d2p(2*BLOCK_PADDING), d2p((i+1) * STATION_HEIGHT  - LINE_MARGIN_BOTTOM));
				}
				else
				{
					pathLine.moveTo(getRowHead(stationViewItems, i).rect.right + d2p(2*BLOCK_PADDING), d2p(i * STATION_HEIGHT));
					pathLine.lineTo(getRowHead(stationViewItems, i).rect.right + d2p(2*BLOCK_PADDING), d2p((i+1) * STATION_HEIGHT  - LINE_MARGIN_BOTTOM));
				}
			}
			
			// 从左到右
			if (0 == i % 2)
			{
				pathLine.lineTo(getRowTail(stationViewItems, i).rect.right + d2p(2*BLOCK_PADDING), d2p((i + 1) * STATION_HEIGHT - LINE_MARGIN_BOTTOM));
				// 有下一行，向下画线
				if (i < rows - 1)
				{
					pathLine.lineTo(getRowHead(stationViewItems, i + 1).rect.right + d2p(2*BLOCK_PADDING), d2p((i + 1) * STATION_HEIGHT + 2*BLOCK_PADDING));
				}
			}
			else
			{
				pathLine.lineTo(getRowTail(stationViewItems, i).rect.left - d2p(2*BLOCK_PADDING), d2p((i + 1) * STATION_HEIGHT - LINE_MARGIN_BOTTOM));
				// 有下一行，向下画线
				if (i < rows - 1)
				{
					pathLine.lineTo(getRowHead(stationViewItems, i + 1).rect.left - d2p(2*BLOCK_PADDING), d2p((i + 1) * STATION_HEIGHT + 2*BLOCK_PADDING));
				}
			}

			paintLine.setPathEffect(pathLineEffect);
			canvas.drawPath(pathLine, paintLine);
		}
	}

	/**
	 * 生成待经过线路路径
	 */
	private void getUnpassedLinePath()
	{
			pathLineUnpassed.reset();
			
			//多个车，只需要计算最远的路径即可
			boolean findFarestStation = false;
			for (int i = 0; i < stationViewItems.size(); i++)
			{
				BusStationViewItem item = stationViewItems.get(i);
				if (item.isWattingForPassing)
				{
					int row = getRow(i);
					
					if(!item.isTargetStation)
					{
						// 起点
						if (!findFarestStation && item.isArrivingStation)
						{
							findFarestStation = true;
							pathLineUnpassed.moveTo(item.rect.left + d2p(STATION_WIDTH) / 2, item.rect.bottom - d2p(LINE_MARGIN_BOTTOM));
	
						}
						// 起点和中间点
						if (0 == row % 2)
						{
							pathLineUnpassed.lineTo(item.rect.right, item.rect.bottom - d2p(LINE_MARGIN_BOTTOM));
						}
						else
						{
							pathLineUnpassed.lineTo(item.rect.left, item.rect.bottom - d2p(LINE_MARGIN_BOTTOM));
						}
	
						// 行尾，要绘制边界
						if (getRowTail(stationViewItems, row) == item)
						{
							// 从左到右
							if (0 == row % 2)
							{
								pathLineUnpassed.lineTo(item.rect.right + d2p(BLOCK_PADDING * 2), d2p((row + 1) * STATION_HEIGHT - LINE_MARGIN_BOTTOM));
								pathLineUnpassed.lineTo(stationViewItems.get(i + 1).rect.right + d2p(BLOCK_PADDING * 2), d2p((row + 2) * STATION_HEIGHT
										- LINE_MARGIN_BOTTOM));
							}
							else
							{
								pathLineUnpassed.lineTo(item.rect.left - d2p(BLOCK_PADDING * 2), d2p((row + 1) * STATION_HEIGHT - LINE_MARGIN_BOTTOM));
								pathLineUnpassed.lineTo(stationViewItems.get(i + 1).rect.left - d2p(BLOCK_PADDING * 2), d2p((row + 2) * STATION_HEIGHT
										- LINE_MARGIN_BOTTOM));
							}
						}
					}
					// 结束点
					else
					{
						// 从左到右
						if (0 == row % 2)
						{
							pathLineUnpassed.lineTo(item.rect.right - d2p(STATION_WIDTH) / 2 + d2p(CORNER_RADIUS), item.rect.bottom - d2p(LINE_MARGIN_BOTTOM));
						}
						else
						{
							pathLineUnpassed.lineTo(item.rect.right - d2p(STATION_WIDTH) / 2 - d2p(CORNER_RADIUS), item.rect.bottom - d2p(LINE_MARGIN_BOTTOM));
						}
	
						break;
					}
				}
			}
			paintLineUnpassed.setPathEffect(pathLineEffect);
	}

	/**
	 * 获取第row行第一个元素（按照左到右或右到左的真实顺序）
	 * 
	 * @param stationItems
	 * @param row
	 * @return
	 */
	private BusStationViewItem getRowHead(List<BusStationViewItem> stationItems, int row)
	{
		int last = row * STATIONS_PER_LINE;

		last = (last > stationItems.size() - 1) ? stationItems.size() - 1 : last;

		return stationItems.get(last);
	}

	/**
	 * 获取第row行第一个元素（按照左到右或右到左的真实顺序）
	 * 
	 * @param stationItems
	 * @param row
	 * @return
	 */
	private BusStationViewItem getRowTail(List<BusStationViewItem> stationItems, int row)
	{
		int last = (row + 1) * STATIONS_PER_LINE - 1;

		last = (last > stationItems.size() - 1) ? stationItems.size() - 1 : last;

		return stationItems.get(last);
	}

	/**
	 * 添加一个站点
	 * 
	 * @param station
	 */
	public void addStation(BusStation station)
	{
		stations.add(station);

		int index = stationViewItems.size();
		BusStationViewItem item = new BusStationViewItem(station, index);
		stationViewItems.add(item);

		Rect rect = new Rect();

		int row = getRow(index);

		rect.top = row * d2p(STATION_HEIGHT);
		rect.bottom = rect.top + d2p(STATION_HEIGHT);

		int indexInRow = getIndexInRow(index);

		if (isItemInLTRRow(index))
		{
			rect.left = indexInRow * d2p(STATION_WIDTH);
		}
		else
		{
			rect.left = (STATIONS_PER_LINE - indexInRow - 1) * d2p(STATION_WIDTH);
		}
		rect.right = rect.left + d2p(STATION_WIDTH);

		item.rect = rect;
		
		this.setVisibility(GONE);
		this.setVisibility(VISIBLE);
	}

	private void onMeasureFinished()
	{
		measured = true;
		
		//measure前就设置了站点，需要重绘
		if(!stations.isEmpty())
		{
			List<BusStation> tmp = new ArrayList<BusStation>();
			tmp.addAll(stations);
			stations.clear();
			setStations(tmp);
		}
		
		if(-1 != pendingClickStationIndex)
		{
			clickStation(pendingClickStationIndex);
			pendingClickStationIndex = -1;
		}
	}
	
	public void setStations(List<BusStation> busStations )
	{
		if(measured)
		{
			stationViewItems.clear();
			stations.clear();
			
			for(BusStation station : busStations)
			{
				addStation(station);
			}
		}
		//not measured, 暂存到stations， measure后会再次调用setStations
		else
		{
			stations = busStations;
		}

	}
	
	public List<BusStation> getStations()
	{
		return stations;
	}

	/**
	 * 查询公交当前位置后，设置界面显示
	 * 
	 * @param positions
	 * @param stationClicked
	 */
	public void setBusPositions(List<BusPosition> positions, BusStation stationClicked)
	{
		if (null != positions)
		{
			// 找到当前点击的站点索引
			int stationClickedIndex = -1;
			for (int i = 0; i < stations.size(); i++)
			{
				if (stations.get(i) == stationClicked)
				{
					stationClickedIndex = i;
				}
			}

			if (-1 != stationClickedIndex)
			{
				resetStations();
				BusStationViewItem stationViewClicked = stationViewItems.get(stationClickedIndex);
				stationViewClicked.isWattingForPassing = true;
				stationViewClicked.isTargetStation = true;
				stationViewClicked.isSelected = true;

				for (BusPosition position : positions)
				{
					int stationNum = Integer.valueOf(position.getStationNum());
					// 公交当前位置
					BusStationViewItem item = stationViewItems.get(stationClickedIndex - stationNum);

					// 显示公交车图片和背景
					item.isWattingForPassing = true;
					item.isArrivingStation = true;
					
					// 显示车辆到达时间
					item.positionInfoList.add(position);

					//中间的站点设置为待经过
					for(int i=1; i<stationNum; i++)
					{
						stationViewItems.get(stationClickedIndex - i).isWattingForPassing = true;
					}
					
				}
				
				postInvalidate();
			}
		}
	}

	/**
	 * 将每一站的背景还原
	 */
	public void resetStations()
	{
		for (int i = 0; i < stationViewItems.size(); i++)
		{
			BusStationViewItem temp = stationViewItems.get(i);

			temp.isWattingForPassing = false;
			temp.isArrivingStation = false;
			temp.isTargetStation = false;
			temp.isSelected = false;
			
			temp.positionInfoList.clear();
		}
	}

	/**
	 * 获取station对应的显示项
	 * 
	 * @param station
	 * @return
	 */
	public BusStationViewItem getBusStationViewItem(BusStation station)
	{
		for (BusStationViewItem item : stationViewItems)
		{
			if (item.station == station)
			{
				return item;
			}
		}
		return null;
	}

	/**
	 * 判断第index个元素所在行是否是从左往右排列
	 * 
	 * @param index
	 * @return
	 */
	private boolean isItemInLTRRow(int index)
	{
		return isLTRRow(getRow(index));
	}

	/**
	 * 判断第row行是否是从左往右排列
	 * 
	 * @param row
	 * @return
	 */
	private boolean isLTRRow(int row)
	{
		return row % 2 == 0;
	}

	/**
	 * 计算index个元素在第几行，以0为起始
	 * 
	 * @param index
	 * @return
	 */
	public int getRow(int index)
	{
		return (int) Math.floor((index + 0.0) / STATIONS_PER_LINE);
	}

	/**
	 * 在当前行的索引，从0开始
	 * 
	 * @param index
	 * @return
	 */
	public int getIndexInRow(int index)
	{
		return index % STATIONS_PER_LINE;
	}

	public class BusStationViewItem
	{
		/**
		 * 车辆是否需要通过
		 */
		public boolean isWattingForPassing;

		/**
		 * 待到达的站点
		 */
		public boolean isTargetStation;

		/**
		 * 当前已到达的站点
		 */
		public boolean isArrivingStation;
		
		/**
		 * 选中的站点
		 */
		private boolean isSelected;

		public BusStation station;
		
		public List<BusPosition> positionInfoList = new ArrayList<BusPosition>();

		/**
		 * 当前站点在列表中的索引
		 */
		public int index;

		private Rect rect;
		
		/**
		 * @param station
		 * @param rect
		 */
		public BusStationViewItem(BusStation station, int index)
		{
			this.station = station;
			this.index = index;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int measuredHeight = measureHeight(heightMeasureSpec);

		int measuredWidth = measureWidth(widthMeasureSpec);

		setMeasuredDimension(measuredWidth, measuredHeight);
		
		onMeasureFinished();
	}

	private int measureHeight(int measureSpec)
	{
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		// Default size if no limits are specified.
		int result = (getRow(stationViewItems.size() - 1) + 1) * d2p(STATION_HEIGHT);
		if (specMode == MeasureSpec.AT_MOST)
		{
			// Calculate the ideal size of your
			// control within this maximum size.
			// If your control fills the available
			// space return the outer bound.

			result = specSize;
		}
		else if (specMode == MeasureSpec.EXACTLY)
		{
			// If your control can fit within these bounds return that value.
			result = specSize;
		}

		return result;
	}

	private int measureWidth(int measureSpec)
	{
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		// Default size if no limits are specified.
		int result = 0;
		if (specMode == MeasureSpec.AT_MOST)
		{
			// Calculate the ideal size of your control
			// within this maximum size.
			// If your control fills the available space
			// return the outer bound.
			result = specSize;
			
			if(!measured)
			{
				 int padding = d2p(BLOCK_PADDING);
				//重新计算每行个数
				STATIONS_PER_LINE = (result - padding) / STATION_WIDTH;
				//微调宽度
				int space = (result - padding) % STATION_WIDTH;
				
				//剩余空间较大，每行多添一个
				if(space > STATION_WIDTH/STATIONS_PER_LINE)
				{
					STATIONS_PER_LINE++;
					
					STATION_WIDTH -= space / STATIONS_PER_LINE;
				}
				//微调
				else
				{
					STATION_WIDTH += space/STATIONS_PER_LINE;
				}
				Log.d("修改宽度", STATIONS_PER_LINE + "--" + space + "--" + STATION_WIDTH);
			}
		}

		else if (specMode == MeasureSpec.EXACTLY)
		{
			// If your control can fit within these bounds return that value.
			result = specSize;

			//应该将dp转为px，但是要转换的太多，不如将宽度转为dp
			int tmpWidth = p2d(result);
			Log.d("tmpWidth", tmpWidth + "--");
			if(!measured)
			{
				 int padding = (4+STATIONS_PER_LINE)*BLOCK_PADDING;
				//重新计算每行个数
				STATIONS_PER_LINE = (tmpWidth - padding) / STATION_WIDTH;
				Log.d("每行", STATIONS_PER_LINE + "--" + STATION_WIDTH);
				//微调宽度
				int space = (tmpWidth - padding) % STATION_WIDTH;
				Log.d("剩余", space + "--");
				
				//剩余空间较大，每行多添一个
				if(space > STATION_WIDTH/STATIONS_PER_LINE)
				{
					STATIONS_PER_LINE++;

					STATION_WIDTH = (tmpWidth - padding) / STATIONS_PER_LINE;
				}
				else
				{
					STATION_WIDTH += space/STATIONS_PER_LINE;
				}
				Log.d("修改宽度2", STATIONS_PER_LINE + "--" + space + "--" + STATION_WIDTH);
			}
		}
		else
		{
			result = STATIONS_PER_LINE * d2p(STATION_WIDTH) + 2*d2p(BLOCK_PADDING);
		}

		return result;
	}
	
	private int d2p(float dipValue)
	{
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
	private int p2d(float pxValue)
	{
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}


	public int getPendingClickStationIndex()
	{
		return pendingClickStationIndex;
	}


	public void setPendingClickStationIndex(int pendingClickStationIndex)
	{
		this.pendingClickStationIndex = pendingClickStationIndex;
	}
}
