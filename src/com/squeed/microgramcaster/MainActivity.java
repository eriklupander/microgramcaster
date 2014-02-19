package com.squeed.microgramcaster;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;

import com.google.cast.ApplicationChannel;
import com.google.cast.ApplicationMetadata;
import com.google.cast.ApplicationSession;
import com.google.cast.CastContext;
import com.google.cast.CastDevice;
import com.google.cast.MediaRouteAdapter;
import com.google.cast.MediaRouteHelper;
import com.google.cast.MediaRouteStateChangeListener;
import com.google.cast.SessionError;

import com.squeed.microgramcaster.server.WebServerService;

public class MainActivity extends ActionBarActivity implements MediaRouteAdapter  {

    private static final String TAG = "MainActivity";
	
	private static final String APP_NAME = "fc91668a-cf4b-4a18-9611-f2c120d0bf07_1";
    private static final String PROTOCOL = "com.squeed.microgramcaster";
	
	private CastContext mCastContext;
    private CastDevice mSelectedDevice;
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
	private ApplicationSession mSession;
    private SessionListener mSessionListener;
	
	

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startWebServer();
        initMediaRouter() ;
        listVideoFiles();
    }
	
	private void initMediaRouter() {
		mSessionListener = new SessionListener();
        mMessageStream = new CustomHipstaCasterStream();

        mCastContext = new CastContext(getApplicationContext());
        MediaRouteHelper.registerMinimalMediaRouteProvider(mCastContext, this);
        
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = MediaRouteHelper.buildMediaRouteSelector(
                MediaRouteHelper.CATEGORY_CAST, APP_NAME, null);
        mMediaRouterCallback = new MediaRouterCallback();
	}


	private void listVideoFiles() {
		MediaStoreAdapter mediaStoreAdapter = new MediaStoreAdapter();
        List<String> mp4Files = mediaStoreAdapter.findFiles(this, "%");
        ListView listView = (ListView) findViewById(R.id.videoFiles);
        ListAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mp4Files);
        listView.setAdapter(listAdapter);
	}


	private void startWebServer() {
		Intent webServerService = new Intent(this, WebServerService.class);
        this.startService(webServerService);
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onDestroy() {
    	Intent webServerService = new Intent(this, WebServerService.class);
        this.stopService(webServerService);
        Log.i(TAG, "Stopped web server");
    }
	
	
	
	
	/**
     * An extension of the MessageStream with some local details.
     */
    private class CustomMicrogramCasterStream extends MicrogramCasterMessageStream {

        /**
         * Displays an error dialog.
         */
        @Override
        protected void onError(String errorMessage) {
        	//buildAlertDialog("Error", errorMessage);
        }

        /**
         * Displays a message that the slideshow has ended.
         */
		@Override
		protected void onSlideShowEnded() {
			//buildAlertDialog("Message from ChromeCast", "Slideshow has ended");
		}

		/**
		 * Updates the textView with the currently viewed photo from the slideshoe (n of m)
		 */
		@Override
		protected void onCurrentSlideShowImageMessage(String message) {
			//mInfoView.setText(message);
		}		
    }
    
    
    


    /**
     * An extension of the MediaRoute.Callback so we can invoke our own onRoute selected/unselected
     */
    private class MediaRouterCallback extends MediaRouter.Callback {    	

		@Override
        public void onRouteSelected(MediaRouter router, android.support.v7.media.MediaRouter.RouteInfo route) {
            Log.i(TAG, "onRouteSelected: " + route);
            MainActivity.this.onRouteSelected(route);
        }

        @Override
        public void onRouteUnselected(MediaRouter router, android.support.v7.media.MediaRouter.RouteInfo route) {
        	Log.i(TAG, "onRouteUnselected: " + route);
            MainActivity.this.onRouteUnselected(route);
        }
    }

    @Override
    public void onDeviceAvailable(CastDevice device, String routeId,
                                  MediaRouteStateChangeListener listener) {
       // sLog.d("onDeviceAvailable: %s (route %s)", device, routeId);
        setSelectedDevice(device);
    }

	
	private boolean isCastSessionActive() {
		return mSession != null && mSession.hasChannel() && mSession.hasStarted();
	}	
    
}
