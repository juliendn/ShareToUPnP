package fr.spaz.upnp.activities;

import java.io.IOException;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.binding.LocalServiceBindingException;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.connectionmanager.ConnectionManagerService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import fr.spaz.upnp.R;
import fr.spaz.upnp.activities.ShareHttpServer.ShareHttpBinder;
import fr.spaz.upnp.services.ShareContentDirectoryService;
import fr.spaz.upnp.upnp.UpnpService;
import fr.spaz.upnp.utils.ShareConstants;
import fr.spaz.upnp.utils.UPnPException;

/**
 * 
 * @author Spaz
 * 
 */
public class ShareControlPointActivity extends Activity implements OnSeekBarChangeListener
{
	private static final String TAG = "ShareControlPointActivity";
	private SeekBar mSeek;
	private SeekBar mVolume;

	private LocalDevice mLocalDevice;

	private ShareHttpBinder mHttpService;
	private AndroidUpnpService mUpnpService;
	
	private HttpServiceConnection mHttpServiceConnection;
	private UpnpControlPointServiceConnection mUpnpServiceConnection;

	private Uri mMediaUri;
	private String mDeviceName;
	private String mUUID;
	private String mMediaType;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controlpoint_layout);

		mSeek = (SeekBar) findViewById(R.id.seekBar1);
		mSeek.setOnSeekBarChangeListener(this);
		mVolume = (SeekBar) findViewById(R.id.seekBar2);
		mVolume.setOnSeekBarChangeListener(this);

		Bundle callBundle = getIntent().getExtras();
		mMediaUri = (Uri) callBundle.get(Intent.EXTRA_STREAM);
		mMediaType = getIntent().getType();
		mDeviceName = callBundle.getString(ShareConstants.NAME);
		mUUID = callBundle.getString(ShareConstants.UDN);
		Log.d(TAG, "uri: " + mMediaUri.toString());
		Log.d(TAG, "deviceName: " + mDeviceName);
		Log.d(TAG, "udn: " + mUUID);

		mHttpServiceConnection = new HttpServiceConnection();
		mUpnpServiceConnection = new UpnpControlPointServiceConnection();

		// Start http service
		final Intent intent = new Intent(this, ShareHttpServer.class);
		getApplicationContext().bindService(intent, mHttpServiceConnection, Context.BIND_AUTO_CREATE);

	}

	@Override
	public void onDestroy()
	{
		// if (null != mUpnpService)
		// {
		// mUpnpService.getRegistry().removeDevice(mLocalDevice);
		// Log.i(TAG, "removeDevice");
		// }
		if(null!=mHttpService)
		{
			getApplicationContext().unbindService(mHttpServiceConnection);
		}
		if(null!=mUpnpService)
		{
			getApplicationContext().unbindService(mUpnpServiceConnection);
		}
		super.onDestroy();
	}

	// Seek bar listener
	@Override
	public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser)
	{

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekbar)
	{

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekbar)
	{

	}

	@SuppressWarnings("unused")
	private LocalDevice createDevice() throws ValidationException, LocalServiceBindingException, IOException
	{

		DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("ShareToUPnP"));

		DeviceType type = new UDADeviceType("MediaServer", 1);

		DeviceDetails details = new DeviceDetails("ShareToUPnP_Server", new ManufacturerDetails("Spaz"), new ModelDetails("Android", "ShareToUPnP Media Server", "v1"));

		@SuppressWarnings("unchecked")
		LocalService<ShareContentDirectoryService> contentDirectoryService = new AnnotationLocalServiceBinder().read(ShareContentDirectoryService.class);
		contentDirectoryService.setManager(new DefaultServiceManager<ShareContentDirectoryService>(contentDirectoryService, ShareContentDirectoryService.class));

		@SuppressWarnings("unchecked")
		LocalService<ConnectionManagerService> connectionManagerService = new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
		connectionManagerService.setManager(new DefaultServiceManager<ConnectionManagerService>(connectionManagerService, ConnectionManagerService.class));

		return new LocalDevice(identity, type, details, new LocalService<?>[]{contentDirectoryService, connectionManagerService});

	}
	
	private class HttpServiceConnection implements ServiceConnection
	{
		
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder)
		{
			mHttpService = (ShareHttpBinder)binder;
			mHttpService.serUri(mMediaType, mMediaUri);
			mHttpService.start();
			
//			Log.d(TAG, "url: " + mHttpService.getUrl());
			
			// Start upnp service
			final Intent intent = new Intent(ShareControlPointActivity.this, UpnpService.class);
			getApplicationContext().bindService(intent, mUpnpServiceConnection, Context.BIND_AUTO_CREATE);
		}

		@Override
		public void onServiceDisconnected(ComponentName className)
		{
			mHttpService.stop();
			mHttpService = null;
		}
		
	}

	/**
	 * 
	 * @author Spaz
	 * 
	 */
	private class UpnpControlPointServiceConnection implements ServiceConnection
	{

		@Override
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			mUpnpService = (AndroidUpnpService) service;

			try
			{
				// // Add the bound local device to the registry
				// mLocalDevice = createDevice();
				// mUpnpService.getRegistry().addDevice(createDevice());
				// Log.i(TAG, "addDevice");

				final RemoteDevice renderer = mUpnpService.getRegistry().getRemoteDevice(UDN.valueOf(mUUID), true);
				if (null != renderer)
				{
					final RemoteService avTransportService = renderer.findService(new UDAServiceType("AVTransport", 1));
					if (null != avTransportService)
					{
						Log.d(TAG, "launch setAVTransportURI");
						mUpnpService.getControlPoint().execute(new SetAVTransportURI(new UnsignedIntegerFourBytes(0), avTransportService, mHttpService.getUrl(), "NO METADATA")
						{
							@SuppressWarnings("rawtypes")
							@Override
							public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
							{
								Log.d(TAG, "setAVTransportURI failure");
								Log.d(TAG, "operation: " + operation.getStatusCode() + " " + operation.getStatusMessage());
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
										Log.d(TAG, "operation: " + operation.getStatusCode() + " " + operation.getStatusMessage());
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
				}
				else
				{
					throw new UPnPException("No Device found");
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className)
		{
			try
			{
				mUpnpService.getRegistry().removeDevice(mLocalDevice);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			mUpnpService = null;
		}

	}
}
