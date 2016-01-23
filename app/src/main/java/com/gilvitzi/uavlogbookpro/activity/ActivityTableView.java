package com.gilvitzi.uavlogbookpro.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.export.ExportTableToExcelTask;
import com.gilvitzi.uavlogbookpro.export.ExportTableToHTML;
import com.gilvitzi.uavlogbookpro.util.FileDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;

import java.io.File;
import java.util.ArrayList;
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
	AdView adView;
	AdRequest adRequest;

	//----- MENU ACTIONS -----
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.action_export_table_to_excel:
		        exportTableToExcel();

		        //Analytics Tracking
		        mTracker.send(new HitBuilders.EventBuilder()
		        .setCategory("Export")
		        .setAction("Export Table " + title)
		        .build());
		        return true;
            case R.id.action_share:
                actionShare();

                //Analytics Tracking
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Share")
                        .setAction("Share Table " + title)
                        .build());
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}

	private void exportTableToExcel(){
	    //create new file dialog
        FileDialog folderDialog;
        Log.v(LOG_TAG,"Select File Dialog Invoked");
        File mPath = new File(Environment.getExternalStorageDirectory() +"//"); // + "//DIR//"
        folderDialog = new FileDialog(this, mPath);
        folderDialog.setFileEndsWith(".xls");
        folderDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
            public void directorySelected(File directory) {
                Log.d(getClass().getName(), "selected dir " + directory.toString());
                Log.v(LOG_TAG, "Exporting Table To Excel");
                String fileName = title;
                ExportTableToExcelTask exportTask = new ExportTableToExcelTask(ActivityTableView.this, getDatasource(), fileName, directory.toString(), query);
                exportTask.execute();
            }
        });
        folderDialog.setSelectDirectoryOption(true);
        folderDialog.showDialog();

        Log.v(LOG_TAG, "Folder Dialog Invoked");
	}
	public void onResume(){
		super.onResume();
		//Analytics

        refreshTableIfNeeded();
        adView.resume();
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
	  adView.pause();
	  super.onPause();
	}

	@Override
	protected void onDestroy() {
	    adView.destroy();
	    super.onDestroy();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_table_view);
		openOptionsMenu();
		context = this;
		thisActivity = this;

        showGoogleAdMobAds();

		selectedRows = new ArrayList<Integer>();
		row_count = 0;

        getActivityExtras();

        refreshTable();
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
	   //delete last table if exist
	    try{
	        setContentView(R.layout.activity_table_view);
	        showGoogleAdMobAds();
	    }catch(Exception ignore){}

	  //Please Wait... message
        progressDialog = ProgressDialog.show(context, "", getResources().getString(R.string.please_wait_progress), true);

        getTableValues = new GetTableValues();
        getTableValues.execute();
	}

	/**
	 * This method will initiate The AdView Object
	 * and attach it to the viewport
	 */
	private void showGoogleAdMobAds(){
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
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.table_view, menu);
        contextMenu = menu;
        return true;
    }

    private void actionShare()
    {
        ExportTableToHTML exporter = new ExportTableToHTML(thisActivity,datasource,query);
        exporter.setOnDataReadyHandler(new ExportTableToHTML.OnDataReadyHandler() {
            @Override
            public void onDataReady(String dataAsHTML) {
                onDataReadyToShare(dataAsHTML);
            }
        });
        exporter.execute();
    }

    private void onDataReadyToShare(String data) {
        String subject = String.format(getString(R.string.action_share_subject),title);
        sendEmailSharingIntent(subject, data);
    }

    private void sendEmailSharingIntent(String subject, String body)
    {
        final Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, Html.fromHtml(body));

        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    /**
    * This AsyncTask will query the database and create a Table View from the Results
    * @author Gil Laptop
    *
    */
	private class GetTableValues extends AsyncTask<String, String, Boolean> {
		Cursor cursor;
		String[] columnNames;
		List<List<String>> rows;
		@Override
		protected Boolean doInBackground(String... params) {
			try{
                datasource.open();
				cursor = getDatasource().database.rawQuery(query,null);

	    		columnNames = cursor.getColumnNames();

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

        private void getCursorFieldType() {
            /*
             *  The next code block was removed because cursor.getType() requires minimum API Level 11 (android 3.0.x)
             *  and the current was API Level is 10 (android 2.3 )
            switch (cursor.getType(i)){
                case Cursor.FIELD_TYPE_INTEGER:
                    value = String.valueOf(cursor.getInt(i));
                case Cursor.FIELD_TYPE_FLOAT:
                    value = String.valueOf(cursor.getFloat(i));
                case Cursor.FIELD_TYPE_STRING:
                    value = cursor.getString(i);
                default:
                    try{
                        value = String.valueOf(cursor.getBlob(i));
                    }catch(Exception e){
                        Log.e(LOG_TAG,"Error parsing result value on column " + cursor.getColumnName(i));
                    }
            }
            */
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

           for (String columnName : columnNames) {
               TextView tv = (TextView) View.inflate(context, R.layout.table_view_header_cell, null);
               tv.setText(columnName);
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
           tr = new TableRow(context);     //Create New TableRow
           tl.addView(tr);                 //Append to TableLayout

           // Set Dynamic Parameters
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
               tv_cell.setTag(columnNames[j]);
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
       }

   }

} //END OF Activity
