package com.squeed.microgramcaster.upnp;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Stack;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squeed.microgramcaster.MainActivity;
import com.squeed.microgramcaster.R;
import com.squeed.microgramcaster.media.MediaItem;
import com.squeed.microgramcaster.media.MediaItemComparator;
import com.squeed.microgramcaster.util.TimeFormatter;

public class ContentListingBuilder {
	
	//private Set<MediaItem> items = new ConcurrentHashSet<MediaItem>();
	private final MediaItemComparator mediaItemComparator = new MediaItemComparator();
	private MainActivity activity;
	private UPnPHandler upnpHandler;
	private static final String TAG = "ContentListingBuilder";
	private Stack<String> containerStack = new Stack<String>();
	private SharedPreferences preferences;
	
	public ContentListingBuilder(MainActivity activity, UPnPHandler upnpHandler) {
		this.activity = activity;
		this.upnpHandler = upnpHandler;	
		this.preferences = PreferenceManager.getDefaultSharedPreferences(activity);
	}
	
//	public void startBuildDLNAContentListing() {
//		//items.clear();
//		activity.getMediaItemListAdapter().clear();
//		activity.getMediaItemListAdapter().setSelectedPosition(-1);
//		activity.getLoadingDialog().setMessage("Please wait while retrieving media files from UPnP source");
//		activity.getLoadingDialog().show();
//		//buildFlatSet(upnpHandler.getCurrentService(), "0");
//	}
	
	public void buildFolderListing(String containerId) {
		
		//items.clear();
		activity.getMediaItemListAdapter().clear();
		activity.getMediaItemListAdapter().setSelectedPosition(-1);
		
		activity.getLoadingDialog().setMessage("Please wait while retrieving media files from UPnP source");
		activity.getLoadingDialog().show();
		
		
		
		if(containerId == null)
			containerId = "0";
		
		// Only push if container not already on stack. Check for back / up
		if(!containerStack.contains(containerId)) {
			containerStack.push(containerId);	
		}
		
		buildSingleFolderSet(upnpHandler.getCurrentService(), containerId);
	}

