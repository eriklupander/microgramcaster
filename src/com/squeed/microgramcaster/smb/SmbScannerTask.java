package com.squeed.microgramcaster.smb;

import android.os.AsyncTask;

import com.squeed.microgramcaster.MainActivity;

public class SmbScannerTask extends AsyncTask<Void, Void, Object>{

	private MainActivity ctx;

    public SmbScannerTask(MainActivity ctx) {
    	this.ctx = ctx;	
    }

	@Override
	protected Object doInBackground(Void... params) {
		SambaExplorer smbExplorer = ctx.getSambaExplorer();
		smbExplorer.init();
		return new Object();
	}
}
