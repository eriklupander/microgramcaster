package com.squeed.microgramcaster.util;

public class TimeFormatter {
	public static String formatTime(int progressSeconds) {
		int hour = progressSeconds / (60*60);
		int minute = progressSeconds / 60;
		int second = progressSeconds % 60;
		return buildTimeString(hour, minute - (hour*60), second);
	}
	
	private static StringBuffer buf = new StringBuffer();

	private static String buildTimeString(int hour, int minute, int second) {
		buf.setLength(0);
		if(hour > 0) {
			buf.append(pad(hour)).append(":");
		}
		buf.append(pad(minute)).append(":");
		buf.append(pad(second));
		return buf.toString();
	}
	
	private static String pad(int number) {
		return number < 10 ? "0"+number : ""+number;				
	}
}
