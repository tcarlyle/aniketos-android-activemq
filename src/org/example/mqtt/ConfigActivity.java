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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ConfigActivity extends Activity {

	private final String TAG = "ConfigActivityClient";
	
	EditText destinationET = null;
	EditText nameET = null;
	ListView listview =  null;
	ArrayAdapter<String> listAdapter = null; 

	
	Button addButton = null;
	
	

    @Override
    public boolean onCreateOptionsMenu(Menu menu){        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.config_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
        case R.id.connect:
        	Intent launchNewIntent = new Intent(ConfigActivity.this,MQTTActivity.class);
        	startActivityForResult(launchNewIntent, 0);
            return true;            
        }
        return false;
    }
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);
        setupView();
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
	
	
	
	
	
	
	
    public void setupView()
    {
    	// lock the screen in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	
    	destinationET = (EditText)findViewById(R.id.destinationEditText);
    	destinationET.setText("pub.http://www.eclipse.org/paho/.ThreatLevelChange.Decreasing reputation", android.widget.TextView.BufferType.EDITABLE);
    	
    	nameET = (EditText)findViewById(R.id.nameEditText);
    	
    	listview = (ListView) findViewById(R.id.subscriptionsItemListView);
    	
    	// populating adapter with list of stored subscriptions
    	SharedPreferences keyValues = getSharedPreferences(MqttApplication.sharedPrefName, Context.MODE_PRIVATE);
    	Map<String, String> initialSubs = (Map<String, String>) keyValues.getAll();
   
    	final ArrayList<String> list = new ArrayList<String>();
    	for (Map.Entry<String, String> entry : initialSubs.entrySet()){
    		list.add(entry.getValue());
    		Log.d(TAG, "Added subscription" + entry.getValue());
    	}
    	listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, list);
    	listview.setAdapter(listAdapter);
    	
    	
    	
    	addButton = (Button)findViewById(R.id.addSubscriptionButton);
    	addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	// sanity test
            	String dest = destinationET.getText().toString().trim();
            	String name = nameET.getText().toString().trim();
            	
            	if(null != dest && null != name && !(name.isEmpty()) && !(dest.isEmpty())){
                	SharedPreferences keyValues = getSharedPreferences(MqttApplication.sharedPrefName, Context.MODE_PRIVATE);
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
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
}
