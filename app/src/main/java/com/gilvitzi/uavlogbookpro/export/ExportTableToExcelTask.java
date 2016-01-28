package com.gilvitzi.uavlogbookpro.export;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.gilvitzi.uavlogbookpro.AnalyticsApplication;
import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/*
 * Class ExportTableToExcel gets a query string and filepath and exports the query result to an Excel file (.xls) in the given path
 *  
 */
public class ExportTableToExcelTask extends AsyncTask<String, String, Boolean> {

    public static final String FILE_EXTENTION_XLS = ".xls";
    private final String LOG_TAG = "ExportTableToExcel";
	private final ProgressDialog dialog;
    private Activity mActivity;
	private Context context;
	private SQLiteDatabase db;
	
	private String fileName;
	private String filePath;
	private String query;
	
	//Google Analytics
    private Tracker mTracker;

    //Listeners
    private List<Listener> listeners = new ArrayList<>();

	public ExportTableToExcelTask(Activity activity,LogbookDataSource datasource,String fileName,String filePath, String query){

	    this.context = activity;
        this.mActivity = activity;

        datasource.open();

	    this.db = datasource.database;
	    
	    this.query = query;
	    this.fileName = fileName; 
	    this.filePath = filePath + "/" + fileName + FILE_EXTENTION_XLS;
		this.dialog = new ProgressDialog(mActivity);
		
	}
	
	@Override
	protected void onPreExecute(){
        ExportTableToExcelTask.this.dialog.setMessage(context.getResources().getString(R.string.export_table_dialog_initial_msg));
        ExportTableToExcelTask.this.dialog.show();

        //Google Analytics:
        mTracker = AnalyticsApplication.getDefaultTracker(mActivity);
	}
	
	Cursor cursor;
    String[] columnNames;
    List<List<String>> records;
    
    @Override
    protected Boolean doInBackground(String... params) {
        try{
            cursor = db.rawQuery(query,null);
            columnNames = cursor.getColumnNames();
            
            //Iteration
            records = new LinkedList<List<String>>();
            List<String> record;
            String value = "";
            
            try {
                if (cursor.moveToFirst()) {
                    do {                   
                        record = new ArrayList<String>();
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

                //publish total count
                publishProgress("0", String.valueOf(records.size()));
                boolean success = createExcelFile();
                
                return success;
                
            } finally {
                try { 
                    cursor.close();
                    db.close();
                } catch (Exception ignore) {}
            }
        }catch (Exception e){
            Log.e(LOG_TAG,"Retriving Data from DB Failed: " + e);
            return false;
        }
    }
    
    private boolean createExcelFile(){
        HSSFWorkbook hwb = new HSSFWorkbook();
        HSSFSheet sheet = hwb.createSheet("logbook");
        
        //Headers Row:
        HSSFRow row = sheet.createRow((short) 0);
        putHeadersToExcelRow(row);
        
        //Data Rows:
        for(int r=0;r<records.size();r++){
            List<String> record = records.get(r);
            row = sheet.createRow((short) 1+r);
            putSessionToExcelRow(row,record);
            publishProgress(String.valueOf(1+r),String.valueOf(records.size()));
        }

        boolean success = writeFileToDisk(hwb);
        return success;
    }

    private boolean writeFileToDisk(HSSFWorkbook hwb) {
        FileOutputStream fileOut;
        boolean success = true;
        try {
            fileOut = new FileOutputStream(filePath);
            hwb.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            success = false;
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    private void putSessionToExcelRow(HSSFRow row,List<String> record){
		HSSFCell cell;	
	    
	    for (int i = 0;i < record.size();i++){
	        cell = row.createCell(i);
	        cell.setCellType(Cell.CELL_TYPE_STRING);
	        cell.setCellValue(record.get(i));
	    }
	    
	}
	
	private void putHeadersToExcelRow(HSSFRow row){
		HSSFCell cell;
		for (int i = 0;i < columnNames.length;i++){
			cell = row.createCell(i);
			cell.setCellType(Cell.CELL_TYPE_STRING);
		    cell.setCellValue(columnNames[i]);
		}
	    
	}
	
	protected void onPostExecute(final Boolean success){
	    if (this.dialog.isShowing()){
	        this.dialog.dismiss();
	    }
	    if (success) {
	        //Analytics Tracking
	        mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(context.getResources().getString(R.string.analytics_event_category_export))
                    .setAction(context.getResources().getString(R.string.analytics_event_action_export_successful))
                    .build());

            String logMessage = String.format("File %1$s was exported successfully\nFile saved in: %2$s",fileName,filePath);
            Log.i(LOG_TAG, logMessage);
	    } else {
	    	//Analytics Tracking
	        mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(context.getResources().getString(R.string.analytics_event_category_export))
                    .setAction(context.getResources().getString(R.string.analytics_event_action_export_failed))
                    .build());

	        Toast.makeText(context, "File failed to build", Toast.LENGTH_SHORT).show();
	    }

        onTaskCompleted();
	}
	
	@Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        Log.i(LOG_TAG, "onProgressUpdate(): total: " + values[1] + "  added: " + values[0]);
        String messageFormat = context.getResources().getString(R.string.exporting_progress_dialog_message);
        String message = String.format(messageFormat,values[0],String.valueOf(values[1]));
        this.dialog.setMessage(message);
    }

    public void addListnener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void onTaskCompleted()
    {
        for (Listener listener : listeners) {
            listener.onTaskCompleted();
        }
    }

    public interface Listener {
        public void onTaskCompleted();
    }
}