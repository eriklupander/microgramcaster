package com.squeed.microgramcaster.media;

import java.util.Comparator;

public class MediaItemComparator implements Comparator<MediaItem> {

	@Override
	public int compare(MediaItem lhs, MediaItem rhs) {
		if(lhs.getName().equalsIgnoreCase("Back")) {
			return -1;
		}
		if(rhs.getName().equalsIgnoreCase("Back")) {
			return 1;
		}
		return lhs.getName().compareTo(rhs.getName());
	}

}
