package com.squeed.microgramcaster;

import org.json.JSONObject;

import android.util.Log;

import com.google.cast.MessageStream;

/**
 * An abstract class which encapsulates control for sending and receiving messages to/from the receiver app.
 */
public abstract class MicrogramCasterMessageStream extends MessageStream {
	private static final String TAG = MicrogramCasterMessageStream.class.getSimpleName();

	private static final String NAMESPACE = "urn:x-cast:com.squeed.microgramcaster";
	
	private static final String KEY_COMMAND = "command";



	/**
	 * Constructs a new HipstaCasterMessageStream with HIPSTACASTER_NAMESPACE as the namespace
	 * used by the superclass.
	 */
	protected MicrogramCasterMessageStream() {
		super(NAMESPACE);
	}

	@Override
	public void onMessageReceived(JSONObject message) {
		//try {
			Log.d(TAG, "onMessageReceived: " + message);
			
		//} catch (JSONException e) {
		//	Log.w(TAG, "Message doesn't contain an expected key.", e);
		//}
	}	
}
