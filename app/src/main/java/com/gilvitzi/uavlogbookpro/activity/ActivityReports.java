package com.gilvitzi.uavlogbookpro.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import com.gilvitzi.uavlogbookpro.AnalyticsApplication;
import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;
import com.gilvitzi.uavlogbookpro.database.LogbookSQLite;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ActivityReports extends DatabaseActivity {

    private static final String LOG_TAG = "ActivityReports";
    private static final String screen_name = "Reports";
    
    private static final int CUSTOM_TEXT_TAG = 0;
    private int customTextInputType = 0;
    protected Context context;

    private ArrayAdapter<String> tags_values_adp;
    private List<String> availableYearsFromDatabase;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reports);
		context = this;

		//Google Analytics:
		mTracker = AnalyticsApplication.getDefaultTracker(this);
        
		datasource = new LogbookDataSource(context);
		
		setFooterButton();
        
        //Get AutoComplete Tags
        GetAllTagsTask getAllTagsTask = new GetAllTagsTask();
        getAllTagsTask.execute();

        GetAllYearsTask mGetAllYears = new GetAllYearsTask();
        mGetAllYears.execute();
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.reports, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	public void action_goHome(){
        Intent intent = new Intent(this, ActivityHome.class);
        startActivity(intent);
        this.finish();
    }
	
	//Report Links
	public void goToShowAllSessions(View view){
		String query = LogbookSQLite.SELECT_ALL_SESSIONS +
						" ORDER BY date DESC";
		
		Intent intent = new Intent(this, ActivitySessionsTable.class);
		intent.putExtra("query", query);
		intent.putExtra("title", getResources().getString(R.string.report_all_sessions));
    	startActivity(intent);
    	
	}

	public void goToSessionsThisYear(View view){
		Calendar c = Calendar.getInstance(); 
		int year = c.get(Calendar.YEAR);
		String time = year + "-01-01 00:00:00";
		String query = LogbookSQLite.SELECT_ALL_SESSIONS +
						" WHERE " + LogbookSQLite.COLUMN_DATE + ">= '" + time + "'";
		
		Intent intent = new Intent(this, ActivitySessionsTable.class);
		intent.putExtra("query", query);
		intent.putExtra("title", getResources().getString(R.string.report_sessions_this_year));
    	startActivity(intent);
    	
	}
	
	public void goToSessionsCountedActivities(View view){
		String query = "SELECT platform_type AS 'Platform Type'," +
				"SUM(takeoffs) AS 'Takeoffs'," +
				"SUM(landings) AS 'Landings'," +
				"SUM(go_arounds) AS 'Go Arounds' " +
				"FROM logbook GROUP BY platform_type";
		
		Intent intent = new Intent(this, ActivityTableView.class);
		intent.putExtra("query", query);
		intent.putExtra("title", "Counted Activities");
    	startActivity(intent);
    	
	}

	public void goToLastSessionDatePerPlatform(View view){
		String query = "SELECT " +
				"(platform_type || ' ' || platform_variation) AS 'Platform'," +
				"strftime('%d.%m.%Y',MAX(date)) AS 'Last Session Date' " +				
				"FROM logbook " +
				"GROUP BY platform_type,platform_variation " +
				"ORDER BY MAX(date) DESC";
		
		Intent intent = new Intent(this, ActivityTableView.class);
		intent.putExtra("query", query);
		intent.putExtra("title", getResources().getString(R.string.report_last_session_date));
    	startActivity(intent);
	}
	
	public void goToHoursPerPlatform(View view){
		String query = "SELECT " +
				"(platform_type || ' ' || platform_variation) AS 'Platform', " +
		        
                LogbookSQLite.DURATION_SUM_HOURS + " AS 'Hours', "+
				
				"COUNT(*) AS 'Sessions'" +
				"FROM logbook " +
				"GROUP BY platform_type,platform_variation " +
				"ORDER BY SUM(duration) DESC";
		Intent intent = new Intent(this, ActivityTableView.class);
		Log.v(LOG_TAG,query);
		intent.putExtra("query", query);
		intent.putExtra("title", getResources().getString(R.string.report_hours_per_platform));
    	startActivity(intent);
	}
	
	public void goToHoursPerLocation(View view){
		String query = "SELECT " +
				"( aerodrome_name || ' (' || icao || ')') AS 'Aerodrome'," +
				LogbookSQLite.DURATION_SUM_HOURS + " AS 'Hours', "+	
				"COUNT(*) AS 'Sessions'" +
				"FROM logbook " +
				"GROUP BY icao,aerodrome_name " +
				"ORDER BY SUM(duration) DESC";
		
		Intent intent = new Intent(this, ActivityTableView.class);
		intent.putExtra("query", query);
		intent.putExtra("title", getResources().getString(R.string.report_hours_per_location));
    	startActivity(intent);
	}
	
	public void goToHoursPerYear(View view){
		String query = "SELECT " +
				"strftime('%Y',date) AS 'Year'," +
				LogbookSQLite.DURATION_SUM_HOURS + " AS 'Hours', "+	
				"COUNT(*) AS 'Sessions'" +
				"FROM logbook " +
				"GROUP BY strftime('%Y',date) " +
				"ORDER BY strftime('%Y',date) DESC";
		
		Intent intent = new Intent(this, ActivityTableView.class);
		intent.putExtra("query", query);
		intent.putExtra("title", getResources().getString(R.string.report_hours_per_year));
    	startActivity(intent);
	}
	
	public void goToSearchByTag(View view){
        showTagSelectionDialog();
    }

    private void showTagSelectionDialog() {
        final AutoCompleteTextView acTextView = new AutoCompleteTextView(this);
        acTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        acTextView.setAdapter(tags_values_adp);

        String selectString = getResources().getString(R.string.select);
        String cancelString = getResources().getString(R.string.cancel);
        String dialogTitle = getResources().getString(R.string.dialog_tag_selection_title);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(dialogTitle)
                .setCancelable(true)
                .setView(acTextView)
                .setPositiveButton(selectString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        searchByTag(acTextView.getText().toString());
                    }
                })
                .setNegativeButton(cancelString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });;
        builder.create().show();
    }

	private void searchByTag(String tag){
	    String query = LogbookSQLite.SELECT_ALL_SESSIONS + 
                " WHERE tags LIKE '%" + tag + "%'" +
                " ORDER BY date DESC";

        Intent intent = new Intent(this, ActivitySessionsTable.class);
        intent.putExtra("query", query);
        intent.putExtra("title", getResources().getString(R.string.report_search_by_tag));
        startActivity(intent);
	}
	public void goToCAAReport(View view){
		String query = "SELECT " +
		        "strftime('%d.%m.%Y',date) AS 'Date'," +
		        LogbookSQLite.DURATION_HOURS + " AS 'Hours', "+
		        "(platform_type || ' ' || platform_variation) AS 'Platform',"+
		        "icao AS 'Location',"+
		        "registration AS 'Reg.',"+
		        "tail_number AS 'Tail No.',"+
		        "(CASE WHEN "+ 
		            "((command=='PIC' OR command=='Instructor') AND sim_actual<>'Simulator') "+
		            "THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") "+
		            "ELSE 0 "+
		        "END) "+
		        " AS 'PIC',"+
		        "(CASE command WHEN 'SIC' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'SIC',"+
		        "(CASE command WHEN 'Instructor' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'Instructor',"+
		        "(CASE command WHEN 'Trainee' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'Trainee',"+
		        "(CASE sim_actual WHEN 'Simulator' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'Sim',"+
		        "day_night AS 'Day / Night' "+
		        " FROM logbook";
		
		Intent intent = new Intent(this, ActivityTableView.class);
		intent.putExtra("query", query);
		intent.putExtra("title", getResources().getString(R.string.report_caa_report));
    	startActivity(intent);
	}
	
	public void goToYearlyCAAReport(View view){
        showCAAReportYearSelectionDialog();
    }

    private void showCAAReportYearSelectionDialog() {
        final CharSequence years[] = availableYearsFromDatabase.toArray(new CharSequence[availableYearsFromDatabase.size()]);
        String title = getResources().getString(R.string.dialog_caa_report_year_selection_title);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(years, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int year = Integer.parseInt((String)years[which]);
                String query = getCAAReportorYearQueryString(year);

                Intent intent = new Intent((Activity)context, ActivityTableView.class);
                intent.putExtra("query", query);
                intent.putExtra("title", String.format("CAA Report For %1$s",year));
                startActivity(intent);
            }
        });
        builder.show();
    }

    @NonNull
    private String getCAAReportorYearQueryString(int year) {
        String query =  "SELECT " +
                "strftime('%d.%m.%Y',date) AS 'Date'," +
                LogbookSQLite.DURATION_HOURS + " AS 'Hours', "+
                "(platform_type || ' ' || platform_variation) AS 'Platform',"+
                "icao AS 'Location',"+
                "registration AS 'Reg.',"+
                "tail_number AS 'Tail No.',"+
                "(CASE WHEN "+
                    "((command=='PIC' OR command=='Instructor') AND sim_actual<>'Simulator') "+
                    "THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") "+
                    "ELSE 0 "+
                "END) "+
                " AS 'PIC',"+
                "(CASE command WHEN 'SIC' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'SIC',"+
                "(CASE command WHEN 'Instructor' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'Instructor',"+
                "(CASE command WHEN 'Trainee' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'Trainee',"+
                "(CASE sim_actual WHEN 'Simulator' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'Sim',"+
                "day_night AS 'Day / Night' "+
                " FROM logbook " +
                " WHERE strftime('%Y', date) = '" + year + "'";
        return query;
    }

    public void customTextInputSubmit(View view){
	    LinearLayout mInputCustomTextWindow = (LinearLayout) findViewById(R.id.custom_text_window);
        mInputCustomTextWindow.setVisibility(View.GONE);
        AutoCompleteTextView customTextView = (AutoCompleteTextView) findViewById(R.id.custom_text_input_field);
        String customText = customTextView.getText().toString();
	    switch(customTextInputType){
	        case CUSTOM_TEXT_TAG:
	            searchByTag(customText);
	            break;
	        default:
	            break;
	    }
	}
	
	//Footer Buttons
	public void footer_goHome(View view){
		Intent intent = new Intent(this, ActivityHome.class);
    	startActivity(intent);
    	this.finish();
    }
    
    public void footer_goReports(View view){
        // Do Nothing - this is reports page
    }
    
    public void footer_goAddSession(View view){
    	Intent intent = new Intent(this, ActivityAddSession.class);
    	startActivity(intent);
    }
    
    /*
     * //set Search Tag AutoComplete Values

     */
    private class GetAllTagsTask extends AsyncTask<String, String, Boolean> {
        private static final String LOG_TAG = "GetAllTagsTask";
        private List<String> tags_values_list = new ArrayList<String>();
        @Override
        protected Boolean doInBackground(String... params) {
            try{
                datasource.open();
                tags_values_list = datasource.getDistinctTags();
                datasource.close();
                return true;
            }catch(Exception e){
                Log.e(LOG_TAG,"Error: " + e);
                datasource.close();
            }
            return false;
        }
        
        @Override
        protected void onPostExecute(final Boolean success) {
            if (success){
                //Update Views:
                tags_values_adp = new ArrayAdapter<String>(getBaseContext(),R.layout.autocomplete_dropdown,tags_values_list);
                tags_values_adp.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                AutoCompleteTextView customTextView = (AutoCompleteTextView) findViewById(R.id.custom_text_input_field);
                customTextView.setAdapter(tags_values_adp);

            }
        }
    }

    private class GetAllYearsTask extends AsyncTask<String, String, Boolean> {
        private static final String LOG_TAG = "GetAllYearsTask";
        private List<String> tags_values_list = new ArrayList<String>();
        @Override
        protected Boolean doInBackground(String... params) {
            try{
                datasource.open();
                availableYearsFromDatabase = datasource.getAllYears();
                datasource.close();
                return true;
            }catch(Exception e){
                Log.e(LOG_TAG,"Error: " + e);
                datasource.close();
            }
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setFooterButton()
    {
//        TextView tv = (TextView) findViewById(R.id.btn_home_text);
//        tv.setTextColor(getResources().getColor(R.color.black));

        LinearLayout homeBtnContainer = (LinearLayout) findViewById(R.id.btn_reports_container);
        homeBtnContainer.setBackgroundColor(getResources().getColor(R.color.darker_red_style));

    }
}
