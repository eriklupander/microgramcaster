package com.squeed.microgramcaster;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.squeed.microgramcaster.channel.Command;
import com.squeed.microgramcaster.channel.CommandFactory;
import com.squeed.microgramcaster.channel.MicrogramCasterChannel;
import com.squeed.microgramcaster.drawer.DrawerItem;
import com.squeed.microgramcaster.drawer.DrawerItemArrayAdapter;
import com.squeed.microgramcaster.drawer.DrawerItemFactory;
import com.squeed.microgramcaster.media.MediaItem;
import com.squeed.microgramcaster.media.MediaStoreAdapter;
import com.squeed.microgramcaster.server.MyHTTPD;
import com.squeed.microgramcaster.server.WebServerService;
import com.squeed.microgramcaster.smb.SambaExplorer;
import com.squeed.microgramcaster.smb.SmbReadFolderTask;
import com.squeed.microgramcaster.smb.SmbScannerTask;
import com.squeed.microgramcaster.source.NetworkSourceArrayAdapter;
import com.squeed.microgramcaster.source.NetworkSourceDialogBuilder;
import com.squeed.microgramcaster.upnp.UPnPHandler;
import com.squeed.microgramcaster.util.PathStack;
import com.squeed.microgramcaster.util.TimeFormatter;
import com.squeed.microgramcaster.util.TitleFormatter;
import com.squeed.microgramcaster.util.VideoTypes;
import com.squeed.microgramcaster.util.WifiHelper;

/**
 * Start Activity for the MicrogramCaster Android app.
 * 
 * Lists castable files, starts the HTTP server through a Service Intent and
 * provides the Google Cast plumbing.
 * 
 * Derived from Google's android-helloworld examples at github.com (TODO add
 * full URL)
 * 
 * @author Erik
 * 
 */
public class MainActivity extends ActionBarActivity {

	private static final String TAG = "MainActivity";

	private static final String APP_NAME = "4E4599F7";
	//private static final String APP_NAME_DEV = "210EE372";
	
	private CurrentSource currentSource = CurrentSource.LOCAL;

	private CastDevice mSelectedDevice;
	private MediaRouter mMediaRouter;
	private MediaRouteSelector mMediaRouteSelector;
	private MediaRouter.Callback mMediaRouterCallback;

	private GoogleApiClient mApiClient;
	private Cast.Listener mCastListener;
	private ConnectionCallbacks mConnectionCallbacks;
	private ConnectionFailedListener mConnectionFailedListener;

	private MicrogramCasterChannel mMicrogramCasterChannel;

	private boolean mApplicationStarted;
	private boolean mWaitingForReconnect;

	private MenuItem rotateIcon;
	private ImageView playIcon;
	private ImageView pauseIcon;
	private ImageView placeholderIcon;

	private SeekBar seekBar;
	private MediaItemArrayAdapter adapter;
	

	private TextView currentPosition;
	private TextView totalDuration;
	private TextView statusText;
	
	private ProgressDialog loadingDialog;
	private AlertDialog dialog;
	
	private SharedPreferences preferences;
	
	private MediaStoreAdapter mediaStoreAdapter;
	private UPnPHandler uPnPHandler;
	private SambaExplorer sambaExplorer;
	
	// Side-drawer stuff

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    
    private NetworkSourceDialogBuilder bldr;

