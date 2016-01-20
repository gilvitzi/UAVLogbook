package com.gilvitzi.uavlogbookpro.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.gilvitzi.uavlogbookpro.AnalyticsApplication;
import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;
import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.model.Session;
import com.gilvitzi.uavlogbookpro.util.Duration;
import com.gilvitzi.uavlogbookpro.util.NameValuePair;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

public class ActivitySessionsTable extends ActionBarActivity {
    
    private static final String LOG_TAG = "ActivitySessionsTable";
    private static final String screen_name = "Sessions Table";
    
	Context context;
	Activity thisActivity;
	private LogbookDataSource datasource;
	private List<Session> sessions;
	
	public  ProgressDialog progressDialog;
	private Menu contextMenu;
	private int row_count;
	private ArrayList<Integer> selectedRows;
	private GetAllSessionsTask getSessionsTask;
	
	private String query;
	
	//Google AdMob Ads Banner
	private AdView adView;
	private AdRequest adRequest;
	
	//Google Analytics
    private Tracker mTracker;
    
	//----- MENU ACTIONS
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.menu_delete:
		    	menu_deleteSessions();
		    	
		    	mTracker.send(new HitBuilders.EventBuilder()
		        .setCategory("Sessions")
		        .setAction("Delete")
		        .setValue(selectedRows.size())
		        .build());
		    	
		        return true;
		    case R.id.menu_edit:		    
		    	Intent intent = new Intent(this, ActivityAddSession.class);
		    	int id = selectedRows.get(0);
		    	Log.i(LOG_TAG,"Sending Record ID " + id + " To Editing");
		    	intent.putExtra("SessionID", id);
		    	datasource.close();
		    	
		    	mTracker.send(new HitBuilders.EventBuilder()
		        .setCategory("Sessions")
		        .setAction("Edit")
		        .build());
		    	
