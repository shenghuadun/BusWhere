package com.greenidea.buswhere.bean;

import java.util.ArrayList;
import java.util.List;

public class StationLinesBean
{
	private String stationName;
	private List<String> lineIds;
	private List<String> lineNames;
	
	
	public String getStationName()
	{
		return stationName;
	}
	public void setStationName(String stationName)
	{
		this.stationName = stationName;
	}
	public List<String> getLineIds()
	{
		return lineIds;
	}
	public void setLineIds(List<String> lineIds)
	{
		this.lineIds = lineIds;
	}
	public List<String> getLineNames()
	{
		return lineNames;
	}
	public void setLineNames(List<String> lineNames)
	{
		this.lineNames = lineNames;
	}
	
	public static StationLinesBean from(String value)
	{
		StationLinesBean bean = new StationLinesBean();
		
		List<String> lineIds = new ArrayList<String>();
		List<String> lineNames = new ArrayList<String>();
		
		String[] tmp = value.split("@");
		
		int i=0;
		for(String s : tmp)
		{
			if(0 == i)
			{
				bean.setStationName(s);
			}
			else
			{
				String[] idName = s.split("#");
				lineIds.add(idName[0]);
				lineNames.add(idName[1]);
			}
			i++;
		}
		
		bean.lineIds = lineIds;
		bean.lineNames = lineNames;
		
		return bean;
	}
	
	@Override
	public String toString()
	{
		String result = this.stationName;
		
		int i=0;
		for(String id : lineIds)
		{
			result += "@" + id + "#" + lineNames.get(i);
			i++;
		}
		
		return result;
	}
	
	
}
