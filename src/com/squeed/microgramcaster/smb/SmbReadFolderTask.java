package com.squeed.microgramcaster.smb;

import java.io.IOException;
import java.net.MalformedURLException;

import jcifs.smb.SmbFile;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.squeed.microgramcaster.Constants;
import com.squeed.microgramcaster.MainActivity;
import com.squeed.microgramcaster.R;
import com.squeed.microgramcaster.media.MediaItem;

public class SmbReadFolderTask extends AsyncTask<String, Void, Object>{

	private MainActivity ctx;

    public SmbReadFolderTask(MainActivity ctx) {
    	this.ctx = ctx;	
    }

	@Override
	protected Object doInBackground(String... params) {
		final SambaExplorer smbExplorer = ctx.getSambaExplorer();
		final String folder = params[0];
		// Only push if container not already on stack. Check for back / up
		if(!smbExplorer.getContainerStack().contains(folder)) {
			smbExplorer.getContainerStack().push(folder);	
		}
		try {
			ctx.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					final MediaItem back = new MediaItem();
					back.setName("");
					back.setData(smbExplorer.getParentContainerIdFromStack());			
					back.setType(Constants.SMB_BACK);
					back.setThumbnail(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_menu_back));
					ctx.getMediaItemListAdapter().clear();
					if(smbExplorer.getContainerStack().size() > 1) {
						ctx.getMediaItemListAdapter().add(back);
					}					
					ctx.getMediaItemListAdapter().notifyDataSetChanged();
				}
				
			});
			smbExplorer.traverseSMB(new SmbFile(folder), 2);
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
