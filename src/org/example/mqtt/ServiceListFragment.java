package org.example.mqtt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ServiceListFragment extends Fragment {

	private final String TAG = "ConfigActivityClient";
	
	EditText destinationET = null;
	EditText nameET = null;
	ListView listview =  null;
	ArrayAdapter<String> listAdapter = null; 

	
	Button addButton = null;
	
	

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.service_list, container, false);
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

    	Activity parent = getActivity();
    	
    	destinationET = (EditText) rootView.findViewById(R.id.destinationEditText);
    	destinationET.setText("pub.http://www.eclipse.org/paho/.ThreatLevelChange.Decreasing reputation", android.widget.TextView.BufferType.EDITABLE);
    	
    	nameET = (EditText) rootView.findViewById(R.id.nameEditText);
    	
    	listview = (ListView) rootView.findViewById(R.id.subscriptionsItemListView);
    	
    	// populating adapter with list of stored subscriptions
    	SharedPreferences keyValues = parent.getSharedPreferences(MqttApplication.sharedPrefName, Context.MODE_PRIVATE);
    	Map<String, String> initialSubs = (Map<String, String>) keyValues.getAll();
   
    	final ArrayList<String> list = new ArrayList<String>();
    	for (Map.Entry<String, String> entry : initialSubs.entrySet()){
    		list.add(entry.getValue());
    		Log.d(TAG, "Added subscription" + entry.getValue());
    	}
    	listAdapter = new ArrayAdapter<String>(parent.getBaseContext(),android.R.layout.simple_list_item_1, list);
    	listview.setAdapter(listAdapter);
    	
    	
    	
    	addButton = (Button) rootView.findViewById(R.id.addSubscriptionButton);
    	addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	// sanity test
            	String dest = destinationET.getText().toString().trim();
            	String name = nameET.getText().toString().trim();
            	
            	if(null != dest && null != name && !(name.isEmpty()) && !(dest.isEmpty())){
                	SharedPreferences keyValues = getActivity().getSharedPreferences(MqttApplication.sharedPrefName, Context.MODE_PRIVATE);
                	if (keyValues.contains(dest)){
                		toast("you are already subscribed to that destination");
                	}
                	else{
	                	SharedPreferences.Editor keyValuesEditor = keyValues.edit();
	                	keyValuesEditor.putString(dest,name);
	                	keyValuesEditor.commit();
	                	listAdapter.add(name);
                	}	

            	}else{
            		toast("Please add name and destination");
            	}
            	

            	
            }
        });
  
    }

	private void toast(String message)
	{
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
	}
}
