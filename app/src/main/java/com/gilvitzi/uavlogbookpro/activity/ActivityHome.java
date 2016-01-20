package com.gilvitzi.uavlogbookpro.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gilvitzi.uavlogbookpro.database.AerodromesDataSource;
import com.gilvitzi.uavlogbookpro.export.ExportDBExcelTask;
import com.gilvitzi.uavlogbookpro.export.GoogleDriveSyncTask;
import com.gilvitzi.uavlogbookpro.export.ImportDBExcelTask;
import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.model.Session;
import com.gilvitzi.uavlogbookpro.util.Duration;
import com.gilvitzi.uavlogbookpro.util.FileDialog;
import com.gilvitzi.uavlogbookpro.util.NameValuePair;
import com.gilvitzi.uavlogbookpro.util.QuickStartButton;
import com.google.android.gms.analytics.HitBuilders;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ActivityHome extends DatabaseActivity {

    protected LinearLayout content_view = null;

	private static final String LOG_TAG = "ActivityHome";

	public HomePageData homePageData;
	public ArrayList<NameValuePair> pageData;
	
	private FileDialog fileDialog;
    private FileDialog folderDialog;

	private QuickStartButton qsButton;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

	    switch (item.getItemId()) {
		    case R.id.action_export:
		    	menu_exportToExcel();
		    	return true;
		    case R.id.action_import:
		    	menu_importFromExcel();
		    	return true;
//		    case R.id.action_google_drive_sync:
//		    	menu_GoogleDriveSync();
//		    	return true;
		    case R.id.about:
                menu_GoToAbout();
                return true;

		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	

    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setFooterButton();

        // Show What's New in this Version Screen
        checkWhatsNew();


        AerodromesDataSource mAerodromesDataSource = new AerodromesDataSource(this);
        mAerodromesDataSource.initiate();

	    //Get Total Hours & Sessions
		homePageData = new HomePageData();
		homePageData.execute();
        
		//GoogleDriveSync Task
		GoogleDriveSyncTask googleDriveSyncTask = new GoogleDriveSyncTask(this, this.getDatasource(), null);
		//googleDriveSyncTask.execute();
		
		qsButton = (QuickStartButton)findViewById(R.id.btn_quick_start);
		
        showMessageIfExists();

        tryForcingActionBar();
    }

    private void tryForcingActionBar() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void showMessageIfExists() {
        try{
            Bundle extras = getIntent().getExtras();
            String message = "";
            String title = "Message";
            if (extras != null) {
                message = extras.getString("message");
                title = extras.getString("title");
            }

            if (!message.isEmpty())
                showMessge(title, message);

        }catch(Exception e){
            e.printStackTrace();
            finish();
        }
    }

    private void showMessge(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
        .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onResume(){
        super.onResume();

        refreshHomePageData();
        refreshLastSessionData();
    }

    private void refreshHomePageData() {
        if (homePageData!=null){
            if (homePageData.getStatus() != AsyncTask.Status.RUNNING){
                homePageData = new HomePageData();
                homePageData.execute();
            }
        }else{
            homePageData = new HomePageData();
            homePageData.execute();
        }
    }

    public void footer_goHome(View view){
    	//Do Nothing..
    }
    

    
    public void footer_goReports(View view){
        Intent intent = new Intent(this, ActivityReports.class);
    	startActivity(intent);
    	this.finish();
    }
    
    public void footer_goAddSession(View view){
    	Intent intent = new Intent(this, ActivityAddSession.class);
    	startActivity(intent);
    }

    public void menu_exportToExcel(){
        File mPath = new File(Environment.getExternalStorageDirectory() +"//");
        folderDialog = new FileDialog(this, mPath);
        folderDialog.setFileEndsWith(".xls");
        folderDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
            public void directorySelected(File directory) {

                ExportDBExcelTask exportTask = new ExportDBExcelTask((Activity) ActivityHome.this, getDatasource(), directory.toString());
                exportTask.execute();

                Log.d(getClass().getName(), "selected dir " + directory.toString());
                Log.v("ImportExport", "Exporting Data To Excel");

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("ImportExport")
                        .setAction("DB Export")
                        .build());
            }
        });
        folderDialog.setSelectDirectoryOption(true);
        folderDialog.showDialog();

    }
    
    public void menu_importFromExcel(){
        File mPath = new File(Environment.getExternalStorageDirectory() +"//");
        fileDialog = new FileDialog(this, mPath);
        fileDialog.setFileEndsWith(".xls");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                ImportDBExcelTask importTask =  new ImportDBExcelTask((Activity)ActivityHome.this,file.toString());
                importTask.execute();

                Log.d(getClass().getName(), "selected file " + file.toString());
                Log.v("ImportExport", "Importing Data From Excel");

                mTracker.send(new HitBuilders.EventBuilder()
	   	        .setCategory("ImportExport")
	   	        .setAction("DB Import")
	   	        .build());
            }
        });
        fileDialog.setSelectDirectoryOption(false);
        fileDialog.showDialog();         
            
    }
    
    public void menu_GoToAbout(){
        Intent intent = new Intent(this, ActivityAbout.class);
        startActivity(intent);
    }
    
    public void menu_GoogleDriveSync(){
    	try {
	    	Intent intent = new Intent(this, ActivityGoogleDriveSync.class);
	    	//String ACTION_DRIVE_OPEN = "com.google.android.apps.drive.DRIVE_OPEN";
	    	//intent.setAction(ACTION_DRIVE_OPEN);
	    	startActivity(intent);
    	}
    	catch(Exception e)
    	{
    		Log.e("GoogleDriveSync", "Activity Sync Start Failed with error:\n" + e);
    	}
    	
    }

    private class HomePageData extends AsyncTask<List<NameValuePair>, String, Boolean> {
    	private String totalHours = "00:00";
    	private int totalSessions = 0;
    	
		@Override
		protected Boolean doInBackground(List<NameValuePair>... params) {
			try{
				totalHours = getDatasource().getTotalHours();
				if (totalHours == null){
				    totalHours = "00:00";
				}
				totalSessions = getDatasource().countRecords();
			}catch(Exception e){
				Log.e("HomePageData","Error: " + e);
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			if (success){
				//Update Views:
				
				TextView tv_flight_hours = (TextView) findViewById(R.id.total_flight_hours);
				tv_flight_hours.setText("Flight Hours: " + totalHours);
				
				TextView tv_sessions = (TextView) findViewById(R.id.total_sessions);
				tv_sessions.setText("Sessions: " + totalSessions);
			}
		}
    }

    
    private Date getQuickStartTime()
    {
    	long millis = 0;
    	try{

    		SharedPreferences settings = getSharedPreferences("UserInfo", 0);
    		millis = settings.getLong("quick_start_time", 0L);

    	}catch(Exception e){
    		Log.e("ActivityHome","QuickStart get Failed: " + e);
    	}

    	return new Date(millis);
    }


    @SuppressLint("DefaultLocale")
	private String durationToHMS(long millis)
    {
    	long hours = TimeUnit.MILLISECONDS.toHours(millis);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1);
		return String.format("%02d:%02d:%02d",hours , minutes, seconds );
    }


    private void checkWhatsNew()
    {
    	/*
    	 * In prefereneces file we save a string of the app current version
    	 * whenever What's New screen was showen
    	 * then we check if the current version equals the version in shared preferences
    	 * if they are not, we will show the whatsnew screen
    	 */
    	String last_version_shown = "";
    	try{
    		SharedPreferences settings = getSharedPreferences("UserInfo", 0);
    		last_version_shown = settings.getString("whats_new_screen_shown", last_version_shown);
        	
    		//if was not_shown -> show screen
    		if (!last_version_shown.equals(getResources().getText(R.string.app_version)))
    			showWhatsNewScreen();
    		
    	}catch(Exception e){
    		Log.e("ActivityHome","showWhatsNew get from Preferences Failed: " + e);
    		showWhatsNewScreen();
    	}

    }
    
    private void showWhatsNewScreen()
    {
		try
		{
			Intent intent = new Intent(this, ActivityWhatsNew.class);
        	startActivity(intent);
        	
        	//set screen was shown
    		SharedPreferences settings = getSharedPreferences("UserInfo", 0);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putString("whats_new_screen_shown",(String)getResources().getText(R.string.app_version));
    		editor.commit();
		}
		catch(Exception e)
		{
			Log.e("ActivityHome", "showWhatsNewScreen Failed: " + e);
		}
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setFooterButton()
    {

        LinearLayout homeBtnContainer = (LinearLayout) findViewById(R.id.btn_home_container);
        homeBtnContainer.setBackgroundColor(getResources().getColor(R.color.darker_red_style));

    }

    private void refreshLastSessionData()
    {
        Session session = getDatasource().getLastSession();
        if (session != null) {
            ((TextView) findViewById(R.id.session_list_item_date)).setText(session.getDateString());
            ((TextView) findViewById(R.id.session_list_item_icao)).setText(session.getICAO());
            TextView bottomLeftTv = ((TextView) findViewById(R.id.session_list_item_platform_type_and_variation));
            String sim_or_nothing = (session.getSimActual().equalsIgnoreCase("simulator"))?"(SIM)":"";
            bottomLeftTv.setText(session.getPlatformType() + " " + session.getPlatformVariation() + sim_or_nothing);
            Duration duration = new Duration();
            duration.setISO8601(session.getDuration());
            ((TextView) findViewById(R.id.session_list_item_Duration)).setText(duration.getString());
        }else{
            ((TextView) findViewById(R.id.session_list_item_date)).setText(R.string.home_page_no_sessions_found);
            ((TextView) findViewById(R.id.session_list_item_icao)).setText("");
            ((TextView) findViewById(R.id.session_list_item_platform_type_and_variation)).setText("");
            ((TextView) findViewById(R.id.session_list_item_Duration)).setText("");
        }
    }
}