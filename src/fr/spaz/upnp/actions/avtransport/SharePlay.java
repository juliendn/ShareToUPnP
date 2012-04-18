package fr.spaz.upnp.actions.avtransport;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.callback.Play;

import android.util.Log;

public class SharePlay extends Play
{
	private static final String TAG = "SharePlay";

	public SharePlay(Service<?, ?> service, String speed)
	{
		super(service, speed);
	}

	public SharePlay(Service<?, ?> service)
	{
		super(service);
	}

	public SharePlay(UnsignedIntegerFourBytes instanceId, Service<?, ?> service, String speed)
	{
		super(instanceId, service, speed);
	}

	public SharePlay(UnsignedIntegerFourBytes instanceId, Service<?, ?> service)
	{
		super(instanceId, service);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void success(ActionInvocation invocation)
	{
		super.success(invocation);
		Log.i(TAG, "success");
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
