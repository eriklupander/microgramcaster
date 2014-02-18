package com.squeed.microgramcaster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

public class MediaStoreAdapter {
	
	public List<String> findFiles(Context context, String filePath) {
		String[] retCol = { MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA , MediaStore.Video.Media.SIZE};
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
				String displayName = cur.getString(1); // cur.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
				Serializable data = cur.getBlob(2);
				Long size = cur.getLong(3);
				names.add(displayName);
			}		
			
			cur.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return names;
	}

	public MediaItem findFile(Context context, String requestedFile) {
		requestedFile = requestedFile.replaceAll("%20", " ");
		String[] retCol = { MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA , MediaStore.Video.Media.SIZE};
		Cursor cur = context.getContentResolver().query(
			    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
			    retCol, 
			    MediaStore.MediaColumns.DISPLAY_NAME + "='" + requestedFile + "'", null, null); //MediaStore.MediaColumns.DATA + "='" + filePath + "'"
		try {
			
			
			
				if(cur.moveToNext()) {
					int id = cur.getInt(0);
					String displayName = cur.getString(1); // cur.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
					String data = cur.getString(2);
					Long size = cur.getLong(3);
					MediaItem mi = new MediaItem();
					mi.setId(id);
					mi.setName(displayName);
					mi.setSize(size);
					mi.setData(data);
					return mi;
				}		
				return null;
				
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally {
			cur.close();
		}
	}

}
