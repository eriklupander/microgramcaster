package com.squeed.microgramcaster.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.channels.FileChannel;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.squeed.microgramcaster.ArrayAdapterItem;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
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
	
	/**
	 * Only returns mp4 and ogv files.
	 * 
	 * @param context
	 * @param filePath
	 * @return
	 */
	public List<MediaItem> findFiles(Context context) {
		String[] retCol = { 
				MediaStore.Video.Media._ID, 
				MediaStore.Video.Media.DISPLAY_NAME, 
				MediaStore.Video.Media.DATA, 
				MediaStore.Video.Media.SIZE,
				MediaStore.Video.Media.DATE_MODIFIED,
				MediaStore.Video.Media.DURATION};
		Cursor cur = context.getContentResolver().query(
		    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
		    retCol, 
		    "(" + MediaStore.Video.Media.DISPLAY_NAME + " like '%.mp4' OR " + 
		    MediaStore.Video.Media.DISPLAY_NAME + " like '%.ogv')", null, null);
		if (cur.getCount() == 0) {
		    return new ArrayList<MediaItem>();
		}
		 List<MediaItem> mediaItems = new ArrayList<MediaItem>();
		try {
			while(cur.moveToNext()) {
				MediaItem mi = new MediaItem();
				mi.setId(cur.getInt(0));
				mi.setName(cur.getString(1));
				mi.setData(cur.getString(2));
				mi.setSize(cur.getLong(3));
				mi.setLastModified(headerDateFormat.format(new Date(cur.getLong(4))));
				mi.setDuration(cur.getLong(5));
				
				 Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(
						 	context.getContentResolver(),
						 	mi.getId(),
			                MediaStore.Video.Thumbnails.MICRO_KIND,
			                (BitmapFactory.Options) null );
				 mi.setThumbnail(bitmap);
				mediaItems.add(mi);
			}		
			
			cur.close();
		} catch (Exception e) {
			Log.e("MediaStoreAdapter", "Error fetching MediaStore data, returning empty list. Error: " + e.getMessage());
		}
		return mediaItems;
	}
	
	/**
	 * Only returns mp4 and ogv files.
	 * 
	 * @param context
	 * @param filePath
	 * @return
	 * 		true if at least one file was found
	 */
	public boolean findFilesAsync(Context context, ArrayAdapterItem adapter) {
		String[] retCol = { 
				MediaStore.Video.Media._ID, 
				MediaStore.Video.Media.DISPLAY_NAME, 
				MediaStore.Video.Media.DATA, 
				MediaStore.Video.Media.SIZE,
				MediaStore.Video.Media.DATE_MODIFIED,
				MediaStore.Video.Media.DURATION};
		Cursor cur = context.getContentResolver().query(
		    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
		    retCol, 
		    "(" + MediaStore.Video.Media.DISPLAY_NAME + " like '%.mp4' OR " + 
		    MediaStore.Video.Media.DISPLAY_NAME + " like '%.ogv')", null, null);
		if (cur.getCount() == 0) {
		    return false;
		}
	
		try {
			while(cur.moveToNext()) {
				
				MediaItem mi = new MediaItem();
				mi.setId(cur.getInt(0));
				mi.setName(cur.getString(1));
				mi.setData(cur.getString(2));
				mi.setSize(cur.getLong(3));
				mi.setLastModified(headerDateFormat.format(new Date(cur.getLong(4))));
				mi.setDuration(cur.getLong(5));
				MediaItemPopulatorTask task = new MediaItemPopulatorTask(context, adapter);
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mi);
//				 Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(
//						 	context.getContentResolver(),
//						 	mi.getId(),
//			                MediaStore.Video.Thumbnails.MICRO_KIND,
//			                (BitmapFactory.Options) null );
//				 mi.setThumbnail(bitmap);
//				mediaItems.add(mi);
			}		
			
			cur.close();
		} catch (Exception e) {
			Log.e("MediaStoreAdapter", "Error fetching MediaStore data, returning empty list. Error: " + e.getMessage());
		}
		return true;
		
	}

	public MediaItem findFile(Context context, String requestedFile) {
		requestedFile = requestedFile.replaceAll("%20", " ");
		String[] retCol = { 
				MediaStore.Video.Media._ID, 
				MediaStore.Video.Media.DISPLAY_NAME, 
				MediaStore.Video.Media.DATA, 
				MediaStore.Video.Media.SIZE,
				MediaStore.Video.Media.DATE_MODIFIED,
				MediaStore.Video.Media.DURATION};
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
				mi.setDuration(cur.getLong(5));
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
	
	public String getFileDataPath(Context context, String requestedFile) {
		requestedFile = requestedFile.replaceAll("%20", " ");
		String[] retCol = { 
				MediaStore.Video.Media.DATA};
		Cursor cur = context.getContentResolver().query(
			    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
			    retCol, 
			    MediaStore.MediaColumns.DISPLAY_NAME + "='" + requestedFile + "'", null, null); //MediaStore.MediaColumns.DATA + "='" + filePath + "'"
		try {
			if (cur.moveToNext()) {
				return cur.getString(0);
			}
			return null;

		} catch (Exception e) {
			Log.e("MediaStoreAdapter", "Error fetching MediaStore item, returning null. Error: " + e.getMessage());
			return null;
		} finally {
			cur.close();
		}
	}
	
	public FileChannel getFileChannel(Context context, String requestedFile) {
		String fullPathToFile = getFileDataPath(context, requestedFile);
		if(fullPathToFile != null) {
			File sd = Environment.getExternalStorageDirectory();
	        if (sd.canRead()) {

	            File source = new File(fullPathToFile );
	            try {
					return new FileInputStream(source).getChannel();
				} catch (FileNotFoundException e) {
					return null;
				}
	        }
		}
		return null;
	}

}
