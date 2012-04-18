package fr.spaz.upnp.activities;

import org.teleal.cling.android.AndroidUpnpService;

import fr.spaz.upnp.upnp.UPnPDeviceDisplay;

public interface IRenderSelection
{
	public AndroidUpnpService getUPnPService();
	public void setCurrentRenderer(UPnPDeviceDisplay device);
	public UPnPDeviceDisplay detCurrentRenderer();
}
