package com.squeed.microgramcaster.util;

public class TitleFormatter {

	public static String format(String title) {
		if(title == null) return "";
		
		if(title.trim().length() > 40) {
			return title.substring(0, 40) + "...";
		}
		return title;
	}
	
}
