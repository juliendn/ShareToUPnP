package fr.spaz.upnp.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

public class NetworkUtils
{
	public static String getIp(Context context)
	{
		final WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		int ip = manager.getConnectionInfo().getIpAddress();
		
		return Formatter.formatIpAddress(ip);
	}
}
