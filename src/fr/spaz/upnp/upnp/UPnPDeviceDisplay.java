package fr.spaz.upnp.upnp;

import org.teleal.cling.model.meta.Device;

import android.graphics.drawable.Drawable;

public class UPnPDeviceDisplay
{
	Device<?, ?, ?> mDevice;
	Drawable mDrawable;

	public UPnPDeviceDisplay(Device<?, ?, ?> device)
	{
		mDevice = device;
	}

	public Device<?, ?, ?> getDevice()
	{
		return mDevice;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UPnPDeviceDisplay that = (UPnPDeviceDisplay) o;
		return mDevice.equals(that.getDevice());
	}

	@Override
	public int hashCode()
	{
		return mDevice.hashCode();
	}

	@Override
	public String toString()
	{
		// Display a little star while the device is being loaded
		return mDevice.isFullyHydrated() ? mDevice.getDisplayString() : mDevice.getDisplayString() + " *";
	}
	
	public void setDrawable(Drawable _drawable)
	{
		mDrawable = _drawable;
	}

	public Drawable getDrawable()
	{
		return mDrawable;
	}
}