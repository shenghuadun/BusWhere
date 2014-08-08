package com.greenidea.buswhere.bean;

import javax.xml.transform.Templates;

import com.gigi.buslocation.bean.BusStation;

public class FavStationBean
{
	private String lineId;
	private String stationName;
	private String stationId;
	private String direction;
	
	public FavStationBean(BusStation station)
	{
		lineId = station.getLineId();
		stationName = station.getStationName();
		stationId = station.getStationId();
		direction = station.getDirection();
	}

	public FavStationBean()
	{
	}

	public String toString()
	{
		return 	this.getLineId()+ "@_@" + 
				this.getStationName() + "@_@" + 
				this.getStationId() + "@_@" + 
				this.getDirection();
	}
	
	public static FavStationBean fromString(String string)
	{
		FavStationBean bean = new FavStationBean();
		
		String[] tmp = string.split("@_@");
		
		bean.lineId = tmp[0];
		bean.stationName = tmp[1];
		bean.stationId = tmp[2];
		bean.direction = tmp[3];
		
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

	
}
