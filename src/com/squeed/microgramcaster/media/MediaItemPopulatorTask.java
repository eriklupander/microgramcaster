package com.squeed.microgramcaster.media;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.squeed.microgramcaster.MediaItemArrayAdapter;

public class MediaItemPopulatorTask extends AsyncTask<MediaItem, Void, MediaItem>{
	
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat headerDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

	private final WeakReference<MediaItemArrayAdapter> listViewReference;

	private Context ctx;

    public MediaItemPopulatorTask(Context ctx, MediaItemArrayAdapter adapter) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
    	this.ctx = ctx;
    	this.listViewReference = new WeakReference<MediaItemArrayAdapter>(adapter);
    	
    }

    // Decode image in background.
    @Override
    protected MediaItem doInBackground(MediaItem... mediaItems) {
    	if(mediaItems.length == 0) {
    		return null;
    	}
		MediaItem mi = mediaItems[0];
		
		 Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(
				 	ctx.getContentResolver(),
				 	mi.getId(),
	                MediaStore.Video.Thumbnails.MICRO_KIND,
	                (BitmapFactory.Options) null );
		 mi.setThumbnail(bitmap);
		 return mi;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(MediaItem mediaItem) {
        if (listViewReference != null && mediaItem != null) {
            final MediaItemArrayAdapter adapter = listViewReference.get();
            if (adapter != null) {            
            	adapter.add(mediaItem);            
            }
        }
    }

}
