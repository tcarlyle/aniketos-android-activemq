package org.example.mqtt;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
	
	@Override
	public Fragment getItem(int index) {

	       switch (index) {
	        case 0:
	            // all mqtt updates
	            return new StatusListFragment();
	        case 1:
	            // service list
	            return new ServiceListFragment();
	        case 2:
	            // test
	            return new ConfigFragment();
	        }
	 
	        return null;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 3;
	}

}
