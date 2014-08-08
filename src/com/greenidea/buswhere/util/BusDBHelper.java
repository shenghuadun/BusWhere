package com.greenidea.buswhere.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gigi.buslocation.bean.BusStation;

class BusDBHelper extends SQLiteOpenHelper
{
	//路线表
    private static final String LINE_TABLE_CREATE ="CREATE TABLE " + 
    		Constants.TABLENAME_LINEINFO +
    		" (" +
    		BusStation.STATION_ID + " TEXT not null, " +
    		BusStation.STATION_NAME + " TEXT not null, " +
    		BusStation.SEQ + " TEXT, " +
    		BusStation.LINE_ID + " TEXT not null, " +
    		BusStation.DIRECTION + " TEXT, " +
    		BusStation.SEGMENT_ID + " TEXT, " +
    		BusStation.STATUS_DATE + " TEXT not null" +
    		");";
    

	public BusDBHelper(Context context)
	{
		super(context, "BUSDB", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		Log.d("", "创建数据库");
		db.execSQL(LINE_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{

	}

}
