package com.gilvitzi.uavlogbookpro.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.ads.GoogleAdMobBanner;
import com.gilvitzi.uavlogbookpro.database.GetTableValues;
import com.gilvitzi.uavlogbookpro.export.ShareTableAsExcelFileTask;
import com.gilvitzi.uavlogbookpro.util.OnResult;
import com.google.android.gms.analytics.HitBuilders;

import java.util.List;


public class ActivityTableView extends DatabaseActivity {
	private final String LOG_TAG = "ActivityTableView";
	private static final String SCREEN_NAME = "Table View";

	Context context;
    ActivityTableView thisActivity;

	public  ProgressDialog progressDialog;
	private Menu contextMenu;
	private int row_count;

	private GetTableValues getTableValues;

    private String query;
	private String title;

	//Google AdMob Ads
    GoogleAdMobBanner adBottomBannerManager;
    private boolean showAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_view);
        openOptionsMenu();
        context = this;
        thisActivity = this;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        showAds = settings.getBoolean("show_ads", true);

        if (showAds)
            initGoogleAdMob();

        row_count = 0;

        getActivityExtras();
        sendAnalyticsEventReportLoaded();
        refreshTable();
    }

    private void sendAnalyticsEventReportLoaded() {
        String category = getResources().getString(R.string.analytics_event_report_loaded);
        String actionFormat = getResources().getString(R.string.analytics_event_report_loaded_format);
        String action = String.format(actionFormat, title);

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

	    switch (item.getItemId()) {
            case R.id.action_share:
                shareMenuItemClicked();
                sendAnalyticsEventShareTable();
            default:
		        return super.onOptionsItemSelected(item);
	    }
	}

    private void sendAnalyticsEventShareTable() {
        String category = getResources().getString(R.string.analytics_event_category_share);
        String actionFormat = getResources().getString(R.string.analytics_event_category_share_table_name);
        String action = String.format(actionFormat, title);

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());
    }

    public void onResume(){
		super.onResume();
        refreshTableIfNeeded();
        if (showAds)
            adBottomBannerManager.resume();
	}

    private void refreshTableIfNeeded() {
        if (getTableValues!=null){
            if (getTableValues.getStatus()!= AsyncTask.Status.RUNNING){
                refreshTable();
            }
        }else{
            refreshTable();
        }
    }

	@Override
	public void onPause() {
        if (showAds)
            adBottomBannerManager.pause();
	    super.onPause();
	}

	@Override
	protected void onDestroy() {
        if (showAds)
            adBottomBannerManager.destroy();
	    super.onDestroy();
	}

    private void initGoogleAdMob() {
        ViewGroup adContainer = (ViewGroup) findViewById(R.id.adBanner);
        adBottomBannerManager = new GoogleAdMobBanner(context,adContainer);
        adBottomBannerManager.show();
    }

    private void getActivityExtras() {
        try{
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                query = extras.getString("query");
                title = extras.getString("title");
                setTitle(title);
                getSupportActionBar().setTitle(title);

            }
            Log.d(LOG_TAG, "Query Received: " + query);
        }catch(Exception e){
            e.printStackTrace();
            finish();
        }
    }

    /**
     * This method will refresh the Data Table
     * it will set the Contents view from scratch so any add view should be
     * added after calling this method or called in side this method after setContentView
     */
	private void refreshTable(){
        getTableValues = new GetTableValues(this, query);
        getTableValues.onFinished = new CreateTableView();
        getTableValues.execute();
	}

   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.table_view, menu);
        contextMenu = menu;
        return true;
    }

    private void shareMenuItemClicked() {
        new ShareTableAsExcelFileTask(this, query, title).execute();
    }

    private class CreateTableView implements OnResult<GetTableValues.QueryResults> {
	    List<GetTableValues.ColumnNameType> columns;
	    List<List<String>> rows;

        @Override
        public void onResult(boolean success, GetTableValues.QueryResults returnValue) {
            columns = returnValue.columns;
            rows = returnValue.rows;

            if (showAds)
                adBottomBannerManager.show();

            updateContextMenu();
            removeLastTableView();
            createTableViewFromData(success);
        }

        private void removeLastTableView() {
            try{
                //delete last table if exist
                TableLayout tl = (TableLayout) findViewById(R.id.tbl_view);
                tl.removeAllViews();
            }catch(Exception ignore){}
        }

        private void updateContextMenu() {
            if (contextMenu != null) {
                contextMenu.getItem(0).setVisible(true);
                Log.d(LOG_TAG, "contextMenu is Null in " + LOG_TAG);
            }
        }

        private void createTableViewFromData(boolean success) {
            if (success) {
                TableLayout tl = createTableLayout();
                addHeader(tl);
                addTableRows(tl);
            } else {
               /*
                * error occured in loading data from database,
                * could show message to user too try again later
                */
            }
        }

        private void addTableRows(TableLayout tl) {
            for (int i = 0;i < rows.size();i++){
                List<String> rowStrings  = rows.get(i);
                row_count++; //add to row counter
                createRow(tl, i, rowStrings);
            }
        }

        @NonNull
        private TableLayout createTableLayout() {
            TableLayout tl = (TableLayout) findViewById(R.id.tbl_view);
            tl.setTag("TableView");
            tl.setId(R.id.tbl_view);
            return tl;
        }

        @NonNull
        private void addHeader(TableLayout tl) {
            TableRow tr = (TableRow) View.inflate(context, R.layout.table_view_header, null);
            tr.setTag("header");
            tl.addView(tr);

            for (GetTableValues.ColumnNameType column : columns) {
                TextView tv = (TextView) View.inflate(context, R.layout.table_view_header_cell, null);
                tv.setText(column.Name);
                tr.addView(tv);
            }
        }

        private void createRow(TableLayout tl, int i, List<String> rowStrings) {
            TableRow tr;
            tr = initRowParams(tl, i);
            setRowBackgroundColor(tr, i);
            populateRowViews(tr, rowStrings);
        }

        @NonNull
        private TableRow initRowParams(TableLayout tl, int i) {
            TableRow tr;
            tr = new TableRow(context);
            tl.addView(tr);

            tr.setTag(i); //row index will be the session index.
            return tr;
        }

        private void setRowBackgroundColor(TableRow tr, int i) {
            //set Background color according to odd / even
            if (i%2==1){
                tr.setBackgroundColor(getResources().getColor(R.color.table_row_odd));
            }else{
                tr.setBackgroundColor(getResources().getColor(R.color.table_row_even));
            }
        }

        private void populateRowViews(TableRow tr, List<String> row) {
            for (int j=0;j<row.size();j++){
                TextView tv_cell = (TextView) View.inflate(context, R.layout.table_view_row_cell, null);
                tv_cell.setText(row.get(j));
                tv_cell.setTag(columns.get(j).Name);
                tr.addView(tv_cell);
            }
        }
    }
}