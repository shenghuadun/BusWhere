package com.greenidea.buswhere.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gigi.buslocation.bean.BusLine;
import com.gigi.buslocation.bean.BusStation;

public class BusDBHelper extends SQLiteOpenHelper
{
	//线路表
	public static final String LINE_TABLE_CREATE ="CREATE TABLE " + 
    		Constants.TABLENAME_LINEINFO +
    		" (" +
    		BusLine.LINEID + " TEXT not null, " +
    		BusLine.LINENAME + " TEXT not null, " +
    		BusLine.MAINSTATIONSDESC + " TEXT, " +
    		BusLine.SEARCHKEY + " TEXT, " +
    		BusLine.PRICE + " TEXT, " +
    		BusLine.TOTALLENGTH + " TEXT, " +
    		BusLine.DOWNAVILABLETIME + " TEXT, " +
    		BusLine.DOWNDESC + " TEXT, " +
    		BusLine.UPAVILABLETIME + " TEXT, " +
    		BusLine.UPDESC + " TEXT" +
    		");";
	
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
    

	public BusDBHelper(Context context)
	{
		super(context, "BUSDB", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		Log.d("", "创建数据库");
//		db.execSQL(LINE_TABLE_CREATE);
		db.execSQL(STATION_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{

	}
	

    private static void copyDatabaseFile(Context context, boolean isfored) 
    {  
        File dir = new File("/data/data/com.greenidea.buswhere/database");  
        if (!dir.exists() || isfored) 
        {  
            try 
            {  
                dir.mkdir();  
            } 
            catch (Exception e) 
            {  
                e.printStackTrace();  
            }  
        }  
          
        File dest = new File(dir, "busdb.db");  
        if(dest.exists() && !isfored)
        {  
            return ;  
        }  
          
        try 
        {  
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
