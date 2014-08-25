package com.greenidea.buswhere.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gigi.buslocation.bean.BusStation;
import com.greenidea.buswhere.bean.FavStationBean;
import com.greenidea.buswhere.bean.HisLineBean;
import com.greenidea.buswhere.bean.MultiLineStation;

public class BusDBHelper extends SQLiteOpenHelper
{
	//查询历史
	public static final String HIS_TABLE_CREATE ="CREATE TABLE " + 
    		Constants.TABLENAME_HIS +
    		" (" +
    		HisLineBean.LINEID + " TEXT not null, " +
    		HisLineBean.LINENAME + " TEXT not null, " +
    		HisLineBean.GROUP + " TEXT not null, " +
    		HisLineBean.TIME + " TEXT not null" +
    		");";
	//收藏
	public static final String FAV_TABLE_CREATE ="CREATE TABLE " + 
			Constants.TABLENAME_FAV +
			" (" +
			FavStationBean.LINEID + " TEXT not null, " +
			FavStationBean.STATIONID + " TEXT not null, " +
			FavStationBean.STATIONAME + " TEXT not null, " +
			FavStationBean.DIRECTION + " TEXT not null, " +
			FavStationBean.TIME + " TEXT not null" +
			");";


//CREATE TABLE line AS SELECT route_id lineid, route_name lineName, route_id searchKey,
//landmark mainstationsdesc, line1 downDesc, line2 upDesc, worktime downAvilableTime,
//worktime1 upAvilableTime, len totalLength, price price,  group_name groupName FROM qdbus.routes
//where status = 1;
//	
//update MAIN.[line] set searchkey = 'special' where lineName like '隧道%' or lineName like '世园%'  or  lineName like '高新快线%';
	
//	CREATE TABLE line (
//			lineId TEXT not null, 
//			lineName TEXT not null, 
//			mainStationsDesc TEXT, 
//			searchKey TEXT, 
//			price TEXT, 
//			totalLength TEXT, 
//			downAvilableTime TEXT, 
//			downDesc TEXT, 
//			upAvilableTime TEXT, 
//			upDesc TEXT);
	
	//站点表
    private static final String STATION_TABLE_CREATE ="CREATE TABLE " + 
    		Constants.TABLENAME_STATIONINFO +
    		" (" +
    		BusStation.STATION_ID + " TEXT not null, " +
    		BusStation.STATION_NAME + " TEXT not null, " +
    		BusStation.SEQ + " TEXT, " +
    		BusStation.LINE_ID + " TEXT not null, " +
    		BusStation.DIRECTION + " TEXT, " +
    		BusStation.SEGMENT_ID + " TEXT, " +
    		BusStation.STATUS_DATE + " TEXT not null" +
    		");";

	//多路线站点表
    private static final String MULTI_LINE_STATION_TABLE_CREATE ="CREATE TABLE " + 
    		Constants.TABLENAME_MULTI_LINE_STATION +
    		" (" +
    		MultiLineStation.STATIONNAME + " TEXT not null, " +
    		MultiLineStation.STATIONID + " TEXT not null, " +
    		MultiLineStation.LINEID + " TEXT, " +
    		MultiLineStation.LINENAME + " TEXT not null, " +
    		MultiLineStation.TIME + " TEXT" +
    		");";

	public BusDBHelper(Context context)
	{
		super(context, "BUSDB", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		Log.d("", "创建数据库");
		db.execSQL(HIS_TABLE_CREATE);
		db.execSQL(FAV_TABLE_CREATE);
		db.execSQL(STATION_TABLE_CREATE);
		db.execSQL(MULTI_LINE_STATION_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{

	}
	

    public static void copyDatabaseFile(Context context, String dbPath) 
    {  
        File dest = new File(dbPath, "BUSDB"); 
        
        try 
        {  
        	if(!new File(dbPath).exists())
        	{
        		new File(dbPath).mkdir();
        	}
            if(dest.exists())
            {  
                dest.delete();  
            }  

            dest.createNewFile();
           
            InputStream in = context.getResources().getAssets().open("busdb.db");   

            FileOutputStream out = new FileOutputStream(dest);  
            
            byte[] buffer = new byte[7168];   
            
            int count = 0;     
            while ((count = in.read(buffer)) > 0)
            {     
            	out.write(buffer, 0, count);     
            }     
            out.close();     
            in.close();    
                 
        } 
        catch (Exception e)
        {  
            e.printStackTrace();  
        }  
    }  
}
