package com.gilvitzi.uavlogbookpro.export;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.gilvitzi.uavlogbookpro.AnalyticsApplication;
import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.activity.ActivityHome;
import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;
import com.gilvitzi.uavlogbookpro.model.Session;
import com.gilvitzi.uavlogbookpro.util.DateTimeConverter;
import com.gilvitzi.uavlogbookpro.model.Duration;
import com.gilvitzi.uavlogbookpro.util.OnResult;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class ImportDBExcelTask extends AsyncTask<String, Integer, Boolean> {

    public static final int NUM_OF_SESSIONS_PER_BLOCK = 20;
    private final String LOG_TAG = "ImportTask";
	private final ProgressDialog dialog;
	private Context context;
	private Activity mActivity;
	private LogbookDataSource datasource;
	
	private Uri excelFileUri;
	private int sessionsCount = 0;
	private int sessionsFailedCount = 0;

	public ArrayList<ExcelParserException> errors;
	//Google Analytics
    private Tracker mTracker;
    public OnResult<String> onFinished;

    private enum FieldName{
        DATE,
        DURATION,
        PLATFORM_TYPE,
        PLATFORM_VARIATION,
        REGISTRATION,
        TAIL_NUMBER,
        ICAO_CODE,
        AERODROME_NAME,
        SIM_ACTUAL,
        DAY_NIGHT,
        COMMAND,
        SEAT,
        FLIGHT_TYPE,
        TAGS,
        TAKEOFFS,
        LANDINGS,
        GO_AROUNDS,
        REMARKS
    };
    
	public ImportDBExcelTask(Activity activity,Uri excelFileUri){
		this.context = activity;
		mActivity = activity;
		this.excelFileUri = excelFileUri;
		this.dialog = new ProgressDialog(context);
        datasource = new LogbookDataSource(context);

	}
	
	@Override
	protected void onPreExecute(){
		this.dialog.setMessage(context.getResources().getString(R.string.progress_importing_from_excel));
		this.dialog.show();
		errors = new ArrayList<ExcelParserException>();

		//Google Analytics:
		mTracker = AnalyticsApplication.getDefaultTracker(mActivity);
        
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
        datasource.open();

	    List<Session> sessions = new LinkedList<Session>();
	    try{
            InputStream fileInputStream = context.getContentResolver().openInputStream(excelFileUri);

			HSSFWorkbook hwb = new HSSFWorkbook(fileInputStream);
	    	HSSFSheet sheet = hwb.getSheet("logbook");
			int rowCount = getRowCount(sheet);
			HSSFRow row;
			long firstCellValue;


			//publish total count
	    	publishProgress(sessionsCount,rowCount,sessionsFailedCount);
	    	
	    	//Data Rows:
	    	row = sheet.getRow(0);
	    	firstCellValue = -1;
	    	int rowNum = 1;
	    	boolean parseSuccess = false;

	    	try{
	    	    row = sheet.getRow((short) rowNum);
	    	    firstCellValue = (long) row.getCell(0).getNumericCellValue();
	    	}catch(Exception e){
	    	   e.printStackTrace();
	    	}

	    	while (firstCellValue != 0){
	    		try{
	    		    Session tempSession = new Session();
	    		    parseSuccess = getSessionFromExcelRow(row,tempSession);
	    		    if (!parseSuccess){
	    		        publishProgress(sessionsCount,rowCount,++sessionsFailedCount);
	    		    }else{
	    		        sessions.add(tempSession);
	    		        publishProgress(++sessionsCount,rowCount,sessionsFailedCount);
	    		    }
	    		}catch(Exception e){
	    		    firstCellValue = 0;
	    		}
	    		rowNum++;
                row = sheet.getRow((short) rowNum);
                //try getting next row FirstCellValue
                try{
                    firstCellValue = (long)Double.parseDouble(getCellValue(row, 0));
                }catch(Exception e){
                    Log.e(LOG_TAG,"firstCellValue Failed: " + e);
                    firstCellValue = 0;
                }
	    		//in-Case of many Sessions - FreeUp Memory
	    		if (sessions.size() >= NUM_OF_SESSIONS_PER_BLOCK){
                    addSessionsBlockToDatabase(sessions);
	    			sessions.clear();
	    		}
	    	}

            addSessionsBlockToDatabase(sessions);
		    return true;
	    } catch (Exception e) {
	    	Log.e(LOG_TAG, e.getMessage());
            return false;
	    }
        finally
        {
            datasource.close();
        }
	}

    private void addSessionsBlockToDatabase(List<Session> sessions) {
        ListIterator<Session> itr = sessions.listIterator();
        try {

            while (itr.hasNext()) {
                Session session = itr.next();
                datasource.addSession(session);
            }

        }
        catch(Exception e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }
    }


	private int getRowCount(HSSFSheet sheet) {
		//Check Row Count
		HSSFRow row = sheet.getRow(0);
        String firstCellValue = "-";
		int rowCount = 0;
		while (firstCellValue != ""){
            row = sheet.getRow((short) 1+rowCount);
            try{
                firstCellValue = getCellValue(row, 0);
                if (firstCellValue != ""){
                    rowCount++;
                }
            }catch(Exception e){
                firstCellValue = "";
            }
        }
		return rowCount;
	}

    private boolean getSessionFromExcelRow(HSSFRow row,Session session){
	    String LOG_TAG = "Excel Parser"; //LOG_TAG
		HSSFCell cell;

		int cellIndex = 0;
		
		try {
		    // catch Duration / Date String / int Errors.
		    //ID
    		cell = row.getCell(cellIndex++);

            getId(session, cell);
            getDate(row, session, cellIndex++);
			getDuration(row, session, cellIndex++);
			getPlatformType(row, session, cellIndex++);
			getPlatformVariation(row, session, cellIndex++);
			getRegistration(row, session, cellIndex++);
			getTailNumber(row, session, cellIndex++);
			getIcaoCode(row, session, cellIndex++);
			getAerodromeName(row, session, cellIndex++);
            getSimOrActual(row, session, cellIndex++);
			getDayOrNight(row, session, cellIndex++);
			getCommand(row, session, cellIndex++);
			getSeat(row, session, cellIndex++);
			getFlightType(row, session, cellIndex++);
			getTags(row, session, cellIndex++);
			getTakeoffs(row, session, cellIndex++);
			getLandings(row, session, cellIndex++);
			getGoArounds(row, session, cellIndex++);
			getRemarks(row, session, cellIndex++);

		} catch(Exception e){

			//Google Analytics
			mTracker.send(new HitBuilders.ExceptionBuilder()
                .setDescription(Thread.currentThread().getName())
                .setFatal(false)
                .build());

		    Log.e(LOG_TAG, "Session Parsing Failed: " + e);
		    return false;
		}
        return true;
	}

    private void getId(Session session, HSSFCell cell) {
        String stringValue = getCellValue(cell.getRow(), 0);
        double num = Double.parseDouble(stringValue);
        long id = (long)num;
        session.setId(id);
    }

    private String getCellValue(HSSFRow row,int cellIndex)
            throws ArrayIndexOutOfBoundsException {
        String value = "";
        FieldName fieldName = FieldName.values()[cellIndex];
        try {

            HSSFCell cell = getHSSFCell(row, cellIndex);

            switch (cell.getCellType()) {
                case HSSFCell.CELL_TYPE_NUMERIC:
                    value = String.valueOf(cell.getNumericCellValue());
                    break;
                case HSSFCell.CELL_TYPE_STRING:
                    value = cell.getStringCellValue();
                    break;
                case HSSFCell.CELL_TYPE_BLANK:
                    value = "";
                    break;
                case HSSFCell.CELL_TYPE_FORMULA:
                    throw new ExcelCellTypeIsFormula(row, fieldName);
                case HSSFCell.CELL_TYPE_BOOLEAN:
                case HSSFCell.CELL_TYPE_ERROR:
                default:
                    throw new ExcelCellTypeError(row, fieldName);

            }


        } catch (ExcelCellIsNull nullCellException){
            errors.add(nullCellException);
            return "";
        }catch (ExcelCellTypeError | ExcelCellTypeIsFormula excelCellError) {
            errors.add(excelCellError);
            throw new RuntimeException(excelCellError.getMessage());
        }
        return value;
    }

    private HSSFCell getHSSFCell(HSSFRow row,int cellIndex) throws ExcelCellIsNull {
        HSSFCell cell = row.getCell(cellIndex);
        if (cell == null)
            throw new ExcelCellIsNull(row, FieldName.values()[cellIndex]);

        return cell;
    }

    private void getRemarks(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell = row.getCell(cellIndex);
		try{
            if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
                session.setComments(cell.getStringCellValue());
            }else{
                errors.add(new ExcelParserException(row.getRowNum(),
                        "Remarks",
                        "",
                        Log.WARN,"Remarks is not a Text Cell")
                );
            }
        }catch(Exception e){
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Remarks",
                    "",
                    Log.ERROR,e.toString())
            );
        }
	}

	private void getGoArounds(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell = row.getCell(cellIndex);
        if (cell == null){
            session.setGoArounds(0);
        }else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            session.setGoArounds(Integer.valueOf(cell.getStringCellValue()));
        }else if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK){
            //Do Nothing
        }else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
            session.setGoArounds((int)cell.getNumericCellValue());
        }else{
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Go-Arounds",
                    "",
                    Log.VERBOSE,
                    "Go-Arounds value is not a Number")
            );
        }

	}

	private void getLandings(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell = row.getCell(cellIndex);

        if (cell == null){
            session.setLandings(0);
        }else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            session.setLandings(Integer.valueOf(cell.getStringCellValue()));
        }else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
            session.setLandings((int) cell.getNumericCellValue());
        }else if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK){
            //Do Nothing
        }else{
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Landings",
                    "",
                    Log.VERBOSE,
                    "Landings value is not a Number")
            );
        }

	}

	private void getTakeoffs(HSSFRow row, Session session, int cellIndex) {
        HSSFCell cell = row.getCell(cellIndex);
        if (cell == null){
            session.setTakeoffs(0);
        }else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            session.setTakeoffs(Integer.valueOf(cell.getStringCellValue()));
        }else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
            session.setTakeoffs((int)cell.getNumericCellValue());
        }else if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK){
            //Do Nothing
        }else{
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Takeoffs",
                    "",
                    Log.VERBOSE,
                    "Takeoffs value is not a Number")
            );
        }
	}

	private void getTags(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell = row.getCell(cellIndex);
		try{
            if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
                session.setTags(cell.getStringCellValue());
            }else{
                errors.add(new ExcelParserException(row.getRowNum(),
                        "Tags",
                        "",
                        Log.WARN,"Tags is not a Text Cell")
                );
            }
        }catch(Exception e){
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Tags",
                    "",
                    Log.ERROR,e.toString())
            );
        }
	}

	private void getFlightType(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell = row.getCell(cellIndex);
		if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            session.setFlightType(cell.getStringCellValue());
        }else{
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Flight Type",
                    "",
                    Log.WARN,"Flight Type is not a Text Cell")
            );
        }
	}

	private void getSeat(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell = row.getCell(cellIndex);
		if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            session.setSeat(cell.getStringCellValue());
        }else{
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Seat",
                    "",
                    Log.WARN,"Seat is not a Text Cell")
            );
        }
	}

	private void getCommand(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell = row.getCell(cellIndex);
		if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            session.setCommand(cell.getStringCellValue());
        }else{
            errors.add(new ExcelParserException(row.getRowNum(),
                            "Command",
                            "",
                            Log.WARN, "Command is not a Text Cell")
            );
        }
	}

	private void getDayOrNight(HSSFRow row, Session session, int cellIndex) throws ExcelParserException {
		HSSFCell cell = row.getCell(cellIndex);

        if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            String value = cell.getStringCellValue().toLowerCase();
            String day = context.getResources().getString(R.string.field_day);
            String night = context.getResources().getString(R.string.field_night);
            if (value.equals(day.toLowerCase())){
                session.setDayNight(day);
            }else if(value.equals(night.toLowerCase())){
                session.setDayNight(night);
            }else if (value.isEmpty()){
                session.setDayNight("");
            }else{
                ExcelParserException ex = new ExcelParserException(row.getRowNum(),
                        "Day / Night",
                        String.valueOf(cell.getStringCellValue()),
                        Log.WARN,"Day / Night is in wrong format - only '" + day + "' Or '" + night + "' are allowed");

                errors.add(ex);
                throw ex;
            }
        }else{
            session.setDayNight("");
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Day / Night",
                    "",
                    Log.WARN,"Day / Night Unparsable Value")
            );
        }
	}

	private void getSimOrActual(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell = row.getCell(cellIndex);

		if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            String sim_actual = cell.getStringCellValue().toLowerCase();
            String sim = context.getResources().getString(R.string.field_simulator);
            String sim_short = context.getResources().getString(R.string.field_simulator_short);
            String actual = context.getResources().getString(R.string.field_actual_flight);
            String actual_short = context.getResources().getString(R.string.field_actual_flight_short);

            if ((sim_actual.equals(sim.toLowerCase()))||
                (sim_actual.equals(sim_short))){
                sim_actual = sim;
                session.setSimActual(cell.getStringCellValue());
            }else if ((sim_actual.equals(actual.toLowerCase()))||
                    (sim_actual.equals(actual_short))){
                sim_actual = actual;
                session.setSimActual(cell.getStringCellValue());
            }else{
                errors.add(new ExcelParserException(row.getRowNum(),
                        "Sim / Actual",
                        String.valueOf(cell.getStringCellValue()),
                        Log.WARN,"Sim / Actual is in wrong format - only '" + sim + "' Or '" + actual + "' are allowed")
                );
            }

        }else{
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Sim / Actual",
                    "",
                    Log.WARN,"Sim / Actual is not a Text Cell")
            );
        }
	}

	private void getAerodromeName(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell;
		cell = row.getCell(cellIndex);
		if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            session.setAerodromeName(cell.getStringCellValue());
        }else{
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Aerodrome Name",
                    String.valueOf(cell.getNumericCellValue()),
                    Log.WARN,"Aerodrome Name is not a Text Cell")
            );
        }
	}

	private void getIcaoCode(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell;
		cell = row.getCell(cellIndex);

            if (cell == null) {
                session.setICAO("");
            }else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
                session.setICAO(cell.getStringCellValue());
            }else{
                errors.add(new ExcelParserException(row.getRowNum(),
                                "ICAO",
                                "",
                                Log.ERROR,"ICAO Code cell is not a Text Cell")
                );
            }

	}

	private void getTailNumber(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell;
		cell = row.getCell(cellIndex);

        if (cell == null) {
            session.setTailNumber("");
        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            session.setTailNumber(cell.getStringCellValue());
        }else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
            session.setTailNumber(String.valueOf((int)cell.getNumericCellValue()));
        }else {
            String fieldValue = String.valueOf(cell.getNumericCellValue());
            String logMessage = "Tail Number Cell is not a Text Cell";

            errors.add(new ExcelParserException(row.getRowNum(),
                            "Tail Number", fieldValue, Log.WARN, logMessage)
            );
        }

	}

	private void getRegistration(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell = row.getCell(cellIndex);
        if (cell == null) {
            session.setRegistration("");
        }else if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            session.setRegistration(cell.getStringCellValue());
        }else{
            String fieldValue = String.valueOf(cell.getNumericCellValue());
            String logMessage = "Registration Cell is not a Text Cell";

            session.setRegistration(fieldValue); //add the value anyway
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Registration",fieldValue,Log.WARN, logMessage)
            );
        }
	}

	private void getPlatformVariation(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell = row.getCell(cellIndex);
		if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            session.setPlatformVariation(cell.getStringCellValue());
        }else{
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Platform Variation",
                    String.valueOf(cell.getNumericCellValue()),
                    Log.WARN,"Platform Variation is not a Text Cell")
            );
        }
	}

	private void getPlatformType(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell = row.getCell(cellIndex);
		if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            session.setPlatformType(cell.getStringCellValue());
        } else {
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Platform Type",
                    String.valueOf(cell.getNumericCellValue()),
                    Log.WARN,"Platform Type is not a Text Cell")
            );
        }
	}

	private void getDate(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell = row.getCell(cellIndex);
		try{
            String cellStringValue = getCellValue(row,cellIndex);
            Date dt = DateTimeConverter.parseDateLocal(context, cellStringValue);
            String parsedDate = DateTimeConverter.getDateDBFormat(dt);
            session.setDate(parsedDate);
        }catch(Exception e){
            errors.add(new ExcelParserException(row.getRowNum(),
                    "Date",
                    "",
                    Log.ERROR,e.toString())
            );
        }
	}

	private void getDuration(HSSFRow row, Session session, int cellIndex) {
		HSSFCell cell = row.getCell(cellIndex);
		if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            try{
                Duration d = new Duration(context);
                d.setString(cell.getStringCellValue());
                session.setDuration(d);
            }catch(Exception e){
                errors.add(new ExcelParserException(row.getRowNum(),
                        "Duration",
                        "",
                        Log.ERROR,
                        e.toString()));
                throw e;
            }
        }else{
            try{
                String duration = DateTimeConverter.parseExcerlDuration((double) cell.getNumericCellValue());
                Log.v(LOG_TAG,"Duration Numeric Format Found, value: " + duration);
                Duration d = new Duration(context);
                d.setExcel(cell.getNumericCellValue());
                session.setDuration(d);
            }catch(Exception e){
                errors.add(new ExcelParserException(row.getRowNum(),
                        "Duration",
                        "",
                        Log.ERROR,
                        e.toString()));
                throw e;
            }
        }
	}

	@Override
	protected void onPostExecute(final Boolean success) {
        super.onPostExecute(success);
        if (this.dialog.isShowing()) {
            this.dialog.dismiss();

        }

        String message = "";
        if (success) {
            message = sessionsCount + " " + context.getResources().getString(R.string.toast_sessions_imported_successfuly);

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Import")
                    .setAction("Import Successful")
                    .setValue(sessionsCount)
                    .build());

            if (sessionsFailedCount != 0) {
                message += "\n" + sessionsFailedCount + " " + context.getResources().getString(R.string.toast_sessions_failed);

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Import")
                        .setAction("Import With Failed Records")
                        .setValue(sessionsFailedCount)
                        .build());
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
            if (errors.size() > 0) {
                String full_message = message + "\n";

                for (ExcelParserException error : errors) {
                    full_message += "\n" + error.userMessage();
                }
                Log.v(LOG_TAG, full_message);
                mActivity.finish();
                Intent intent = new Intent(context, ActivityHome.class);
                intent.putExtra("message", full_message);
                intent.putExtra("title", "Importing Failures");
                context.startActivity(intent);
            }

        } else {
            Toast.makeText(context, context.getResources().getString(R.string.warning_importing_failed), Toast.LENGTH_SHORT).show();

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Import")
                    .setAction("Import Failed")
                    .build());
        }

        if (onFinished != null) {
            onFinished.onResult(success, message);
        }
    }

	@Override
    protected void onProgressUpdate(Integer... values) {
	    super.onProgressUpdate(values);
        Log.i(LOG_TAG, "onProgressUpdate(): total: " + String.valueOf(values[1]) + "  added: " + String.valueOf(values[0]) + "  failed: " + String.valueOf(values[2]));
        String message = context.getResources().getString(R.string.toast_importing) + " ";
        message +=  String.valueOf(values[0]) + "/" +  String.valueOf(values[1]);
        if (values[2]!=0){
            message += "\n" + String.valueOf(values[2]);
            message += " " + context.getResources().getString(R.string.toast_failed);
        }
        this.dialog.setMessage(message);
    }
	

	private class ExcelParserException extends Exception {
	    
        private static final long serialVersionUID = 1L;
        
        public int row;
	    public String col;
	    public String value;
	    public int level;
	    public String error;
	    
	    ExcelParserException(int row,String col,String value,int level,String error){
	        this.row = row;
	        this.col = col;
	        this.value = value;
	        this.level = level;
	        this.error = error;
	        Log.println(level,"Excel Parser Error","Line " + row + " Column '" + col + "' Error: " + error);
	    }
	    
	    public String toString(){
	        String value_str = (value.isEmpty())?"":" with value='" + value + "' ";
	        return "(" + getLogLevel(level) + ")" + " Line " + row + " Column '" + col + "'" + value_str + " Error: " + error;
	    }
	    
	    public String userMessage(){
	        String error_str = (level == Log.ERROR)?"Unparsable Value":error;
	        String value_str = (value.isEmpty())?"":" with value='" + value + "' ";
	        String action = (level == Log.ERROR)?"Error":"Ignored";
            return "Line " + row + " Column '" + col + "'" + value_str + " " + action + ": " + error_str;
	    }
	    
	    private String getLogLevel(int priority){
	        String level = "ERROR";
	        switch (priority){
	            case Log.ASSERT:
	                level = "ASSERT";
	                break;
	            case Log.DEBUG:
	                level = "DEBUG";
                    break;
	            case Log.ERROR:
	                level = "ERROR";
                    break;
	            case Log.INFO:
	                level = "INFO";
                    break;
	            case Log.VERBOSE:
	                level = "VERBOSE";
                    break;
	            case Log.WARN:
	                level = "WARN";
                    break;
                default:
                    level = "ERROR";
                    break;
	        }
	        return level;
	    }
	    
	}

    private class ExcelCellIsNull extends ExcelParserException
    {
        ExcelCellIsNull(HSSFRow row,  FieldName fieldName) {
            super(row.getRowNum(), fieldName.name(),"", Log.WARN, "Cell is null.");
        }
    }

    private class ExcelCellIsBlank extends ExcelParserException
    {
        ExcelCellIsBlank(HSSFRow row,  FieldName fieldName) {
            super(row.getRowNum(), fieldName.name(),"", Log.VERBOSE, "Cell is blank.");
        }
    }

    private class ExcelCellTypeError extends ExcelParserException
    {
        ExcelCellTypeError(HSSFRow row,  FieldName fieldName) {
            super(row.getRowNum(), fieldName.name(),"", Log.WARN, "Cell type error.");
        }
    }

    private class ExcelCellTypeIsFormula extends ExcelParserException
    {
        ExcelCellTypeIsFormula(HSSFRow row, FieldName fieldName) {
            super(row.getRowNum(), fieldName.name(),"", Log.WARN, "Cell type is Formula.");
        }
    }
}