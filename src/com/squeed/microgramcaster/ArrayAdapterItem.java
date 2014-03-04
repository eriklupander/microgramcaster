package com.squeed.microgramcaster;

import java.util.ArrayList;

import com.squeed.microgramcaster.media.MediaItem;
import com.squeed.microgramcaster.util.TimeFormatter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ArrayAdapterItem extends ArrayAdapter<MediaItem> {

    private Context mContext;
    private int layoutResourceId;
    private  ArrayList<MediaItem> data = null;
    private int selectedPosition = -1;

    public ArrayAdapterItem(Context mContext, int layoutResourceId, ArrayList<MediaItem> data) {

        super(mContext, layoutResourceId, data);

        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        /*
         * The convertView argument is essentially a "ScrapView" as described is Lucas post 
         * http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
         * It will have a non-null value when ListView is asking you recycle the row layout. 
         * So, when convertView is not null, you should simply update its contents instead of inflating a new row layout.
         */
        if(convertView==null){
            // inflate the layout
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        // object item based on the position
        MediaItem objectItem = data.get(position);
        convertView.setTag(objectItem.getName());

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
    
    public void setSelectedPosition(int selectedPosition) {
    	this.selectedPosition = selectedPosition;
    }

}
