package com.squeed.microgramcaster.channel;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.squeed.microgramcaster.MainActivity;

public class MicrogramCasterChannel implements MessageReceivedCallback {
	
	public static final String PROTOCOL = "urn:x-cast:com.squeed.microgramcaster";
	public static final String TAG = "MicrogramCasterChannel";
	
	private MainActivity activity;

	public MicrogramCasterChannel(MainActivity activity) {
		this.activity = activity;		
	}

	/**
	 * @return custom namespace
	 */
	public String getNamespace() {
		return PROTOCOL;
	}

	/*
	 * Receive message from the receiver app
	 */
	@Override
	public void onMessageReceived(CastDevice castDevice, String namespace,
			String message) {
		Log.d(TAG, "onMessageReceived: " + message);
		
		if(!namespace.equalsIgnoreCase(PROTOCOL)) {
			Log.i(TAG, "Discarded message from unknown namespace: " + namespace);
		}
		try {
			JSONObject msg = new JSONObject(message);
			String msgType = msg.getString(ChannelDef.TYPE);
			if(msgType.equalsIgnoreCase(ChannelDef.EVENT_TYPE)) {
				EventDef evt = EventDef.valueOf(msg.getString(ChannelDef.EVENT_ID));
				switch(evt) {
				case EVENT_PLAYING:
					activity.onEventPlaying(msg.getInt(ChannelDef.PARAM_POSITION_SECONDS));
					break;
				case EVENT_PAUSED:
					activity.onEventPaused(msg.getInt(ChannelDef.PARAM_POSITION_SECONDS));
					break;
				case EVENT_FINISHED:
					activity.onEventFinished();
					break;
				default:
					break;
				}
			}
			
			else if(msgType.equalsIgnoreCase(ChannelDef.RESPONSE_TYPE)) {
				ResponseDef rsp = ResponseDef.valueOf(msg.getString(ChannelDef.RESPONSE_ID));
				switch(rsp) {
				case REQUESTED_POSITION:
					activity.onRequestedPosition(msg.getInt(ChannelDef.PARAM_POSITION_SECONDS));
					break;
				}
			}
			
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
}
