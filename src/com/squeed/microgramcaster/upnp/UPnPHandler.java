package com.squeed.microgramcaster.upnp;

import java.util.ArrayList;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.RouterException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squeed.microgramcaster.MainActivity;
import com.squeed.microgramcaster.R;


public class UPnPHandler {
	 private BrowseRegistryListener registryListener = new BrowseRegistryListener();

	    private AndroidUpnpService upnpService;

		private MainActivity activity;

		private static final String TAG = "UPnPHandler";
	    
	    public UPnPHandler(MainActivity activity) {
			this.activity = activity;
	    }

	    private ServiceConnection serviceConnection = new ServiceConnection() {

	        public void onServiceConnected(ComponentName className, IBinder service) {
	            upnpService = (AndroidUpnpService) service;

	            // Clear the list
	           // listAdapter.clear();

	            // Get ready for future device advertisements
	            upnpService.getRegistry().addListener(registryListener);

	            // Now add all devices to the list we already know about
	            for (Device device : upnpService.getRegistry().getDevices()) {
	                registryListener.deviceAdded(device);
	            }
	            arrayAdapter.notifyDataSetChanged();

	            // Search asynchronously for all devices, they will respond soon
	            upnpService.getControlPoint().search();
	        }

	        public void onServiceDisconnected(ComponentName className) {
	            upnpService = null;
	        }
	    };

		private UPnPDeviceAdapter arrayAdapter;

		private ContentListingBuilder dlnaContentListingBuilder;
	    
	    
	    protected class BrowseRegistryListener extends DefaultRegistryListener {


			/* Discovery performance optimization for very slow Android devices! */
//	        @Override
//	        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
//	            deviceAdded(device);
//	        }
//
//	        @Override
//	        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
//	            deviceRemoved(device);
//	        }
	        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

	        @Override
	        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
	            deviceAdded(device);
	        }

	        @Override
	        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
	            deviceRemoved(device);
	        }

	        @Override
	        public void localDeviceAdded(Registry registry, LocalDevice device) {
	            deviceAdded(device);
	        }

	        @Override
	        public void localDeviceRemoved(Registry registry, LocalDevice device) {
	            deviceRemoved(device);
	        }

	        public void deviceAdded(final Device device) {
	        	activity.runOnUiThread(new Runnable() {
	                public void run() {
	                    DeviceDisplay d = new DeviceDisplay(device);
	                    int position = arrayAdapter.getPosition(d);
	                    if (position >= 0) {
	                        // Device already in the list, re-set new value at same position
	                    	arrayAdapter.remove(d);
	                    	arrayAdapter.insert(d, position);
	                    } else {
	                    	arrayAdapter.add(d);
	                    }
	                    arrayAdapter.notifyDataSetChanged();
	                    Log.i(TAG, "Added UPNP device: " + d.toString() +  "\n\n" + d.getDetailsMessage());
	                }
	            });
	        }

	        public void deviceRemoved(final Device device) {
	        	activity.runOnUiThread(new Runnable() {
	                public void run() {
	                    //listAdapter.remove(new DeviceDisplay(device));
	                	Log.i("UPnPHandler", "Removed UPNP device: " + device.getDisplayString());
	                }
	            });
	        }
	    }

	   
	    public void initUPnpService() {
	    	arrayAdapter = new UPnPDeviceAdapter(activity, R.layout.upnp_device_listview_item, new ArrayList<DeviceDisplay>());
			// This will start the UPnP service if it wasn't already started
	    	activity.getApplicationContext().bindService(
	            new Intent(activity, BrowserUpnpService.class),
	            serviceConnection,
	            Context.BIND_AUTO_CREATE
	        );
	        
	        dlnaContentListingBuilder = new ContentListingBuilder(activity, this);
		}
	    
	    public void destroyUPnpService() {
	    	
	    	if (upnpService != null) {
	    		upnpService.getRegistry().removeListener(registryListener);	    		
	    	}
	        // This will stop the UPnP service if nobody else is bound to it
	    	activity.getApplicationContext().unbindService(serviceConnection);	    	
	    }
	    
	    public void searchUPnp() {
	    	arrayAdapter.clear();
	    	arrayAdapter.notifyDataSetChanged();
	    	
	    	if (upnpService != null) {
	    		upnpService.getRegistry().removeAllRemoteDevices();
	            upnpService.getControlPoint().search();
	    	} else {
	    		// Probably unnecessary...
	    		// Toast.makeText(activity, "UPnpService not initialized.", Toast.LENGTH_LONG).show();
	    	}
	    	showUPnPDeviceListDialog();
	    }

	    
	    Dialog upnpDeviceDialog;

		protected RemoteService currentService;

		
	    
	    private void showUPnPDeviceListDialog() {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	    	builder.setTitle("Select Media Source");
	    	//builder.setMessage("UPnP scan may take a few moments...");
	    	LinearLayout layout = new LinearLayout(activity);
	    	layout.setOrientation(LinearLayout.VERTICAL);
	    	
	    	ListView upnpDeviceList = new ListView(activity);
	    	
	    	
	    	upnpDeviceList.setAdapter(arrayAdapter);
	    	upnpDeviceList.setOnItemClickListener(new OnItemClickListener() {
				
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					
					Device unspecDevice = arrayAdapter.getItem(position).getDevice();
					if(unspecDevice instanceof RemoteDevice) {
						RemoteDevice currentDevice = (RemoteDevice) unspecDevice; 
						for (RemoteService service : currentDevice.getServices()) {
			                if (service.getServiceType().getType().equals("ContentDirectory")) {
			                	currentService = service;	
			                	
			                	dlnaContentListingBuilder.startBuildDLNAContentListing();
			                }
			            }
					}
					
					upnpDeviceDialog.dismiss();
				}
			});
	    	
	    	TextView localText = new TextView(activity);
	    	localText.setPadding(4, 4, 4, 0);
	    	localText.setTextSize(16);
	    	localText.setTypeface(null, Typeface.BOLD);
	    	localText.setText("Local file system");
	    	OnClickListener cl = new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					upnpDeviceDialog.dismiss();
					activity.listVideoFiles();
				}
			};
	    	localText.setOnClickListener(cl);
	    	TextView underText = new TextView(activity);
	    	underText.setPadding(4, 0, 4, 0);
	    	underText.setTextSize(14);
	    	underText.setText("Browse files on your local file system");
	    	underText.setOnClickListener(cl);
	    	
	    	layout.addView(localText);
	    	layout.addView(underText);
	    	layout.addView(upnpDeviceList);
	    	
	    	builder.setView(layout);
	    	upnpDeviceDialog = builder.create();

	    	upnpDeviceDialog.show();
	    }
	    

		public Service getCurrentService() {
			return currentService;
		}

		public AndroidUpnpService getUPnPService() {
			return upnpService;
		}

		public void buildContentListing() {
			dlnaContentListingBuilder.startBuildDLNAContentListing();
		}
}
