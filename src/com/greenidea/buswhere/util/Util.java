package com.greenidea.buswhere.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.ParseException;
import org.htmlparser.util.ParserException;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gigi.buslocation.bean.BusLine;
import com.gigi.buslocation.bean.BusPosition;
import com.gigi.buslocation.bean.BusStation;
import com.gigi.buslocation.util.BusUtil;
import com.greenidea.buswhere.bean.FavStationBean;
import com.greenidea.buswhere.bean.HisLineBean;
import com.greenidea.buswhere.bean.OneLineStation;


public class Util
{
	private static Util instance = new Util();
	
	private Context context;
	private static BusDBHelper helper;
	
	private Util()
	{}
	
	/**
	 * 返回工具实例
	 * @param context
	 * @return
	 */
	public static Util getInstance(Context context)
	{
		if(null == context)
		{
			return null;
		}
		if(null == helper)
		{
			helper = new BusDBHelper(context);
		}
		instance.context = context;
		return instance;
	}
	
	/**
	 * 查询指定路线和方向的公交站信息
	 * @param lineId 路线
	 * @param direction 方向
	 * @return null表示无法联网，空列表表示未查询到此路车信息
	 */
	public List<BusStation> getBusStations(String lineId, String direction)
	{
		List<BusStation> result = new ArrayList<BusStation>();
		
		//最早的有效日期，一个月
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		DateFormat format = SimpleDateFormat.getDateInstance();
		String dateString = format.format(calendar.getTime());
		Log.d("最早的有效日期，一个月",	dateString);
		
		//查询数据库中有没有
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.query(Constants.TABLENAME_STATIONINFO, 
				new String[]
				{
					BusStation.STATION_ID, 
					BusStation.STATION_NAME, 
					BusStation.SEQ, 
					BusStation.LINE_ID, 
					BusStation.DIRECTION, 
					BusStation.SEGMENT_ID, 
					BusStation.STATUS_DATE
				},
//				BusStation.LINE_ID + " = ? and " + BusStation.DIRECTION +" = ?", 
//				new String[]{"\"" + lineId + "\"", "\"" + direction + "\""}, null, null, null);
				BusStation.LINE_ID + "=?", 
				new String[]{lineId}, null, null, null);

		//将所有在有效期内的数据生成站点信息
		Calendar c = GregorianCalendar.getInstance();
		Log.d("", cursor.getCount() + "");
		for (int i = 0; i < cursor.getCount(); i++)
		{
			cursor.moveToNext();
			
			//仅处理本方向的
			if(!direction.equals(cursor.getString(cursor.getColumnIndex(BusStation.DIRECTION))))
			{
				continue;
			}
			
			try
			{
				String statusDate = cursor.getString(cursor.getColumnIndex(BusStation.STATUS_DATE));
				c.setTime(format.parse(statusDate));
			}
			catch (ParseException e)
			{
				Log.e("BusUtil.getBusStations", "数据库中日期格式不正确" + cursor.getString(cursor.getColumnIndex("statusDate")));
				break;
			}
			catch (java.text.ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//数据超出有效期
			if(c.before(calendar))
			{
				Log.d(calendar.toString(), c.toString());
				break;
			}
			
			BusStation busStation = new BusStation();
			
			busStation.setStationId(cursor.getString(cursor.getColumnIndex(BusStation.STATION_ID)));
			busStation.setStationName(cursor.getString(cursor.getColumnIndex(BusStation.STATION_NAME)));
			busStation.setSeq(cursor.getString(cursor.getColumnIndex(BusStation.SEQ)));
			busStation.setLineId(cursor.getString(cursor.getColumnIndex(BusStation.LINE_ID)));
			busStation.setDirection(cursor.getString(cursor.getColumnIndex(BusStation.DIRECTION)));
			busStation.setSegmentId(cursor.getString(cursor.getColumnIndex(BusStation.SEGMENT_ID)));

			result.add(busStation);
		}
		
		//数据库中没有数据或者数据太旧
		if(result.isEmpty())
		{
			Log.e("BusUtil.getBusStations", "数据库中没有数据或者数据太旧");
			try
			{
				result = BusUtil.getInstance().getBusStations(lineId, direction);
				saveLineInfoToDB(result, lineId, direction);
			}
			catch (ParserException e)
			{
				Log.e("BusUtil.getBusStations", "无法联网获取公交线路信息" + e.getMessage());
				return null;
			}
			
		}
		cursor.close();
		db.close();
		helper.close();
		return result;
	}
	
	/**
	 * 查询指定关键字的线路
	 * @param key 关键字
	 * @return 
	 */
	public List<BusLine> queryLines(String key)
	{
		List<BusLine> result = new ArrayList<BusLine>();
		
		//空的时候不显示任何数据
		if(!"".equals(key))
		{
			//查询数据库中有没有
			SQLiteDatabase db = helper.getWritableDatabase();
			Cursor cursor = db.query(Constants.TABLENAME_LINEINFO, 
					null,
					BusLine.SEARCHKEY + " like '" + key + "%'", 
					null, null, null, BusLine.LINENAME + " ASC");
	
			Log.d("", cursor.getCount() + "");
			for (int i = 0; i < cursor.getCount(); i++)
			{
				cursor.moveToNext();
				
				BusLine line = new BusLine();
				
				line.setLineId(cursor.getString(cursor.getColumnIndex(BusLine.LINEID)));
				line.setLineName(cursor.getString(cursor.getColumnIndex(BusLine.LINENAME)));
				line.setMainStationsDesc(cursor.getString(cursor.getColumnIndex(BusLine.MAINSTATIONSDESC)));
				line.setDownAvilableTime(cursor.getString(cursor.getColumnIndex(BusLine.DOWNAVILABLETIME)));
				line.setDownDesc(cursor.getString(cursor.getColumnIndex(BusLine.DOWNDESC)));
				line.setUpAvilableTime(cursor.getString(cursor.getColumnIndex(BusLine.UPAVILABLETIME)));
				line.setUpDesc(cursor.getString(cursor.getColumnIndex(BusLine.UPDESC)));
				line.setGroupName(cursor.getString(cursor.getColumnIndex(BusLine.GROUP_NAME)));
				line.setPrice(cursor.getString(cursor.getColumnIndex(BusLine.PRICE)));
				line.setSearchKey(cursor.getString(cursor.getColumnIndex(BusLine.SEARCHKEY)));
				line.setTotalLength(cursor.getString(cursor.getColumnIndex(BusLine.TOTALLENGTH)));
	
				result.add(line);
			}
			
			cursor.close();
			db.close();
			helper.close();
		}
		return result;
	}

	/**
	 * 将公交线路信息保存
	 * @param busStationList
	 */
	private void saveLineInfoToDB(List<BusStation> busStationList, String lineId, String direction)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		
		db.beginTransaction();
		try 
		{
			String sql = "Delete from " + Constants.TABLENAME_STATIONINFO +
					" where " + BusStation.LINE_ID + "=\"" + lineId + "\" and " + BusStation.DIRECTION + "=\"" + direction + "\";";
			db.delete(Constants.TABLENAME_STATIONINFO, BusStation.LINE_ID + "=? and " + BusStation.DIRECTION + "=?", new String[]{lineId, direction});
			
			for(BusStation station : busStationList)
			{
				sql = toInsertSQLString(station);
				Log.d("",	 sql);
				db.execSQL(sql);
			}
			
			db.setTransactionSuccessful();
		} 
		finally 
		{
			db.endTransaction();
		}
		
		db.close();
		helper.close();
	}


