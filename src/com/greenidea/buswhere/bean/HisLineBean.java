package com.greenidea.buswhere.bean;

public class HisLineBean
{
	private String lineId;
	private String lineName;
	private String group;
	private String time;
	
	public static final String LINEID = "lineId";
	public static final String LINENAME = "lineName";
	public static final String GROUP = "groupName";
	public static final String TIME = "time";
	
	
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
	public String getGroup()
	{
		return group;
	}
	public void setGroup(String group)
	{
		this.group = group;
	}

	public String getTime()
	{
		return time;
	}
	public void setTime(String time)
	{
		this.time = time;
	}
	
	public static HisLineBean from(String string)
	{
		String[] temp = string.split("@");
		
		HisLineBean bean = new HisLineBean();
		bean.setLineId(temp[0]);
		bean.setLineName(temp[1]);
		bean.setGroup(temp[2]);
		bean.setTime(temp[3]);
		
		return bean;
	}
	@Override
	public String toString()
	{
		return lineId + "@" + lineName + "@" + group + "@" + time;
	}
	
}
