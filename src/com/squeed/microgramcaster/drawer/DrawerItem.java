package com.squeed.microgramcaster.drawer;

import android.graphics.Bitmap;

public class DrawerItem {
	private String title;
	private String description;
	private Bitmap icon;
	
	public DrawerItem() {}
	
	public DrawerItem(String title, String description, Bitmap icon) {
		super();
		this.title = title;
		this.description = description;
		this.icon = icon;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Bitmap getIcon() {
		return icon;
	}
	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}
	
	
}
