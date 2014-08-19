package com.greenidea.buswhere.component;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gigi.buslocation.bean.BusLine;
import com.greenidea.buswhere.R;
import com.greenidea.buswhere.activity.MainActivity;
import com.greenidea.buswhere.util.Util;

public class HintAdapter extends BaseAdapter
{
	private MainActivity context;
    private LayoutInflater mInflater;
    private Bitmap gj;
    private Bitmap jy;
    private List<BusLine> lineList = new ArrayList<BusLine>();

    public HintAdapter(MainActivity context) 
    {
    	this.context = context;
        mInflater = LayoutInflater.from(context);

        gj = BitmapFactory.decodeResource(context.getResources(), R.drawable.bus_left);
        jy = BitmapFactory.decodeResource(context.getResources(), R.drawable.bus_right);
    }
    
    /**
     * 根据key重新查询线路
     * @param key
     */
    public void refreshLines(String key)
    {
    	lineList = Util.getInstance(context).queryLines(key);
    }

    public int getCount() 
    {
        return lineList.size();
    }

    public Object getItem(int position) 
    {
        return position;
    }

    public long getItemId(int position) 
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        if (convertView == null) 
        {
            convertView = mInflater.inflate(R.layout.hintitem, null);

            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.lineName = (TextView) convertView.findViewById(R.id.lineName);
            holder.lineDesc = (TextView) convertView.findViewById(R.id.lineDesc);
            holder.price = (TextView) convertView.findViewById(R.id.price);

            convertView.setTag(holder);
            
            convertView.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					context.queryBus(((ViewHolder)v.getTag()).lineId, null, null);	
				}
			});
        } 
        else 
        {
            holder = (ViewHolder) convertView.getTag();
        }
        BusLine line = lineList.get(position);
        holder.icon.setImageBitmap(line.getGroupName().equals("gj") ? gj : jy);
        holder.lineName.setText(line.getLineName());
        holder.lineDesc.setText(line.getDownDesc());
        holder.price.setText(line.getPrice());
        holder.lineId = line.getLineId();
        
        return convertView;
    }

    static class ViewHolder
    {
        ImageView icon;
        TextView lineName;
        TextView lineDesc;
        TextView price;
        
        String lineId;
    }

}
