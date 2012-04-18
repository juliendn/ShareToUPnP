package fr.spaz.upnp.activities;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.Device;

public interface IRenderSelection
{
	public AndroidUpnpService getUPnPService();
	public Device<?, ?, ?> getCurrentRenderer();
}
