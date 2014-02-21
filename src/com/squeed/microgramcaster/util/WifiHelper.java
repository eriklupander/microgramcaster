public class WiFiHelper {

	public static String getLanIP(Context context) {
	    WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
		int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
		return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
				(ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
	}
}