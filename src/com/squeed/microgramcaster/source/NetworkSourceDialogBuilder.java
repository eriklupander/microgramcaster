package com.squeed.microgramcaster.source;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.squeed.microgramcaster.MainActivity;
import com.squeed.microgramcaster.R;

public class NetworkSourceDialogBuilder {
	
	private MainActivity activity;
	private Dialog networkSourceDialog;
	private NetworkSourceArrayAdapter arrayAdapter;
	
	

	public NetworkSourceDialogBuilder(MainActivity activity) {
		this.activity = activity;	
		this.arrayAdapter = new NetworkSourceArrayAdapter(activity, R.layout.upnp_device_listview_item, new ArrayList<NetworkSourceItem>());
	}
	
	public NetworkSourceArrayAdapter getAdapter() {
		return arrayAdapter;
	}
	
	public void showNetworkSourceDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    	builder.setTitle("UPnP scan may take a few moments...");
    	//builder.setMessage("UPnP scan may take a few moments...");
    	LinearLayout layout = new LinearLayout(activity);
    	layout.setOrientation(LinearLayout.VERTICAL);
    	
    	ListView sourceList = new ListView(activity);
    	
    	
    	sourceList.setAdapter(arrayAdapter );
    	sourceList.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				
				NetworkSourceItem item = arrayAdapter.getItem(position);
				
				switch(item.getType()) {
				case UPNP:
					activity.getUPnPHandler().handleNetworkSourceSelected(item);					
					break;
				case SMB:
					activity.getSambaExplorer().handleNetworkSourceSelected(item);
					break;
				}
				
				networkSourceDialog.dismiss();
			}
		});
    	
    	layout.addView(sourceList);
    	
    	
    	builder.setView(layout);
    	networkSourceDialog = builder.create();

    	networkSourceDialog.show();
	}
}
