package com.squeed.microgramcaster.upnp;

import java.net.URI;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Person;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squeed.microgramcaster.Constants;
import com.squeed.microgramcaster.MainActivity;
import com.squeed.microgramcaster.R;
import com.squeed.microgramcaster.media.MediaItem;
import com.squeed.microgramcaster.media.MediaItemComparator;
import com.squeed.microgramcaster.util.PathStack;
import com.squeed.microgramcaster.util.TimeFormatter;

public class ContentListingBuilder {
	
	//private Set<MediaItem> items = new ConcurrentHashSet<MediaItem>();
	private final MediaItemComparator mediaItemComparator = new MediaItemComparator();
	private MainActivity activity;
	private UPnPHandler upnpHandler;
	private static final String TAG = "ContentListingBuilder";
	
	private SharedPreferences preferences;
	
	public ContentListingBuilder(MainActivity activity, UPnPHandler upnpHandler) {
		this.activity = activity;
		this.upnpHandler = upnpHandler;	
		this.preferences = PreferenceManager.getDefaultSharedPreferences(activity);
	}
	
	public void buildFolderListing(String containerId) {
		
		//items.clear();
		activity.getMediaItemListAdapter().clear();
		activity.getMediaItemListAdapter().setSelectedPosition(-1);
		
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				activity.getMediaItemListAdapter().notifyDataSetChanged();
				activity.getLoadingDialog().setMessage("Please wait while retrieving media files from UPnP source");
				activity.getLoadingDialog().show();
			}			
		});
		
		
		
		
		if(containerId == null)
			containerId = "0";
		
		// Only push if container not already on stack. Check for back / up
		if(!PathStack.get().contains(containerId)) {
			PathStack.get().push(containerId);	
		}
		
		buildSingleFolderSet(upnpHandler.getCurrentService(), containerId);
	}

	private void addBackItem() {
		if(PathStack.get().size() < 2) {
			return;
		}
		final MediaItem mi = new MediaItem();
		mi.setName("");
		mi.setData(popParentContainerIdFromStack());			
		mi.setType(Constants.DLNA_BACK);
		mi.setThumbnail(BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_menu_back));
		
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {										
				activity.getMediaItemListAdapter().add(mi);			
				activity.getMediaItemListAdapter().notifyDataSetChanged();					
			}
		});
	}
	
	
	private String popParentContainerIdFromStack() {
		if(PathStack.get().size() > 1) {
			return PathStack.get().elementAt(PathStack.get().size() - 2); // Peek the PARENT containerId
		} else if(PathStack.get().size() == 1) {
			return PathStack.get().elementAt(PathStack.get().size() - 1);
		} else {
			return "0";	
		}
	}

	private void buildSingleFolderSet(final Service service, final String containerId) {
		Browse b = new Browse(service, containerId, BrowseFlag.DIRECT_CHILDREN) {

		    @Override
		    public void received(ActionInvocation actionInvocation, final DIDLContent didl) {
		    	boolean itemAdded = false;

		    	boolean backItemAdded = false;
		    	if(didl.getItems() != null && didl.getItems().size() > 0) {
		    		if(!backItemAdded) {
			    		addBackItem();
		    			backItemAdded = true;
		    		}
					for(Item item : didl.getItems()) {
						final MediaItem mi = new MediaItem();
						mi.setName(item.getTitle());
						mi.setProducer(item.getCreator());
						mi.setData(item.getFirstResource().getValue());
						// At least from Windows Media Server, format is H:mm:ss.SSS
						String durStr = item.getFirstResource().getDuration();
						Long durationSeconds = TimeFormatter.hhmmssToMilliSeconds(durStr);
						mi.setDuration(durationSeconds); // TODO FIX
						mi.setSize(item.getFirstResource().getSize());
						mi.setType(Constants.DLNA_ITEM);
						mi.setExternalId(item.getId());
												
					
						// For now, only add mp4 files.
						
						if(preferences.getBoolean("show_unplayable", false) || mi.getData().toLowerCase().trim().endsWith("mp4")) {
							itemAdded = true;
							addThumbnailBitmap(item, mi);
							
							activity.runOnUiThread(new Runnable() {
								
								@Override
								public void run() {										
									activity.getMediaItemListAdapter().add(mi);
									activity.getMediaItemListAdapter().sort(mediaItemComparator);
									activity.getMediaItemListAdapter().notifyDataSetChanged();
									if(activity.getLoadingDialog().isShowing()) {
										activity.getLoadingDialog().hide();
									}
								}
							});
						}							
					}
					
				}
		    	if(didl.getContainers() != null && didl.getContainers().size() > 0){
		    		if(!backItemAdded) {
		    			addBackItem();
		    			backItemAdded = true;
		    		}
					for(Container item : didl.getContainers()) {
							itemAdded = true;
							
							final MediaItem mi = new MediaItem();
							mi.setName(item.getTitle());
							
							mi.setProducer(item.getCreator());
							mi.setData(item.getId());
							boolean thumbNailAdded = addThumbnailBitmap(item, mi);
							if(!thumbNailAdded) {
								mi.setThumbnail(BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_menu_archive));
							}
							mi.setType(Constants.DLNA_FOLDER);
							activity.runOnUiThread(new Runnable() {
								
								@Override
								public void run() {										
									activity.getMediaItemListAdapter().add(mi);
									activity.getMediaItemListAdapter().sort(mediaItemComparator);
									activity.getMediaItemListAdapter().notifyDataSetChanged();
									if(activity.getLoadingDialog().isShowing()) {
										activity.getLoadingDialog().hide();
									}
								}
							});
												
					}
					
				}
		    	
		    	// Make sure we hide the loading spinner if no items were present.
		    	if(!itemAdded) {		    		
					hideLoadingDialogOnUIThread();
				}
		    	if(!backItemAdded) {
		    		addBackItem();
	    			backItemAdded = true;
	    		}
		    }

			private boolean addThumbnailBitmap(DIDLObject item, final MediaItem mi) {
				URI firstPropertyValue = item.getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
				String thumbnailUrl = firstPropertyValue != null ? firstPropertyValue.toString() : null;
				mi.setThumbnailUrl(thumbnailUrl);	
				return thumbnailUrl != null;				
			}
		    
		    @Override
		    public void updateStatus(Status status) {
		    	Log.d(TAG , "ENTER - updateStatus Browse callback");
		    }
		
		    @Override
		    public void failure(ActionInvocation invocation,
		                        UpnpResponse operation,
		                        String defaultMsg) {
		    	Log.e(TAG , "ENTER - failure Browse callback: " + defaultMsg);
		    	hideLoadingDialogOnUIThread();
		    }
		};
		upnpHandler.getUPnPService().getControlPoint().execute(b);
	}

	

	private void hideLoadingDialogOnUIThread() {
		if(activity.getLoadingDialog().isShowing()) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {			
					activity.getLoadingDialog().hide();
				}
			});						
		}
	}
}
