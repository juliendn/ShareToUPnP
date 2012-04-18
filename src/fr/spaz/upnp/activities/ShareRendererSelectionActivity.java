package fr.spaz.upnp.activities;

import java.io.File;
import java.io.IOException;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import fr.spaz.upnp.upnp.UpnpBrowseRegistryListener;
import fr.spaz.upnp.upnp.UpnpDeviceDisplay;
import fr.spaz.upnp.upnp.UpnpService;
import fr.spaz.upnp.utils.NanoHTTPD;
import fr.spaz.upnp.utils.NetworkUtils;
import fr.spaz.upnp.utils.UPnPException;

public class ShareRendererSelectionActivity extends ListActivity implements OnItemClickListener
{

	private static final String TAG = "SharePictureBroadcastReceiver";

	private ArrayAdapter<UpnpDeviceDisplay> mListAdapter;
	private AndroidUpnpService mUpnpService;
	private UpnpBrowseRegistryListener mRegistryListener;
	private ServiceConnection mServiceConnection;
	
	private NanoHTTPD mHttpd;
	private String mPath;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		final Bundle callBundle = getIntent().getExtras();
		final Uri uri = (Uri) callBundle.get(Intent.EXTRA_STREAM);
		Log.d(TAG, "uri: " + uri.toString());
		
		// Get file path
		final String[] proj = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.MIME_TYPE};
		final Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
		if (cursor.moveToFirst())
		{
			mPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
			// final String mimetype = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
			try
			{
				mHttpd = new NanoHTTPD(0, new File("/"));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		if(null!=cursor)
		{
			cursor.close();
		}

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
		
		// Http service
		try
		{
			mHttpd = new NanoHTTPD(0, new File("/"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy()
	{
		if (null != mHttpd)
		{
			mHttpd.stop();
			mHttpd = null;
		}
		
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
		try
		{
			if (null != mUpnpService)
			{
				UpnpDeviceDisplay deviceDisplay = (UpnpDeviceDisplay) listview.getItemAtPosition(position);
				Device<?, ?, ?> renderer = deviceDisplay.getDevice();
				
				final Service<?, ?> avTransportService = renderer.findService(new UDAServiceType("AVTransport", 1));
				if (null != avTransportService)
				{
					Log.d(TAG, "launch setAVTransportURI");
					final String url = String.format("http://%s:%d%s",NetworkUtils.getIp(getBaseContext()), mHttpd.getPort(), mPath);
					Log.d(TAG, "url: " + url);
					mUpnpService.getControlPoint().execute(new SetAVTransportURI(new UnsignedIntegerFourBytes(0), avTransportService, url, "NO METADATA")
					{
						@SuppressWarnings("rawtypes")
						@Override
						public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
						{
							Log.d(TAG, "setAVTransportURI failure");
							Log.d(TAG, "invocation: " + invocation.getFailure().getMessage());
							// Log.d(TAG, "operation: " + operation.getStatusCode() + " " + operation.getStatusMessage());
						}
	
						@SuppressWarnings("rawtypes")
						@Override
						public void success(ActionInvocation invocation)
						{
							super.success(invocation);
							
							Log.d(TAG, "setAVTransportURI success");
							Log.d(TAG, "launch play");
							mUpnpService.getControlPoint().execute(new Play(new UnsignedIntegerFourBytes(0), avTransportService, "1")
							{
								@Override
								public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
								{
									Log.d(TAG, "play failure");
									Log.d(TAG, "invocation: " + invocation.getFailure().getMessage());
									// Log.d(TAG, "operation: " + operation.getStatusCode() + " " + operation.getStatusMessage());
								}
	
								@Override
								public void success(ActionInvocation invocation)
								{
									super.success(invocation);
									Log.d(TAG, "play success");
								}
							});
						}
					});
				}
				else
				{
					throw new UPnPException("No AVTransportService found");
				}
				
	//			Log.d(TAG, "start ControlPointActivity");
	//			Intent intent = new Intent(this, ShareControlPointActivity.class);
	//
	//			intent.setType(getIntent().getType());
	//			intent.putExtra(Intent.EXTRA_STREAM, getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
	//			intent.putExtra(ShareConstants.NAME, deviceDisplay.toString());
	//			intent.putExtra(ShareConstants.UDN, renderer.getIdentity().getUdn().toString());
	//
	//			startActivity(intent);
	
			}
		}
		catch(UPnPException e)
		{
			e.printStackTrace();
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
