package com.squeed.microgramcaster;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.transition.Visibility;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.cast.JsonUtils;
import com.squeed.microgramcaster.media.IsoFileUtil;
import com.squeed.microgramcaster.media.MediaItem;
import com.squeed.microgramcaster.media.MediaStoreAdapter;
import com.squeed.microgramcaster.server.MyHTTPD;
import com.squeed.microgramcaster.server.WebServerService;
import com.squeed.microgramcaster.util.WifiHelper;

/**
 * Start Activity for the MicrogramCaster Android app.
 * 
 * Lists castable files, starts the HTTP server through a Service Intent and provides the Google Cast
 * plumbing.
 * 
 * Derived from Google's android-helloworld examples at github.com (TODO add full URL)
 * 
 * @author Erik
 *
 */
public class MainActivity extends ActionBarActivity {

	private static final String TAG = "MainActivity";

	private static final String APP_NAME = "4E4599F7";
	private static final String PROTOCOL = "urn:x-cast:com.squeed.microgramcaster";

	private CastDevice mSelectedDevice;
	private MediaRouter mMediaRouter;
	private MediaRouteSelector mMediaRouteSelector;
	private MediaRouter.Callback mMediaRouterCallback;

	private GoogleApiClient mApiClient;
	private Cast.Listener mCastListener;
	private ConnectionCallbacks mConnectionCallbacks;
	private ConnectionFailedListener mConnectionFailedListener;

	private MicrogramCasterChannel mHelloWorldChannel;

	private boolean mApplicationStarted;
	private boolean mWaitingForReconnect;

	private MenuItem playIcon;
	private MenuItem pauseIcon;
	
