package com.squeed.microgramcaster.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Gets the IP address of the Wifi network interface, typically a 192.168 address the Chromecast will
 * be able to access our web server served files at.
 * 
 * Derived from stackoverflow answer: http://stackoverflow.com/questions/17252018/getting-my-lan-ip-address-192-168-xxxx-ipv4
 * 
 * @author Erik
 *
 */
public class WifiHelper {

	@SuppressLint("DefaultLocale")
	public static String getLanIP(Context context) {
	    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
		return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
				(ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
	}
}