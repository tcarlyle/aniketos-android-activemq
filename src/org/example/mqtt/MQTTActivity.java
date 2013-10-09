package org.example.mqtt;

import java.net.URISyntaxException;
import java.util.ArrayList;

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
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
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
	
	EditText destinationET = null;
	EditText messageET = null;
	//EditText receiveET = null;
	EditText userNameET = null;
	EditText passwordET = null;
	ListView listview =  null;
	ArrayAdapter<String> listAdapter = null; 
	 
	Button connectButton = null;
	Button disconnectButton = null;
	Button sendButton = null;
	
	private ProgressDialog progressDialog = null;
	
	static String sAddress = "tcp://83.212.116.137:1883";
	String sUserName = null;
	String sPassword = null;
	String sDestination = null;
	String sMessage = null;
	
	MQTT mqtt = null;
	
	CallbackConnection  connection = null;
	
	private static boolean activityVisible;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
    
    
    public void goToConnectedMode(){
		userNameET.setEnabled(false);
		passwordET.setEnabled(false);
		destinationET.setEnabled(false);
		
		connectButton.setEnabled(false);
		sendButton.setEnabled(true);
		disconnectButton.setEnabled(true);
    }
    
    public void goToDisconnectedMode(){
    	
		userNameET.setEnabled(true);
		passwordET.setEnabled(true);
		destinationET.setEnabled(true);
    	
		connectButton.setEnabled(true);
    	sendButton.setEnabled(false);
    	disconnectButton.setEnabled(false);
    }

    
    
    public void setupView()
    {
    	// lock the screen in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	
    	userNameET = (EditText)findViewById(R.id.userNameEditText);
    	userNameET.setText("topicadmin", android.widget.TextView.BufferType.EDITABLE);
    	passwordET = (EditText)findViewById(R.id.passwordEditText);
    	passwordET.setText("xvAFhQtg", android.widget.TextView.BufferType.EDITABLE);
    	destinationET = (EditText)findViewById(R.id.destinationEditText);
    	destinationET.setText("TrustworthinessComponent", android.widget.TextView.BufferType.EDITABLE);
    	messageET = (EditText)findViewById(R.id.messageEditText);
    	//receiveET = (EditText)findViewById(R.id.receiveEditText);
    	listview = (ListView) findViewById(R.id.receiveItemListView);
    	
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
    	
    	
    	sendButton = (Button)findViewById(R.id.sendButton);
    	sendButton.setOnClickListener(this);
    	sendButton.setEnabled(false);
    }

	public void onClick(View v) {
		if(v == connectButton)
		{	
			sUserName = userNameET.getText().toString().trim();
			sPassword = passwordET.getText().toString().trim();
			sDestination = destinationET.getText().toString().trim();
			
			if(sAddress.equals(""))
			{
				toast("Address must be provided");
			}
			else
			{
				connect();
			}
		}
		
		if(v == disconnectButton)
		{
			disconnect();
		}
		
		if(v == sendButton)
		{
			
			sMessage = messageET.getText().toString().trim();
			send();

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
			mqtt.setHost(sAddress);
			Log.d(TAG, "Address set: " + sAddress);
		}
		catch(URISyntaxException urise)
		{
			Log.e(TAG, "URISyntaxException connecting to " + sAddress + " - " + urise);
		}
		
		if(sUserName != null && !sUserName.equals(""))
		{
			mqtt.setClientId(sUserName);
			mqtt.setUserName(sUserName);
			Log.d(TAG, "UserName set: [" + sUserName + "]");
		}
		
		if(sPassword != null && !sPassword.equals(""))
		{
			mqtt.setPassword(sPassword);
			Log.d(TAG, "Password set: [" + sPassword + "]");
		}
		
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
				
				
				// we add "?consumer.retroactive=true" to the topic to specify that the connection is retroactive
				// as per http://activemq.apache.org/retroactive-consumer.html
				Topic[] topics = {new Topic(UTF8Buffer.utf8(sDestination), QoS.EXACTLY_ONCE) };
				
				// now trying to connect
				connection.subscribe(topics,onui (new OnsubscribeCallback()));
				
			}
			public void onFailure(Throwable e) {
				Log.d(TAG, "on failure connection ");
				toast("Problem connecting to host");
				Log.e(TAG, "Exception connecting to " + sAddress + " - " + e);
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
				
				connection.unsubscribe(new UTF8Buffer[]{UTF8Buffer.utf8(sDestination)}, onui(new UnsubscribeCallback()));
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
						Log.e(TAG, "Exception disconnecting from " + sAddress + " - " + e);
					}
				}));
			}

		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception " + e);
		}
	}
	
	private void send()
	{
		if(connection != null)
		{
					// publish message
					connection.publish(sDestination, sMessage.getBytes(), QoS.EXACTLY_ONCE, true, onui(new Callback<Void>() {
			            public void onSuccess(Void v) {
			                // the pubish operation completed successfully.
							//destinationET.setText("");
							messageET.setText("");
							toast("Message sent");
							Log.d(TAG, "sending done");
			              }
			              public void onFailure(Throwable value) {
			            	  toast("Message failed");
			              }
			          }));
	
		}
					
	}
	
	
	// subscribed callback
	private class OnsubscribeCallback  implements Callback <byte[]> {
		public void onSuccess(byte[] subscription) {
			
			Log.d(TAG, "Destination: " + sDestination);
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
	
	// subscribed callback
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
			// but the payload seems to in fact consists of 0x32 0xlen (maybe more than a byte) 0x(topic) 0x(message number - in 2 bytes) 0x(message)   
			String receivedMesageTopic =  topic.toString();
            String[] fullPayLoadParts = fullPayLoad.split(receivedMesageTopic);// TODO: I should probably check if there are characters that needs to be scaped
                        
            if(fullPayLoadParts.length == 2){
                String messagePayLoad = fullPayLoadParts[1].substring(2);
                this.insertMessage(messagePayLoad);
    			runOnUiThread(new updateMsgClass(messagePayLoad));
            }
			
	        // Once process execute the ack runnable.
	        ack.run();
	    }
	    public void onFailure(Throwable value) {
			connection.suspend();// perhaps change for disconnect
			//connectButton.setEnabled(true);
			Log.d(TAG, "On failure in the listener...");
	    }

	    // parse the json message and instert it on the db
	    private void insertMessage(String messagePayLoad){
	    	try {
				JSONObject jObject = new JSONObject(messagePayLoad);
				
				
				notificationData.insert(jObject.getString("serviceId"), jObject.getString("alertType")
			,jObject.getString("description"), jObject.getString("serverTime"), jObject.getString("value"), jObject.getString("threatId"),
			jObject.getInt("threshold"));
				
			} catch (JSONException e) {
				Log.d(TAG, "Failure parsing json message + " + messagePayLoad);
				e.printStackTrace();
			}
	    	
	    	
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