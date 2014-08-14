package com.greenidea.buswhere.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.ParseException;
import org.htmlparser.util.ParserException;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gigi.buslocation.bean.BusPosition;
import com.gigi.buslocation.bean.BusStation;
import com.gigi.buslocation.util.BusUtil;


public class Util
{
	private static Util instance = new Util();
	
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
		return instance;
	}
	
	/**
	 * 查询指定路线和方向的公交站信息
	 * @param lineId 路线
	 * @param direction 方向
	 * @return 
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
			}
			catch (ParserException e)
			{
				Log.e("BusUtil.getBusStations", "无法联网获取公交线路信息" + e.getMessage());
			}
			
			saveLineInfoToDB(result, lineId, direction);
		}
		cursor.close();
		db.close();
		helper.close();
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


}