		    	startActivity(intent);
		        return true;
	
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void menu_deleteSessions(){
		// Invoke "Are You Sure" Dialog
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		            //Yes button clicked
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder
			.setMessage("This will Delete the selected rows, \n Are You Sure?")
			.setNegativeButton("No", dialogClickListener)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			    @SuppressWarnings("unchecked")
				public void onClick(DialogInterface dialog, int which) {
			        //User clicked yes!
			        progressDialog = ProgressDialog.show(context, "", getResources().getString(R.string.please_wait_progress), true);
					DeleteSessionTask delSessionTask = new DeleteSessionTask();
			    	delSessionTask.execute();
			    }
			})
		    .show();
		
	}
	
	@Override
    public void onPause() {
      adView.pause();
      super.onPause();
    }
	
	public void onResume(){
		super.onResume();
		//Analytics
    	Log.i(LOG_TAG, "Setting screen name: " + screen_name);
    	mTracker.setScreenName("Image~" + screen_name);
    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    	
    	//Database
		if(null != datasource)
	    	datasource.open();
		if (getSessionsTask!=null){
		    if (getSessionsTask.getStatus()!=AsyncTask.Status.RUNNING){
		    	progressDialog = ProgressDialog.show(context, "", getResources().getString(R.string.please_wait_progress), true);
		    	setContentView(R.layout.activity_sessions_table);
		    	getSessionsTask = new GetAllSessionsTask();
		    	getSessionsTask.execute();
		    }
	    }else{
	    	progressDialog = ProgressDialog.show(context, "", getResources().getString(R.string.please_wait_progress), true);
	    	setContentView(R.layout.activity_sessions_table);
	    	getSessionsTask = new GetAllSessionsTask();
	    	getSessionsTask.execute();
	    }
		
	}
	
	public void onStop(){
		if(null != datasource)
	    	datasource.close();
		super.onStop();
	}
	
	public void onStart(){
		super.onStart();
	}
	
	
	
	@Override
	protected void onDestroy() {
	    adView.destroy();
	    super.onDestroy();
	    if (datasource != null) {
	    	datasource.close();
	    }
	}
	
	public void finalize() throws Throwable {
	    if(null != datasource)
	    	datasource.close();
	    super.finalize();
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sessions_table);
		openOptionsMenu();
		context = this;
		thisActivity = this;

		//Google Analytics:
		mTracker = AnalyticsApplication.getDefaultTracker(this);
        
        
		datasource = new LogbookDataSource(this);
	    datasource.open();
	    
		selectedRows = new ArrayList<Integer>();
		
		//Get Sessions Data:
		//get Intents Query
		try{
			Bundle extras = getIntent().getExtras(); 

			if (extras != null) {
				query = extras.getString("query");
				String title = extras.getString("title");
				setTitle(title);
				getSupportActionBar().setTitle(title);
			}
			Log.i(LOG_TAG,"Query Received: " + query);
		}catch(Exception e){
			e.printStackTrace();
			finish();
		}
		
		
		//Please Wait... message
		progressDialog = ProgressDialog.show(context, "", getResources().getString(R.string.please_wait_progress), true);

		getSessionsTask = new GetAllSessionsTask();
		getSessionsTask.execute();
	}
	
	/**
     * This method will initiate The AdView Object
     * and attach it to the viewport
     */
    private void showAds(){
      //Google AdMob Ads
        //adView = (AdView)this.findViewById(R.id.adView);
        adView = new AdView(this);
        adView.setAdUnitId(getResources().getString(R.string.ads_unit_id));
        adView.setAdSize(AdSize.BANNER);
        LinearLayout ll= (LinearLayout) findViewById(R.id.adBanner);
        ll.addView(adView);  
        adRequest = new AdRequest.Builder()
                                            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                                            .addTestDevice(getResources().getString(R.string.test_device_id_galaxy_ace))
                                            .addTestDevice(getResources().getString(R.string.test_device_id_thl_w8s))
                                            .addTestDevice(getResources().getString(R.string.test_device_id_lg_g2))
                                            .build();
        adView.loadAd(adRequest);
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sessions_table, menu);
		contextMenu = menu;
		return true;
	}

	private class GetAllSessionsTask extends AsyncTask<String, String, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try{
				sessions = datasource.getSessionsByQuery(query);
			}catch (Exception e){
				Log.e(LOG_TAG,"Retriving sessions from DB Failed: " + e);
				return false;
			}
			return true;
		}
		
		protected void onPostExecute(final Boolean success) {
			if (success){
			    
			    TableLayout tl = (TableLayout) findViewById(R.id.tbl_all_sessions);
                
                //Add Header:
                @SuppressWarnings("unused")
                LayoutInflater inflater = 
                          (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                TableRow tr = (TableRow) View.inflate(context, R.layout.table_row_header_session, null);
                tr.setTag("header");
                tl.addView(tr);

//				TableRow trFooter = (TableRow) View.inflate(context, R.layout.table_row_footer, null);
//				trFooter.setTag("footer");
//				tl.addView(trFooter);

                CheckBox cb = (CheckBox) tr.findViewWithTag("check");

                cb.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        //get Checked state:
                        Boolean isChecked = ((CheckBox)v).isChecked();
                        ViewGroup tableLayout = (ViewGroup) v.getParent().getParent();      
                        View row; //Table Row
                        for (int i = 0;i < tableLayout.getChildCount();i++){
                            row = tableLayout.getChildAt(i);
                            CheckBox checkbox = (CheckBox)row.findViewWithTag("check");
                            checkbox.setChecked(isChecked);
                        }
                    }
                });
                
                selectedRows.clear();
                row_count = 0;
                //Initialize Context Menu items as Hidden
                try{
                    //Show the items
                    contextMenu.getItem(0).setVisible(true);
                    contextMenu.getItem(1).setVisible(true);
                    
                    //Disable the Items
                    contextMenu.getItem(0).setEnabled(false);    //Edit unavailable
                    contextMenu.getItem(1).setEnabled(false);    //Delete unavailable
                }catch(Exception e){
                    Log.e(LOG_TAG,"context menu initialzing failed: " + e);
                }
                
                for (int i = 0;i < sessions.size();i++){
                    Session session  = sessions.get(i);
                                        
                    row_count++; //add to row counter
                    
                    //Create New TableRow
                    tr = (TableRow) View.inflate(context, R.layout.table_row_session, null);
                    tl.addView(tr); //Append to TableLayout
                    
                    // Set Dynamic Parameters
                    tr.setTag(session.getId()); //row index will be the session index.
                                            
                    //set Background color according to odd / even
                    if (i%2==1){
                        tr.setBackgroundColor(getResources().getColor(R.color.table_row_odd));
                    }else{
                        tr.setBackgroundColor(getResources().getColor(R.color.table_row_even));
                    }
                    
                    
                    
                    //Populate Row Views:
                    
                    //Date
                    TextView tv_date = (TextView) tr.findViewWithTag("date");
                    tv_date.setText(session.getDateStringShort());
                                            
                    TextView tv_duration = (TextView) tr.findViewWithTag("duration");
                    Duration d = new Duration();
                    d.setISO8601(session.getDuration());
                    tv_duration.setText(d.getString());
                    
                    TextView tv_platform = (TextView) tr.findViewWithTag("platform");
                    tv_platform.setText(session.getPlatformType() + " " + session.getPlatformVariation());

                    TextView tv_reg = (TextView) tr.findViewWithTag("reg_no");
                    tv_reg.setText(session.getRegistration());
                    
                    TextView tv_icao = (TextView) tr.findViewWithTag("icao");
                    tv_icao.setText(session.getICAO());
                    
                    cb = (CheckBox) tr.findViewWithTag("check");
                    
                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                           @Override
                           public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                               int row_id = Integer.parseInt(((View) buttonView.getParent()).getTag().toString());
                               if(isChecked){
                                   selectedRows.add(row_id);
                               }else{
                                   selectedRows.remove((Object) row_id);
                               }
                               
                            // Set Header Checkbox to UNCHECHEKED
                               TableLayout tl = (TableLayout) buttonView.getParent().getParent();
                               TableRow row_hdr = (TableRow) tl.findViewWithTag("header");
                               CheckBox cb_hdr = (CheckBox) row_hdr.findViewWithTag("check");
                               if (selectedRows.size() == row_count){
                                   cb_hdr.setChecked(true);
                               }else{
                                   cb_hdr.setChecked(false);
                               }
                               //Update Menu Actions Availability
                               if (selectedRows.size()>1){ 
                                   //More than 1 row selected  
                                   contextMenu.getItem(0).setEnabled(false); //Edit unavailable
                                   contextMenu.getItem(1).setEnabled(true); //Delete available
                                   
                               }else if(selectedRows.size()==1){
                                   //1 row selected
                                   contextMenu.getItem(0).setEnabled(true); //Edit available
                                   contextMenu.getItem(1).setEnabled(true); //Delete available
                                       
                               }else{
                                   //No selected rows
                                   
                                   //Set Context Menu items
                                   contextMenu.getItem(0).setEnabled(false); //Edit unavailable
                                   contextMenu.getItem(1).setEnabled(false);    //Delete unavailable
                               }
                           }
                    });//END OF onCheckedChanged (Listener)
                }
                showAds(); //AdMob Ads Banner
                progressDialog.dismiss();
			}else{
			    progressDialog.dismiss();
			}
				
	    }

	}
		
	
	
	private class DeleteSessionTask extends AsyncTask<List<NameValuePair>, String, Boolean>{

		@Override
		protected Boolean doInBackground(List<NameValuePair>... params) {
			//delete each selected row id
			for (int i = 0;i < selectedRows.size();i++){
				datasource.deleteSession(selectedRows.get(i));
			}
			
			return true;
		}
		
		
		@Override
		protected void onPostExecute(final Boolean success) {
		    String message = selectedRows.size() + " Sessions Deleted";
		    Toast.makeText(context,message, Toast.LENGTH_LONG).show();
			Log.i(LOG_TAG,message);
			progressDialog.dismiss();
			finish();
			startActivity(getIntent());
		}


	}
} //END OF Activity
