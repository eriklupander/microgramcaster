package com.squeed.microgramcaster.server;

import com.squeed.microgramcaster.MediaStoreAdapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Derived from http://www.integratingstuff.com/2011/10/24/adding-a-webserver-to-an-android-app/
 * 
 * 
 * @author Erik
 *
 */
public class WebServerService extends Service {

	private WebServer server = null;

	@Override
	public void onCreate() {
		try {
			super.onCreate();
			server = new WebServer(this, new MediaStoreAdapter());
			server.startServer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		server.stopServer();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
