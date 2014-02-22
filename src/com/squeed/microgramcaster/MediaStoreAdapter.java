package com.squeed.microgramcaster;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Perhaps rename this...
 * 
 * Provides some simple querying for MediaStore resources.
 * 
 * @author Erik
 */
public class MediaStoreAdapter {
	
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat headerDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	
	public List<String> findFiles(Context context, String filePath) {
		String[] retCol = { MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME};
		Cursor cur = context.getContentResolver().query(
		    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
		    retCol, 
		    null, null, null); //MediaStore.MediaColumns.DATA + "='" + filePath + "'" TODO Fix mp4 filtering.
		if (cur.getCount() == 0) {
		    return new ArrayList<String>();
		}
		 List<String> names = new ArrayList<String>();
		try {
			while(cur.moveToNext()) {
				int id = cur.getInt(0);
				String displayName = cur.getString(1);
				names.add(displayName);
			}		
			
			cur.close();
		} catch (Exception e) {
			Log.e("MediaStoreAdapter", "Error fetching MediaStore data, returning empty list. Error: " + e.getMessage());
		}
		return names;
	}

	public MediaItem findFile(Context context, String requestedFile) {
		requestedFile = requestedFile.replaceAll("%20", " ");
		String[] retCol = { 
				MediaStore.Video.Media._ID, 
				MediaStore.Video.Media.DISPLAY_NAME, 
				MediaStore.Video.Media.DATA, 
				MediaStore.Video.Media.SIZE,
				MediaStore.Video.Media.DATE_MODIFIED};
		Cursor cur = context.getContentResolver().query(
			    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
			    retCol, 
			    MediaStore.MediaColumns.DISPLAY_NAME + "='" + requestedFile + "'", null, null); //MediaStore.MediaColumns.DATA + "='" + filePath + "'"
		try {
			if (cur.moveToNext()) {

				MediaItem mi = new MediaItem();
				mi.setId(cur.getInt(0));
				mi.setName(cur.getString(1));
				mi.setData(cur.getString(2));
				mi.setSize(cur.getLong(3));
				mi.setLastModified(headerDateFormat.format(new Date(cur.getLong(4))));
				return mi;
			}
			return null;

		} catch (Exception e) {
			Log.e("MediaStoreAdapter", "Error fetching MediaStore item, returning null. Error: " + e.getMessage());
			return null;
		} finally {
			cur.close();
		}
	}

}