	private String toInsertSQLString(BusStation station) 
	{
		return "INSERT INTO " + Constants.TABLENAME_STATIONINFO + " VALUES(\"" + 
				station.getStationId() + "\", \"" + 
				station.getStationName() + "\", \"" + 
				station.getSeq() + "\", \"" + 
				station.getLineId() + "\", \"" + 
				station.getDirection() + "\", \"" + 
				station.getSegmentId() + "\", \"" + 
				new Date().toLocaleString() + "\")"; 
	}

	/**
	 * 查询公交车的当前位置信息，如果未发车或无车，返回空列表
	 * @param lineId 公交线路
	 * @param stationId 站点ID
	 * @param segmentId 区域ID
	 * @return
	 * @throws ParserException 网络有问题时抛出
	 */
	public List<BusPosition> getBusPosition(String lineId, String stationId, String segmentId) throws ParserException
	{
		return BusUtil.getInstance().getBusPosition(lineId, stationId, segmentId);
	}

	/**
	 * 查询公交车的当前位置信息，如果未发车或无车，返回null
	 * @param station 站点
	 * @return
	 * @throws ParserException 网络有问题时抛出
	 */
	public List<BusPosition> getBusPosition(BusStation station) throws ParserException
	{
		return getBusPosition(station.getLineId(), station.getStationId(), station.getSegmentId());
	}

