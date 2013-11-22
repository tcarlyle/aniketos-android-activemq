package org.example.mqtt;

import android.app.Application;

public class MqttApplication extends Application {

	public static final int STATUS_LIST_LOADER = 0x01;
	
	public static String sharedPrefName = "subscriptionList"; 
	public static String address = "tcp://83.212.116.137:1883"; // TODO: possibly add an input for this
	
	//public static String StatusListFragTag = "status_list_frag_tag";
	//public static String ServicesListFragTag = "services_list_frag_tag";
	//public static String ConfigFragTag = "config_frag_tag";
	
	public boolean connection = false;
	
	
	
    public boolean isConnection() {
		return connection;
	}



	public void setConnection(boolean connection) {
		this.connection = connection;
	}

	public static final String statusTab = "Status";
	public static final String servicesTab = "Services";
	public static final String settingsTab = "Settings" ;

	// Tab titles
    public static String[] tabs = { statusTab, servicesTab,settingsTab };

	
}
