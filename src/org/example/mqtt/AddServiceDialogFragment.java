package org.example.mqtt;

import java.util.ArrayList;

import org.example.mqtt.model.NotifService;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class AddServiceDialogFragment extends DialogFragment implements OnClickListener{

	EditText servUriEditText;
	EditText nameEditText; // this is the one that has the done option set on the layout
	
	Button saveButton;
	Button cancelButton;
	
	AddServiceDialogFragmentListener activityCallback;
	
	
	// interface that the activity must implement in order to get a notification
	// when the service is added to the shared preferences
    public interface AddServiceDialogFragmentListener {
        void onAddedService(NotifService service);
    }
	
	// pass as in put a serviceList if you want to update it
/*	public static AddServiceDialogFragment newInstance(ArrayList<NotifService> serviceList){
		AddServiceDialogFragment dialogFragment = new AddServiceDialogFragment();
	    Bundle bundle = new Bundle();
	    bundle.putSerializable("list", serviceList);
	    dialogFragment.setArguments(bundle);

	    return dialogFragment;
	}*/
    
    // override the regular onAttach to set the callback to the activity
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
        	activityCallback = (AddServiceDialogFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_service_dialog_frag, container);
        servUriEditText = (EditText) view.findViewById(R.id.txt_service_uri);
        servUriEditText.setText("pub.http://www.eclipse.org/paho/.ThreatLevelChange.Decreasing reputation", android.widget.TextView.BufferType.EDITABLE);
        nameEditText = (EditText) view.findViewById(R.id.txt_name);
        getDialog().setTitle("Add Service");
        
        saveButton = (Button) view.findViewById(R.id.buttonSave);
        cancelButton = (Button) view.findViewById(R.id.buttonCancel);
        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);


        
        return view;
    }

	@Override
	public void onClick(View v) {
		if(v == saveButton) {

			// Try to insert
        	// sanity test
        	String dest = servUriEditText.getText().toString().trim();
        	String name = nameEditText.getText().toString().trim();
        	
        	if(null != dest && null != name && !(name.isEmpty()) && !(dest.isEmpty())){
            	SharedPreferences keyValues = getActivity().getSharedPreferences(MqttApplication.sharedPrefName, Context.MODE_PRIVATE);
            	if (keyValues.contains(dest)){
            		toast("you are already subscribed to that destination");
            	}
            	else{
                	SharedPreferences.Editor keyValuesEditor = keyValues.edit();
                	keyValuesEditor.putString(dest,name);
                	keyValuesEditor.commit();
                	// Call the activity to notify that the service has been added
                	activityCallback.onAddedService(new NotifService(dest,name));
            	}	

        	}else{
        		toast("sanity check failed");
        	}

        }
		if(v == cancelButton) {
			toast("service not added");
		}
		this.dismiss();
	}
	
	private void toast(String message)
	{
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
	}

	
}
