package fr.spaz.upnp.fragments;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.types.UDAServiceType;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

import fr.spaz.upnp.activities.IRenderSelection;
import fr.spaz.upnp.upnp.UPnPBrowseRegistryListener;
import fr.spaz.upnp.upnp.UPnPDeviceDisplay;

public class ShareRendererSelectionFragment extends SherlockListFragment
{
	private static final int SEARCH_TIME = 10000;

	private ArrayAdapter<UPnPDeviceDisplay> mListAdapter;
	private UPnPBrowseRegistryListener mRegistryListener;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE);

		final AndroidUpnpService service = ((IRenderSelection) getActivity()).getUPnPService();
		mListAdapter = new ArrayAdapter<UPnPDeviceDisplay>(getActivity(), android.R.layout.simple_list_item_1);
		mRegistryListener = new UPnPBrowseRegistryListener(getActivity(), mListAdapter);

		for (Device<?, ?, ?> device : service.getRegistry().getDevices(new UDAServiceType("AVTransport")))
		{
			mRegistryListener.deviceAdded(device);
		}
		service.getRegistry().addListener(mRegistryListener);
		service.getControlPoint().search(SEARCH_TIME);
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		setListAdapter(mListAdapter);
		setEmptyText("No renderer found");
		getListView().setOnItemClickListener((OnItemClickListener) getActivity());
    }

	@Override
	public void onDestroy()
	{
		final AndroidUpnpService service = ((IRenderSelection) getActivity()).getUPnPService();

		if (service != null)
		{
			service.getRegistry().removeListener(mRegistryListener);
		}
		getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE);
		super.onDestroy();
	}

	@Override
	public void onPause()
	{
		 final AndroidUpnpService service = ((IRenderSelection) getActivity()).getUPnPService();
		
		 if (null != service && null != service.getRegistry())
		 {
		 service.getRegistry().pause();
		 }
		super.onPause();
	}

	@Override
	public void onResume()
	{
		 final AndroidUpnpService service = ((IRenderSelection) getActivity()).getUPnPService();
		
		 if (null != service && null != service.getRegistry())
		 {
		 service.getRegistry().resume();
		 }
		super.onResume();
	}
}
