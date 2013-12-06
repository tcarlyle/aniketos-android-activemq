package org.example.mqtt;

import org.example.mqtt.data.NotificationContentProvider;
import org.example.mqtt.data.NotificationCursorAdapter;
import org.example.mqtt.data.NotificationData;
import org.example.mqtt.model.NotifService;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class StatusListFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {

	private final String TAG = "StatusListFragment";
	
	private NotificationCursorAdapter adapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] uiBindFrom = { NotificationData.SERVICE_ID, NotificationData.ALERT_TYPE, NotificationData.SERVER_TIME, NotificationData.DESCRIPTION, NotificationData.VALUE };
	    int[] uiBindTo = { R.id.service, R.id.alert, R.id.timestamp, R.id.description, R.id.value }; // from all_status_row_layout.xml
	    getLoaderManager().initLoader(MqttApplication.STATUS_LIST_LOADER, null, this);
	    adapter = new NotificationCursorAdapter(
	            getActivity().getApplicationContext(), R.layout.all_status_row_layout,
	            null, uiBindFrom, uiBindTo,
	            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
	    setListAdapter(adapter);
	    Log.d(TAG, "On Create");
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = { NotificationData._ID, NotificationData.ALERT_TYPE, NotificationData.DESCRIPTION,
				NotificationData.SERVER_TIME, NotificationData.SERVICE_ID, NotificationData.THREAT_ID,
				NotificationData.THRESHOLD, NotificationData.VALUE,NotificationData.SERVICE_FULL_URI};

        CursorLoader cursorLoader = new CursorLoader(getActivity(),
        		NotificationContentProvider.CONTENT_URI, projection, null, null,NotificationData.SERVER_TIME + " DESC");
        return cursorLoader;

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
	  public void onListItemClick(ListView l, View v, int position, long id) {
	    // do something with the data
		  Cursor  cursor = (Cursor) getListAdapter().getItem(position);
		  String serviceUri = cursor.getString(cursor.getColumnIndex(NotificationData.SERVICE_FULL_URI));
		  Log.d(TAG, "selected service " + serviceUri);
		  MainActivity m = (MainActivity) getActivity();
		  m.showServiceSpecificNotifications(serviceUri);
	  }

}
