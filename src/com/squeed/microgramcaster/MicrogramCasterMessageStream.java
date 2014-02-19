package com.squeed.microgramcaster;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.cast.MessageStream;

/**
 * An abstract class which encapsulates control for sending and receiving messages to/from the receiver app.
 * 
 * Derived from the tic-tac-tie example at: https://github.com/googlecast/cast-android-tictactoe
 */
public abstract class MicrogramCasterMessageStream extends MessageStream {
	private static final String TAG = MicrogramCasterMessageStream.class.getSimpleName();

	private static final String NAMESPACE = "com.squeed.microgramcaster";

	// Receivable event types
	private static final String KEY_EVENT = "event";
	private static final String KEY_ERROR = "error";
	private static final String KEY_MESSAGE = "message";
	
	private static final String KEY_COMMAND = "command";
	
	private static final String KEY_TEXT = "text";
	private static final String KEY_SLIDESHOW_ENDED = "slideshow_ended";
	private static final String KEY_SLIDESHOW_CURRENT_IMAGE_MSG = "slideshow_current_image";


	/**
	 * Constructs a new HipstaCasterMessageStream with HIPSTACASTER_NAMESPACE as the namespace
	 * used by the superclass.
	 */
	protected MicrogramCasterMessageStream() {
		super(NAMESPACE);
	}

	protected abstract void onSlideShowEnded();
	protected abstract void onCurrentSlideShowImageMessage(String message);
	protected abstract void onError(String errorMessage);
	
	/**
	 * Sends image data for a single image to the cast device
	 * @param title
	 * 		Title of the photo.
	 * @param url
	 * 		The URL from where to load the actual image.
	 * @param ownerName
	 * 		Real name or username of the owner of the photo, as provided by the 3rd party photo service. (E.g. flickr)
	 * @param description
	 * 		Textual description of the photo. If null, the receiver app should handle that.
	 */
	public final void openPhotoOnChromecast(String title, String url, String ownerName, String description) {
        try {
            Log.d(TAG, "openPhotoOnChromecast: " + url);
            JSONObject payload = new JSONObject();
            payload.put(KEY_COMMAND, "viewphoto");
            payload.put("fullsizeUrl", url);
            payload.put("ownerName", ownerName);
            payload.put("title", title);
            payload.put("description", description);
           
            sendMessage(payload);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot parse or serialize data for openPhotoOnChromecast", e);
        } catch (IOException e) {
            Log.e(TAG, "Unable to send openPhotoOnChromecast message", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Message Stream is not attached", e);
        }
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
