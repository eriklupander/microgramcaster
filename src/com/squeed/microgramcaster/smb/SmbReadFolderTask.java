package com.squeed.microgramcaster.smb;

import java.io.IOException;
import java.net.MalformedURLException;

import jcifs.smb.SmbFile;
import android.os.AsyncTask;

import com.squeed.microgramcaster.MainActivity;

public class SmbReadFolderTask extends AsyncTask<String, Void, Object>{

	private MainActivity ctx;

    public SmbReadFolderTask(MainActivity ctx) {
    	this.ctx = ctx;	
    }

	@Override
	protected Object doInBackground(String... params) {
		SambaExplorer smbExplorer = ctx.getSambaExplorer();
		try {
			smbExplorer.traverseSMB(new SmbFile(params[0]), 2);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Object();
	}
}
