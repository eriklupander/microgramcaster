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

import com.squeed.microgramcaster.server.WebServerService;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startWebServer();
        
        listVideoFiles();
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
    
}
