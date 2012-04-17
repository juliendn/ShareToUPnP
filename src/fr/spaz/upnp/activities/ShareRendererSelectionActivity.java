package fr.spaz.upnp.activities;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.types.UDADeviceType;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import fr.spaz.upnp.R;
import fr.spaz.upnp.upnp.UPnPBrowseRegistryListener;
import fr.spaz.upnp.upnp.UPnPDeviceDisplay;
import fr.spaz.upnp.upnp.UPnPService;
import fr.spaz.upnp.utils.ShareConstants;

public class ShareRendererSelectionActivity extends ListActivity implements OnItemClickListener
{

	private static final String TAG = "SharePictureBroadcastReceiver";

	private UpnpDeviceAdapter mListAdapter;
	private AndroidUpnpService mUpnpService;
	private UPnPBrowseRegistryListener mRegistryListener;
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
		// mListAdapter = new ArrayAdapter<UpnpDeviceDisplay>(this,
		// android.R.layout.simple_list_item_1);
		mListAdapter = new UpnpDeviceAdapter(this);
		mRegistryListener = new UPnPBrowseRegistryListener(this, mListAdapter);
		mServiceConnection = new UpnpBrowseServiceConnection(mRegistryListener);

		// Set List
		setListAdapter(mListAdapter);
		getListView().setOnItemClickListener(this);

		// Start upnp browse service
		Intent intent = new Intent(this, UPnPService.class);
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
			UPnPDeviceDisplay deviceDisplay = (UPnPDeviceDisplay) listview.getItemAtPosition(position);
			Device<?, ?, ?> device = deviceDisplay.getDevice();

			Log.d(TAG, "start ControlPointActivity");
			Intent intent = new Intent(this, ShareControlPointActivity.class);

			intent.setType(getIntent().getType());
			intent.putExtra(Intent.EXTRA_STREAM, getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
			intent.putExtra(ShareConstants.NAME, deviceDisplay.toString());
			intent.putExtra(ShareConstants.UDN, device.getIdentity().getUdn().toString());

			startActivity(intent);

			// Service<?, ?> connectionManager =
			// device.findService(ServiceType.valueOf("urn:schemas-upnp-org:service:ConnectionManager:1"));
			// if (null != connectionManager)
			// {
			// Log.d(TAG, "GetProtocolInfo");
			// // mUpnpService.getControlPoint().execute(new
			// ShareGetProtocolInfo(connectionManager));
			// // mUpnpService.getControlPoint().execute(new
			// SharePrepareForConnection(connectionManager, ));
			// }
			// else
			// {
			// Log.d(TAG, "pas de ConnectionManager");
			// }

			// Service<?, ?> avTransport =
			// device.findService(ServiceType.valueOf("urn:schemas-upnp-org:service:AVTransport:1"));
			// if (null != connectionManager)
			// {
			// Log.d(TAG, "GetProtocolInfo");
			// mUpnpService.getControlPoint().execute(new
			// ShareSetAVTransportURI(avTransport, ""));
			// // mUpnpService.getControlPoint().execute(new
			// SharePrepareForConnection(connectionManager, ));
			// }
			// else
			// {
			// Log.d(TAG, "pas de ConnectionManager");
			// }
		}
	}

	private class UpnpBrowseServiceConnection implements ServiceConnection
	{

		UPnPBrowseRegistryListener mListener;

		public UpnpBrowseServiceConnection(UPnPBrowseRegistryListener listener)
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

	public class UpnpDeviceAdapter extends ArrayAdapter<UPnPDeviceDisplay>
	{
		public UpnpDeviceAdapter(Context context)
		{
			super(context, -1, new ArrayList<UPnPDeviceDisplay>());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if (null == view)
			{
				view = LayoutInflater.from(getContext()).inflate(R.layout.device_item, parent, false);
			}
			final ImageView icon = (ImageView) view.findViewById(R.id.device_icon);
			final TextView first = (TextView) view.findViewById(R.id.device_firstLine);
			final TextView second = (TextView) view.findViewById(R.id.device_secondLine);

			UPnPDeviceDisplay device = getItem(position);
			
			if(null == device.getDrawable())
			{
				URL iconUrl = null;
				for (Icon iconIt : device.getDevice().getIcons())
				{
					try
					{
//						iconUrl = new URL(device.getDevice().getDetails().getPresentationURI().getScheme()
//								+ "://"
//								+ device.getDevice().getDetails().getPresentationURI().getHost()
//								+ iconIt.getUri().toString());
						iconUrl = new URL(device.getDevice().getDetails().getPresentationURI().toString()
								+ iconIt.getUri().toString());
								
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				if(null!=iconUrl)
				{
					final PictureLoader loader = new PictureLoader(device, view);
					loader.execute(iconUrl);
				}
			}
			else
			{
				icon.setImageDrawable(device.getDrawable());
			}
			first.setText(device.getDevice().getDisplayString());
			second.setText(device.getDevice().getIdentity().getUdn().toString());

			return view;
		}
	}
	
	public class PictureLoader extends AsyncTask<URL, Integer, Drawable>
	{
		private UPnPDeviceDisplay mDevice;
		private View mView;
		
		public PictureLoader(UPnPDeviceDisplay _device, View _view)
		{
			mDevice = _device;
			mView = _view;
		}

		@Override
		protected Drawable doInBackground(URL... _params)
		{
			try
			{
				return Drawable.createFromStream(_params[0].openStream(), "thumbnail");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Drawable _result)
		{
			if(null != _result)
			{
				mDevice.setDrawable(_result);
				final ImageView thumbnail = (ImageView) mView.findViewById(R.id.device_icon);
				thumbnail.setImageDrawable(_result);
			}
		}
		
	}
}
