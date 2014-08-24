package com.greenidea.buswhere.bean;

import com.gigi.buslocation.bean.BusStation;

public class FavStationBean
{
	private String lineId;
	private String stationName;
	private String stationId;
	private String direction;
	private String time;
	

	public static final String LINEID = "lineId";
	public static final String STATIONAME = "stationName";
	public static final String STATIONID = "stationId";
	public static final String DIRECTION = "direction";
	public static final String TIME = "time";
	
	
	public FavStationBean(BusStation station)
	{
		lineId = station.getLineId();
		stationName = station.getStationName();
		stationId = station.getStationId();
		direction = station.getDirection();
		
		time = String.valueOf(System.currentTimeMillis());
	}

	public FavStationBean()
	{
	}

	public String toString()
	{
		return 	this.getLineId()+ "@_@" + 
				this.getStationName() + "@_@" + 
				this.getStationId() + "@_@" + 
				this.getDirection() + "@_@" + this.getTime();
	}
	
	public static FavStationBean fromString(String string)
	{
		FavStationBean bean = new FavStationBean();
		
		String[] tmp = string.split("@_@");
		
		bean.lineId = tmp[0];
		bean.stationName = tmp[1];
		bean.stationId = tmp[2];
		bean.direction = tmp[3];
		bean.time = tmp[4];
		
		return bean;
	}

	public String getLineId()
	{
		return lineId;
	}

	public void setLineId(String lineId)
	{
		this.lineId = lineId;
	}

	public String getStationName()
	{
		return stationName;
	}

	public void setStationName(String stationName)
	{
		this.stationName = stationName;
	}

	public String getStationId()
	{
		return stationId;
	}

	public void setStationId(String stationId)
	{
		this.stationId = stationId;
	}

	public String getDirection()
	{
		return direction;
	}

	public void setDirection(String direction)
	{
		this.direction = direction;
	}

	public String getTime()
	{
		return time;
	}

	public void setTime(String time)
	{
		this.time = time;
	}

	
}
