package com.squeed.microgramcaster.upnp;

import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.squeed.microgramcaster.MainActivity;
import com.squeed.microgramcaster.media.MediaItem;
import com.squeed.microgramcaster.media.MediaItemComparator;

public class ContentListingBuilder {
	
	private Set<MediaItem> items = new ConcurrentHashSet<MediaItem>();
	private final MediaItemComparator mediaItemComparator = new MediaItemComparator();
	private MainActivity activity;
	private UPnPHandler upnpHandler;
	
	public ContentListingBuilder(MainActivity activity, UPnPHandler upnpHandler) {
		this.activity = activity;
		this.upnpHandler = upnpHandler;		
	}
	
	public void startBuildDLNAContentListing() {
		items.clear();
		activity.getMediaItemListAdapter().clear();
		activity.getMediaItemListAdapter().setSelectedPosition(-1);
		activity.getLoadingDialog().setMessage("Please wait while retrieving media files from UPnP source");
		activity.getLoadingDialog().show();
		buildFlatSet(upnpHandler.getCurrentService(), "0");
	}
	
	
	private void buildFlatSet(final Service service, String containerId) {
		Browse b = new Browse(service, containerId, BrowseFlag.DIRECT_CHILDREN) {

		    @Override
		    public void received(ActionInvocation actionInvocation, final DIDLContent didl) {
		    	if(didl.getItems() != null && didl.getItems().size() > 0) {
				
					for(Item item : didl.getItems()) {
						final MediaItem mi = new MediaItem();
						mi.setName(item.getTitle());
						mi.setData(item.getFirstResource().getValue());
						
						// At least from Windows Media Server, format is H:mm:ss.SSS
						String durStr = item.getFirstResource().getDuration();
						Long durationSeconds = parse(durStr);
						mi.setDuration(durationSeconds); // TODO FIX
						mi.setSize(item.getFirstResource().getSize());
						mi.setType("DLNA_ITEM");
						mi.setExternalId(item.getId());
												
						if(!items.contains(mi)) {
							items.add(mi);
							
							// For now, only add mp4 files.
							if(mi.getData().toLowerCase().trim().endsWith("mp4")) {
								String thumbnailUrl = item.getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class).toString();
								if(thumbnailUrl != null) {
									try{							
										mi.setThumbnail(
												Bitmap.createScaledBitmap(
														BitmapFactory.decodeStream((InputStream) new URL(thumbnailUrl).getContent())
												,96,96, true));
							        } catch (Exception e){
							        	Log.e("ContentListingBuilder", e.getMessage());
							        }
								}
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
				}
		    	if(didl.getContainers() != null && didl.getContainers().size() > 0){
			
					for(Container item : didl.getContainers()) {
						buildFlatSet(service, item.getId());						
					}
				}
		    }
		    
		    @Override
		    public void updateStatus(Status status) {
		        
		    }
		
		    @Override
		    public void failure(ActionInvocation invocation,
		                        UpnpResponse operation,
		                        String defaultMsg) {
		    }
		};
		upnpHandler.getUPnPService().getControlPoint().execute(b);
	}


	// TODO Move to util class...
	private Long parse(String durStr) {
		if(durStr == null || durStr.length() < 3) {
			return 0L;
		}
		if(durStr.indexOf(".") > -1) {
			durStr = durStr.substring(0, durStr.indexOf("."));
		}
		String[] parts = durStr.split(":");
		Integer seconds = Integer.parseInt(parts[parts.length - 1]);
		Integer minutes = 0;
		Integer hours = 0;
		if(parts.length - 2 >= 0) {
			 minutes = Integer.parseInt(parts[parts.length - 2]);	
		}
		if(parts.length - 3 >= 0) {
			 hours = Integer.parseInt(parts[parts.length - 3]);	
		}
		Long duration = hours*60L*60L + minutes*60L + seconds;
		return duration*1000L;
	}



	
}
