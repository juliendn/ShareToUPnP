package fr.spaz.upnp.upnp;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;

import android.app.Activity;
import android.widget.Toast;
import fr.spaz.upnp.activities.ShareRendererSelectionActivity.UpnpDeviceAdapter;

public class UPnPBrowseRegistryListener extends DefaultRegistryListener
{

	private UpnpDeviceAdapter mAdapter;
	private Activity mUiThread;

	public UPnPBrowseRegistryListener(Activity uiThread, UpnpDeviceAdapter adapter)
	{
		mUiThread = uiThread;
		mAdapter = adapter;
	}

	@Override
	public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device)
	{
		deviceAdded(device);
	}

	@Override
	public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex)
	{
		mUiThread.runOnUiThread(new Runnable()
		{
			public void run()
			{
				Toast.makeText(mUiThread, "Discovery failed of '" + device.getDisplayString() + "': " + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"), Toast.LENGTH_LONG).show();
			}
		});
		deviceRemoved(device);
	}

	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice device)
	{
		deviceAdded(device);
	}

	@Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice device)
	{
		deviceRemoved(device);
	}

	@Override
	public void localDeviceAdded(Registry registry, LocalDevice device)
	{
		deviceAdded(device);
	}

	@Override
	public void localDeviceRemoved(Registry registry, LocalDevice device)
	{
		deviceRemoved(device);
	}

	public void deviceAdded(final Device<?, ?, ?> device)
	{
		if ("MediaRenderer".equals(device.getType().getType()))
		{
			mUiThread.runOnUiThread(new Runnable()
			{
				public void run()
				{
					UPnPDeviceDisplay d = new UPnPDeviceDisplay(device);
					int position = mAdapter.getPosition(d);
					if (position >= 0)
					{
						// Device already in the list, re-set new value at same position
						mAdapter.remove(d);
						mAdapter.insert(d, position);
					}
					else
					{
						mAdapter.add(d);
					}
				}
			});
		}
	}

	public void deviceRemoved(final Device<?, ?, ?> device)
	{
		if ("MediaRenderer".equals(device.getType().getType()))
		{
			mUiThread.runOnUiThread(new Runnable()
			{
				public void run()
				{
					mAdapter.remove(new UPnPDeviceDisplay(device));
				}
			});
		}
	}
}
