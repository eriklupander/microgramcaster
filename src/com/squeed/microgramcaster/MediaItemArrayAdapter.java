package com.squeed.microgramcaster;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squeed.microgramcaster.media.MediaItem;
import com.squeed.microgramcaster.util.TimeFormatter;

// TODO Implement viewHolder pattern
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
    	

//        if(convertView==null){
//            // inflate the layout
//            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
//            convertView = inflater.inflate(layoutResourceId, parent, false);
//        }
        
        

        // object item based on the position
        MediaItem objectItem = data.get(position);
        row.setTag(R.id.name, objectItem.getName());
        row.setTag(R.id.externalId, objectItem.getExternalId());
        row.setTag(R.id.type, objectItem.getType());
        
        row.setTag(R.id.dlna_url, objectItem.getData());
        row.setTag(R.id.dlna_name, objectItem.getName());
        row.setTag(R.id.dlna_duration, objectItem.getDuration());

        // get the TextView and then set the text (item name) and tag (item ID) values
        //TextView title = (TextView) convertView.findViewById(R.id.title);
        //TextView duration = (TextView) convertView.findViewById(R.id.duration);
        
        if(!objectItem.getData().endsWith(".mp4") && objectItem.getType().equals("DLNA_ITEM")) {
        	holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
        	holder.title.setPaintFlags( holder.title.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
        holder.title.setText(objectItem.getName());
        holder.title.setTag(objectItem.getName());
        
        
        if(objectItem.getDuration() != null) {
        	holder.duration.setText(TimeFormatter.formatTime((int)(objectItem.getDuration() / 1000)));
        } else if(objectItem.getType().equals("DLNA_BACK")) {        	
        	holder.duration.setText("");
        } else {
        	holder.duration.setText("Folder");
        }
        
        
        
        //ImageView thumb = (ImageView) convertView.findViewById(R.id.thumb);
        if(objectItem.getThumbnail() != null) {
        	holder.thumb.setImageBitmap(objectItem.getThumbnail());	
        } else {
        	holder.thumb.setImageBitmap(null);
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
        
        
        //int position;
    }
}
