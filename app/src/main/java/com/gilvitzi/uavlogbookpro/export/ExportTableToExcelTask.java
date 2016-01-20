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
import com.gilvitzi.uavlogbookpro.activity.ActivityTableView;
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

    private final String LOG_TAG = "ExportTableToExcel";
	private final ProgressDialog dialog;
    private Activity mActivity;
	private Context context;
	private SQLiteDatabase db;
	
	private String fileName = "";
	private String filePath = "";
	private String query = "";
	
	//Google Analytics
    private Tracker mTracker;
    
	public ExportTableToExcelTask(ActivityTableView activity,LogbookDataSource datasource,String fileName,String filePath, String query){
	    this.context = activity;
        this.mActivity = activity;
	    if (datasource.database==null){
	        datasource.open();
        }
	    this.db = datasource.database;
	    
	    this.query = query;
	    this.fileName = fileName; 
	    this.filePath = filePath + "/" + fileName + ".xls";
		this.dialog = new ProgressDialog(context);
		
	}
	
	@Override
	protected void onPreExecute(){
        ExportTableToExcelTask.this.dialog.setMessage("Exporting Table to Excel...");
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
            //Map Results:
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
                publishProgress("0",String.valueOf(records.size()));
                createExcelFile();
                
                db.close();
                return true;
                
            } finally {
                try { 
                    cursor.close();                         
                } catch (Exception ignore) {}
            }
        }catch (Exception e){
            Log.e(LOG_TAG,"Retriving Data from DB Failed: " + e);
            return false;
        }
    }
    
    private void createExcelFile(){
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
        
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(filePath);
            hwb.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Your Excel file has been generated");
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
	    if (success){
	      //Analytics Tracking
	        mTracker.send(new HitBuilders.EventBuilder()
	        .setCategory("Export")
	        .setAction("Export Table Successful")
	        .build());
	        
	        Toast.makeText(context, fileName + " Exported Successfully", Toast.LENGTH_SHORT).show();
	        Toast.makeText(context, "File Saved: " + filePath, Toast.LENGTH_LONG).show();
	    }else{
	    	//Analytics Tracking
	        mTracker.send(new HitBuilders.EventBuilder()
	        .setCategory("Export")
	        .setAction("Export Table Failed")
	        .build());
	        Toast.makeText(context, "file fail to build", Toast.LENGTH_SHORT).show();
	    }
	}
	
	@Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        Log.i(LOG_TAG, "onProgressUpdate(): total: " + values[1] + "  added: " + values[0]);
        String message = "Exporting Table To Excel " + "\n";
        message += values[0] + "/" +  String.valueOf(values[1]);
        this.dialog.setMessage(message);
    }

}