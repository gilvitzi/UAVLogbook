package com.gilvitzi.uavlogbookpro.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.ads.GoogleAdMobBanner;
import com.gilvitzi.uavlogbookpro.export.ShareTableAsExcelFileTask;
import com.gilvitzi.uavlogbookpro.util.DateTimeConverter;
import com.gilvitzi.uavlogbookpro.util.Duration;
import com.google.android.gms.analytics.HitBuilders;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class ActivityTableView extends DatabaseActivity {
	private final String LOG_TAG = "ActivityTableView";
	private static final String SCREEN_NAME = "Table View";

	Context context;
    ActivityTableView thisActivity;

	public  ProgressDialog progressDialog;
	private Menu contextMenu;
	private int row_count;

    private ArrayList<Integer> selectedRows;
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

        selectedRows = new ArrayList<Integer>();
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

	    try{
	        //setContentView(R.layout.activity_table_view);    - was restarting the AdView Container and therefore adView was needed to be restarted in order for AdBanner to show.
            //delete last table if exist
            TableLayout tl = (TableLayout) findViewById(R.id.tbl_view);
            tl.removeAllViews();
            if (showAds)
                adBottomBannerManager.show();
	    }catch(Exception ignore){}

	    //Please Wait... message
        progressDialog = ProgressDialog.show(context, "", getResources().getString(R.string.please_wait_progress), true);

        getTableValues = new GetTableValues();
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
        ShareTableAsExcelFileTask shareTask = new ShareTableAsExcelFileTask(this, datasource, query, title);
        shareTask.execute();
    }

    private void onDataReadyToShare(String data) {
        String subject = String.format(getString(R.string.action_share_subject), title);
        sendEmailSharingIntent(subject, data);
    }

    private void sendEmailSharingIntent(String subject, String body)
    {
        final Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, Html.fromHtml(body));

        startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.send_email)));
    }

    /**
    * This AsyncTask will query the database and create a Table View from the Results
    * @author Gil Laptop
    *
    */
	private class GetTableValues extends AsyncTask<String, String, Boolean> {
		Cursor cursor;
        List<ColumnNameType> columns = new ArrayList<ColumnNameType>();
		List<List<String>> rows;
		@Override
		protected Boolean doInBackground(String... params) {
			try{
                datasource.open();
				cursor = getDatasource().database.rawQuery(query, null);

                getColumnNamesAndTypes(cursor.getColumnNames());
	    		//Iteration
	    		rows = new ArrayList<List<String>>();
	    		List<String> row;
	    		String value;

                getAllDataRows();

            }catch (Exception e){
				Log.e(LOG_TAG,"Retriving Data from DB Failed: " + e);
				return false;
			} finally {
                datasource.close();
            }
            return true;
		}

        private void getColumnNamesAndTypes(String[] columnNamesArray) {
            for (String columnName : columnNamesArray) {
                columns.add(new ColumnNameType(columnName));
            }
        }

        private void getAllDataRows() {
            List<String> row;
            try {
                if (cursor.moveToFirst()) {
                    do {
                        row = getRowFromQuery();
                        rows.add(row);
                    } while (cursor.moveToNext());
                }
            } finally {
                try {
                    cursor.close();
                } catch (Exception ignore) {}
            }
        }

        @NonNull
        private List<String> getRowFromQuery() {
            List<String> row;
            String value;
            row = new ArrayList<String>();
            for (int columnIndex = 0; columnIndex < cursor.getColumnCount() ; columnIndex++){
                //getCursorFieldType(); //only on API 10+
                try{
                    value = cursor.getString(columnIndex);
                    row.add(value);
                }catch(Exception ignore){
                    try{
                        value = String.valueOf(cursor.getInt(columnIndex));
                        row.add(value);
                    }catch(Exception ignored){}
                }
            }
            return row;
        }

       protected void onPostExecute(final Boolean success) {
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

           whenFinishedTask();
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

           for (ColumnNameType column : columns) {
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

               String value = "";
               String columnName = columns.get(j).Name;
               String columnType = columns.get(j).Type;

               switch(columnType) {
                   case "Duration":
                       String secondsString = row.get(j);
                       long seconds = Integer.parseInt(secondsString);
                       value = new Duration(context, seconds * 1000).getString();
                       break;
                   case "Date":
                       try {
                           String valueString = row.get(j);
                           Date prasedDate = DateTimeConverter.parseDate(valueString, DateTimeConverter.ISO8601);
                           value = DateTimeConverter.getFormattedDate(context, prasedDate);
                       } catch (Exception e) {
                           value = row.get(j);
                       }
                       break;
                   case "String":
                       value = row.get(j);
                       break;
                   default:
                       Log.e(LOG_TAG,"Unknown columnType " + columnType);
               }

               tv_cell.setText(value);
               tv_cell.setTag(columns.get(j).Name);
               tr.addView(tv_cell);
           }
       }

       private void whenFinishedTask() {
            progressDialog.dismiss();
            if (contextMenu != null)
            {
               contextMenu.getItem(0).setVisible(true);
               Log.d(LOG_TAG, "contextMenu is Null in " + LOG_TAG);
            }

            if (showAds)
                adBottomBannerManager.show();
       }
   }

   private class ColumnNameType {
       private String Name;
       private String Type;

       ColumnNameType(String SQLColumnName) {
           if (SQLColumnName.contains("^")) {
               String[] parts = SQLColumnName.split("\\^");
               Type = parts[0];
               Name = parts[1];
           } else {
               Type = "String";
               Name = SQLColumnName;
           }
       }
   }
}