package com.gilvitzi.uavlogbookpro.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.ads.GoogleAdMobBanner;
import com.gilvitzi.uavlogbookpro.export.ShareTableAsExcelFileTask;
import com.gilvitzi.uavlogbookpro.model.Session;
import com.gilvitzi.uavlogbookpro.util.Duration;
import com.gilvitzi.uavlogbookpro.util.NameValuePair;
import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;
import java.util.List;

public class ActivitySessionsTable extends DatabaseActivity {

    private static final String LOG_TAG = "ActivitySessionsTable";
    private static final String screen_name = "Sessions Table";
    public static final String EXTRA_KEY_SESSION_ID = "SessionID";
    public static final int FIRST_ROW_INDEX = 0;

    Context context;
	Activity thisActivity;
	private List<Session> sessions;
	
	public  ProgressDialog progressDialog;
	private Menu contextMenu;
	private int row_count;

    private ArrayList<Integer> selectedRows;

	private GetAllSessionsTask getSessionsTask;

	private String query;

	//Google AdMob Ads Banner
	GoogleAdMobBanner adBottomBanner;
    private TableLayout tableLayout;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions_table);
        tableLayout = (TableLayout) findViewById(R.id.tbl_all_sessions);

        openOptionsMenu();
        context = this;
        thisActivity = this;

        initGoogleAdMob();

        selectedRows = new ArrayList<Integer>();

        getExtras();

        //Please Wait... message
        progressDialog = ProgressDialog.show(context, "", getResources().getString(R.string.please_wait_progress), true);

        getSessionsTask = new GetAllSessionsTask();
        getSessionsTask.execute();
    }

    private void initGoogleAdMob() {
        ViewGroup adContainer = (ViewGroup) findViewById(R.id.adBanner);
        adBottomBanner = new GoogleAdMobBanner(context,adContainer);
        adBottomBanner.show();
    }

    private void getExtras() {
        try{
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                query = extras.getString("query");
                title = extras.getString("title");
                setTitle(title);
                getSupportActionBar().setTitle(title);
            }
            Log.i(LOG_TAG, "Query Received: " + query);
        }catch(Exception e){
            e.printStackTrace();
            finish();
        }
    }

    //----- MENU ACTIONS
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.action_delete:
		    	menu_deleteSessions();
                sendAnalyticsEventDeleteSessions();
                return true;

		    case R.id.action_edit:
                editMenuItemClicked();
		        return true;

            case R.id.action_share:
                shareMenuItemClicked();
                return true;

		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}

    private void shareMenuItemClicked() {
        ShareTableAsExcelFileTask shareTask = new ShareTableAsExcelFileTask(this, datasource, query, title);
        shareTask.execute();
    }

    private void editMenuItemClicked() {
        Intent intent = new Intent(this, ActivityAddSession.class);
        int id = selectedRows.get(FIRST_ROW_INDEX);
        intent.putExtra(EXTRA_KEY_SESSION_ID, id);
        Log.i(LOG_TAG, String.format("Sending Record ID %1$d To Editing", id));

        datasource.close();

        sendAnalyticsEventEditSessions();
        startActivity(intent);
    }

    private void sendAnalyticsEventEditSessions() {
        String category = getResources().getString(R.string.analytics_event_category_sessions);
        String action = getResources().getString(R.string.analytics_event_action_sessions_edit);

        mTracker.send(new HitBuilders.EventBuilder()
            .setCategory(category)
            .setAction(action)
            .build());
    }

    private void sendAnalyticsEventDeleteSessions() {
        String category = getResources().getString(R.string.analytics_event_category_sessions);
        String action = getResources().getString(R.string.analytics_event_action_sessions_delete);

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setValue(selectedRows.size())
                .build());
    }

    public void menu_deleteSessions(){
		// Invoke "Are You Sure" Dialog

//		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//		    @Override
//		    public void onClick(DialogInterface dialog, int which) {
//		        switch (which){
//		        case DialogInterface.BUTTON_POSITIVE:
//		            //Yes button clicked
//		            break;
//
//		        case DialogInterface.BUTTON_NEGATIVE:
//		            //No button clicked
//		            break;
//		        }
//		    }
//		};

        String messageFormat = getResources().getString(R.string.alert_message_delete_sessions);
        String message = String.format(messageFormat, selectedRows.size());
        String str_no = getResources().getString(R.string.no);
        String str_yes = getResources().getString(R.string.yes);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder
			.setMessage(message)
			.setNegativeButton(str_no, null)
			.setPositiveButton(str_yes, new DialogInterface.OnClickListener() {
			    @SuppressWarnings("unchecked")
				public void onClick(DialogInterface dialog, int which) {
			        progressDialog = ProgressDialog.show(context, "", getResources().getString(R.string.please_wait_progress), true);
					DeleteSessionTask delSessionTask = new DeleteSessionTask();
			    	delSessionTask.execute();
			    }
			})
		    .show();
	}

	@Override
    public void onPause() {
        adBottomBanner.pause();
        super.onPause();
    }

	public void onResume(){
		super.onResume();

        //GoogleAdMob
        adBottomBanner.resume();

        //Database
		if (getSessionsTask!=null){
		    if (getSessionsTask.getStatus()!=AsyncTask.Status.RUNNING){
		    	progressDialog = ProgressDialog.show(context, "", getResources().getString(R.string.please_wait_progress), true);
                tableLayout.removeAllViews();
		    	getSessionsTask = new GetAllSessionsTask();
		    	getSessionsTask.execute();
		    }
	    } else {
	    	progressDialog = ProgressDialog.show(context, "", getResources().getString(R.string.please_wait_progress), true);
	    	setContentView(R.layout.activity_sessions_table);
	    	getSessionsTask = new GetAllSessionsTask();
	    	getSessionsTask.execute();
	    }
	}

	@Override
	protected void onDestroy() {
        adBottomBanner.destroy();
	    super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sessions_table, menu);
		contextMenu = menu;
        initContextMenuItems();

		return true;
	}

    private void initContextMenuItems() {
        try{
            //Disable the Items
            contextMenu.getItem(0).setEnabled(false);    //Edit unavailable
            contextMenu.getItem(1).setEnabled(false);    //Delete unavailable
        }catch(Exception e){
            Log.e(LOG_TAG, "context menu initialzing failed: " + e);
        }
    }

	private class GetAllSessionsTask extends AsyncTask<String, String, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {

			try {
                datasource.open();
				sessions = datasource.getSessionsByQuery(query);
			} catch (Exception e) {
				Log.e(LOG_TAG,"Retriving sessions from DB Failed: " + e);
				return false;
			} finally {
                datasource.close();
            }

			return true;
		}
		
		protected void onPostExecute(final Boolean success) {
			if (success){
			    
			    TableLayout tl = (TableLayout) findViewById(R.id.tbl_all_sessions);
                addTableHeader(tl);

                selectedRows.clear();
                row_count = sessions.size();

                TableRow tr;
                for (int i = 0;i < sessions.size();i++){
                    Session session  = sessions.get(i);
                    tr = createTableRow(tl, i, session.getId());
                    populateRowViews(tr, session);
                }

                adBottomBanner.show();
                progressDialog.dismiss();
			}else{
			    progressDialog.dismiss();
			}
	    }

        private void addTableHeader(TableLayout tl) {
//            LayoutInflater inflater =
//                      (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TableRow tr = (TableRow) View.inflate(context, R.layout.table_row_header_session, null);
            tr.setTag("header");
            tl.addView(tr);

            CheckBox cb = (CheckBox) tr.findViewWithTag("check");

            cb.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    Boolean isChecked = ((CheckBox)v).isChecked();
                    ViewGroup tableLayout = (ViewGroup) v.getParent().getParent();
                    setAllCheckBoxesValue(tableLayout, isChecked);
                }
            });
        }

        private void setAllCheckBoxesValue(ViewGroup tableLayout, Boolean isChecked) {
            View row; //TableRow
            for (int i = 0;i < tableLayout.getChildCount();i++){
                row = tableLayout.getChildAt(i);
                CheckBox checkbox = (CheckBox)row.findViewWithTag("check");
                checkbox.setChecked(isChecked);
            }
        }

        @NonNull
        private TableRow createTableRow(TableLayout tl, int i, long sessionId) {
            TableRow tr;
            tr = (TableRow) View.inflate(context, R.layout.table_row_session, null);
            tl.addView(tr);

            tr.setTag(sessionId); //row index will be the session index.

            //set Background color according to odd / even
            int colorEvenRow = getResources().getColor(R.color.table_row_even);
            int colorOddRow = getResources().getColor(R.color.table_row_odd);
            int color = ( i % 2 == 0) ?  colorEvenRow : colorOddRow;
            tr.setBackgroundColor(color);

            return tr;
        }

        private void populateRowViews(TableRow tr, Session session) {
            CheckBox cb;//Populate Row Views:
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
                       int row_id = getRowID(buttonView);

                       if(isChecked){
                           selectedRows.add(row_id);
                       }else{
                           selectedRows.remove((Object) row_id);
                       }

                       setHeaderCheckBox(buttonView);
                       updateMenuActionsAvailability();

                   }

                private void updateMenuActionsAvailability() {
                    //Update Menu Actions Availability
                    int selectedRowCount = selectedRows.size();
                    if (selectedRowCount > 1) {
                        contextMenu.getItem(0).setEnabled(false);//Edit unavailable
                        contextMenu.getItem(1).setEnabled(true); //Delete available

                    } else if (selectedRowCount == 1) {
                        contextMenu.getItem(0).setEnabled(true); //Edit available
                        contextMenu.getItem(1).setEnabled(true); //Delete available

                    } else {  //No selected rows
                        //Set Context Menu items
                        contextMenu.getItem(0).setEnabled(false); //Edit unavailable
                        contextMenu.getItem(1).setEnabled(false); //Delete unavailable
                    }
                }

                private void setHeaderCheckBox(CompoundButton buttonView) {
                    // Set Header Checkbox to UNCHECHEKED
                    TableLayout tl = (TableLayout) buttonView.getParent().getParent();
                    TableRow row_hdr = (TableRow) tl.findViewWithTag("header");
                    CheckBox cb_hdr = (CheckBox) row_hdr.findViewWithTag("check");
                    if (selectedRows.size() == row_count){
                        cb_hdr.setChecked(true);
                    }else{
                        cb_hdr.setChecked(false);
                    }
                }

                private int getRowID(CompoundButton buttonView) {
                    View parent = (View) buttonView.getParent();
                    String rowIDString = parent.getTag().toString();
                    return Integer.parseInt(rowIDString);
                }
            });//END OF onCheckedChanged (Listener)
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
