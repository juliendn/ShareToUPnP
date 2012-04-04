package fr.spaz.upnp.actions.connectionmanager;

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.ServiceReference;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.connectionmanager.callback.PrepareForConnection;
import org.teleal.cling.support.model.ConnectionInfo.Direction;
import org.teleal.cling.support.model.ProtocolInfo;

import android.util.Log;

public class SharePrepareForConnection extends PrepareForConnection
{

	private static final String TAG = "SharePrepareForConnection";

	public SharePrepareForConnection(Service<?, ?> service, ControlPoint controlPoint, ProtocolInfo remoteProtocolInfo, ServiceReference peerConnectionManager, int peerConnectionID, Direction direction)
	{
		super(service, controlPoint, remoteProtocolInfo, peerConnectionManager, peerConnectionID, direction);
	}

	public SharePrepareForConnection(Service<?, ?> service, ProtocolInfo remoteProtocolInfo, ServiceReference peerConnectionManager, int peerConnectionID, Direction direction)
	{
		super(service, remoteProtocolInfo, peerConnectionManager, peerConnectionID, direction);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void received(ActionInvocation invocation, int connectionID, int rcsID, int avTransportID)
	{
		Log.d(TAG, "received");
		// TODO: store rcsID
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
	{
		Log.d(TAG, "failure");
	}

}
