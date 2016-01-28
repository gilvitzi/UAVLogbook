package com.gilvitzi.uavlogbookpro.export;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.gilvitzi.uavlogbookpro.AnalyticsApplication;
import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;
import com.gilvitzi.uavlogbookpro.database.LogbookSQLite;
import com.gilvitzi.uavlogbookpro.model.Session;
import com.gilvitzi.uavlogbookpro.util.Duration;
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
import java.util.List;

public class ExportDBExcelTask extends AsyncTask<String, Void, Boolean> {

    public static final String EXCEL_SHEET_NAME = "logbook";
    private final ProgressDialog dialog;
	private Context context;
	private Activity mActivity;
	private LogbookDataSource datasource;
	
	private String outFilePath = "";
    
    private Tracker mTracker; //Google Analytics

    private List<Listener> listeners = new ArrayList<>(); // Event Listeners

	public ExportDBExcelTask(Activity activity,LogbookDataSource datasource,String filePath, String outFileName){
	    this.context = activity;
	    mActivity = activity;
	    outFilePath = filePath + "/" + outFileName;
		this.dialog = new ProgressDialog(context);
		this.datasource = datasource;
        datasource.open();
	}

	@Override
	protected void onPreExecute(){
		this.dialog.setMessage(context.getResources().getString(R.string.export_table_dialog_initial_msg));
		this.dialog.show();

		//Google Analytics:
		mTracker = AnalyticsApplication.getDefaultTracker(mActivity);
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
	    List<Session> sessions = null;
        boolean success = false;

	    try {
	    	sessions = datasource.getAllSessions();
            HSSFWorkbook hwb = createExcelWorkbook(sessions);
            writeWorkbookToFile(hwb);
		    success = true;
	    } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            datasource.close();
        }

        return success;
	}

    private void writeWorkbookToFile(HSSFWorkbook hwb) throws FileNotFoundException {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(outFilePath);
            hwb.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    private HSSFWorkbook createExcelWorkbook(List<Session> sessions) {
        HSSFWorkbook hwb = new HSSFWorkbook();
        HSSFSheet sheet = hwb.createSheet(EXCEL_SHEET_NAME);

        //Headers Row:
        HSSFRow row = sheet.createRow((short) 0);
        putHeadersToExcelRow(row);

        //Data Rows:
        for(int r = 0; r < sessions.size() ; r++){
            Session session = sessions.get(r);
            row = sheet.createRow((short)1 + r);
            putSessionToExcelRow(row,session);
        }

        return hwb;
    }

    private void putSessionToExcelRow(HSSFRow row,Session session){
		HSSFCell cell;
		int i = 0;
		cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
	    cell.setCellValue(session.getId());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getDateString());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
		Duration d = new Duration();
		d.setISO8601(session.getDuration());
	    cell.setCellValue(d.getString());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getPlatformType());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getPlatformVariation());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getRegistration());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getTailNumber());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getICAO());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getAerodromeName());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getSimActual());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getDayNight());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getCommand());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getSeat());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getFlightType());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getTags());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
	    cell.setCellValue(session.getTakeoffs());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
	    cell.setCellValue(session.getLandings());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
	    cell.setCellValue(session.getGoArounds());
	    
	    cell = row.createCell(i++);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	    cell.setCellValue(session.getComments());
	    
	}
	
	private void putHeadersToExcelRow(HSSFRow row){
		
		String[] headers = {
				LogbookSQLite.COLUMN_ID,
				LogbookSQLite.COLUMN_DATE,
				LogbookSQLite.COLUMN_DURATION,
				LogbookSQLite.COLUMN_PLATFORM_TYPE,
				LogbookSQLite.COLUMN_PLATFORM_VARIATION,
				LogbookSQLite.COLUMN_REGISTRATION,
				LogbookSQLite.COLUMN_TAIL_NUMBER,
				LogbookSQLite.COLUMN_ICAO,
				LogbookSQLite.COLUMN_AERODROME_NAME,
				LogbookSQLite.COLUMN_SIM_ACTUAL,
				LogbookSQLite.COLUMN_DAY_NIGHT,
				LogbookSQLite.COLUMN_COMMAND,
				LogbookSQLite.COLUMN_SEAT,
				LogbookSQLite.COLUMN_FLIGHT_TYPE,
				LogbookSQLite.COLUMN_TAGS,
				LogbookSQLite.COLUMN_TAKEOFFS,
				LogbookSQLite.COLUMN_LANDINGS,
				LogbookSQLite.COLUMN_GO_AROUNDS,
				LogbookSQLite.COLUMN_COMMENTS
		};
		
		HSSFCell cell;
		for (int i = 0;i < headers.length;i++){
			cell = row.createCell(i);
			cell.setCellType(Cell.CELL_TYPE_STRING);
		    cell.setCellValue(headers[i]);
		}
	    
	}
	
	protected void onPostExecute(final Boolean success){
	    if (this.dialog.isShowing()){
	        this.dialog.dismiss();
	    }
	    if (success){
//	        Toast.makeText(context, "Logbook Exported Successfully", Toast.LENGTH_SHORT).show();
//	        Toast.makeText(context, "File Saved: " + outFilePath, Toast.LENGTH_LONG).show();
	        
            //Google Analytics
	        mTracker.send(new HitBuilders.EventBuilder()
	        .setCategory("Export")
	        .setAction("Export Successful")
	        .build());

//	        mActivity.finish();
//            Intent intent = new Intent(context, ActivityHome.class);
//            intent.putExtra("title", "Export To Excel");
//            intent.putExtra("message", "Logbook Exported Successfully" + "\n" + "File Saved: " + outFilePath);
//            context.startActivity(intent);
	    }else{
	        Toast.makeText(context, "Export Failed", Toast.LENGTH_SHORT).show();
            //Google Analytics
	        mTracker.send(new HitBuilders.EventBuilder()
	        .setCategory("Export")
	        .setAction("Export Failed")
	        .build());
	    }

        onTaskCompleted();
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