package org.example.mqtt;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.json.JSONException;
import org.json.JSONObject;

import org.example.mqtt.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MQTTActivity extends Activity implements OnClickListener{
	
	protected NotificationData notificationData;
	
	private final String TAG = "MQTTClient";
	
	ListView listview =  null;
	ArrayAdapter<String> listAdapter = null; 
	 
	Button connectButton = null;
	Button disconnectButton = null;

	
	private ProgressDialog progressDialog = null;
	
	static final int DISCONNECT = 0;
	static final int CONNECT = 1;

	
	MQTT mqtt = null;
	
	CallbackConnection  connection = null;
	
	Handler mHandler = null;
	
	private static boolean activityVisible;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    	mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                int arg = msg.arg1;
                
                switch (arg) {                
	                case DISCONNECT: 
	                	goToDisconnectedMode();
						if(activityVisible)
							toast("Disconnected");
	                break;
	                
	                case CONNECT:  
	                	goToConnectedMode();
	                break;
                
                }
                
                //call setText here
            }
    	};
        
        notificationData = new NotificationData(this);
        activityVisible = true;
        setupView();
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	activityVisible = false;
    	
    	disconnect();
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	activityVisible = true;
    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.connect_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
        case R.id.config:
        	Intent launchNewIntent = new Intent(MQTTActivity.this,ConfigActivity.class);
        	startActivityForResult(launchNewIntent, 0);
            return true;            
        }
        return false;
    }
    
    
    public void goToConnectedMode(){
	
		connectButton.setEnabled(false);
		disconnectButton.setEnabled(true);
    }
    
    public void goToDisconnectedMode(){
    	

		connectButton.setEnabled(true);
    	disconnectButton.setEnabled(false);
    }

    
    
    public void setupView()
    {
    	// lock the screen in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	
    	
    	listview = (ListView) findViewById(R.id.receiveItemListView);
    	
    	// TODO: get them from DB
    	String[] values = new String[] { "empty"};
    	final ArrayList<String> list = new ArrayList<String>();
    	for (int i = 0; i < values.length; ++i) {
	      list.add(values[i]);
	    }
    	listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, list);
    	listview.setAdapter(listAdapter);
    	
    	connectButton = (Button)findViewById(R.id.connectButton);
    	connectButton.setOnClickListener(this);
    	
    	disconnectButton = (Button)findViewById(R.id.disconnectButton);
    	disconnectButton.setOnClickListener(this);
    	
    }

	public void onClick(View v) {
		if(v == connectButton)
		{	
			// check if app has been configured, in other words, if some subscriptions have been added
        	SharedPreferences keyValues = getSharedPreferences(MqttApplication.sharedPrefName, Context.MODE_PRIVATE);
        	if (keyValues.getAll().isEmpty()){
        		toast("you should first add some subscriptions on the config tab");
        	}else{
				connect();
        	}
		}
		
		if(v == disconnectButton)
		{
			disconnect();
		}
		

	}
	
	// callback used for Future
	<T> Callback<T> onui(final Callback<T> original) {
		return new Callback<T>() {
			public void onSuccess(final T value) {
				runOnUiThread(new Runnable(){
					public void run() {
						original.onSuccess(value);
					}
				});
			}
			public void onFailure(final Throwable error) {
				runOnUiThread(new Runnable(){
					public void run() {
						original.onFailure(error);
					}
				});
			}
		};
	}
	
	private void connect()
	{
		mqtt = new MQTT();
		mqtt.setKeepAlive((short)2);

		try
		{
			mqtt.setHost(MqttApplication.address);
			Log.d(TAG, "Address set: " + MqttApplication.address);
		}
		catch(URISyntaxException urise)
		{
			Log.e(TAG, "URISyntaxException connecting to - " + urise);
		}
	    TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
	    mqtt.setClientId(tm.getDeviceId());
		mqtt.setCleanSession(false);
		connection = mqtt.callbackConnection();
		progressDialog = ProgressDialog.show(this, "", 
                "Connecting...", true);
		connection.connect(onui(new Callback<Void>(){
			public void onSuccess(Void value) {
				//connectButton.setEnabled(false);
				Log.d(TAG, "on success connection");
				connection.listener(new MessageListener());
				progressDialog.dismiss();
				//toast("Connected");
				Log.d(TAG, "Connected ");
				
				// get topics from shared preferences to subscribe
				
		    	SharedPreferences keyValues = getSharedPreferences(MqttApplication.sharedPrefName, Context.MODE_PRIVATE);
		    	Map<String, String> initialSubs = (Map<String, String>) keyValues.getAll();
		    	Topic[] topics = new Topic[initialSubs.size()]; 

		    	int i= 0;
		    	for (Map.Entry<String, String> entry : initialSubs.entrySet()){
		    		topics[i] = new Topic(UTF8Buffer.utf8(entry.getKey()), QoS.EXACTLY_ONCE);
		    		Log.d(TAG, "Added topic" + entry.getKey());
		    		i++;
		    	}
							
				// now trying to subscribe
				connection.subscribe(topics,onui (new OnsubscribeCallback()));
				
			}
			public void onFailure(Throwable e) {
				Log.d(TAG, "on failure connection ");
				toast("Problem connecting to host");
				Log.e(TAG, "Exception connecting to " + MqttApplication.address + " - " + e);
				progressDialog.dismiss();
			}
		}));

	}
	private void toast(String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	
	private void disconnect()
	{
		//connectButton.setEnabled(true);
		try
		{
			if(connection != null )
			{
				
				
				// get topics from shared preferences to subscribe
				
		    	SharedPreferences keyValues = getSharedPreferences(MqttApplication.sharedPrefName, Context.MODE_PRIVATE);
		    	Map<String, String> initialSubs = (Map<String, String>) keyValues.getAll();
		    	UTF8Buffer[] topics = new UTF8Buffer[initialSubs.size()]; 

		    	int i= 0;
		    	for (Map.Entry<String, String> entry : initialSubs.entrySet()){
		    		topics[i] = new UTF8Buffer(entry.getKey());
		    		Log.d(TAG, "Unsubscribed topic" + entry.getKey());
		    		i++;
		    	}
					
				
				
				connection.unsubscribe(topics, onui(new UnsubscribeCallback()));
				connection.disconnect(onui(new Callback<Void>(){
					public void onSuccess(Void value) {
						//connectButton.setEnabled(true);
						goToDisconnectedMode();
						if(activityVisible)
								toast("Disconnected");
						connection = null;
					}
					public void onFailure(Throwable e) {
						toast("Problem disconnecting");
						Log.e(TAG, "Exception disconnecting from " + MqttApplication.address + " - " + e);
					}
				}));
			}

		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception " + e);
		}
	}
	
	
	
	// subscribed callback
	private class OnsubscribeCallback  implements Callback <byte[]> {
		public void onSuccess(byte[] subscription) {
			
			goToConnectedMode();
			toast("Connected and Subscribed");
		}	
			
				
		public void onFailure(Throwable e) {
			connection.suspend();// perhaps change for disconnect
			//connectButton.setEnabled(true);
			Log.e(TAG, "Exception when subscribing: " + e);
			toast("Subscription failed");
		}

	}
	
	// unsubscribed callback
	private class UnsubscribeCallback  implements Callback <Void> {
		public void onSuccess(Void subscription) {
			
			Log.d(TAG, "Unsubscription worked");

		}	
			
				
		public void onFailure(Throwable e) {
			toast("UnSubscription failed");
		}

	}
	
	

	
	// listener
	
	private class MessageListener implements Listener {

	    public void onDisconnected() {
	    	Log.d(TAG, "got disconnected");
	    	//connectButton.setEnabled(true);
	    	
	    }
	    public void onConnected() {
	    	Log.d(TAG, "got connected on message listener");
	    }

	    public void onPublish(UTF8Buffer topic, Buffer payload, Runnable ack) {
	    	Log.d(TAG, "on publish called");
	    	
	    	
	    	
	        // You can now process a received message from a topic
			String fullPayLoad = new String(payload.data); // I did not find documentation on this, 
			Log.d(TAG, "String size is " + fullPayLoad.length() + " , and data size is " + payload.length);
			
			// but the payload seems to in fact consists of 0x32 0xlen (maybe more than a byte) 0x(topic) 0x(message number - in 2 bytes) 0x(message)   
			String receivedMesageTopic =  topic.toString();
            String[] fullPayLoadParts = fullPayLoad.split(receivedMesageTopic);// TODO: I should probably check if there are characters that needs to be scaped
                        
            Log.d(TAG, "fullpayload = " + fullPayLoad);
            if(fullPayLoadParts.length == 2){
            	// sometimes the payload includes the message ID (2 bytes), sometimes it doesnt....
            	// if the first character is a "{" then it didnt
            	String messagePayLoad;
            	if(fullPayLoadParts[1].charAt(0) == '{')
            		messagePayLoad = fullPayLoadParts[1];
            	else
            		messagePayLoad = fullPayLoadParts[1].substring(2);
                String val = this.insertMessage(messagePayLoad);
    			runOnUiThread(new updateMsgClass(val));
            }
			
	        // Once process execute the ack runnable.
	        ack.run();
	    }
	    public void onFailure(Throwable value) {
			connection.suspend();// perhaps change for disconnect
			//connectButton.setEnabled(true);
			Log.d(TAG, "On failure in the listener...");
			android.os.Message msg = new android.os.Message();
			msg.arg1 = DISCONNECT;
			mHandler.sendMessage(msg);	
		
			
	    }

	    // parse the json message, instert it on the db and return the value
	    private String insertMessage(String messagePayLoad){
	    	String value = null;
	    	try {
				JSONObject jObject = new JSONObject(messagePayLoad);
				
				
				notificationData.insert(jObject.getString("serviceId"), jObject.getString("alertType")
			,jObject.getString("description"), jObject.getString("serverTime"), jObject.getString("value"), jObject.getString("threatId"),
			jObject.getInt("threshold"));
				
				value = jObject.getString("value");
				
			} catch (JSONException e) {
				Log.d(TAG, "Failure parsing json message + " + messagePayLoad);
				e.printStackTrace();
			}
	    	
	    	return value;
	    }
	
	}
	
	private class updateMsgClass implements Runnable {
		
		String mPayLoad;
		
		public updateMsgClass (String mPayLoad){
			this.mPayLoad = mPayLoad;
		}
		
		public void run() {
			//receiveET.setText(mPayLoad);
			listAdapter.add(mPayLoad);
		}
		
	}
	

	

}