	private void addBackItem() {
		final MediaItem mi = new MediaItem();
		mi.setName("Back");
		mi.setData(popParentContainerIdFromStack());			
		mi.setType("DLNA_BACK");
		// mi.setThumbnail(BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_menu_back));
		
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {										
				activity.getMediaItemListAdapter().add(mi);			
				activity.getMediaItemListAdapter().notifyDataSetChanged();					
			}
		});
	}
	
	
	private String popParentContainerIdFromStack() {
		if(containerStack.size() > 1) {
			return containerStack.elementAt(containerStack.size() - 2); // Peek the PARENT containerId
		} else if(containerStack.size() == 1) {
			return containerStack.elementAt(containerStack.size() - 1);
		} else {
			return "0";	
		}
	}

	private void buildSingleFolderSet(final Service service, final String containerId) {
		Log.i(TAG , "Building FLAT set for containerId " + containerId);
		Browse b = new Browse(service, containerId, BrowseFlag.DIRECT_CHILDREN) {

		    @Override
		    public void received(ActionInvocation actionInvocation, final DIDLContent didl) {
		    	Log.i(TAG , "ENTER - received Browse callback");
		    	boolean itemAdded = false;

		    	boolean backItemAdded = false;
		    	if(didl.getItems() != null && didl.getItems().size() > 0) {
		    		if(!backItemAdded) {
			    		addBackItem();
		    			backItemAdded = true;
		    		}
					for(Item item : didl.getItems()) {
						Log.i(TAG , "Found item " + item.getTitle());
						final MediaItem mi = new MediaItem();
						mi.setName(item.getTitle());
						mi.setData(item.getFirstResource().getValue());
						// At least from Windows Media Server, format is H:mm:ss.SSS
						String durStr = item.getFirstResource().getDuration();
						Long durationSeconds = TimeFormatter.hhmmssToMilliSeconds(durStr);
						mi.setDuration(durationSeconds); // TODO FIX
						mi.setSize(item.getFirstResource().getSize());
						mi.setType("DLNA_ITEM");
						mi.setExternalId(item.getId());
												
					
						// For now, only add mp4 files.
						
						if(preferences.getBoolean("show_unplayable", false) || mi.getData().toLowerCase().trim().endsWith("mp4")) {
							itemAdded = true;
							Log.i(TAG , "Found mp4 " + mi.getData());
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
							mi.setData(item.getId());
							boolean thumbNailAdded = addThumbnailBitmap(item, mi);
							if(!thumbNailAdded) {
								Bitmap scaledBitmap = Bitmap.createScaledBitmap(
										BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_menu_archive96)
								,96,96, true);
								mi.setThumbnail(scaledBitmap);
							}
							mi.setType("DLNA_FOLDER");
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
				if(thumbnailUrl != null) {
					try{			
						Bitmap scaledBitmap = Bitmap.createScaledBitmap(
								BitmapFactory.decodeStream((InputStream) new URL(thumbnailUrl).getContent())
						,96,96, true);
						mi.setThumbnail(scaledBitmap);
						return true;
				    } catch (Exception e){
				    	Log.e("ContentListingBuilder", e.getMessage());
				    }
				}
				return false;
			}
		    
		    @Override
		    public void updateStatus(Status status) {
		    	Log.i(TAG , "ENTER - updateStatus Browse callback");
		    }
		
		    @Override
		    public void failure(ActionInvocation invocation,
		                        UpnpResponse operation,
		                        String defaultMsg) {
		    	Log.i(TAG , "ENTER - failure Browse callback: " + defaultMsg);
		    	hideLoadingDialogOnUIThread();
		    }
		};
		upnpHandler.getUPnPService().getControlPoint().execute(b);
	}

	public void popContainerIdStack() {
		containerStack.pop();
	}
	
	public void clearContainerIdStack() {
		containerStack.clear();
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

//	private void buildFlatSet(final Service service, String containerId) {
//		Log.i(TAG , "Building FLAT set for containerId " + containerId);
//		Browse b = new Browse(service, containerId, BrowseFlag.DIRECT_CHILDREN) {
//
//		    @Override
//		    public void received(ActionInvocation actionInvocation, final DIDLContent didl) {
//		    	if(didl.getItems() != null && didl.getItems().size() > 0) {
//				
//					for(Item item : didl.getItems()) {
//						Log.i(TAG , "Found item " + item.getTitle());
//						final MediaItem mi = new MediaItem();
//						mi.setName(item.getTitle());
//						mi.setData(item.getFirstResource().getValue());
//						
//						// At least from Windows Media Server, format is H:mm:ss.SSS
//						String durStr = item.getFirstResource().getDuration();
//						Long durationSeconds = parse(durStr);
//						mi.setDuration(durationSeconds); // TODO FIX
//						mi.setSize(item.getFirstResource().getSize());
//						mi.setType("DLNA_ITEM");
//						mi.setExternalId(item.getId());
//												
//						if(!items.contains(mi)) {
//							items.add(mi);
//							
//							// For now, only add mp4 files.
//							if(mi.getData().toLowerCase().trim().endsWith("mp4")) {
//								Log.i(TAG , "Found mp4 " + mi.getData());
//								String thumbnailUrl = item.getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class).toString();
//								if(thumbnailUrl != null) {
//									try{			
//										Bitmap scaledBitmap = Bitmap.createScaledBitmap(
//												BitmapFactory.decodeStream((InputStream) new URL(thumbnailUrl).getContent())
//										,96,96, true);
//										mi.setThumbnail(scaledBitmap);
//							        } catch (Exception e){
//							        	Log.e("ContentListingBuilder", e.getMessage());
//							        }
//								}
//								activity.runOnUiThread(new Runnable() {
//									
//									@Override
//									public void run() {										
//										activity.getMediaItemListAdapter().add(mi);
//										activity.getMediaItemListAdapter().sort(mediaItemComparator);
//										activity.getMediaItemListAdapter().notifyDataSetChanged();
//										if(activity.getLoadingDialog().isShowing()) {
//											activity.getLoadingDialog().hide();
//										}
//									}
//								});
//							}							
//						}
//					}
//				}
//		    	if(didl.getContainers() != null && didl.getContainers().size() > 0){
//			
//					for(Container item : didl.getContainers()) {
//						if(item.getChildCount() > 0) {
//							buildFlatSet(service, item.getId());	
//						}						
//					}
//				}
//		    }
//		    
//		    @Override
//		    public void updateStatus(Status status) {
//		        
//		    }
//		
//		    @Override
//		    public void failure(ActionInvocation invocation,
//		                        UpnpResponse operation,
//		                        String defaultMsg) {
//		    }
//		};
//		upnpHandler.getUPnPService().getControlPoint().execute(b);
//	}


	// TODO Move to util class...
	



	
}
