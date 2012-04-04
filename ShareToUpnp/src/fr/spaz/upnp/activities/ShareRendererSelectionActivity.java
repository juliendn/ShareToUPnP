package fr.spaz.upnp.activities;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.types.UDADeviceType;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import fr.spaz.upnp.upnp.UpnpBrowseRegistryListener;
import fr.spaz.upnp.upnp.UpnpDeviceDisplay;
import fr.spaz.upnp.upnp.UpnpService;
import fr.spaz.upnp.utils.ShareConstants;

public class ShareRendererSelectionActivity extends ListActivity implements OnItemClickListener
{

	private static final String TAG = "SharePictureBroadcastReceiver";

	private ArrayAdapter<UpnpDeviceDisplay> mListAdapter;
	private AndroidUpnpService mUpnpService;
	private UpnpBrowseRegistryListener mRegistryListener;
	private ServiceConnection mServiceConnection;
	
	private Uri mMediaUri;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		Bundle callBundle = getIntent().getExtras();
		mMediaUri = (Uri) callBundle.get(Intent.EXTRA_STREAM);
		Log.d(TAG, "uri: " + mMediaUri.toString());
		
		

		// Init
		mListAdapter = new ArrayAdapter<UpnpDeviceDisplay>(this, android.R.layout.simple_list_item_1);
		mRegistryListener = new UpnpBrowseRegistryListener(this, mListAdapter);
		mServiceConnection = new UpnpBrowseServiceConnection(mRegistryListener);

		// Set List
		setListAdapter(mListAdapter);
		getListView().setOnItemClickListener(this);

		// Start upnp browse service
		Intent intent = new Intent(this, UpnpService.class);
		getApplicationContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

	}

	@Override
	protected void onDestroy()
	{
		if (mUpnpService != null)
		{
			mUpnpService.getRegistry().removeListener(mRegistryListener);
		}
		getApplicationContext().unbindService(mServiceConnection);
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		if (null != mUpnpService && null != mUpnpService.getRegistry())
		{
			mUpnpService.getRegistry().pause();
		}
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		if (null != mUpnpService && null != mUpnpService.getRegistry())
		{
			mUpnpService.getRegistry().resume();
		}
		super.onResume();
	}

	@Override
	public void onItemClick(AdapterView<?> listview, View view, int position, long id)
	{
		if (null != mUpnpService)
		{
			UpnpDeviceDisplay deviceDisplay = (UpnpDeviceDisplay) listview.getItemAtPosition(position);
			Device<?, ?, ?> device = deviceDisplay.getDevice();
			
			Log.d(TAG, "start ControlPointActivity");
			Intent intent = new Intent(this, ShareControlPointActivity.class);

			intent.setType(getIntent().getType());
			intent.putExtra(Intent.EXTRA_STREAM, getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
			intent.putExtra(ShareConstants.NAME, deviceDisplay.toString());
			intent.putExtra(ShareConstants.UDN, device.getIdentity().getUdn().toString());

			startActivity(intent);

			// Service<?, ?> connectionManager = device.findService(ServiceType.valueOf("urn:schemas-upnp-org:service:ConnectionManager:1"));
			// if (null != connectionManager)
			// {
			// Log.d(TAG, "GetProtocolInfo");
			// // mUpnpService.getControlPoint().execute(new ShareGetProtocolInfo(connectionManager));
			// // mUpnpService.getControlPoint().execute(new SharePrepareForConnection(connectionManager, ));
			// }
			// else
			// {
			// Log.d(TAG, "pas de ConnectionManager");
			// }

			// Service<?, ?> avTransport = device.findService(ServiceType.valueOf("urn:schemas-upnp-org:service:AVTransport:1"));
			// if (null != connectionManager)
			// {
			// Log.d(TAG, "GetProtocolInfo");
			// mUpnpService.getControlPoint().execute(new ShareSetAVTransportURI(avTransport, ""));
			// // mUpnpService.getControlPoint().execute(new SharePrepareForConnection(connectionManager, ));
			// }
			// else
			// {
			// Log.d(TAG, "pas de ConnectionManager");
			// }
		}
	}

	private class UpnpBrowseServiceConnection implements ServiceConnection
	{

		UpnpBrowseRegistryListener mListener;

		public UpnpBrowseServiceConnection(UpnpBrowseRegistryListener listener)
		{
			mListener = listener;
		}

		@Override
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			mUpnpService = (AndroidUpnpService) service;

			// Refresh the list with all known devices
			for (Device<?, ?, ?> device : mUpnpService.getRegistry().getDevices(new UDADeviceType("MediaRenderer", 1)))
			{
				mListener.deviceAdded(device);
			}

			// Getting ready for future device advertisements
			mUpnpService.getRegistry().addListener(mListener);

			// Search asynchronously for all devices
			mUpnpService.getControlPoint().search();
		}

		@Override
		public void onServiceDisconnected(ComponentName className)
		{
			mUpnpService = null;
		}

	}
}
