package org.example.mqtt;

import java.util.List;
import java.util.Vector;

import org.example.mqtt.AddServiceDialogFragment.AddServiceDialogFragmentListener;
import org.example.mqtt.MQTTSubscriberService.IncomingHandler;
import org.example.mqtt.model.NotifService;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends FragmentActivity implements TabListener, AddServiceDialogFragmentListener
, OnClickListener{

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
		
		List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(this, StatusListFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, ServicesListFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, ConfigFragment.class.getName()));
		
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager(),fragments);

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


	// method called when a NotifService has been added by the dialog gragment
	@Override
	public void onAddedService(NotifService service) {
		// TODO: add the service
		Log.d(TAG, "adding Notification service");
		ServicesListFragment frag = (ServicesListFragment) mAdapter.findFragmentByPosition(1);
		if(null != frag){
			if (frag.addService(service) == false)
				Log.d(TAG, "failed to add the service on the fragment");
		}
		
	   
	}

	// onClick to capture the need to call the AddServiceDialog
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
        case R.id.addServiceButton:
            // it was the addServiceButton
            FragmentManager fm =  getSupportFragmentManager();
            AddServiceDialogFragment frag = new AddServiceDialogFragment();
            frag.show(fm, AddServiceDialogFragment.class.getName());
            break;
		}
		
	}
	
	// this function can be called by fragments to invoke the creation and
	// display of a fragment with the service specific notifications
	public void showServiceSpecificNotifications(String serviceURI){
		// Create new fragment and transaction
		Fragment newFragment = Fragment.instantiate(this, ServiceSpecifNotListFragment.class.getName());
		Bundle bundle = new Bundle();
		bundle.putString(MqttApplication.SERVICE_URI_BUNDLE_TAG, serviceURI);
		newFragment.setArguments(bundle);
		
		
		
		android.support.v4.app.FragmentTransaction fft = getSupportFragmentManager().beginTransaction();

		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack

		// test 1
		//Fragment oldServiceFragment = mAdapter.findFragmentByPosition(1);
		//fft.replace(oldServiceFragment., newFragment, "specific_service_notif_list");

		// test 2
		//fft.add(R.id.service_notif_list_fragment, newFragment);
		//fft.show(newFragment);
		//fft.addToBackStack(null);

		// Commit the transaction
		fft.commit();
		

		Log.d(TAG, "show service finished to be called");
	}
	
	// TODO: review this one
	// This the important bit to make sure the back button works when you're nesting fragments. Very hacky, all it takes is some Google engineer to change that ViewPager view tag to break this in a future Android update.
	@Override
	public void onBackPressed() {
	    Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":"+viewPager.getCurrentItem());
	    if (fragment != null) // could be null if not instantiated yet
	    {
	        if (fragment.getView() != null) {
	            // Pop the backstack on the ChildManager if there is any. If not, close this activity as normal.
	            if (!fragment.getChildFragmentManager().popBackStackImmediate()) {
	                finish();
	            }
	        }
	    }
	}

}
