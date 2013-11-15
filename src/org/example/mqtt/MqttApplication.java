package org.example.mqtt;

import android.app.Application;

public class MqttApplication extends Application {

	public static final int STATUS_LIST_LOADER = 0x01;
	
	public static String sharedPrefName = "subscriptionList"; 
	public static String address = "tcp://83.212.116.137:1883"; // TODO: possibly add an input for this
	
	public boolean connection = false;
	
	
	
    public boolean isConnection() {
		return connection;
	}



	public void setConnection(boolean connection) {
		this.connection = connection;
	}



	// Tab titles
    public static String[] tabs = { "Status", "Services", "Settings" };

	
}
