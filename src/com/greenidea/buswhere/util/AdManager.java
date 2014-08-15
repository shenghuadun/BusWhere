package com.greenidea.buswhere.util;

public class AdManager
{
	private boolean isShowAd;
	
	/**
	 * 是否要显示广告
	 * @return
	 */
	public boolean isShowAd()
	{
		return isShowAd;
	}
	
	/**
	 * 用户手动关闭广告
	 */
	public void onUserCloseAd()
	{
		isShowAd = false;
	}
	
	/**
	 * 软件退出，重置数据
	 */
	public void onExit()
	{
		isShowAd = true;
	}
}
