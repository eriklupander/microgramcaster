package com.squeed.microgramcaster.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.squeed.microgramcaster.MediaStoreAdapter;
import com.squeed.microgramcaster.util.WifiHelper;

/**
 * Derived from http://www.integratingstuff.com/2011/10/24/adding-a-webserver-to-an-android-app/
 *  
 * @author Erik
 *
 */
public class WebServerService extends Service {

	private MyHTTPD server = null;

	@Override
	public void onCreate() {
		try {
			super.onCreate();
			server = new MyHTTPD(WifiHelper.getLanIP(this), MyHTTPD.WEB_SERVER_PORT, this, new MediaStoreAdapter());
			server.start();
		} catch (Exception e) {
			Log.e("WebServerService", "Error starting MyHTTPD server: " + e.getMessage());
		}
	}

	@Override
	public void onDestroy() {
		server.stop();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
