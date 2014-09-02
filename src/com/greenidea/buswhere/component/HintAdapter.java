package com.greenidea.buswhere.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
import com.greenidea.buswhere.interfaces.OnHintClickListener;
import com.greenidea.buswhere.util.Util;

public class HintAdapter extends BaseAdapter
{
	private OnHintClickListener listener;
    private LayoutInflater mInflater;
    private Bitmap gj;
    private Bitmap jy;
    private List<BusLine> hintList = new ArrayList<BusLine>();
    

	/**
	 * 所有线路信息
	 */
	private List<BusLine> allBusLines = null;

    public HintAdapter(OnHintClickListener listener, Context context) 
    {
    	this.listener = listener;
        mInflater = LayoutInflater.from(context);

        gj = BitmapFactory.decodeResource(context.getResources(), R.drawable.gj);
        jy = BitmapFactory.decodeResource(context.getResources(), R.drawable.jy);
        
        allBusLines = Util.getInstance(context).queryLines("%");
    }
    
    /**
     * 根据key重新查询线路
     * @param key
     */
    public void refreshLines(String key)
    {
    	hintList.clear();
    	
    	int size = allBusLines.size();
    	for(int i=0; i<size; i++)
    	{
    		if(allBusLines.get(i).getSearchKey().startsWith(key))
    		{
    			hintList.add(allBusLines.get(i));
    		}
    	}
    }

    public int getCount() 
    {
        return hintList.size();
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
					listener.onHintItemClicked(((ViewHolder)v.getTag()).lineId);
				}
			});
        } 
        else 
        {
            holder = (ViewHolder) convertView.getTag();
        }
        BusLine line = hintList.get(position);
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
