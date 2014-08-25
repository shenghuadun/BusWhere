package com.greenidea.buswhere.bean;

import java.util.ArrayList;
import java.util.List;

public class MultiLineStation
{
	private String stationName;
	private String stationId;
	private String lineId;
	private String lineName;
	private String direction;
	private String time;
	
	public static final String STATIONNAME = "stationName";
	public static final String STATIONID = "statiostationIdsnName";
	public static final String LINEID = "lineId";
	public static final String LINENAME = "lineName";
	public static final String DIRECTION = "direction";
	public static final String TIME = "time";
	
	
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

	public String getLineId()
	{
		return lineId;
	}

	public void setLineId(String lineId)
	{
		this.lineId = lineId;
	}

	public String getLineName()
	{
		return lineName;
	}

	public void setLineName(String lineName)
	{
		this.lineName = lineName;
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
	
	@Override
	public String toString()
	{
		String result = stationName + "-" + stationId + "-"  + lineId + "-"  + lineName + "-" + direction + "-"  + time;
		
		return result;
	}
	
	
}