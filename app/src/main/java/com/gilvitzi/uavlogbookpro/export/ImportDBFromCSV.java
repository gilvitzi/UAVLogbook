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
import com.gilvitzi.uavlogbookpro.model.Duration;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class ImportDBFromCSV extends AsyncTask<String, Integer, Boolean> {


    public static final int NUM_OF_SESSIONS_PER_BLOCK = 100;

    private final String LOG_TAG = "RestoreDB";
    private final ProgressDialog dialog;
    private Context context;
    private Activity mActivity;
    private LogbookDataSource datasource;

    private Uri filePath;

    private int sessionsCount = 0;
    private int sessionsFailedCount = 0;

    private List<Session> sessions;
    private List<String> errors;
    //Google Analytics
    private Tracker mTracker;

    private enum FieldName {
        ID,
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
    }

    ;

    public ImportDBFromCSV(Activity activity, Uri fileUri) {
        this.context = activity;
        mActivity = activity;
        this.filePath = fileUri;
        this.dialog = new ProgressDialog(mActivity);
        datasource = new LogbookDataSource(context);
    }

    @Override
    protected void onPreExecute() {
        this.dialog.setMessage("Restoring DB from file");
        this.dialog.show();

        errors = new LinkedList<String>();
        //Google Analytics:
        mTracker = AnalyticsApplication.getDefaultTracker(mActivity);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        datasource.open();

        sessions = new LinkedList<Session>();
        try {
            InputStream inputFile = context.getContentResolver().openInputStream(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputFile));
            String headers = reader.readLine();

            String line = reader.readLine();
            while (line != null) {
                if (!line.isEmpty())
                    processLine(line);

                //in-Case of many Sessions - FreeUp Memory
                if (sessions.size() >= NUM_OF_SESSIONS_PER_BLOCK) {
                    addSessionsBlockToDatabase(sessions);
                    sessions.clear();
                }

                line = reader.readLine();
            }
            reader.close();
            addSessionsBlockToDatabase(sessions);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void processLine(String line) {
        Session session;
        try {
            session = parseSessionFromCSVLine(line);
            sessions.add(session);
            sessionsCount++;
        } catch (Exception ex) {
            sessionsFailedCount++;
        }

        publishProgress(sessionsCount ,sessionsFailedCount);
    }

    private Session parseSessionFromCSVLine(String line) {

        String[] cols = line.split(",",-1);

        Session session = new Session();

        String dt = cols[FieldName.DATE.ordinal()];
        session.setDate(dt);

        String duraionISO = cols[FieldName.DURATION.ordinal()];
        session.setDuration(Duration.parseISO8601(context, duraionISO));
        session.setPlatformType(cols[FieldName.PLATFORM_TYPE.ordinal()]);
        session.setPlatformVariation(cols[FieldName.PLATFORM_VARIATION.ordinal()]);
        session.setRegistration(cols[FieldName.REGISTRATION.ordinal()]);
        session.setTailNumber(cols[FieldName.TAIL_NUMBER.ordinal()]);
        session.setICAO(cols[FieldName.ICAO_CODE.ordinal()]);
        session.setAerodromeName(cols[FieldName.AERODROME_NAME.ordinal()]);
        session.setDayNight(cols[FieldName.DAY_NIGHT.ordinal()]);
        session.setSimActual(cols[FieldName.SIM_ACTUAL.ordinal()]);
        session.setCommand(cols[FieldName.COMMAND.ordinal()]);
        session.setSeat(cols[FieldName.SEAT.ordinal()]);
        session.setFlightType(cols[FieldName.FLIGHT_TYPE.ordinal()]);
        session.setTags(cols[FieldName.TAGS.ordinal()]);
        session.setTakeoffs(Integer.parseInt(cols[FieldName.TAKEOFFS.ordinal()]));
        session.setLandings(Integer.parseInt(cols[FieldName.LANDINGS.ordinal()]));
        session.setGoArounds(Integer.parseInt(cols[FieldName.GO_AROUNDS.ordinal()]));
        session.setComments(cols[FieldName.REMARKS.ordinal()]);

        return session;
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

	@Override
	protected void onPostExecute(final Boolean success){
	    super.onPostExecute(success);
	    if (this.dialog.isShowing()){
	        this.dialog.dismiss();
	        
	    }
	    if (success){
	        String message = sessionsCount + " " + context.getResources().getString(R.string.toast_sessions_imported_successfuly);

	        mTracker.send(new HitBuilders.EventBuilder()
	        .setCategory("Import")
	        .setAction("Import Successful")
	        .setValue(sessionsCount)
	        .build());
	        
	        if (sessionsFailedCount!=0){
	            message += "\n" + sessionsFailedCount + " " + context.getResources().getString(R.string.toast_sessions_failed);

		        mTracker.send(new HitBuilders.EventBuilder()
		        .setCategory("Import")
		        .setAction("Import With Failed Records")
		        .setValue(sessionsFailedCount)
		        .build());
	            Toast.makeText(context,message, Toast.LENGTH_SHORT).show();
	        }
	        if(errors.size() > 0){
	            String full_message = message + "\n";

	            for (String error : errors) {
	                Log.e(LOG_TAG,error);
                }

	            Log.v(LOG_TAG,full_message);
	            mActivity.finish();
	            Intent intent = new Intent(context, ActivityHome.class);
	            intent.putExtra("message", full_message);
	            intent.putExtra("title", "Importing Failures");
	            context.startActivity(intent);
	        }
	        
	    }else{
	        Toast.makeText(context, context.getResources().getString(R.string.warning_importing_failed), Toast.LENGTH_SHORT).show();

	        mTracker.send(new HitBuilders.EventBuilder()
	        .setCategory("Import")
	        .setAction("Import Failed")
	        .build());
	    }
	}

	@Override
    protected void onProgressUpdate(Integer... values) {
	    super.onProgressUpdate(values);
        Log.i(LOG_TAG, "onProgressUpdate(): added: " + String.valueOf(values[0]) + "  failed: " + String.valueOf(values[1]));
        String message = context.getResources().getString(R.string.toast_importing) + " ";
        message +=  "added: " + String.valueOf(values[0]);
        if (values[1]!=0){
            message += " failed: " + String.valueOf(values[1]);
        }
        this.dialog.setMessage(message);
    }
}