package com.gilvitzi.uavlogbookpro.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.UAVLogbookApplication;
import com.gilvitzi.uavlogbookpro.database.AerodromesDataSource;
import com.gilvitzi.uavlogbookpro.database.LogbookReportQuery;
import com.gilvitzi.uavlogbookpro.export.ImportDBExcelTask;
import com.gilvitzi.uavlogbookpro.export.ShareDBAsExcelFileTask;
import com.gilvitzi.uavlogbookpro.model.Session;
import com.gilvitzi.uavlogbookpro.util.Duration;
import com.gilvitzi.uavlogbookpro.util.NameValuePair;
import com.gilvitzi.uavlogbookpro.util.OnResult;
import com.google.android.gms.analytics.HitBuilders;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ActivityHome extends DatabaseActivity {


	private static final String LOG_TAG = "ActivityHome";
    private static final int SELECT_FILE_TO_IMPORT = 100;

    public HomePageData homePageData;
    private Session lastSession;

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
            case R.id.settings:
                menu_GoToSettings();
                return true;

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

    public void menu_exportToExcel() {
        String query = LogbookReportQuery.getAllSessions();
        ShareDBAsExcelFileTask shareTask = new ShareDBAsExcelFileTask(this, datasource, query);
        shareTask.execute();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("ImportExport")
                .setAction("DB Export")
                .build());
    }

    public void menu_importFromExcel() {
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), SELECT_FILE_TO_IMPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==SELECT_FILE_TO_IMPORT && resultCode==RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file
            importExcelFromUri(selectedfile);
        }
    }

    private void importExcelFromUri(Uri selectedFile) {
        ImportDBExcelTask importTask =  new ImportDBExcelTask((Activity)ActivityHome.this, selectedFile);
        importTask.onFinished = new OnResult() {
            @Override
            public void onResult(boolean success, String message) {
                refreshHomePageData();
            }
        };

        importTask.execute();

        Log.v("ImportExport", "Importing Data From Excel");
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("ImportExport")
                .setAction("DB Import")
                .build());
    }

    private void menu_GoToSettings() {
        Intent intent = new Intent(this, ActivitySettings.class);
        startActivity(intent);
    }

    public void menu_GoToAbout(){
        Intent intent = new Intent(this, ActivityAbout.class);
        startActivity(intent);
    }

    private class HomePageData extends AsyncTask<List<NameValuePair>, String, Boolean> {
    	private Duration totalHours = new Duration(context);
    	private int totalSessions = 0;
    	
		@Override
		protected Boolean doInBackground(List<NameValuePair>... params) {
			try{
                datasource.open();
                long seconds = datasource.getTotalHours();
				totalHours.setMillis(seconds * 1000);
                lastSession = datasource.getLastSession();
				totalSessions = getDatasource().countRecords();
			} catch(Exception e) {
				Log.e("HomePageData","Error: " + e);
			} finally {
                datasource.close();
            }
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			if (success){
                updateHoursAndSessionsViews();
                updateLastSessionDataView();
            }
		}

        private void updateHoursAndSessionsViews() {
            String hoursText = String.format(context.getResources().getString(R.string.home_page_flight_hours_text), totalHours.getString());
            TextView tv_flight_hours = (TextView) findViewById(R.id.total_flight_hours);
            tv_flight_hours.setText(hoursText);

            String sessionsText = String.format(context.getResources().getString(R.string.home_page_sessions_count_text), String.valueOf(totalSessions));
            TextView tv_sessions = (TextView) findViewById(R.id.total_sessions);
            tv_sessions.setText(sessionsText);
        }

        private void updateLastSessionDataView()
        {
            if (lastSession != null) {
                setSessionDataInViews();
            }else{
                setNoSessionFound();
            }
        }

        private void setSessionDataInViews() {
            ((TextView) findViewById(R.id.session_list_item_date)).setText(lastSession.getDateString(context));

            String location = lastSession.getAerodromeName();
            String icao = lastSession.getICAO();
            if (!icao.isEmpty())
                location += " (" + icao + ")";

            ((TextView) findViewById(R.id.session_list_item_icao)).setText(location);
            TextView bottomLeftTv = ((TextView) findViewById(R.id.session_list_item_platform_type_and_variation));
            String sim_or_nothing = (lastSession.getSimActual().equalsIgnoreCase("simulator"))?"(SIM)":"";
            bottomLeftTv.setText(lastSession.getPlatformType() + " " + lastSession.getPlatformVariation() + sim_or_nothing);
            Duration duration = new Duration(context);
            duration.setISO8601(lastSession.getDuration());
            ((TextView) findViewById(R.id.session_list_item_Duration)).setText(duration.getString());
        }

        private void setNoSessionFound() {
            ((TextView) findViewById(R.id.session_list_item_date)).setText(R.string.home_page_no_sessions_found);
            ((TextView) findViewById(R.id.session_list_item_icao)).setText("");
            ((TextView) findViewById(R.id.session_list_item_platform_type_and_variation)).setText("");
            ((TextView) findViewById(R.id.session_list_item_Duration)).setText("");
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
            UAVLogbookApplication app = (UAVLogbookApplication) getApplication();
    		if (!last_version_shown.equals(app.getVersionName())) {
                showWhatsNewScreen();
                doVersionInitializations();
            }
    		
    	}catch(Exception e){
    		Log.e("ActivityHome","showWhatsNew get from Preferences Failed: " + e);
    		showWhatsNewScreen();
    	}
    }

    private void doVersionInitializations() {
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);

        if (!settings.contains("show_ads")) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("show_ads", true);
            editor.commit();
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
    		UAVLogbookApplication app = (UAVLogbookApplication) getApplication();
    		editor.putString("whats_new_screen_shown", app.getVersionName());
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
}