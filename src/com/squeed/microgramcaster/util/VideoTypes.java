package com.squeed.microgramcaster.util;

import java.util.HashSet;

public class VideoTypes {
	
	private static HashSet<String> suffixes = new HashSet<String>();
	static {
		suffixes.add("mp4");
		suffixes.add("ogv");
		suffixes.add("ogg");
		suffixes.add("mkv");
		suffixes.add("avi");
		suffixes.add("mpg");
		suffixes.add("wmv");
		suffixes.add("mov");
	}
	
	private static HashSet<String> playableSuffixes = new HashSet<String>();
	static {
		playableSuffixes.add("mp4");
		//playableSuffixes.add("ogv");
		playableSuffixes.add("ogg");
		playableSuffixes.add("mkv");		
	}
	
	public static boolean isVideo(String file) {
		if(file.contains(".")) {
			String suffix = file.substring(file.lastIndexOf(".")+1);
			return suffixes.contains(suffix);
		} else {
			return false;
		}
	}
	
	public static boolean isPlayableVideo(String file) {
		if(file.contains(".")) {
			String suffix = file.substring(file.lastIndexOf(".")+1);
			return playableSuffixes.contains(suffix);
		} else {
			return false;
		}
	}
}
