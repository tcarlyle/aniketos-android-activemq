package org.example.mqtt;

import java.util.ArrayList;

import org.example.mqtt.data.NotificationData;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AllMQTTUpdatesFragment extends Fragment {
	
	protected NotificationData notificationData;
	
	private final String TAG = "Status fragment";
	
	ListView listview =  null;
	ArrayAdapter<String> listAdapter = null; 
	 


	

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.status_list, container, false);

        setupView(rootView);
        return rootView;
    }
    
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	
    }

    
    
    public void setupView(View rootView)
    {
    	
    	
    	listview = (ListView) rootView.findViewById(R.id.receiveItemListView);
    	Activity parent = getActivity();
    	
    	// TODO: get them from DB
    	String[] values = new String[] { "empty"};
    	final ArrayList<String> list = new ArrayList<String>();
    	for (int i = 0; i < values.length; ++i) {
	      list.add(values[i]);
	    }
    	listAdapter = new ArrayAdapter<String>(parent,android.R.layout.simple_expandable_list_item_1, list);
    	listview.setAdapter(listAdapter);
    	

    	
    }



	

}