	/**
	 * 根据站名查询站点信息
	 * @param busStations
	 * @param stationName
	 * @return
	 */
	public static BusStation getStationByName(List<BusStation> busStations, String stationName)
	{
		BusStation station = null;
		if(null != busStations)
		{
			for(BusStation tmp : busStations)
			{
				if(tmp.getStationName().equals(stationName))
				{
					station = tmp;
				}
			}
		}
		return station;
	}

	/**
	 * 判断当前站名是不是在positions中
	 * @param stationName
	 * @param positions
	 * @return
	 */
	public boolean busInPosition(String stationName, List<BusPosition> positions)
	{
		boolean inPosition = false;
		for(BusPosition position : positions)
    	{
			if(position.getStationName().equals(stationName))
			{
				inPosition = true;
				break;
			}
    	}
		return inPosition;
	};  
	

	public static int dip2px(float dipValue, Resources resource)
	{
		final float scale = resource.getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(float pxValue, Resources resource)
	{
		final float scale = resource.getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public BusLine getBusLine(String lineId)
	{
		BusLine result = new BusLine();
	
		//查询数据库中有没有
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.query(Constants.TABLENAME_LINEINFO, 
				null,
				BusLine.LINEID + " = '" + lineId + "'", 
				null, null, null, null);
	
		Log.d("", cursor.getCount() + "");
		
		for (int i = 0; i < cursor.getCount(); i++)
		{
			cursor.moveToNext();
			
			result.setLineId(cursor.getString(cursor.getColumnIndex(BusLine.LINEID)));
			result.setLineName(cursor.getString(cursor.getColumnIndex(BusLine.LINENAME)));
			result.setMainStationsDesc(cursor.getString(cursor.getColumnIndex(BusLine.MAINSTATIONSDESC)));
			result.setDownAvilableTime(cursor.getString(cursor.getColumnIndex(BusLine.DOWNAVILABLETIME)));
			result.setDownDesc(cursor.getString(cursor.getColumnIndex(BusLine.DOWNDESC)));
			result.setUpAvilableTime(cursor.getString(cursor.getColumnIndex(BusLine.UPAVILABLETIME)));
			result.setUpDesc(cursor.getString(cursor.getColumnIndex(BusLine.UPDESC)));
			result.setGroupName(cursor.getString(cursor.getColumnIndex(BusLine.GROUP_NAME)));
			result.setPrice(cursor.getString(cursor.getColumnIndex(BusLine.PRICE)));
			result.setSearchKey(cursor.getString(cursor.getColumnIndex(BusLine.SEARCHKEY)));
			result.setTotalLength(cursor.getString(cursor.getColumnIndex(BusLine.TOTALLENGTH)));
	
			break;
		}
		
		cursor.close();
		db.close();
		helper.close();
		return result;
	}

	/**
	 * 查询历史记录
	 * @param key
	 * @return
	 */
	public List<HisLineBean> queryHis()
	{
		List<HisLineBean> result = new ArrayList<HisLineBean>();
		
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.query(Constants.TABLENAME_HIS, 
				null,
				null, 
				null, null, null, HisLineBean.TIME + " DESC");

		Log.d("历史记录个数", cursor.getCount() + "");
		for (int i = 0; i < cursor.getCount(); i++)
		{
			cursor.moveToNext();
			
			HisLineBean hisBean = new HisLineBean();
			
			hisBean.setLineId(cursor.getString(cursor.getColumnIndex(HisLineBean.LINEID)));
			hisBean.setLineName(cursor.getString(cursor.getColumnIndex(HisLineBean.LINENAME)));
			hisBean.setGroup(cursor.getString(cursor.getColumnIndex(HisLineBean.GROUP)));
			hisBean.setTime(cursor.getString(cursor.getColumnIndex(HisLineBean.TIME)));

			result.add(hisBean);
		}
		
		cursor.close();
		db.close();
		helper.close();
		return result;
	}

	/**
	 * 保存查询历史
	 * @return
	 */
	public boolean saveHis(HisLineBean bean)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		
		//将之前本线路记录删除
		db.delete(Constants.TABLENAME_HIS, HisLineBean.LINEID + "=?", new String[]{bean.getLineId()});
		
		//删除多余的记录
		String sql = "delete from " + Constants.TABLENAME_HIS + " where " + HisLineBean.TIME + " not in( select " 
				+ HisLineBean.TIME + " from " +  Constants.TABLENAME_HIS + " order by " + HisLineBean.TIME 
				+ " limit " + String.valueOf(Constants.MAX_HIS_COUNT) + ");";
		db.execSQL(sql);
		
		//保存本记录
		ContentValues values = new ContentValues();
		values.put(HisLineBean.LINEID, bean.getLineId());
		values.put(HisLineBean.LINENAME, bean.getLineName());
		values.put(HisLineBean.GROUP, bean.getGroup());
		values.put(HisLineBean.TIME, bean.getTime());
		long rowid = db.insert(Constants.TABLENAME_HIS, null, values);
		db.close();
		helper.close();
		return rowid != -1;
	}
	

	
	/**
	 * 查询收藏站点
	 * @return
	 */
	public List<FavStationBean> queryFav()
	{
		List<FavStationBean> result = new ArrayList<FavStationBean>();
		
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.query(Constants.TABLENAME_FAV, 
				null,
				null, 
				null, null, null, HisLineBean.TIME + " DESC");

		Log.d("收藏记录个数", cursor.getCount() + "");
		for (int i = 0; i < cursor.getCount(); i++)
		{
			cursor.moveToNext();
			
			FavStationBean hisBean = new FavStationBean();
			
			hisBean.setLineId(cursor.getString(cursor.getColumnIndex(FavStationBean.LINEID)));
			hisBean.setStationId(cursor.getString(cursor.getColumnIndex(FavStationBean.STATIONID)));
			hisBean.setStationName(cursor.getString(cursor.getColumnIndex(FavStationBean.STATIONAME)));
			hisBean.setDirection(cursor.getString(cursor.getColumnIndex(FavStationBean.DIRECTION)));
			hisBean.setTime(cursor.getString(cursor.getColumnIndex(FavStationBean.TIME)));

			result.add(hisBean);
		}
		
		cursor.close();
		db.close();
		helper.close();
		return result;
	}

	/**
	 * 保存收藏
	 * @return
	 */
	public boolean saveFav(FavStationBean bean)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		
		//保存本记录
		ContentValues values = new ContentValues();
		values.put(FavStationBean.LINEID, bean.getLineId());
		values.put(FavStationBean.STATIONID, bean.getStationId());
		values.put(FavStationBean.STATIONAME, bean.getStationName());
		values.put(FavStationBean.DIRECTION, bean.getDirection());
		values.put(FavStationBean.TIME, bean.getTime());
		long rowid = db.insert(Constants.TABLENAME_FAV, null, values);
		db.close();
		helper.close();
		return rowid != -1;
	}

