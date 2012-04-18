package fr.spaz.upnp.upnp;

import org.teleal.cling.android.AndroidUpnpServiceImpl;

import android.util.Log;

public class UPnPService extends AndroidUpnpServiceImpl
{

	private static final String TAG = "UpnpService";

	@Override
	public void onCreate()
	{
		Log.i(TAG, "onCreate");
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		Log.i(TAG, "onCreate");
		super.onDestroy();
	}

}
