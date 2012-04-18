package fr.spaz.upnp.fragments;

import java.util.List;
import java.util.Map;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.controlpoint.SubscriptionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.gena.CancelReason;
import org.teleal.cling.model.gena.GENASubscription;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.state.StateVariableValue;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.callback.GetPositionInfo;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.Seek;
import org.teleal.cling.support.avtransport.callback.Stop;
import org.teleal.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.teleal.cling.support.lastchange.Event;
import org.teleal.cling.support.lastchange.EventedValue;
import org.teleal.cling.support.lastchange.InstanceID;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.SeekMode;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.actionbarsherlock.app.SherlockFragment;

import fr.spaz.upnp.R;
import fr.spaz.upnp.activities.IRenderSelection;

public class ShareControlFragment extends SherlockFragment implements OnClickListener, OnSeekBarChangeListener
{

	private enum PlayState
	{
		PLAY, PAUSE, STOP
	}

	private final static String TAG = "ShareControlFragment";
	private static final int SUB_TIMEOUT = 600;
	private PlayState mState;
	private SubscriptionCallback mLastchange;
	private SeekBar mSeek;
	private TimePositionPoller mPoller;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final AndroidUpnpService service = ((IRenderSelection) getActivity()).getUPnPService();
		final Service<?, ?> avTransportService = ((IRenderSelection) getActivity()).getCurrentRenderer().findService(new UDAServiceType("AVTransport"));

		// callback

		mLastchange = new SubscriptionCallback(avTransportService, SUB_TIMEOUT)
		{

			@Override
			protected void failed(GENASubscription sub, UpnpResponse arg1, Exception arg2, String arg3)
			{
				Log.i(TAG, "subscribtion fail");
			}

			@Override
			protected void eventsMissed(GENASubscription sub, int numberOfMissedEvents)
			{
				Log.i(TAG, "missed event: " + numberOfMissedEvents);
			}

			@Override
			protected void eventReceived(GENASubscription sub)
			{

				try
				{
					Log.i(TAG, "// event received \\\\");
					final Map<String, StateVariableValue<?>> values = sub.getCurrentValues();
					final StateVariableValue<?> lastchange = values.get("LastChange");
					final AVTransportLastChangeParser lastChangeParser = new AVTransportLastChangeParser();
					final Event event = lastChangeParser.parse(lastchange.toString());

					// parse event
					final InstanceID instanceId = event.getInstanceID(new UnsignedIntegerFourBytes(0l));
					final List<EventedValue> list = instanceId.getValues();

					// if(list.contains(PlayMode.)

					for (EventedValue<?> eventedValue : list)
					{
						Log.i(TAG, eventedValue.getName() + ": " + eventedValue.getValue());
					}

					Log.i(TAG, "// \\\\");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			@Override
			protected void established(GENASubscription arg0)
			{
				Log.i(TAG, "subscribtion established");
			}

			@Override
			protected void ended(GENASubscription sub, CancelReason reason, UpnpResponse response)
			{
				Log.i(TAG, "subscribtion ended");
			}
		};
		service.getControlPoint().execute(mLastchange);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.control, container, false);

		view.findViewById(R.id.prev).setOnClickListener(this);
		view.findViewById(R.id.play).setOnClickListener(this);
		view.findViewById(R.id.stop).setOnClickListener(this);
		view.findViewById(R.id.next).setOnClickListener(this);
		mSeek = (SeekBar) view.findViewById(R.id.seek);
		mSeek.setOnSeekBarChangeListener(this);

		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		mPoller = new TimePositionPoller();
		mPoller.execute();
	}

	@Override
	public void onPause()
	{
		mPoller.cancel(false);
		super.onPause();
	}

	@Override
	public void onDestroy()
	{
		// final AndroidUpnpService service = ((IRenderSelection) getActivity()).getUPnPService();
		if (null != mLastchange)
		{
			mLastchange.end();
		}
		super.onDestroy();
	}

	@Override
	public void onProgressChanged(SeekBar view, int position, boolean fromUser )
	{
		if(fromUser)
		{
			final AndroidUpnpService service = ((IRenderSelection) getActivity()).getUPnPService();
			final Device<?, ?, ?> renderer = ((IRenderSelection) getActivity()).getCurrentRenderer();
			if (null != renderer)
			{
				final Service<?, ?> avTransport = renderer.findService(new UDAServiceType("AVTransport"));
				if (null != avTransport)
				{
					final int h = position / 3600;
					final int m = (position % 3600) / 60;
					final int s = position % 60;
					final String time = String.format("%02d:%02d:%02d", h, m, s);
					Log.i(TAG, "time:" + time);
					service.getControlPoint().execute(new Seek(new UnsignedIntegerFourBytes(0), avTransport, SeekMode.REL_TIME, time)
					{
	
						@Override
						public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
						{
							Log.i(TAG, "fail seek");
						}
	
						@Override
						public void success(ActionInvocation invocation)
						{
							super.success(invocation);
							Log.i(TAG, "success seek");
						}
					});
				}
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v)
	{
		final Device<?, ?, ?> currentDevice = ((IRenderSelection) getActivity()).getCurrentRenderer();
		final AndroidUpnpService service = ((IRenderSelection) getActivity()).getUPnPService();

		if (null != currentDevice)
		{
			switch (v.getId())
			{
				case R.id.play :
					service.getControlPoint().execute(new Play(new UnsignedIntegerFourBytes(0), currentDevice.findService(new UDAServiceType("AVTransport")))
					{
						@Override
						public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
						{
							Log.i(TAG, "fail play");
						}

						@Override
						public void success(ActionInvocation invocation)
						{
							super.success(invocation);
							Log.i(TAG, "success play");
						}
					});
					break;

				case R.id.stop :
					service.getControlPoint().execute(new Stop(new UnsignedIntegerFourBytes(0), currentDevice.findService(new UDAServiceType("AVTransport")))
					{
						@Override
						public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
						{
							Log.i(TAG, "fail stop");
						}

						@Override
						public void success(ActionInvocation invocation)
						{
							super.success(invocation);
							Log.i(TAG, "success stop");
						}
					});

					break;

				case R.id.prev :
				case R.id.next :
					break;
			}
		}
	}

	private class TimePositionPoller extends AsyncTask<Void, Integer, Void>
	{

		@Override
		protected Void doInBackground(Void... params)
		{
			final AndroidUpnpService service = ((IRenderSelection) getActivity()).getUPnPService();
			final Device<?, ?, ?> renderer = ((IRenderSelection) getActivity()).getCurrentRenderer();
			if (null != renderer)
			{
				final Service<?, ?> avTransport = renderer.findService(new UDAServiceType("AVTransport"));
				if (null != avTransport)
				{
					Log.i(TAG, "Begin polling position");
					while (!isCancelled())
					{
						service.getControlPoint().execute(new GetPositionInfo(new UnsignedIntegerFourBytes(0), avTransport)
						{

							@Override
							public void failure(ActionInvocation inv, UpnpResponse arg1, String arg2)
							{
								Log.i(TAG, "fail getPositionInfo");
								cancel(false);
							}

							@Override
							public void received(ActionInvocation inv, PositionInfo position)
							{
								TimePositionPoller.this.publishProgress((int) position.getTrackDurationSeconds(), (int) position.getTrackElapsedSeconds());
							}
						});
						try
						{
							Thread.sleep(2000);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
					Log.i(TAG, "Stop polling position");
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			int count = 0;
			while ((count + 2) <= values.length)
			{
				mSeek.setMax(values[count]);
				mSeek.setProgress(values[count + 1]);
				count += 2;
			}
		}
	}
}
