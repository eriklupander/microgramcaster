package com.squeed.microgramcaster.drawer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squeed.microgramcaster.R;

/**
 * Array Adapter for showing file listings, directory browsing etc.
 * 
 * It's a bit bloated, mostly due to it serving both local, upnp or smb structures.
 * 
 * @author Erik
 *
 */
public class DrawerItemArrayAdapter extends ArrayAdapter<DrawerItem> {

    private Context mContext;
    private int layoutResourceId;
    private  ArrayList<DrawerItem> data = null;

    public DrawerItemArrayAdapter(Context mContext, int layoutResourceId, ArrayList<DrawerItem> data) {

        super(mContext, layoutResourceId, data);

        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	View row = convertView;
    	ViewHolder holder = null;        

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new ViewHolder();
            holder.title = (TextView) row.findViewById(R.id.drawer_item_title);
            holder.description = (TextView) row.findViewById(R.id.drawer_item_desc);
            holder.thumb = (ImageView) row.findViewById(R.id.drawer_item_icon);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        // object item based on the position
        DrawerItem objectItem = data.get(position);

        holder.title.setText(objectItem.getTitle());
        holder.description.setText(objectItem.getDescription());
        holder.thumb.setImageBitmap(objectItem.getIcon());	
        // Try to use UIL to load thumbnail before testing the bitmap
        
        return row;
    }
	
	static class ViewHolder {
        TextView title;
        TextView description;
        ImageView thumb;
    }
}
