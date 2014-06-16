package com.squeed.microgramcaster;

import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squeed.microgramcaster.media.MediaItem;
import com.squeed.microgramcaster.util.TimeFormatter;

/**
 * Array Adapter for showing file listings, directory browsing etc.
 * 
 * It's a bit bloated, mostly due to it serving both local, upnp or smb structures.
 * 
 * @author Erik
 *
 */
public class MediaItemArrayAdapter extends ArrayAdapter<MediaItem> {

    private Context mContext;
    private int layoutResourceId;
    private  ArrayList<MediaItem> data = null;
    private int selectedPosition = -1;

    public MediaItemArrayAdapter(Context mContext, int layoutResourceId, ArrayList<MediaItem> data) {

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
            holder.title = (TextView) row.findViewById(R.id.title);
            holder.duration = (TextView) row.findViewById(R.id.duration);
            holder.thumb = (ImageView) row.findViewById(R.id.thumb);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        // object item based on the position
        MediaItem objectItem = data.get(position);
        row.setTag(R.id.name, objectItem.getName());
        row.setTag(R.id.externalId, objectItem.getExternalId());
        row.setTag(R.id.type, objectItem.getType());
        
        row.setTag(R.id.dlna_url, objectItem.getData());
        row.setTag(R.id.dlna_name, objectItem.getName());
        row.setTag(R.id.dlna_duration, objectItem.getDuration());
        
        
        if( !(objectItem.getData().endsWith(".mp4") || objectItem.getData().endsWith(".ogv")) && objectItem.getType().equals(Constants.DLNA_ITEM)) {
        	holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
        	holder.title.setPaintFlags( holder.title.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
        holder.title.setText(objectItem.getName());
        holder.title.setTag(objectItem.getName());
        
        
        if(objectItem.getDuration() != null) {
        	if(!objectItem.getType().equals(Constants.SMB_FILE)) {
        		holder.duration.setText(TimeFormatter.formatTime((int)(objectItem.getDuration() / 1000)));	
        	} else {
        		holder.duration.setText(FileUtils.byteCountToDisplaySize(objectItem.getDuration()));
        	}
        	
        } else if(objectItem.getType().equals(Constants.DLNA_BACK) || objectItem.getType().equals(Constants.SMB_BACK)) {        	
        	holder.duration.setText("");        	
        } else {
        	holder.duration.setText("Folder");
        }

        // Try to use UIL to load thumbnail before testing the bitmap
        if(objectItem.getThumbnailUrl() != null) {
        	row.setTag(R.id.dlna_thumbnail_url, objectItem.getThumbnailUrl());
        	ImageLoader.getInstance().displayImage(objectItem.getThumbnailUrl(), holder.thumb);	
        } else {
            if(objectItem.getThumbnail() != null) {
            	holder.thumb.setImageBitmap(objectItem.getThumbnail());	
            } else {
            	holder.thumb.setImageBitmap(null);
            }
        }
        
        
        if(selectedPosition == position){
        	row.setBackgroundResource(R.color.pressed_color);
        } else {
        	row.setBackgroundResource(android.graphics.Color.TRANSPARENT);
        }
       
        return row;
    }
    
    
    public int getSelectedPosition() {
		return selectedPosition;
	}

	public void setSelectedPosition(int selectedPosition) {
    	this.selectedPosition = selectedPosition;
    }

	
	static class ViewHolder {
		TextView title;
        TextView duration;
        ImageView thumb;
    }
}
