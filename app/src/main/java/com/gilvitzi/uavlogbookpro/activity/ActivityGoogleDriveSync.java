package com.gilvitzi.uavlogbookpro.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.gilvitzi.uavlogbookpro.AnalyticsApplication;
import com.gilvitzi.uavlogbookpro.export.GoogleDriveSyncTask;
import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;
import com.gilvitzi.uavlogbookpro.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;



public class ActivityGoogleDriveSync extends Activity {
	
	private static final String LOG_TAG = "ActivityGoogleDriveSync";
	private static final String screen_name = "Google Drive Sync";
	
	private boolean sync_on;
	
	public LogbookDataSource datasource;
	
    //Google Analytics
    private Tracker mTracker;
    
	@Override
	protected void onResume()
	{
		super.onResume();
    	Log.i(LOG_TAG, "Setting screen name: " + screen_name);
    	mTracker.setScreenName("Image~" + screen_name);
    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google_drive_sync);

		//Google Analytics:
		mTracker = AnalyticsApplication.getDefaultTracker(this);
               		
        
		//get Sync On or Off from Preferences
		sync_on = false;
		SharedPreferences settings = getSharedPreferences("UserInfo", 0);
		sync_on = settings.getBoolean("google_drive_sync",sync_on);
		CompoundButton btn = (CompoundButton) findViewById(R.id.google_drive_sync_switch);
		btn.setChecked(sync_on);
	}

	@Override
	protected void onStart() {
	    super.onStart();
	}
	
	@Override
	public void onStop() {
	    super.onStop();
    }

		
	public void onToggleClicked(View view)
	{
		CompoundButton btn = (CompoundButton)view;
		try
		{
			SharedPreferences settings = getSharedPreferences("UserInfo", 0);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putBoolean("google_drive_sync",btn.isChecked());
    		editor.commit();	
    		
    		if (btn.isChecked())
    		{
    			Log.i(LOG_TAG,"Sync Turned On");
    			    			
    			//setup Database
    			datasource = new LogbookDataSource(this);
    		    datasource.open();
    		    
    			//start GoogleDriveSync Task
    			GoogleDriveSyncTask googleDriveSyncTask = new GoogleDriveSyncTask(this, datasource, null);
    			googleDriveSyncTask.execute();
    			
    		    mTracker.send(new HitBuilders.EventBuilder()
		        .setCategory("GoogleDriveSync")
		        .setAction("Drive Sync Turned On")
		        .build());
    		}
    		else
    		{
    			Log.i(LOG_TAG,"Sync Turned Off");
    			mTracker.send(new HitBuilders.EventBuilder()
		        .setCategory("GoogleDriveSync")
		        .setAction("Drive Sync Turned On")
		        .build());
    		}
		}
		catch(Exception e)
		{
			Log.e(LOG_TAG,"Sync Button Error: " + e);
		}
	}

}
