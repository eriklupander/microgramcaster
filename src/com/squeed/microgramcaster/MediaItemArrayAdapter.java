package com.squeed.microgramcaster;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
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

        if(convertView==null){
            // inflate the layout
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        // object item based on the position
        MediaItem objectItem = data.get(position);
        convertView.setTag(objectItem.getName());
        convertView.setTag(R.id.externalId, objectItem.getExternalId());
        convertView.setTag(R.id.type, objectItem.getType());
        convertView.setTag(R.id.dlna_url, objectItem.getData());
        convertView.setTag(R.id.dlna_name, objectItem.getName());
        convertView.setTag(R.id.dlna_duration, objectItem.getDuration());

        // get the TextView and then set the text (item name) and tag (item ID) values
        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(objectItem.getName());
        title.setTag(objectItem.getName());
        
        TextView duration = (TextView) convertView.findViewById(R.id.duration);
        duration.setText(TimeFormatter.formatTime((int)(objectItem.getDuration() / 1000)));
        
        ImageView thumb = (ImageView) convertView.findViewById(R.id.thumb);
        if(objectItem.getThumbnail() != null) {
        	thumb.setImageBitmap(objectItem.getThumbnail());	
        }
        
        if(selectedPosition == position){
        	convertView.setBackgroundResource(R.color.pressed_color);
        }
        else{
        	convertView.setBackgroundResource(android.graphics.Color.TRANSPARENT);
        }
       
        return convertView;

    }
    
    
    public int getSelectedPosition() {
		return selectedPosition;
	}

	public void setSelectedPosition(int selectedPosition) {
    	this.selectedPosition = selectedPosition;
    }

}