	private SeekBar seekBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.squeed.microgramcaster.R.layout.activity_main);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(
				android.R.color.transparent));

		startWebServer();
		initMediaRouter();
		listVideoFiles();
		initSeekBar();
	}

	private void initSeekBar() {
		seekBar = (SeekBar) findViewById(R.id.seekBar1);
		seekBar.setVisibility(SeekBar.INVISIBLE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Start media router discovery
		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
				MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
			
	}

	@Override
	protected void onPause() {
		if (isFinishing()) {
			// End media router discovery
			mMediaRouter.removeCallback(mMediaRouterCallback);
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		teardown();
		super.onDestroy();
	}

	private void initMediaRouter() {
		mMediaRouter = MediaRouter.getInstance(getApplicationContext());
		mMediaRouteSelector = new MediaRouteSelector.Builder()
				.addControlCategory(
						CastMediaControlIntent.categoryForCast(APP_NAME))
				.build();
		mMediaRouterCallback = new MediaRouterCallback();
	}
	
	
	int currentSeekbarPosition = 0;
	Handler seekBarHandler = new Handler();
	Runnable run = new Runnable() {
		 
        @Override
        public void run() {
            updateSeekBar();
        }
    };
 
    public void updateSeekBar() {
 
        seekBar.setProgress(currentSeekbarPosition++);
        seekBarHandler.postDelayed(run, 1000);
    }
 

	
	private void listVideoFiles() {
		final MediaStoreAdapter mediaStoreAdapter = new MediaStoreAdapter();
		List<String> mp4Files = mediaStoreAdapter.findFiles(this, "%");
		ListView listView = (ListView) findViewById(com.squeed.microgramcaster.R.id.videoFiles);
		ListAdapter listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mp4Files);
		listView.setAdapter(listAdapter);	
		OnItemClickListener listener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				
				
				
				arg0.setSelected(true);
				String fileName = ((TextView)arg1).getText().toString();
				MediaItem mi = mediaStoreAdapter.findFile(MainActivity.this, fileName);
				Long durationMillis = mi.getDuration();
				seekBar.setVisibility(SeekBar.VISIBLE);				
				seekBar.setMax((int) (durationMillis/1000L));
				
				currentSeekbarPosition = 0;
				updateSeekBar();
				
				sendMessage(PlayerCmdFactory.buildPlayUrlCommand(buildMediaItemURL(fileName)));
				pauseIcon.setVisible(true);
			}
		};
		listView.setOnItemClickListener(listener);
		OnItemLongClickListener lcListener = new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String fileName = ((TextView)arg1).getText().toString();
				FileChannel fileChannel = mediaStoreAdapter.getFileChannel(MainActivity.this, fileName);
				String txt =  IsoFileUtil.getInfo(fileChannel);
				Log.i(TAG, txt);
				Toast.makeText(MainActivity.this, txt, Toast.LENGTH_LONG).show();
				return true;
			}
		};
		listView.setOnItemLongClickListener(lcListener);
	}

	

	private void startWebServer() {
		Intent webServerService = new Intent(this, WebServerService.class);
		this.startService(webServerService);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(com.squeed.microgramcaster.R.menu.menu, menu);
		MenuItem mediaRouteMenuItem = menu
				.findItem(com.squeed.microgramcaster.R.id.media_route_menu_item);
		MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat
				.getActionProvider(mediaRouteMenuItem);
		
		
		// Set the MediaRouteActionProvider selector for device discovery.
		if(mediaRouteActionProvider != null) mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
		
		
		
		
		MenuItem refreshIcon = menu.findItem(com.squeed.microgramcaster.R.id.action_refresh);
		refreshIcon.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				listVideoFiles();
				return false;
			}
		});
		
		playIcon = menu
				.findItem(com.squeed.microgramcaster.R.id.action_play);
		playIcon.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				sendMessage(PlayerCmdFactory.buildPlayCommand());
				playIcon.setVisible(false);
				pauseIcon.setVisible(true);
				return false;
			}
		});
		pauseIcon = menu
				.findItem(com.squeed.microgramcaster.R.id.action_pause);
		pauseIcon.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				sendMessage(PlayerCmdFactory.buildPauseCommand());
				playIcon.setVisible(true);
				pauseIcon.setVisible(false);
				return false;
			}
		});
		playIcon.setVisible(false);
		pauseIcon.setVisible(false);
		return true;
	}

	/**
	 * An extension of the MediaRoute.Callback so we can invoke our own onRoute
	 * selected/unselected
	 */
	private class MediaRouterCallback extends MediaRouter.Callback {

		@Override
		public void onRouteSelected(MediaRouter router,
				android.support.v7.media.MediaRouter.RouteInfo route) {
			Log.i(TAG, "onRouteSelected: " + route);
			mSelectedDevice = CastDevice.getFromBundle(route.getExtras());
			launchReceiver();
		}

		@Override
		public void onRouteUnselected(MediaRouter router,
				android.support.v7.media.MediaRouter.RouteInfo route) {
			Log.i(TAG, "onRouteUnselected: " + route);
			teardown();
			mSelectedDevice = null;
			playIcon.setVisible(false);
			pauseIcon.setVisible(false);
		}
	}

	/**
	 * Start the receiver app
	 */
	private void launchReceiver() {
		try {
			mCastListener = new Cast.Listener() {

				@Override
				public void onApplicationDisconnected(int errorCode) {
					Log.d(TAG, "application has stopped");
					teardown();
				}

			};
			// Connect to Google Play services
			mConnectionCallbacks = new ConnectionCallbacks();
			
			mConnectionFailedListener = new ConnectionFailedListener();
			Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
					.builder(mSelectedDevice, mCastListener);
			mApiClient = new GoogleApiClient.Builder(this)
					.addApi(Cast.API, apiOptionsBuilder.build())
					.addConnectionCallbacks(mConnectionCallbacks)
					.addOnConnectionFailedListener(mConnectionFailedListener)
					.build();

			mApiClient.connect();
		} catch (Exception e) {
			Log.e(TAG, "Failed launchReceiver", e);
		}
	}

	/**
	 * Tear down the connection to the receiver
	 */
	private void teardown() {
		if (mApiClient != null) {
			if (mApplicationStarted) {
				try {
					Cast.CastApi.stopApplication(mApiClient);
					if (mHelloWorldChannel != null) {
						Cast.CastApi.removeMessageReceivedCallbacks(mApiClient,
								mHelloWorldChannel.getNamespace());
						mHelloWorldChannel = null;
					}
				} catch (IOException e) {
					Log.e(TAG, "Exception while removing channel", e);
				}
				mApplicationStarted = false;
			}
			if (mApiClient.isConnected()) {
				mApiClient.disconnect();
			}
			mApiClient = null;
		}
		mSelectedDevice = null;
		mWaitingForReconnect = false;
		playIcon.setVisible(false);
		pauseIcon.setVisible(false);
	}

	/**
	 * Google Play services callbacks
	 */
	private class ConnectionCallbacks implements
			GoogleApiClient.ConnectionCallbacks {
		@Override
		public void onConnected(Bundle connectionHint) {
			Log.d(TAG, "onConnected");

			if (mApiClient == null) {
				// We got disconnected while this runnable was pending
				// execution.
				return;
			}

			try {
				if (mWaitingForReconnect) {
					mWaitingForReconnect = false;

					// Check if the receiver app is still running
					if ((connectionHint != null)
							&& connectionHint
									.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
						Log.d(TAG, "App is no longer running");
						teardown();
					} else {
						// Re-create the custom message channel
						try {
							Cast.CastApi.setMessageReceivedCallbacks(
									mApiClient,
									mHelloWorldChannel.getNamespace(),
									mHelloWorldChannel);
						} catch (IOException e) {
							Log.e(TAG, "Exception while creating channel", e);
						}
					}
				} else {
					// Launch the receiver app
					Cast.CastApi
							.launchApplication(mApiClient,
									APP_NAME, false)
							.setResultCallback(
									new ResultCallback<Cast.ApplicationConnectionResult>() {
										@Override
										public void onResult(
												ApplicationConnectionResult result) {
											Status status = result.getStatus();
											Log.d(TAG,
													"ApplicationConnectionResultCallback.onResult: statusCode"
															+ status.getStatusCode());
											if (status.isSuccess()) {
												ApplicationMetadata applicationMetadata = result
														.getApplicationMetadata();
												String sessionId = result
														.getSessionId();
												String applicationStatus = result
														.getApplicationStatus();
												boolean wasLaunched = result
														.getWasLaunched();
												Log.d(TAG,
														"application name: "
																+ applicationMetadata
																		.getName()
																+ ", status: "
																+ applicationStatus
																+ ", sessionId: "
																+ sessionId
																+ ", wasLaunched: "
																+ wasLaunched);
												mApplicationStarted = true;

												// Create the custom message
												// channel
												mHelloWorldChannel = new MicrogramCasterChannel();
												try {
													Cast.CastApi
															.setMessageReceivedCallbacks(
																	mApiClient,
																	mHelloWorldChannel
																			.getNamespace(),
																	mHelloWorldChannel);
												} catch (IOException e) {
													Log.e(TAG,
															"Exception while creating channel",
															e);
												}

												// set the initial instructions
												// on the receiver
												//sendMessage(getString(R.string.app_name));
											} else {
												Log.e(TAG,
														"application could not launch");
												teardown();
											}
										}
									});
				}
			} catch (Exception e) {
				Log.e(TAG, "Failed to launch application", e);
			}
		}

		@Override
		public void onConnectionSuspended(int cause) {
			Log.d(TAG, "onConnectionSuspended");
			mWaitingForReconnect = true;
			playIcon.setVisible(false);
			pauseIcon.setVisible(false);
		}
	}
	
	private void sendMessage(PlayerCmd cmd) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("id", cmd.getId());
			obj.put("params", new JSONObject(cmd.getParams()));
      
			if (mApiClient != null && mHelloWorldChannel != null) {
			    try {
			        Cast.CastApi.sendMessage(mApiClient,
			                mHelloWorldChannel.getNamespace(), obj.toString());
			    } catch (Exception e) {
			        Log.e(TAG, "Exception while sending message", e);
			    }
			} else {
			    Toast.makeText(MainActivity.this, "Unable to send CMD to receiver, no connection", Toast.LENGTH_SHORT)
			    .show();
			    launchReceiver();
			 }
		} catch (JSONException e) {
			Toast.makeText(MainActivity.this, "Unable to serialize CMD into JSON: " + e.getMessage(), Toast.LENGTH_LONG)
		    .show();
		}
	}
	
	/**
	* Send a text message to the receiver
	*
	* @param message
	*/
    private void sendMessage(String message) {
        if (mApiClient != null && mHelloWorldChannel != null) {
            try {
                Cast.CastApi.sendMessage(mApiClient,
                        mHelloWorldChannel.getNamespace(), message)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status result) {
                                if (!result.isSuccess()) {
                                    Log.e(TAG, "Sending message failed");
                                }
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message", e);
            }
        } else {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT)
                    .show();
        }
    }

	private String buildMediaItemURL(String fileName) {
		return MyHTTPD.WEB_SERVER_PROTOCOL + "://" + WifiHelper.getLanIP(MainActivity.this) + ":" + MyHTTPD.WEB_SERVER_PORT + "/" + fileName;
	}

	/**
	 * Google Play services callbacks
	 */
	private class ConnectionFailedListener implements
			GoogleApiClient.OnConnectionFailedListener {
		@Override
		public void onConnectionFailed(ConnectionResult result) {
			Log.e(TAG, "onConnectionFailed ");

			teardown();
		}
	}

	/**
	 * Custom message channel
	 */
	class MicrogramCasterChannel implements MessageReceivedCallback {

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
		}

	}

}
