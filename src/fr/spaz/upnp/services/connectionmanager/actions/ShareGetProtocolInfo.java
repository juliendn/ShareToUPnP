package fr.spaz.upnp.services.connectionmanager.actions;

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.support.connectionmanager.callback.GetProtocolInfo;
import org.teleal.cling.support.model.ProtocolInfos;

import android.util.Log;

public class ShareGetProtocolInfo extends GetProtocolInfo
{

	private static final String TAG = "ShareGetProtocolInfo";

	public ShareGetProtocolInfo(Service<?, ?> service, ControlPoint controlPoint)
	{
		super(service, controlPoint);
	}

	public ShareGetProtocolInfo(Service<?, ?> service)
	{
		super(service);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void received(ActionInvocation actionInvocation, ProtocolInfos sinkProtocolInfos, ProtocolInfos sourceProtocolInfos)
	{
		Log.d(TAG, "received");
		// TODO: Check if mimetype is in sinkProtocolInfos
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
	{
		Log.d(TAG, "failure");
	}

	public static ServiceType getServiceType()
	{
		return ServiceType.valueOf("urn:schemas-upnp-org:service:ConnectionManager:1");
	}

}