	private ActionBarDrawerToggle mDrawerToggle;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.squeed.microgramcaster.R.layout.activity_main);
		setTitle("");
		
		initImageLoader();
		initDialogs();
		initSourceDialogAndUPnP();
		initDrawer();
		startWebServer();
		initMediaRouter();
		listVideoFiles();
		initMediaControlButtons();
		initSeekBar();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
	}

	

	private void initDrawer() {		
		
		//categories = getResources().getStringArray(R.array.src_categories);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_navigation_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
                ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle("");
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle("");
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(mDrawerToggle);
		drawerList = (ListView) findViewById(R.id.left_drawer);
		drawerList.setAdapter(new DrawerItemArrayAdapter(this,
                R.layout.navigation_drawer_item, (ArrayList<DrawerItem>) DrawerItemFactory.buildDrawerItems(this)));
        // Set the list's click listener
		drawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				resetMediaControlsOnRescan();
				
				switch(position) {
				case 0:
					currentSource = CurrentSource.LOCAL;				
					listVideoFiles();
					break;
				case 1:
					currentSource = CurrentSource.UPNP;
					uPnPHandler.searchUPnp();
					break;
				case 2:
					currentSource = CurrentSource.SMB;
					if(sambaExplorer == null) {
						sambaExplorer = new SambaExplorer(MainActivity.this);
					}
					adapter.clear();
					adapter.setSelectedPosition(-1);
					adapter.notifyDataSetChanged();
					new SmbScannerTask(MainActivity.this).execute();
					break;
				}
				drawerList.setItemChecked(position, true);
				drawerLayout.closeDrawers();
			}
		});

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
	}



	private void initSourceDialogAndUPnP() {
		bldr = new NetworkSourceDialogBuilder(MainActivity.this);
		if(uPnPHandler == null) {
			initUPnP();
		}
	}



	private void initImageLoader() {

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) 
			.build();
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
			.defaultDisplayImageOptions(defaultOptions)
			.build();
		
		ImageLoader.getInstance().init(config);
	}



	private void initUPnP() {
		uPnPHandler = new UPnPHandler(this);
		uPnPHandler.initUPnpService();			
	}



	private void initDialogs() {
		this.loadingDialog = new ProgressDialog(this);
		this.loadingDialog.setTitle("Loading");

		this.dialog = new AlertDialog.Builder(this).create();
		this.dialog.setTitle("Information");

		this.dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
	}

	private void startWebServer() {
		Intent webServerService = new Intent(this, WebServerService.class);
		this.startService(webServerService);
	}

	private void initSeekBar() {
		currentPosition = (TextView) findViewById(R.id.currentPosition);
		totalDuration = (TextView) findViewById(R.id.totalDuration);
		seekBar = (SeekBar) findViewById(R.id.seekBar1);
		seekBar.setEnabled(false);

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				currentPosition.setText(TimeFormatter.formatTime(progress));
			}

			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				seekBarHandler.removeCallbacksAndMessages(null);
				playIcon.setVisibility(View.GONE);
				pauseIcon.setVisibility(View.GONE);
				placeholderIcon.setVisibility(View.VISIBLE);
				statusText.setText("Seeking " + TimeFormatter.formatTime(seekBar.getProgress()));
				sendMessage(CommandFactory.buildSeekPositionCommand(seekBar.getProgress()));
			}

		});
		
		statusText = (TextView) findViewById(R.id.statusText);
	}

	private void showSeekbar() {
		seekBar.setEnabled(true);
		seekBar.setProgress(currentSeekbarPosition);
		totalDuration.setVisibility(TextView.VISIBLE);
		currentPosition.setVisibility(TextView.VISIBLE);
	}

	private void hideSeekbar() {
		adapter.setSelectedPosition(-1);
		adapter.notifyDataSetChanged();
		seekBar.setProgress(0);
		seekBar.setEnabled(false);
		seekBarHandler.removeCallbacksAndMessages(null);
		totalDuration.setVisibility(TextView.INVISIBLE);
		currentPosition.setVisibility(TextView.INVISIBLE);
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
		dialog.dismiss();
		loadingDialog.dismiss();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		
		teardown();
		
		if(uPnPHandler != null) {
			uPnPHandler.destroyUPnpService();
		}			
		
		dialog.dismiss();
		loadingDialog.dismiss();
		
		super.onDestroy();
		
	}
	
	 @Override
	    protected void onPostCreate(Bundle savedInstanceState) {
	        super.onPostCreate(savedInstanceState);
	        mDrawerToggle.syncState();
	    }

	    @Override
	    public void onConfigurationChanged(Configuration newConfig) {
	        super.onConfigurationChanged(newConfig);
	        mDrawerToggle.onConfigurationChanged(newConfig);
	    }

	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        // Pass the event to ActionBarDrawerToggle, if it returns
	        // true, then it has handled the app icon touch event
	        if (mDrawerToggle.onOptionsItemSelected(item)) {
	          return true;
	        }
	        return super.onOptionsItemSelected(item);
	    }


	private void initMediaRouter() {
		mMediaRouter = MediaRouter.getInstance(getApplicationContext());
		mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(
				CastMediaControlIntent.categoryForCast(APP_NAME)).build();
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

	private ListView listView;

	protected boolean landscape = true;

	private CurrentMediaItem currentMediaItem;

	public void updateSeekBar() {
		currentPosition.setVisibility(TextView.VISIBLE);
		seekBar.setProgress(currentSeekbarPosition++);
		seekBarHandler.postDelayed(run, 1000);
	}

	public void onEventPlaying(int positionSeconds, int totalDurationSeconds) {
		seekBar.setMax(totalDurationSeconds);
		totalDuration.setText(TimeFormatter.formatTime(totalDurationSeconds));
		currentSeekbarPosition = positionSeconds;
		seekBarHandler.removeCallbacksAndMessages(null);
		playIcon.setVisibility(View.GONE);
		pauseIcon.setVisibility(View.VISIBLE);
		placeholderIcon.setVisibility(View.GONE);
		rotateIcon.setVisible(true);

		loadingDialog.hide();
		
		// Send a position request directly as the sync between what the html5 player callbacks says and the actual
		// time when this callback is invoked differ by a few seconds for some reason. It's a bit like 'playing'
		// fires 2-3 seconds before the playback actually starts.
		sendMessage(CommandFactory.buildRequestPositionCommand());
		statusText.setVisibility(View.VISIBLE);
		if(currentMediaItem !=  null && mSelectedDevice != null) {
			statusText.setText("Currently playing '" + TitleFormatter.format(currentMediaItem.getName()) + "' on '" + mSelectedDevice.getFriendlyName() + "'");	
		}
		
	}

	public void onEventPaused(int positionSeconds, int totalDurationSeconds) {
		seekBar.setMax(totalDurationSeconds);
		currentSeekbarPosition = positionSeconds;
		seekBarHandler.removeCallbacksAndMessages(null);
		playIcon.setVisibility(View.VISIBLE);
		pauseIcon.setVisibility(View.GONE);
		placeholderIcon.setVisibility(View.GONE);
		rotateIcon.setVisible(true);
	}

	public void onEventFinished() {
		resetToLandscape();
		adapter.setSelectedPosition(-1);
		adapter.notifyDataSetChanged();
		currentSeekbarPosition = 0;
		seekBar.setProgress(0);
		loadingDialog.hide();
		seekBarHandler.removeCallbacksAndMessages(null);
		rotateIcon.setVisible(false);

		playIcon.setVisibility(View.VISIBLE);
		pauseIcon.setVisibility(View.GONE);
		placeholderIcon.setVisibility(View.GONE);
		statusText.setText("Playback has finished");
	}

	private void resetToLandscape() {
		landscape = true;
		rotateIcon.setIcon(R.drawable.ic_action_screen_rotation);
	}

	public void onRequestedPosition(int positionSeconds) {
		currentSeekbarPosition = positionSeconds;
		seekBarHandler.removeCallbacksAndMessages(null);
		updateSeekBar();
	}

	public void listVideoFiles() {
		
		mediaStoreAdapter = new MediaStoreAdapter();
		ArrayList<MediaItem> mediaFiles = new ArrayList<MediaItem>();
		listView = (ListView) findViewById(com.squeed.microgramcaster.R.id.videoFiles);
		OnItemClickListener listener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View listItemView, int position, long arg3) {
				if(((String) listItemView.getTag(R.id.type)).equals(Constants.DLNA_ITEM)) {
					playDlnaMedia(listItemView, position, false);
				} 
				else if(((String) listItemView.getTag(R.id.type)).equals(Constants.DLNA_FOLDER)) {
					uPnPHandler.buildContentListing((String) listItemView.getTag(R.id.dlna_url)); // dlna_url == containerId in this case
				} 
				else if(((String) listItemView.getTag(R.id.type)).equals(Constants.DLNA_BACK)) {
					adapter.setSelectedPosition(-1);
					uPnPHandler.handleUpPressed();
					uPnPHandler.buildContentListing((String) listItemView.getTag(R.id.dlna_url)); // dlna_url == containerId in this case
				} 
				else if(((String) listItemView.getTag(R.id.type)).equals(Constants.SMB_BACK)) {
					adapter.setSelectedPosition(-1);
					PathStack.popContainerIdStack();
					MainActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							adapter.clear();
							adapter.notifyDataSetChanged();
						}							
					});
					
					new SmbReadFolderTask(MainActivity.this).execute((String) listItemView.getTag(R.id.dlna_url));
					
				} 
				else if(((String) listItemView.getTag(R.id.type)).equals(Constants.SMB_FILE)) {					
					
					playSmbMedia(listItemView, position, false);
					
				} 
				else if(((String) listItemView.getTag(R.id.type)).equals(Constants.SMB_FOLDER)) {
					String folder = (String) listItemView.getTag(R.id.dlna_url);
					
						MainActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								adapter.clear();
								adapter.notifyDataSetChanged();
							}							
						});
						
						new SmbReadFolderTask(MainActivity.this).execute(folder);						
					
				} else {
					playLocalMedia(listItemView, position, false);	
				}				
			}			
		};
		listView.setOnItemClickListener(listener);

		
		adapter = new MediaItemArrayAdapter(this, R.layout.listview_item, mediaFiles);
		listView.setAdapter(adapter);
		
		try {
			boolean fileFound = mediaStoreAdapter.findFilesAsync(this, adapter);
			if(!fileFound) {
				dialog.setMessage("No .mp4 files found using the MediaStore API on your device. Please add a file " +
								  "to your local file system and try again by selecting local files in the navigation drawer.");
				dialog.show();
			}
		} catch (Throwable t) {
			dialog.setMessage("An unexpected problem occured loading media files from local storage. Please click the reload icon in the actionbar.");
			dialog.show();
			return;
		}		
	}
	
	/**
	 * The provided URL might be something like
	 * 
	 *  smb://STREAMWOLF2/StreamWolfShare/clips/bigbuckbunny720p.mp4
	 *  
	 *  which of course won't be playable directly for the Chromecast, we need our local http server to act
	 *  as proxy to the SMB share.
	 *  
	 *  Construct a URL that can be served with an alternate codepath:
	 *  
	 *  http://{ip.to.device]:[port]/smb/STREAMWOLF2/StreamWolfShare/clips/bigbuckbunny720p.mp4
	 *  
	 *  
	 *  
	 * @param listItemView
	 * @param position
	 */
	private void playSmbMedia(final View listItemView, int position, boolean force) {
		String url = (String) listItemView.getTag(R.id.dlna_url);
		if(!force && !VideoTypes.isPlayableVideo(url.toLowerCase())) {
			showPlayAnywayDialog(url.substring(url.lastIndexOf(".")), listItemView, position);
			return;
		}
		
		String name = (String) listItemView.getTag(R.id.dlna_name);
		Long duration = (Long) listItemView.getTag(R.id.dlna_duration); // We may need to read the duration using isofileparser?
		
		
		preparePlayMediaItem(name, duration != null && duration > -1 ? duration : -1L, position);
		
		String finalUrl = buildSmbItemURL(url);
		Log.i(TAG, "Built SMB proxy URL: " + finalUrl);
		
		Command cmd = CommandFactory.buildPlayUrlCommand(finalUrl, name, null, null);
		sendMessage(cmd);	
		currentMediaItem = new CurrentMediaItem(name, duration, position, cmd);
	}



	private void preparePlayMediaItem(String name, Long durationMs, int position) {
		if (mSelectedDevice == null || !mApiClient.isConnected()) {
			Toast.makeText(MainActivity.this, "No cast device selected", Toast.LENGTH_SHORT).show();
			adapter.setSelectedPosition(-1);
			adapter.notifyDataSetChanged();
			mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
					MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
			return;
		}
		
		adapter.setSelectedPosition(position);
		adapter.notifyDataSetChanged();

		currentSeekbarPosition = 0;
		if(durationMs > -1) {
			seekBar.setMax((int) (durationMs / 1000L));
			totalDuration.setText(TimeFormatter.formatTime((int) (durationMs / 1000L)));
		} else {
			totalDuration.setText("");
		}
		showSeekbar();
		currentPosition.setText(TimeFormatter.formatTime(0));		
		
		
		if (mApiClient.isConnected()) {

			playIcon.setVisibility(View.GONE);
			pauseIcon.setVisibility(View.GONE);

			placeholderIcon.setVisibility(View.VISIBLE);
			resetToLandscape();
			loadingDialog.setTitle("Loading");
			loadingDialog.setMessage("Loading '" + name +"'");
			loadingDialog.show();
		}	
	}

	private void playLocalMedia(View arg1, int position, boolean force) {
		
		String fileName = (String) arg1.getTag(R.id.name);
		MediaItem mi = mediaStoreAdapter.findFile(MainActivity.this, fileName);
		if(mi == null) {
			dialog.setMessage("'" + fileName + "' does not seem to exist on your device anymore, has the file been deleted? Please refresh the video list.");
			dialog.show();
			return;
		}

		if(!force &&!VideoTypes.isPlayableVideo(fileName.toLowerCase())) {
			showPlayAnywayDialog(fileName.substring(fileName.lastIndexOf(".")), arg1, position);
			return;
		}
		preparePlayMediaItem(mi.getName(), mi.getDuration(), position);
		Command cmd = CommandFactory.buildPlayUrlCommand(buildMediaItemURL(fileName), fileName, mi.getProducer(), buildLocalThumbURL(mi.getId().longValue()));
		sendMessage(cmd);		
		currentMediaItem = new CurrentMediaItem(mi.getName(), mi.getDuration(), position, cmd);
	}
	
	private void showPlayAnywayDialog(String suffix, final View listItemView, final int position) {
		// 1. Instantiate an AlertDialog.Builder with its constructor
		  AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

		  // 2. Chain together various setter methods to set the dialog characteristics
		  builder.setMessage("The Chromecast cannot play " + suffix + " files, do you want to try anyway? It probably won't work :(");
		  builder.setTitle("Unplayable video");
		  
		builder.setPositiveButton("Play anyway",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						playSmbMedia(listItemView, position, true);
					}
				});
		  builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	              
	           }
	       });

		  // 3. Get the AlertDialog from create()
		  AlertDialog dialog = builder.create();
		  dialog.show();
	}



	private void playDlnaMedia(View listItemView, int position, boolean force) {
		String url = (String) listItemView.getTag(R.id.dlna_url);

		if(!force && !VideoTypes.isPlayableVideo(url.toLowerCase())) {
			showPlayAnywayDialog(url.substring(url.lastIndexOf(".")), listItemView, position);
			return;
		}
		
		String name = (String) listItemView.getTag(R.id.dlna_name);
		Long duration = (Long) listItemView.getTag(R.id.dlna_duration);
		String producer = (String) listItemView.getTag(R.id.dlna_producer);
		String thumbnailUrl = (String) listItemView.getTag(R.id.dlna_thumbnail_url);
		preparePlayMediaItem(name, duration, position);
		
		String remoteThumbnailurl = buildRemoteThumbURL(thumbnailUrl);
		
		Command cmd = CommandFactory.buildPlayUrlCommand(url, name, producer, remoteThumbnailurl);
		sendMessage(cmd);	
		currentMediaItem = new CurrentMediaItem(name, duration, position, cmd);
	}
	
	private void initMediaControlButtons() {
		playIcon = (ImageView) findViewById(com.squeed.microgramcaster.R.id.action_play);
		playIcon.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				playIcon.setVisibility(View.GONE);
				pauseIcon.setVisibility(View.GONE);
				placeholderIcon.setVisibility(View.VISIBLE);
				
				// Check if we want to replay the last item (a bit ugly) or just resume play of the current one.
				if(currentMediaItem != null && seekBar.getProgress() == 0) {
					preparePlayMediaItem(currentMediaItem.getName(), currentMediaItem.getDuration(), currentMediaItem.getPosition());
					sendMessage(currentMediaItem.getPlayCommand());
				} else {
					sendMessage(CommandFactory.buildPlayCommand());
				}
				
			}
		});
		pauseIcon = (ImageView) findViewById(com.squeed.microgramcaster.R.id.action_pause);
		pauseIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				sendMessage(CommandFactory.buildPauseCommand());
				playIcon.setVisibility(View.VISIBLE);
				pauseIcon.setVisibility(View.GONE);
				placeholderIcon.setVisibility(View.GONE);
			}
		});
		placeholderIcon = (ImageView) findViewById(com.squeed.microgramcaster.R.id.action_placeholder);
		placeholderIcon.setVisibility(View.INVISIBLE);
		hideMediaControlIcons();
	}
	
	/**
	 * Resets the play/pause buttons when searching for new media sources - if no clip is currently playing.
	 */
	private void resetMediaControlsOnRescan() {
		if(currentMediaItem == null || seekBar.getProgress() == 0) {
			currentMediaItem = null;
			playIcon.setVisibility(View.GONE);
			pauseIcon.setVisibility(View.GONE);
			placeholderIcon.setVisibility(View.INVISIBLE);	
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(com.squeed.microgramcaster.R.menu.menu, menu);
		MenuItem mediaRouteMenuItem = menu.findItem(com.squeed.microgramcaster.R.id.media_route_menu_item);
		MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat
				.getActionProvider(mediaRouteMenuItem);

		// Set the MediaRouteActionProvider selector for device discovery.
		if (mediaRouteActionProvider != null)
			mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);

		rotateIcon = menu.findItem(com.squeed.microgramcaster.R.id.action_rotate);
		rotateIcon.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				sendMessage(CommandFactory.buildToggleRotateCommand());
				landscape = !landscape;
				if(landscape ) {
					rotateIcon.setIcon(R.drawable.ic_action_screen_rotation);
				} else {
					rotateIcon.setIcon(R.drawable.ic_action_screen_rotation_blue);
				}
				return false;
			}
		});
		rotateIcon.setVisible(false);

		
		MenuItem settingsIcon = menu.findItem(com.squeed.microgramcaster.R.id.action_settings);
		settingsIcon.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent settingsActivity = new Intent(getBaseContext(),
                        Preferences.class);
				startActivity(settingsActivity);
				return false;
			}			
		});

		return true;
	}

	/**
	 * An extension of the MediaRoute.Callback so we can invoke our own onRoute
	 * selected/unselected
	 */
	private class MediaRouterCallback extends MediaRouter.Callback {

		@Override
		public void onRouteSelected(MediaRouter router, android.support.v7.media.MediaRouter.RouteInfo route) {
			mSelectedDevice = CastDevice.getFromBundle(route.getExtras());
			loadingDialog.setTitle("Connecting...");
			loadingDialog.setMessage("Please wait a moment while connecting to Chromecast");
			loadingDialog.show();
			launchReceiver();
		}

		@Override
		public void onRouteUnselected(MediaRouter router, android.support.v7.media.MediaRouter.RouteInfo route) {
			teardown();
			mSelectedDevice = null;
			hideMediaControlIcons();
			hideSeekbar();
		}
	}

	private void hideMediaControlIcons() {
		playIcon.setVisibility(View.GONE);
		pauseIcon.setVisibility(View.GONE);
		placeholderIcon.setVisibility(View.INVISIBLE);
	}

	/**
	 * Start the receiver app
	 */
	private void launchReceiver() {
		try {
			mCastListener = new Cast.Listener() {

				@Override
				public void onApplicationDisconnected(int errorCode) {
					teardown();
				}

			};
			// Connect to Google Play services
			mConnectionCallbacks = new ConnectionCallbacks();

			mConnectionFailedListener = new ConnectionFailedListener();
			Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(mSelectedDevice, mCastListener);
			mApiClient = new GoogleApiClient.Builder(this).addApi(Cast.API, apiOptionsBuilder.build())
					.addConnectionCallbacks(mConnectionCallbacks)
					.addOnConnectionFailedListener(mConnectionFailedListener).build();

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
					if (mMicrogramCasterChannel != null) {
						Cast.CastApi.removeMessageReceivedCallbacks(mApiClient, mMicrogramCasterChannel.getNamespace());
						mMicrogramCasterChannel = null;
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
		hideMediaControlIcons();
	}

	/**
	 * Google Play services callbacks
	 */
	private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
		@Override
		public void onConnected(Bundle connectionHint) {		

			if (mApiClient == null) {
				// We got disconnected while this runnable was pending
				// execution.
				loadingDialog.hide();
				return;
			}

			try {
				if (mWaitingForReconnect) {
					mWaitingForReconnect = false;

					// Check if the receiver app is still running
					if ((connectionHint != null) && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
						teardown();
					} else {
						// Re-create the custom message channel
						try {
							Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
									mMicrogramCasterChannel.getNamespace(), mMicrogramCasterChannel);
						} catch (IOException e) {
							Log.e(TAG, "Exception while creating channel", e);
						}
					}
				} else {
					
					// Launch the receiver app
					Cast.CastApi.launchApplication(mApiClient, APP_NAME, false).setResultCallback(
							new ResultCallback<Cast.ApplicationConnectionResult>() {
								@Override
								public void onResult(ApplicationConnectionResult result) {
									try {
										Status status = result.getStatus();
										
										if (status.isSuccess()) {
//											
											mApplicationStarted = true;

											// Create the custom message channel
											mMicrogramCasterChannel = new MicrogramCasterChannel(MainActivity.this);
											try {
												Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
														mMicrogramCasterChannel.getNamespace(), mMicrogramCasterChannel);
											} catch (IOException e) {
												Log.e(TAG, "Exception while creating channel", e);
											}
											
										} else {
											Log.e(TAG, "application could not launch");
											teardown();
										}
									} finally {
										loadingDialog.hide();
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
			mWaitingForReconnect = true;
			hideMediaControlIcons();
			hideSeekbar();
		}
	}

	private void sendMessage(Command cmd) {
		if (mSelectedDevice == null || mApiClient == null || (mApiClient != null && !mApiClient.isConnected())) {
			if (mApiClient != null && mApiClient.isConnecting()) {
				Toast.makeText(MainActivity.this,
						"Currently connecting to Cast Device, please try again in a moment...", Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(MainActivity.this,
						"Cast Device not connected, please try to disconnect and connect again", Toast.LENGTH_LONG)
						.show();
			}

			return;
		}
		try {
			JSONObject obj = new JSONObject();
			obj.put("id", cmd.getId());
			obj.put("params", new JSONObject(cmd.getParams()));

			if (mApiClient != null && mMicrogramCasterChannel != null) {
				try {
					Cast.CastApi.sendMessage(mApiClient, mMicrogramCasterChannel.getNamespace(), obj.toString());
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

	private String buildMediaItemURL(String fileName) {
		return MyHTTPD.WEB_SERVER_PROTOCOL + "://" + WifiHelper.getLanIP(MainActivity.this) + ":"
				+ MyHTTPD.WEB_SERVER_PORT + "/" + fileName;
	}
	
	private String buildLocalThumbURL(Long identifier) {
		return MyHTTPD.WEB_SERVER_PROTOCOL + "://" + WifiHelper.getLanIP(MainActivity.this) + ":"
				+ MyHTTPD.WEB_SERVER_PORT + "/thumb/local/" + identifier;
	}
	
	private String buildRemoteThumbURL(String remoteUrl) {
		return MyHTTPD.WEB_SERVER_PROTOCOL + "://" + WifiHelper.getLanIP(MainActivity.this) + ":"
				+ MyHTTPD.WEB_SERVER_PORT + "/thumb/remote/" + remoteUrl;
	}
	
	private String buildSmbItemURL(String smbPath) {
		String finalSmbPath = smbPath.substring(smbPath.toLowerCase().indexOf("smb://")+6);
		return MyHTTPD.WEB_SERVER_PROTOCOL + "://" + WifiHelper.getLanIP(MainActivity.this) + ":"
				+ MyHTTPD.WEB_SERVER_PORT + "/smb/" + finalSmbPath;
	}

	

	/**
	 * Google Play services callbacks
	 */
	private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
		@Override
		public void onConnectionFailed(ConnectionResult result) {
			Log.e(TAG, "onConnectionFailed ");
			loadingDialog.hide();
			teardown();
		}
	}
	
	/**
	 * Listen for volume upp/down presses and propagate them to the receiver.
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		try {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				double volume = Cast.CastApi.getVolume(mApiClient);
				switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					try {
						if (volume < 1.0)
							Cast.CastApi.setVolume(mApiClient, volume + 0.05d);
					} catch (IOException e) {
					}
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					try {
						if (volume > 0.0)
							Cast.CastApi.setVolume(mApiClient, volume - 0.05d);
					} catch (IOException e) {
					}
					return true;
				}
			}
		} catch (Throwable t) {
		}

		return super.dispatchKeyEvent(event);
	}
	
	private final Object mutex = new Object();
	private boolean backPressed = false;
	private Handler handler = new Handler();
	
	Runnable resetBackButtonState = new Runnable() {
		
		@Override
		public void run() {
			synchronized (mutex) {
				backPressed = false;	
			}			
		}
	};

	
	
	@Override
	public void onBackPressed() {
		if(backPressed) {
			handler.removeCallbacks(resetBackButtonState);
			teardown();
			super.onBackPressed();
		} else {
			
			// Check if we want to go up one level in a file/share browse or exit the application
			if(PathStack.get().size() == 1 || currentSource == CurrentSource.LOCAL) {
				Toast.makeText(this, "Are you sure you want to leave the application? Press back again to confirm. " +
						 "If you want to keep casting in the background, use the Home button instead.", Toast.LENGTH_LONG).show();
				synchronized (mutex) {
					backPressed = true;
				}
				handler.postDelayed(resetBackButtonState, 3000);
			} else {
				synchronized (mutex) {
					backPressed = false;
				}
				// Go up one level instead.
				PathStack.popContainerIdStack();
				MainActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						adapter.clear();
						adapter.notifyDataSetChanged();
					}							
				});
				
				// Run either the SMB or the UPNP task to go back one level, LOCAL should never get here...
				switch(currentSource) {
				case LOCAL:
					break;
				case UPNP:
					uPnPHandler.buildContentListing(PathStack.get().peek());
					break;
				case SMB:
					new SmbReadFolderTask(MainActivity.this).execute(PathStack.get().peek());
					break;
				}				
			}			
		}
	}
	
	public MediaItemArrayAdapter getMediaItemListAdapter() {
		return adapter;
	}

	public ProgressDialog getLoadingDialog() {
		return loadingDialog;
	}


	public UPnPHandler getUPnPHandler() {
		return uPnPHandler;
	}
	
	public SambaExplorer getSambaExplorer() {
		return sambaExplorer;
	}

	public NetworkSourceArrayAdapter getNetworkSourceArrayAdapter() {
		return bldr.getAdapter();
	}



	public void showNetworkSourceDialog() {
		bldr.showNetworkSourceDialog();
	}
	
}
