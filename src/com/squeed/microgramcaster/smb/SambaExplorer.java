package com.squeed.microgramcaster.smb;

import java.io.IOException;
import java.net.MalformedURLException;

import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import android.app.AlertDialog;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.squeed.microgramcaster.Constants;
import com.squeed.microgramcaster.MainActivity;
import com.squeed.microgramcaster.R;
import com.squeed.microgramcaster.media.MediaItem;
import com.squeed.microgramcaster.media.MediaItemComparator;

public class SambaExplorer {
	
	private static final String TAG = "SambaExplorer";

	private MainActivity mainActivity;
	
	//public ArrayAdapter<String> mList;

	public String[] mListStrings;
	public String[] mListContents;
	public String mHost;
	int curListID = 0;

	public boolean active;
	private String IPsubnet;

	public SambaExplorer(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	private static String ipAddressToString(int addr) {
		StringBuffer buf = new StringBuffer();
		buf.append(addr & 0xff).append('.').append((addr >>>= 8) & 0xff).append('.').append((addr >>>= 8) & 0xff)
				.append('.').append((addr >>>= 8) & 0xff);
		return buf.toString();
	}

	private static String getIPsubnet(int addr) {
		StringBuffer buf = new StringBuffer();
		buf.append(addr & 0xff).append('.').append((addr >>>= 8) & 0xff).append('.').append((addr >>>= 8) & 0xff)
				.append('.');
		return buf.toString();
	}

//	Thread m_subnetScanThread;
//	public int numThreadsRunning;
//	public int serversScanned;
//
//	class SubnetScanThread implements Runnable {
//		public SambaExplorer mOwner;
//		
//		private Activity activity;
//
//		SubnetScanThread(SambaExplorer owner, MainActivity mainActivity) {
//			mOwner = owner;
//			activity = mainActivity;
//		}
//
//		@Override
//		public void run() {
//
//			int timeout = 1000;
//			int start = 1;
//			int end = 10;
//
//			mOwner.numThreadsRunning++;
//
//			if (mOwner.IPsubnet.endsWith("*")) {
//				mOwner.IPsubnet = mOwner.IPsubnet.substring(0, mOwner.IPsubnet.length() - 1);
//			}
//
//			for (int tries = 0; tries < 1; tries++) {
//				for (int i = start; i <= end; i++) {
//					String serverName = new String(mOwner.IPsubnet + String.valueOf(i));
//					mOwner.serversScanned++;
//
//					while (!mOwner.active) {
//						try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//
//					try {
//						InetAddress serverAddr = InetAddress.getByName(serverName);
//						activity.getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
//								(int) (9999.0 * ((1.0 + mOwner.serversScanned) / 256)));
//						if (serverAddr.isReachable(timeout)) {
//							mOwner.addListItem("smb://" + serverAddr.getCanonicalHostName() + "/");
//						}
//					} catch (Exception e) {
//
//						mOwner.addListItem(e.getMessage());
//
//					}
//				}
//				timeout += 500;
//			}
//
//			if (mOwner.numThreadsRunning == 1) {
//				// if we're the last thread running...
//
//				Runnable alertDialog = new Runnable() {
//					@Override
//					public void run() {
//						Toast.makeText(mainActivity, "Finished scanning " + mOwner.serversScanned + " servers", 0);
//					}
//				};
//
//				mainActivity.runOnUiThread(alertDialog);
//			}
//
//			mOwner.numThreadsRunning--;
//		}
//
//	};

	private String mSubnetOverride;

	
	public void init() {
		
		mListStrings = new String[255];
		mListContents = new String[255];
		for (int i = 0; i < 255; i++) {
			mListStrings[i] = "";
			mListContents[i] = "";
		}

		

		ConnectivityManager cm = (ConnectivityManager) mainActivity.getSystemService(mainActivity.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {
			new AlertDialog.Builder(mainActivity).setMessage("This application is meant for WIFI networks.").show();
			return;
		}

		WifiManager wifi = (WifiManager) mainActivity.getSystemService(mainActivity.WIFI_SERVICE);
		DhcpInfo info = wifi.getDhcpInfo();
		IPsubnet = getIPsubnet(info.ipAddress);

		mHost = null;

		//Intent intent = mainActivity.getIntent();
		//mHost = intent.getDataString();
		mHost = "";
		// mSubnetOverride = intent.getStringExtra("subnet");

		if (mHost == null) {
			// m_subnetScanThread = new Thread(new SubnetScanThread(this, mainActivity));
			// m_subnetScanThread.start();

			// startActivity(new
			// Intent(this,com.shank.SambaExplorer.PickHost.class));

//			try {
//				Intent i = new Intent("com.shank.portscan.PortScan.class");
//				mainActivity.startActivity(i);
//			} catch (ActivityNotFoundException e) {
//				Intent i = new Intent(Intent.ACTION_VIEW);
//				i.setData(Uri.parse("market://search?q=NetScan"));
//				mainActivity.startActivity(i);
//			}
		} else {

			// setLastListItem("Connecting to " + mHost);

			jcifs.Config.setProperty("jcifs.encoding", "Cp1252");
			jcifs.Config.setProperty("jcifs.smb.lmCompatibility", "0");
			jcifs.Config.setProperty("jcifs.netbios.hostname", "AndroidPhone");

			jcifs.Config.registerSmbURLHandler();

			if (!mHost.startsWith("smb:/")) {
				if (mHost.startsWith("/")) {
					mHost = "smb:/" + mHost + "/";
				} else {
					mHost = "smb://" + mHost + "/";
				}
			}

			SmbFile f;
			try {
				f = new SmbFile(mHost);
				
					
				if (f.canRead()) {

					traverseSMB(f, 2);

				}
			} catch (SmbAuthException e) {
				//mainActivity.startActivity(new Intent(this, com.shank.SambaExplorer.SambaLogin.class).putExtra("path", mHost));
				Log.e("SambaExplorer", "SmbAuthException: " + e.getMessage());
			} catch (MalformedURLException e) {
				final MalformedURLException E = e;
				Runnable dialogPopup = new Runnable() {
					@Override
					public void run() {
						String StackTrace = "";
						StackTraceElement[] Stack = E.getStackTrace();
						for (int i = 0; i < Stack.length; i++) {
							StackTrace += Stack[i].toString() + "\n";
						}
						new AlertDialog.Builder(mainActivity).setMessage(StackTrace).setTitle(E.toString())
								.show();
					}
				};
				mainActivity.runOnUiThread(dialogPopup);
			} catch (SmbException e) {
				final SmbException E = e;
				Runnable dialogPopup = new Runnable() {
					@Override
					public void run() {
						String StackTrace = "";
						StackTraceElement[] Stack = E.getStackTrace();
						for (int i = 0; i < Stack.length; i++) {
							StackTrace += Stack[i].toString() + "\n";
						}
						new AlertDialog.Builder(mainActivity).setMessage(StackTrace).setTitle(E.toString())
								.show();
					}
				};
				mainActivity.runOnUiThread(dialogPopup);
			} catch (IOException e) {
				final IOException E = e;
				Runnable dialogPopup = new Runnable() {
					@Override
					public void run() {
						String StackTrace = "";
						StackTraceElement[] Stack = E.getStackTrace();
						for (int i = 0; i < Stack.length; i++) {
							StackTrace += Stack[i].toString() + "\n";
						}
						new AlertDialog.Builder(mainActivity).setMessage(StackTrace).setTitle(E.toString())
								.show();
					}
				};
				mainActivity.runOnUiThread(dialogPopup);
			}
		}
	}

	
//	@Override
//	public void onListItemClick(ListView l, View v, int position, long id) {
//		String share = mListContents[position];
//		if (share.startsWith("smb://")) {
//
//			if (share.endsWith("/")) {
//
//				Intent intent = new Intent(Intent.ACTION_VIEW);
//				intent.setData(Uri.parse(share));
//				startActivity(intent);
//
//			} else {
//				// files
//
//				DownloadService.QueueDownload(this, share);
//
//			}
//
//		}
//
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		super.onCreateOptionsMenu(menu);
//
//		menu.add(0, 0, 0, "Download All");
//		menu.add(0, 1, 1, "Recursive Download All");
//		menu.add(0, 2, 2, "Download Queue");
//		menu.add(0, 3, 3, "Options");
//
//		return true;
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//
//		switch (item.getItemId()) {
//		case 0:
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						downloadDirectory(new SmbFile(mHost), 1);
//					} catch (MalformedURLException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}).start();
//			break;
//		case 1:
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						downloadDirectory(new SmbFile(mHost), 255);
//					} catch (MalformedURLException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}).start();
//			break;
//
//		case 2:
//			Intent intent = new Intent(this, com.shank.SambaExplorer.DownloadQueue.class);
//			startActivity(intent);
//			break;
//		}
//		return false;
//	}

	private Runnable updateAdapter = new Runnable() {
		@Override
		public void run() {
			mainActivity.getMediaItemListAdapter().notifyDataSetChanged();
		}
	};

	public void forceUpdate() {
		mainActivity.runOnUiThread(updateAdapter);
	}

//	public void setLastListItem(String str) {
//		mListStrings[curListID] = str;
//		forceUpdate();
//	}
//
//	public void addListItem(String server) {
//		if (server.endsWith("/")) {
//			String temp = server.substring(0, server.lastIndexOf('/'));
//			mListStrings[curListID] = server.substring(temp.lastIndexOf('/'));
//		} else {
//			mListStrings[curListID] = server.substring(server.lastIndexOf('/'));
//		}
//		mListContents[curListID] = server;
//		curListID++;
//		forceUpdate();
//
//	}

//	void downloadDirectory(SmbFile f, int depth) throws MalformedURLException, IOException {
//
//		if (depth == 0) {
//			return;
//		}
//		try {
//			SmbFile[] l;
//
//			l = f.listFiles();
//
//			for (int i = 0; l != null && i < l.length; i++) {
//				try {
//					if (l[i].isDirectory()) {
//						downloadDirectory(l[i], depth - 1);
//					} else {
//						//downloadService.queueDownload(this, l[i].getPath());
//					}
//
//					Thread.sleep(100);
//				} catch (IOException ioe) {
//
//				}
//			}
//
//		} catch (Exception e) {
//			addListItem(e.toString());
//		}
//	}
	
	private MediaItemComparator comparator = new MediaItemComparator();

	public void traverseSMB(SmbFile f, int depth) throws MalformedURLException, IOException {

		if (depth == 0) {
			return;
		}
		
		try {
			SmbFile[] l;

			l = f.listFiles();

			for (int i = 0; l != null && i < l.length; i++) {
				try {
					if (l[i].isDirectory()) {
	
						String path = l[i].getCanonicalPath();
						String name = null;
						if(path.endsWith("/")) {
							String tmpPath = path.substring(0, path.length() - 1);
							name = tmpPath.substring(tmpPath.lastIndexOf("/")+1);
						} else {
							name = path.substring(path.lastIndexOf("/")+1);
						}
						final MediaItem mi = new MediaItem();
						mi.setType(Constants.SMB_FOLDER);
						mi.setData(path);
						mi.setName(name);
						mi.setDuration(null);
						mi.setThumbnail(BitmapFactory.decodeResource(mainActivity.getResources(), R.drawable.ic_menu_archive));
				
						mainActivity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mainActivity.getMediaItemListAdapter().add(mi);
								mainActivity.getMediaItemListAdapter().sort(comparator);
								mainActivity.getMediaItemListAdapter().notifyDataSetChanged();
							}
							
						});
					} else {
						if(l[i].getCanonicalPath().toLowerCase().endsWith("mp4")) {

							final MediaItem mi = new MediaItem();
							mi.setType(Constants.SMB_FILE);
							mi.setData(l[i].getCanonicalPath());
							mi.setName(l[i].getCanonicalPath().substring(l[i].getCanonicalPath().lastIndexOf("/")+1));
							mi.setDuration((long) l[i].getContentLength());
							
							mainActivity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									mainActivity.getMediaItemListAdapter().add(mi);
									mainActivity.getMediaItemListAdapter().sort(comparator);
									mainActivity.getMediaItemListAdapter().notifyDataSetChanged();
								}
								
							});
						}
					}

				} catch (SmbAuthException e) {
//					startActivity(new Intent(this, com.shank.SambaExplorer.SambaLogin.class).putExtra("path",
//							l[i].getCanonicalPath()));
					Log.e(TAG, "SmbAuthException: " + e.getMessage());
				} catch (IOException ioe) {

				}
			}

		} catch (SmbAuthException e) {
//			mainActivity.startActivity(new Intent(this, com.shank.SambaExplorer.SambaLogin.class).putExtra("path",
//					f.getCanonicalPath()));
		} catch (Exception e) {
			Log.e(TAG, "Exception: " + e.getMessage());
			//addListItem(e.toString());
		}
	}

}
