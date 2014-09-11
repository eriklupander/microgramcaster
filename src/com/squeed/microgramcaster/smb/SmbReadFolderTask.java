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
import com.squeed.microgramcaster.util.PathStack;

public class SmbReadFolderTask extends AsyncTask<String, Void, Object>{

	private MainActivity activity;

    public SmbReadFolderTask(MainActivity activity) {
    	this.activity = activity;	
    }

	@Override
	protected Object doInBackground(String... params) {
		final SambaExplorer smbExplorer = activity.getSambaExplorer();
		final String folder = params[0];
		// Only push if container not already on stack. Check for back / up
		if(!PathStack.get().contains(folder)) {
			PathStack.get().push(folder);	
		}
		try {
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					final MediaItem back = new MediaItem();
					back.setName("");
					back.setData(smbExplorer.getParentContainerIdFromStack());			
					back.setType(Constants.SMB_BACK);
					back.setThumbnail(BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_menu_back));
					activity.getMediaItemListAdapter().clear();
					if(PathStack.get().size() > 1) {
						activity.getMediaItemListAdapter().add(back);
					}					
					activity.getMediaItemListAdapter().notifyDataSetChanged();
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
