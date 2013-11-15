package org.example.mqtt;

import org.example.mqtt.MQTTSubscriberService.IncomingHandler;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Button;

public class MainActivity extends FragmentActivity implements TabListener {

	private final String TAG = "MQTT main activity";
	
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;

    
    static final int MSG_CONNECTED = 1;
    static final int MSG_DISCONNECTED = 2;
    static final int MSG_NEW_MESSAGE = 3;
    
    private Messenger mService = null;// handletToService
    
    boolean isBound = false;
    
    
    // connect == true
    // disconnect == false
    private void setConnectButtons(boolean connected){
    	Button connectButton = (Button) findViewById(R.id.connectButton);
    	Button disconnectButton = (Button) findViewById(R.id.disconnectButton);
    	if(null != connectButton && null != disconnectButton){
    		connectButton.setEnabled(!connected);
			disconnectButton.setEnabled(connected);
    	}
    }
    
    /**
     * Target we publish for clients to send messages to myself.
     */
    final Messenger clientMessenger = new Messenger(new MainActMsgHandler());
    /**
     * Handler of incoming messages from clients
     * As I am expecting to communicate only with the Main activity thread
     * I will not get the replyTo from the message all the time to know who
     * to answer to
     */
    
    
    class MainActMsgHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECTED:
                	setConnectButtons(true);
                	// TODO: maybe add a toast
                    break;
                case MSG_DISCONNECTED:
                	setConnectButtons(false);
                    break;
                case MSG_NEW_MESSAGE:
                    
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    

    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            isBound = true;
            //textStatus.setText("Attached.");
        	Log.d(TAG, "connection created, got the handler to the service in the activy.");
        	
        	// gonna send my handler to activity
        	try {
                Message msg = Message.obtain(null,
                		MQTTSubscriberService.MSG_BIND);
                msg.replyTo = clientMessenger;
                mService.send(msg);

            } catch (RemoteException e) {
            	Log.e(TAG, "exception sending message to service - " + e);
            }


        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            isBound = false;
            //textStatus.setText("Disconnected.");
            Log.d(TAG, "lost the handler to the service in the activy.");
        }
    };
    
    private void doBindService() {
    	Intent bindingIntent = new  Intent(this, MQTTSubscriberService.class);
    	startService(bindingIntent);  
    	bindService(bindingIntent, mConnection, Context.BIND_AUTO_CREATE);
    	Log.d(TAG, "connection to service being created.");
    }
    
 
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initilization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);		

		// Adding Tabs
		for (String tab_name : MqttApplication.tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}
		Log.d(TAG, "going to bind to the service.");
		doBindService();
		
		/**
		 * on swiping the viewpager make respective tab selected
		 * */
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
    	// lock the screen in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
	}
    
	
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());

	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}
	
	public void sendMessageToMQTTservice(int message){ 
		// gonna send my handler to activity
		try {
	        Message msg = Message.obtain(null,
	        		message);
	        msg.replyTo = clientMessenger;
	        mService.send(msg);
	
	    } catch (RemoteException e) {
	    	Log.e(TAG, "exception sending message to service - " + e);
	    }
	}

}
