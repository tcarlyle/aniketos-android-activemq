package org.example.mqtt.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.example.mqtt.R;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

// cursor adaptor that translates the date from long to string
public class NotificationCursorAdapter extends SimpleCursorAdapter {

	private final String TAG = "NotificationCursorAdapter";
	
	public NotificationCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		// TODO Auto-generated constructor stub
	}
	
    @Override
    public void setViewText(TextView v, String text) {
        if (v.getId() == R.id.notif_date || v.getId() == R.id.timestamp) { // time column from service_specifc_notif_row.xml or all_status_row_layout.xml
        		  //Log.d(TAG, "text is "+ text);
        		  long l =  Long.valueOf(text).longValue();
				  Date d = new Date(l);
				    SimpleDateFormat format =
					        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				    try{
				    	text = format.format(d.getTime());
				    }
				    catch(IllegalArgumentException e){
				    	Log.e(TAG, "long is " + l + " and exception is " + e.getMessage());
				    }
			
        }
        super.setViewText(v,text);
        
    }



}
