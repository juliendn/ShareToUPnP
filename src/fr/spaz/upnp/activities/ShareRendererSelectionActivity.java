package fr.spaz.upnp.activities;

import java.io.File;
import java.io.IOException;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import fr.spaz.upnp.R;
import fr.spaz.upnp.fragments.ShareControlFragment;
import fr.spaz.upnp.fragments.ShareRendererSelectionFragment;
import fr.spaz.upnp.upnp.UPnPBrowseRegistryListener;
import fr.spaz.upnp.upnp.UPnPDeviceDisplay;
import fr.spaz.upnp.upnp.UPnPService;
import fr.spaz.upnp.utils.NanoHTTPD;
import fr.spaz.upnp.utils.NetworkUtils;
import fr.spaz.upnp.utils.UPnPException;

public class ShareRendererSelectionActivity extends SherlockFragmentActivity implements IRenderSelection, OnItemClickListener
{

	private static final String TAG = "SharePictureBroadcastReceiver";

	private AndroidUpnpService mUpnpService;
	private ServiceConnection mServiceConnection;

	private NanoHTTPD mHttpd;
	private String mPath;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);

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
		if (null != cursor)
		{
			cursor.close();
		}

		// Init
		mServiceConnection = new UpnpBrowseServiceConnection();

		// Start upnp browse service
		Intent intent = new Intent(this, UPnPService.class);
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
//		
//		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//		ft.replace(R.id.content, new RendererSelectionFragment());
//		ft.commit();
	}

	@Override
	protected void onDestroy()
	{
		if (null != mHttpd)
		{
			mHttpd.stop();
			mHttpd = null;
		}
		getApplicationContext().unbindService(mServiceConnection);
		super.onDestroy();
	}

	@Override
	public AndroidUpnpService getUPnPService()
	{
		return mUpnpService;
	}

	@Override
	public void setCurrentRenderer(UPnPDeviceDisplay device)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public UPnPDeviceDisplay detCurrentRenderer()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onItemClick(AdapterView<?> listview, View view, int position, long id)
	{
		try
		{
			if (null != mUpnpService)
			{
				UPnPDeviceDisplay deviceDisplay = (UPnPDeviceDisplay) listview.getItemAtPosition(position);
				Device<?, ?, ?> renderer = deviceDisplay.getDevice();

				final Service<?, ?> avTransportService = renderer.findService(new UDAServiceType("AVTransport", 1));
				if (null != avTransportService)
				{
					Log.d(TAG, "launch setAVTransportURI");
					final String url = String.format("http://%s:%d%s", NetworkUtils.getIp(getBaseContext()), mHttpd.getPort(), mPath);
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
									
									FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
									ft.replace(R.id.content, new ShareControlFragment());
									ft.commit();
								}
							});
						}
					});
				}
				else
				{
					throw new UPnPException("No AVTransportService found");
				}

				// Log.d(TAG, "start ControlPointActivity");
				// Intent intent = new Intent(this, ShareControlPointActivity.class);
				//
				// intent.setType(getIntent().getType());
				// intent.putExtra(Intent.EXTRA_STREAM, getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
				// intent.putExtra(ShareConstants.NAME, deviceDisplay.toString());
				// intent.putExtra(ShareConstants.UDN, renderer.getIdentity().getUdn().toString());
				//
				// startActivity(intent);

			}
		}
		catch (UPnPException e)
		{
			e.printStackTrace();
		}
	}

	private class UpnpBrowseServiceConnection implements ServiceConnection
	{

		UPnPBrowseRegistryListener mListener;

		public UpnpBrowseServiceConnection(UPnPBrowseRegistryListener listener)
		{
			mListener = listener;
		}

		public UpnpBrowseServiceConnection()
		{
			mListener = null;
		}

		@Override
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			mUpnpService = (AndroidUpnpService) service;


			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.content, new ShareRendererSelectionFragment());
			ft.commit();

			// // Refresh the list with all known devices
			// for (Device<?, ?, ?> device : mUpnpService.getRegistry().getDevices(new UDADeviceType("MediaRenderer", 1)))
			// {
			// mListener.deviceAdded(device);
			// }
			//
			// // Getting ready for future device advertisements
			// mUpnpService.getRegistry().addListener(mListener);
			//
			// // Search asynchronously for all devices
			// mUpnpService.getControlPoint().search();
		}

		@Override
		public void onServiceDisconnected(ComponentName className)
		{
			mUpnpService = null;
		}

	}
}
