package com.greenidea.buswhere.bean;

public class HisLineBean
{
	private String id;
	private String name;
	private String group;
	private String time;
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
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
		bean.setId(temp[0]);
		bean.setName(temp[1]);
		bean.setGroup(temp[2]);
		bean.setTime(temp[3]);
		
		return bean;
	}
	@Override
	public String toString()
	{
		return id + "@" + name + "@" + group + "@" + time;
	}
	
}
