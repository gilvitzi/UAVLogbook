package com.gilvitzi.uavlogbookpro.export;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.gilvitzi.uavlogbookpro.AnalyticsApplication;
import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;
import com.google.android.gms.analytics.Tracker;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Gil on 27/11/2015.
 */
public abstract class ExportTable extends AsyncTask<String, String, Boolean> {
    private final String LOG_TAG = "ExportTable";
    private final ProgressDialog dialog;

    protected OnDataReadyHandler onDataReadyHandler;
    protected Activity mActivity;
    protected Context context;

    private LogbookDataSource datasource;

    private String query;
    private StringBuilder data;

    protected Tracker mTracker; //Google Analytics

    private Cursor cursor;
    private String[] columnNames;
    private List<List<String>> records;


    public void setOnDataReadyHandler(OnDataReadyHandler handler)
    {
        onDataReadyHandler = handler;
    }

    public List<List<String>> getRecords() {
        return records;
    }

    public interface OnDataReadyHandler {
        public void onDataReady(String dataAsCSV);
    }

    protected abstract void createDataObject();
    protected abstract void sendExportSuccessfulHit();
    protected abstract void sendExportFailedHit();

    protected String[] getColumnNames() {
        return columnNames;
    }

    protected void setData(StringBuilder data) {
        this.data = data;
    }

    /* Internal Implementation */
    public ExportTable(Activity activity,LogbookDataSource  datasource,String query) {
        this.context = activity;
        this.mActivity = activity;
        this.datasource = datasource;
        this.query = query;

        dialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute(){
        dialog.setMessage(mActivity.getString(R.string.dialog_exporting_table));
        dialog.show();

        mTracker = AnalyticsApplication.getDefaultTracker(mActivity);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try{
            datasource.open();
            cursor = datasource.database.rawQuery(query,null);
            columnNames = cursor.getColumnNames();
            records = new LinkedList<List<String>>();
            List<String> record;
            String value = "";

            try {
                getDataFromDatabase();
                publishProgress("0", String.valueOf(records.size()));
                createDataObject();

                return true;

            } finally {
                try {
                    cursor.close();
                } catch (Exception ignore) {}
            }
        }catch (Exception e){
            Log.e(LOG_TAG, "Retriving Data from DB Failed: " + e);
            return false;
        }
    }

    private void getDataFromDatabase() {
        List<String> record;
        String value;
        if (cursor.moveToFirst()) {
            do {
                record = new LinkedList<String>();
                for (int i = 0;i<cursor.getColumnCount();i++){
                    try{
                        value = cursor.getString(i);
                        record.add(value);
                    }catch(Exception ignore){
                        try{
                            value = String.valueOf(cursor.getInt(i));
                            record.add(value);
                        }catch(Exception e){}
                    }
                }
                records.add(record);
            } while (cursor.moveToNext());
        }
        //datasource.close();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        String message = String.format(context.getString(R.string.exporting_progress_dialog_message),values[0],values[1]);
        this.dialog.setMessage(message);

        Log.i(LOG_TAG, message);
    }

    protected void onPostExecute(final Boolean success){
        if (this.dialog.isShowing()){
            this.dialog.dismiss();
        }
        if (success){
            sendExportSuccessfulHit();
            onDataReadyHandler.onDataReady(data.toString());

        }else{
            sendExportFailedHit();
            Toast.makeText(context, R.string.ERROR_TOAST_SHARE_EXPORT_FAILED, Toast.LENGTH_SHORT).show();
        }
    }


}
