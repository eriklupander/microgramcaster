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
	
	/**
	 * Takes a HH:mm:ss, mm:ss or ss value and converts it into a Long representation in milliseconds.
	 * 
	 * @param durStr
	 * @return
	 */
	public static Long hhmmssToMilliSeconds(String durStr) {

		if(durStr == null || durStr.length() < 3) {
			return 0L;
		}
		if(durStr.indexOf(".") > -1) {
			durStr = durStr.substring(0, durStr.indexOf("."));
		}
		String[] parts = durStr.split(":");
		Integer seconds = Integer.parseInt(parts[parts.length - 1]);
		Integer minutes = 0;
		Integer hours = 0;
		if(parts.length - 2 >= 0) {
			 minutes = Integer.parseInt(parts[parts.length - 2]);	
		}
		if(parts.length - 3 >= 0) {
			 hours = Integer.parseInt(parts[parts.length - 3]);	
		}
		Long duration = hours*60L*60L + minutes*60L + seconds;
		return duration*1000L;
	}
	
}
