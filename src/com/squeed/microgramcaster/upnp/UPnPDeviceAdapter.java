package com.squeed.microgramcaster.upnp;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squeed.microgramcaster.R;

public class UPnPDeviceAdapter extends ArrayAdapter<DeviceDisplay> {

	private int resource;

	public UPnPDeviceAdapter(Context context, int resource, List<DeviceDisplay> items) {
		super(context, resource, items);
		this.resource = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout alertView;
		DeviceDisplay al = getItem(position);

		// Inflate the view
		if (convertView == null) {
			alertView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi;
			vi = (LayoutInflater) getContext().getSystemService(inflater);
			vi.inflate(resource, alertView, true);
		} else {
			alertView = (LinearLayout) convertView;
		}

		TextView title = 	(TextView) alertView.findViewById(R.id.upnp_device_title);
		TextView subTitle = (TextView) alertView.findViewById(R.id.upnp_device_subtitle);

		title.setText(al.toString());
		subTitle.setText(al.getDevice().getDisplayString());
		alertView.setTag(al.getDevice().getIdentity().getUdn());

		return alertView;
	}

}