	/**
	 * 删除收藏
	 * @param bean
	 * @return
	 */
	public boolean deleteFav(FavStationBean bean)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		
		//将之前本线路记录删除
		int rows = db.delete(Constants.TABLENAME_FAV, 
				FavStationBean.LINEID + "=? and " + 
				FavStationBean.STATIONID + "=? and " + 
				FavStationBean.DIRECTION + "=?" , new String[]{bean.getLineId(), bean.getStationId(), bean.getDirection()});
		
		//保存本记录
		db.close();
		helper.close();
		return rows != 0;
	}
	
	public String getLineNameById(String id)
	{
		String lineId = "";
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.query(Constants.TABLENAME_LINEINFO, 
				new String[]{BusLine.LINENAME},
				BusLine.LINEID + " = ? ", 
				new String[]{id}, null, null, null);

		for (int i = 0; i < cursor.getCount(); i++)
		{
			cursor.moveToNext();
			
			lineId = cursor.getString(0);
		}
		
		cursor.close();
		db.close();
		helper.close();
		return lineId;
	}

	/**
	 * 查询多线路站点
	 * @return 以站名为key的map
	 */
	public Map<String, List<OneLineStation>> queryMultiLineStations()
	{
		Map<String, List<OneLineStation>> result = new HashMap<String, List<OneLineStation>>();
		
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.query(Constants.TABLENAME_MULTI_LINE_STATION, 
				null,
				null, 
				null, null, null, OneLineStation.STATIONNAME + " DESC ," + OneLineStation.TIME + " DESC ," + OneLineStation.LINEID + " DESC");

		Log.d("记录个数", cursor.getCount() + "");

		String stationName = null;
		List<OneLineStation> oneStation = new ArrayList<OneLineStation>();
		for (int i = 0; i < cursor.getCount(); i++)
		{
			cursor.moveToNext();
			
			OneLineStation station = new OneLineStation();
			station.setStationName(cursor.getString(cursor.getColumnIndex(OneLineStation.STATIONNAME)));
			station.setStationId(cursor.getString(cursor.getColumnIndex(OneLineStation.STATIONID)));
			station.setLineId(cursor.getString(cursor.getColumnIndex(OneLineStation.LINEID)));
			station.setLineName(cursor.getString(cursor.getColumnIndex(OneLineStation.LINENAME)));
			station.setSegmentId(cursor.getString(cursor.getColumnIndex(OneLineStation.SEGMENTID)));
			station.setDirection(cursor.getString(cursor.getColumnIndex(OneLineStation.DIRECTION)));
			station.setTime(cursor.getString(cursor.getColumnIndex(OneLineStation.TIME)));

			if(stationName == null)
			{ 
				stationName = station.getStationName();
			}
			else if(!stationName.equals(station.getStationName()))
			{
				result.put(stationName, oneStation);
				oneStation = new ArrayList<OneLineStation>();
				stationName = station.getStationName();
			}
			oneStation.add(station);
		}

		if(!oneStation.isEmpty())
		{
			result.put(stationName, oneStation);
		}
		
		cursor.close();
		db.close();
		helper.close();
		return result;
	}
	

	/**
	 * 查询多线路站点
	 * @return 以站名为key的map
	 */
	public boolean saveMultiLineStation(OneLineStation station)
	{
		SQLiteDatabase db = helper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(OneLineStation.STATIONID, station.getStationId());
		values.put(OneLineStation.STATIONNAME, station.getStationName());
		values.put(OneLineStation.LINEID, station.getLineId());
		values.put(OneLineStation.LINENAME, station.getLineName());
		values.put(OneLineStation.DIRECTION, station.getDirection());
		values.put(OneLineStation.SEGMENTID, station.getSegmentId());
		values.put(OneLineStation.TIME, station.getTime());
		long rowid = db.insert(Constants.TABLENAME_MULTI_LINE_STATION, null, values );
		
		db.close();
		helper.close();
		return rowid > 0;
	}
	
	public boolean isMultiLineStationExists(OneLineStation station)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.query(Constants.TABLENAME_MULTI_LINE_STATION, 
				null,
				OneLineStation.LINEID  + "=? and " + OneLineStation.STATIONID + " = ?", 
				new String[]{station.getLineId(), station.getStationId()}, null, null, null);

		return cursor.getCount() > 0;
	}
}
