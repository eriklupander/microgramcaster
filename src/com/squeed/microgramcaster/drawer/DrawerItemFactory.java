package com.squeed.microgramcaster.drawer;

import java.util.ArrayList;
import java.util.List;

import com.squeed.microgramcaster.R;

import android.app.Activity;
import android.graphics.BitmapFactory;

public class DrawerItemFactory {
	public static List<DrawerItem> buildDrawerItems(Activity activity) {
		List<DrawerItem> items = new ArrayList<DrawerItem>();
		DrawerItem local = new DrawerItem("Local device content", "Browse media on your local device", 
				BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_action_storage));
		DrawerItem upnp = new DrawerItem("UPnP Media Servers", "Browse media on UPnP / DLNA Media Servers", 
				BitmapFactory.decodeResource(activity.getResources(), R.drawable.upnp));
		DrawerItem smb = new DrawerItem("SMB network share", "Browse media on an SMB network share", 
				BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_action_storage));
		
		items.add(local);
		items.add(upnp);
		items.add(smb);
		return items;
	}
}
