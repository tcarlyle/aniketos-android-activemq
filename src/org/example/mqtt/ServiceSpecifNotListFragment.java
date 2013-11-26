package org.example.mqtt;

import org.example.mqtt.data.NotificationContentProvider;
import org.example.mqtt.data.NotificationCursorAdapter;
import org.example.mqtt.data.NotificationData;

import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.Button;

public class ServiceSpecifNotListFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {

	private final String TAG = "ServiceSpecifNotListFragment";
	Button editServiceButton = null;
	Button deleteServiceButton = null;
	String serviceUri;
	
	private NotificationCursorAdapter adapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
			String[] uiBindFrom = { NotificationData.ALERT_TYPE, NotificationData.DESCRIPTION, NotificationData.VALUE,NotificationData.SERVER_TIME };
		    int[] uiBindTo = { R.id.serv_type, R.id.serv_desc,R.id.notif_val ,R.id.notif_date  }; // from service_specific_notif.row

		    getLoaderManager().initLoader(MqttApplication.SERVICE_SPECIFIC_LIST_LOADER, null, this);
		    adapter = new NotificationCursorAdapter(
		            getActivity().getApplicationContext(), R.layout.service_specifc_notif_row,
		            null, uiBindFrom, uiBindTo,
		            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		    setListAdapter(adapter);
		    
		    
	/*	    Bundle arg = this.getArguments();
		    serviceUri = arg.getString(MqttApplication.SERVICE_URI_BUNDLE_TAG);
		    // TODO move the serviceURI null check here*/
		    Log.d(TAG, "On Create ");

	}
	
	  @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	 
	        View rootView = inflater.inflate(R.layout.service_notif_list_fragment, container, false);

	    	Log.d(TAG, "On CreateView");
	        return rootView;
	    }
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundleArg) {
		String[] projection = { NotificationData._ID, NotificationData.ALERT_TYPE, NotificationData.DESCRIPTION,
				NotificationData.SERVER_TIME, NotificationData.SERVICE_FULL_URI, 	NotificationData.THRESHOLD, NotificationData.VALUE};

	    Bundle arg = this.getArguments();
	    serviceUri = arg.getString(MqttApplication.SERVICE_URI_BUNDLE_TAG);
	    // TODO move the serviceURI null check here
	    Log.d(TAG, "On createLoader with serviceuri = " + serviceUri);
		
		if(null != serviceUri){
			String selection = NotificationData.SERVICE_FULL_URI + " = ?";
			String [] selectionArgs =  {serviceUri};
	        CursorLoader cursorLoader = new CursorLoader(getActivity(),
	        		NotificationContentProvider.CONTENT_URI, projection, selection, selectionArgs, NotificationData.SERVER_TIME + " DESC");
	        return cursorLoader;
		}
		else{
			Log.d(TAG, "failed to retrieve service name");
			return null;
			//TODO: find a destroy self
		}
		

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter.swapCursor(cursor);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
		
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
