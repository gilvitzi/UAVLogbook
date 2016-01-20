package com.gilvitzi.uavlogbookpro.export;


import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.gilvitzi.uavlogbookpro.AnalyticsApplication;
import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;
import com.gilvitzi.uavlogbookpro.util.UIMessage;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class GoogleDriveSyncTask 
		extends AsyncTask<String, Integer, Boolean> 
		implements ConnectionCallbacks, OnConnectionFailedListener{

	final static String LOG_TAG = "GoogleDriveSyncTask";
	private static final int RESOLVE_CONNECTION_REQUEST_CODE = 1;
	public GoogleApiClient mGoogleApiClient;
	
    //Google Analytics
    private Tracker mTracker;
    
	private Context context;
	private Activity mActivity;
	private LogbookDataSource db;
	
	private String filePath = "";
	
	boolean sync_on;
	
	public GoogleDriveSyncTask(Activity activity,LogbookDataSource db,String filePath)
	{
		context = activity;
		mActivity = activity;
		this.filePath = filePath;
		this.db = db;
		refreshSyncOnOffStatus();
		if (sync_on)
		{
			buildGoogleAPIClient();
			execute();
		}
	}
	
	private void refreshSyncOnOffStatus()
	{
		try {
			SharedPreferences settings = this.context.getSharedPreferences("UserInfo", 0);
			sync_on = settings.getBoolean("google_drive_sync", false);
		}
		catch(Exception e)
		{
			Log.d(LOG_TAG, e.getMessage()); //debug
		}
	}
	
	@Override
	protected void onPreExecute(){
		if (sync_on) {
			mGoogleApiClient.connect();
			//Google Analytics:
			mTracker = AnalyticsApplication.getDefaultTracker(mActivity);
		}
		else{

		}
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d(LOG_TAG,"Connection Result Parsing: ");
	    if (connectionResult.hasResolution()) {
	        try {
	            connectionResult.startResolutionForResult(mActivity, RESOLVE_CONNECTION_REQUEST_CODE);
	        } catch (IntentSender.SendIntentException e) {
	            // Unable to resolve, message user appropriately
	        	Log.e(LOG_TAG,"Connection Result Undefined: " + e);
	        	
		          //Google Analytics
	            mTracker.send(new HitBuilders.ExceptionBuilder()
	                .setDescription("Connection Result Undefined")
	                .setFatal(false)
	                .build());
	        }
	    } else {
	        GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), mActivity, 0).show();
	    }

        mTracker.send(new HitBuilders.EventBuilder()
        .setCategory("GoogleDriveSync")
        .setAction("Connection Failed")
        .build());
	}

	@Override
	public void onConnected(Bundle arg0) {
		UIMessage.makeToast(mActivity,"Google Drive Connected");
		Log.i(LOG_TAG,"Google Drive Connected");
		Drive.DriveApi.newDriveContents(getGoogleApiClient())
        .setResultCallback(driveContentsCallback);
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		UIMessage.makeToast(mActivity,"Google Drive Connection Suspended");
		Log.d(LOG_TAG, "Google Drive Connection Suspended");
		
	}

	@Override
	protected Boolean doInBackground(String... arg0) {
		Log.d(LOG_TAG,"doInBackground Invoked!");
		
		//1.look for the file
			//1.1 file not found - create new file
			//1.2 find out whose most updated
		
		//2. read from Drive
		
		//3. write to Drive
		return null;
	}

	private void buildGoogleAPIClient()
	{
        //Google Drive API Client Build
		mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
        .addApi(Drive.API)
        .addScope(Drive.SCOPE_FILE)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
	}
	
	final private ResultCallback<DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveContentsResult>() {
        @Override
        public void onResult(DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
            	UIMessage.makeToast(mActivity, "Error while trying to create new file contents");
                return;
            }
            final DriveContents driveContents = result.getDriveContents();
            {/* This is Google Drive API Example Code */
                // Perform I/O off the UI thread.
                new Thread() {
                    @Override
                    public void run() {
                        // write content to DriveContents
                        OutputStream outputStream = driveContents.getOutputStream();
                        Writer writer = new OutputStreamWriter(outputStream);
                        try {
                            writer.write("Hello World!");
                            writer.close();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, e.getMessage());
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("New file From Logbook App")
                                .setMimeType("application/vnd.google-apps.spreadsheet")
                                .setStarred(true).build();

                        /*
                        // create a file on root folder
                        Drive.DriveApi.getRootFolder(getGoogleApiClient())
                                .createFile(getGoogleApiClient(), changeSet, driveContents)
                                .setResultCallback(fileCallback);

                        */
                        try {
                            GoogleDriveSyncTask.fetchSpreasheets();
                        }
                        catch(Exception e)
                        {
                            Log.e(LOG_TAG,e.getMessage());
                        }
                    }
                }.start();


            } /* End of Google Drive API Example Code */
        }
    };

    final private ResultCallback<DriveFileResult> fileCallback = new
            ResultCallback<DriveFileResult>() {
        @Override
        public void onResult(DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                UIMessage.makeToast(mActivity, "Error while trying to create the file");
                return;
            }
            UIMessage.makeToast(mActivity, "Created a file with content: " + result.getDriveFile().getDriveId());
        }
    };
    
    private GoogleApiClient getGoogleApiClient(){
    	return mGoogleApiClient;
    }

    private boolean isFileExistInDrive()
    {
        //TODO: Implement
        return false;
    }






    public static void fetchSpreasheets()
            throws MalformedURLException, IOException,ServiceException {

        SpreadsheetService service =
                new SpreadsheetService("MySpreadsheetIntegration-v1");

        // TODO: Authorize the service object for a specific user (see other sections)

        // Define the URL to request.  This should never change.
        URL SPREADSHEET_FEED_URL = new URL(
                "https://spreadsheets.google.com/feeds/spreadsheets/private/full");

        // Make a request to the API and get all spreadsheets.
        SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
        List<SpreadsheetEntry> spreadsheets = feed.getEntries();

        // Iterate through all of the spreadsheets returned
        for (SpreadsheetEntry spreadsheet : spreadsheets) {
            // Print the title of this spreadsheet to the screen
            System.out.println(spreadsheet.getTitle().getPlainText());
        }
    }

}
