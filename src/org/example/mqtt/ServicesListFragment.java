package org.example.mqtt;

import java.util.ArrayList;
import java.util.Map;

import org.example.mqtt.data.NotificationContentProvider;
import org.example.mqtt.data.NotificationData;
import org.example.mqtt.model.NotifService;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ServicesListFragment extends ListFragment  {

	private final String TAG = "ServicesListFragment";
	Button addButton = null;
	
	ServiceNotifRowAdapter adapter;
	ArrayList<NotifService> list = new ArrayList<NotifService>();
	
	// to be called if one wants to change the dataset
	// basically to be used by the main activity after the Service Add dialog
	// inserts a new service on the dataset
	public boolean addService(NotifService serv){
		boolean ret = list.add(serv);
		if(ret)
			adapter.notifyDataSetChanged();
		return ret;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Activity parent = getActivity();
		
	   	// populating adapter with list of stored subscriptions
    	SharedPreferences keyValues = parent.getSharedPreferences(MqttApplication.sharedPrefName, Context.MODE_PRIVATE);
    	Map<String, String> initialSubs = (Map<String, String>) keyValues.getAll();
   
    	
    	for (Map.Entry<String, String> entry : initialSubs.entrySet()){
    		list.add(new NotifService(entry.getKey(),entry.getValue()));
    		Log.d(TAG, "Retrieved subscription" + entry.getValue());
    	}
    	
    	
    	

    	adapter = new ServiceNotifRowAdapter(getActivity().getApplicationContext(),list);
	    setListAdapter(adapter);
	    Log.d(TAG, "On Create");
	}
	
	  @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	 
	        View rootView = inflater.inflate(R.layout.servicelist_fragment, container, false);

	       	addButton = (Button) rootView.findViewById(R.id.addServiceButton);
	       	MainActivity m = (MainActivity) getActivity();
	    	addButton.setOnClickListener(m);
	    	Log.d(TAG, "On CreateView");
	        return rootView;
	    }
	  
	  @Override
	  public void onListItemClick(ListView l, View v, int position, long id) {
	    // do something with the data
		  NotifService  item = (NotifService) getListAdapter().getItem(position);
		  Log.d(TAG, "selected service " + item.getServiceURI());
		//  MainActivity m = (MainActivity) getActivity();
		//  m.showServiceSpecificNotifications(item.getServiceURI());
		  

		Fragment newFragment = new ServiceSpecifNotListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(MqttApplication.SERVICE_URI_BUNDLE_TAG, item.getServiceURI());
		newFragment.setArguments(bundle);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.servicelist_fragment, newFragment).commit();
		  
	  }

		private void toast(String message)
		{
			Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
		}


		@Override
		public void onDestroyView(){
			super.onDestroyView();
			Log.d(TAG, "on destroy view");
		}
		
		@Override
		public void onDestroy(){
			super.onDestroy();
			Log.d(TAG, "on destroy");
		}	
		
}
