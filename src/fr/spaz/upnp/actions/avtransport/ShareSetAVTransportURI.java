package fr.spaz.upnp.actions.avtransport;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;

import android.util.Log;

public class ShareSetAVTransportURI extends SetAVTransportURI
{

	private static final String TAG = "ShareSetAVTransportURI";

	public ShareSetAVTransportURI(Service<?, ?> service, String uri, String metadata)
	{
		super(service, uri, metadata);
	}

	public ShareSetAVTransportURI(Service<?, ?> service, String uri)
	{
		super(service, uri);
	}

	public ShareSetAVTransportURI(UnsignedIntegerFourBytes instanceId, Service<?, ?> service, String uri, String metadata)
	{
		super(instanceId, service, uri, metadata);
	}

	public ShareSetAVTransportURI(UnsignedIntegerFourBytes instanceId, Service<?, ?> service, String uri)
	{
		super(instanceId, service, uri);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void success(ActionInvocation invocation)
	{
		Log.i(TAG, "success");
		super.success(invocation);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
	{
		Log.i(TAG, "failure");
		Log.i(TAG, "operation: " + operation.getStatusCode() + " " + operation.getStatusMessage());
		Log.i(TAG, "defaultMsg: " + defaultMsg);
	}

}
