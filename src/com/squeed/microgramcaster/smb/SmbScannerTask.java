package com.squeed.microgramcaster.smb;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.squeed.microgramcaster.MainActivity;
import com.squeed.microgramcaster.MediaItemArrayAdapter;

public class SmbScannerTask extends AsyncTask<Void, Void, Object>{
	
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat headerDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

	private final WeakReference<MediaItemArrayAdapter> listViewReference;

	private MainActivity ctx;

    public SmbScannerTask(MainActivity ctx, MediaItemArrayAdapter adapter) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
    	this.ctx = ctx;
    	this.listViewReference = new WeakReference<MediaItemArrayAdapter>(adapter);    	
    }

	@Override
	protected Object doInBackground(Void... params) {
		new SambaExplorer(ctx).init();
		return new Object();
	}


	

}
