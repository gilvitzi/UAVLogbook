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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.ads.GoogleAdMobFullScreenAd;
import com.gilvitzi.uavlogbookpro.database.LogbookReportQuery;
import com.gilvitzi.uavlogbookpro.util.RandomBoolean;
import com.gilvitzi.uavlogbookpro.util.StringValuePair;
import com.gilvitzi.uavlogbookpro.view.PlatformSelectionDialog;
import com.google.android.gms.ads.AdListener;

import java.util.ArrayList;
import java.util.List;

public class ActivityReports extends DatabaseActivity {

    private static final String LOG_TAG = "ActivityReports";

    private static final int CUSTOM_TEXT_TAG = 0;
    public static final String EXTRA_QUERY = "query";
    public static final String EXTRA_TITLE = "title";
    public static final float CHANCE_OF_FULL_SCREEN_AD = 0.6f;
    private int customTextInputType = 0;
    protected Context context;

    private ArrayAdapter<String> tags_values_adp;
    private List<String> availableYearsFromDatabase;
    GoogleAdMobFullScreenAd fullScreenAd;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reports);
		context = this;

        fullScreenAd = new GoogleAdMobFullScreenAd(this);

		setFooterButton();
        
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

	public void goToShowAllSessions(View view){
        fullScreenAd.startActionAfterRandomChanceAd(new Runnable() {
            @Override
            public void run() {
                openAllSessionsReport();
            }
        }, CHANCE_OF_FULL_SCREEN_AD);
	}

    private void openAllSessionsReport() {
        String query = LogbookReportQuery.getAllSessions();

        Intent intent = new Intent(this, ActivitySessionsTable.class);
        intent.putExtra(EXTRA_QUERY, query);
        intent.putExtra(EXTRA_TITLE, getResources().getString(R.string.report_all_sessions));
        startActivity(intent);
    }

    public void goToSessionsThisYear(View view){
        fullScreenAd.startActionAfterRandomChanceAd(new Runnable() {
            @Override
            public void run() {
                openSessionsThisYearReport();
            }
        }, CHANCE_OF_FULL_SCREEN_AD);
	}

    private void openSessionsThisYearReport() {
        String query = LogbookReportQuery.getSessionsThisYear();

        Intent intent = new Intent(this, ActivitySessionsTable.class);
        intent.putExtra(EXTRA_QUERY, query);
        intent.putExtra(EXTRA_TITLE, getResources().getString(R.string.report_sessions_this_year));
        startActivity(intent);
    }

    public void goToSessionsPerPlatform(View view) {

        PlatformSelectionDialog.OnSelectedPlatform openReportAction = new PlatformSelectionDialog.OnSelectedPlatform(){
            @Override
            public void selected(final StringValuePair selectedPlatform) {
                fullScreenAd.startActionAfterRandomChanceAd(new Runnable() {
                    @Override
                    public void run() {
                        openSessionsPerPlatformReport(selectedPlatform);
                    }
                }, CHANCE_OF_FULL_SCREEN_AD);
            }
        };

        PlatformSelectionDialog platformSelectionDialog = new PlatformSelectionDialog(this, datasource, openReportAction);
        platformSelectionDialog.show();
    }

    private void openSessionsPerPlatformReport(StringValuePair platformTypeAndVariation) {
        String query = LogbookReportQuery.getSessionsPerPLatform(platformTypeAndVariation);

        Intent intent = new Intent(this, ActivitySessionsTable.class);
        intent.putExtra(EXTRA_QUERY, query);
        intent.putExtra(EXTRA_TITLE, getResources().getString(R.string.report_sessions_per_platform));
        startActivity(intent);
    }

    public void goToSessionsCountedActivities(View view){
        String query = LogbookReportQuery.getSessionsCountedActivities();

		Intent intent = new Intent(this, ActivityTableView.class);
		intent.putExtra(EXTRA_QUERY, query);
		intent.putExtra(EXTRA_TITLE, getResources().getString(R.string.report_counted_activities));
    	startActivity(intent);
	}

	public void goToLastSessionDatePerPlatform(View view){
        String query = LogbookReportQuery.getSessionDatePerPlatform();

        Intent intent = new Intent(this, ActivityTableView.class);
		intent.putExtra(EXTRA_QUERY, query);
		intent.putExtra(EXTRA_TITLE, getResources().getString(R.string.report_last_session_date));
    	startActivity(intent);
	}
	
	public void goToHoursPerPlatform(View view){
        String query = LogbookReportQuery.getHoursPerPlatform();

        Intent intent = new Intent(this, ActivityTableView.class);
		Log.v(LOG_TAG,query);
		intent.putExtra(EXTRA_QUERY, query);
		intent.putExtra(EXTRA_TITLE, getResources().getString(R.string.report_hours_per_platform));
    	startActivity(intent);
	}
	
	public void goToHoursPerLocation(View view){
        String query = LogbookReportQuery.getHoursPerLocation();

        Intent intent = new Intent(this, ActivityTableView.class);
		intent.putExtra(EXTRA_QUERY, query);
		intent.putExtra(EXTRA_TITLE, getResources().getString(R.string.report_hours_per_location));
    	startActivity(intent);
	}
	
	public void goToHoursPerYear(View view){
		String query = LogbookReportQuery.getHoursPerYear();
		
		Intent intent = new Intent(this, ActivityTableView.class);
		intent.putExtra(EXTRA_QUERY, query);
		intent.putExtra(EXTRA_TITLE, getResources().getString(R.string.report_hours_per_year));
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
	    String query = LogbookReportQuery.getSessionsByTag(tag);

        Intent intent = new Intent(this, ActivitySessionsTable.class);
        intent.putExtra(EXTRA_QUERY, query);
        intent.putExtra(EXTRA_TITLE, getResources().getString(R.string.report_search_by_tag));
        startActivity(intent);
	}

	public void goToCAAReport(View view){
        if (RandomBoolean.get(CHANCE_OF_FULL_SCREEN_AD)) {
            fullScreenAd.addAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    openCAAReport();
                }
            });

            fullScreenAd.show();
        } else {
            openCAAReport();
        }
	}

    private void openCAAReport() {
        String query = LogbookReportQuery.getCaaReport();
        Intent intent = new Intent(this, ActivityTableView.class);
        intent.putExtra(EXTRA_QUERY, query);
        intent.putExtra(EXTRA_TITLE, getResources().getString(R.string.report_caa_report));
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
            public void onClick(DialogInterface dialog,final int which) {

                if (RandomBoolean.get(CHANCE_OF_FULL_SCREEN_AD))
                {
                    fullScreenAd.addAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            openCAAYearlyReport(which);
                        }
                    });

                    fullScreenAd.show();
                } else {
                    openCAAYearlyReport(which);
                }
            }

            private void openCAAYearlyReport(int which) {
                int year = Integer.parseInt((String)years[which]);
                String query = LogbookReportQuery.getYearlyCAAReport(year);

                Intent intent = new Intent((Activity)context, ActivityTableView.class);
                intent.putExtra(EXTRA_QUERY, query);
                intent.putExtra(EXTRA_TITLE, String.format("CAA Report For %1$s",year));
                startActivity(intent);
            }
        });
        builder.show();
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
        LinearLayout homeBtnContainer = (LinearLayout) findViewById(R.id.btn_reports_container);
        homeBtnContainer.setBackgroundColor(getResources().getColor(R.color.darker_red_style));
    }

}