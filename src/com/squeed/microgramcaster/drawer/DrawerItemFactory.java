package com.squeed.microgramcaster.drawer;

import java.util.ArrayList;
import java.util.List;

import com.squeed.microgramcaster.R;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

public class DrawerItemFactory {
	public static List<DrawerItem> buildDrawerItems(Activity activity) {
		List<DrawerItem> items = new ArrayList<DrawerItem>();
		DrawerItem local = new DrawerItem("Local device content", "Browse media on your local device", 
				BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_action_storage));
		DrawerItem upnp = new DrawerItem("UPnP Media Servers", "Browse media on UPnP / DLNA Media Servers", 
				BitmapFactory.decodeResource(activity.getResources(), R.drawable.upnp));
		DrawerItem smb = new DrawerItem("SMB network share", "Browse media on an SMB network share", 
				BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_menu_archive));
		
		
		
		
		items.add(local);
		items.add(upnp);
		items.add(smb);
		if(PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("show_rating", true)) {
			DrawerItem star = new DrawerItem("Rate this application", "Help us out by giving a rating on the Play Store. You can hide this item in the preferences or by submitting a rating. Thanks!", 
					BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_action_star));	
			items.add(star);			
		}
		
		return items;
	}
	
	
}
