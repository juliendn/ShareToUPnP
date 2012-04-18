package fr.spaz.upnp.upnp;

import org.teleal.cling.model.meta.Device;

public class UPnPDeviceDisplay
{
	Device<?, ?, ?> device;

	public UPnPDeviceDisplay(Device<?, ?, ?> device)
	{
		this.device = device;
	}

	public Device<?, ?, ?> getDevice()
	{
		return device;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UPnPDeviceDisplay that = (UPnPDeviceDisplay) o;
		return device.equals(that.device);
	}

	@Override
	public int hashCode()
	{
		return device.hashCode();
	}

	@Override
	public String toString()
	{
		// Display a little star while the device is being loaded
		return device.isFullyHydrated() ? device.getDisplayString() : device.getDisplayString() + " *";
	}
}