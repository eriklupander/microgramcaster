package com.squeed.microgramcaster.media;

import java.util.Comparator;

import com.squeed.microgramcaster.Constants;

public class MediaItemComparator implements Comparator<MediaItem> {

	@Override
	public int compare(MediaItem lhs, MediaItem rhs) {
		if(lhs.getType().equals(Constants.DLNA_BACK)) {
			return -1;
		}
		if(rhs.getType().equals(Constants.DLNA_BACK)) {
			return 1;
		}
		return lhs.getName().compareTo(rhs.getName());
	}

}
