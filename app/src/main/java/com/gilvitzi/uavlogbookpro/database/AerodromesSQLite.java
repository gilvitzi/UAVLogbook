package com.gilvitzi.uavlogbookpro.database;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import com.gilvitzi.uavlogbookpro.AnalyticsApplication;
import com.gilvitzi.uavlogbookpro.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;




public class AerodromesSQLite extends SQLiteOpenHelper {
	public static final String AERODROMES = "aerodromes";
	public static final String PARAMETERS = "parameters";
	
	private static final String LOG_TAG = "AerodromesSQLite";
	public Context context;
	
	// Aerodromes - Columns
	public static final String ID_KEY = "_id";
	public static final String ICAO_KEY = "icao";
	public static final String AIRPORT_NAME_KEY = "name";
	
	private static String DB_PATH;
	private static final String DB_NAME = AERODROMES + ".db";
	private static final int DB_VERSION = 2;

	
	// Database creation sql statement
	  private static final String DATABASE_CREATE = "CREATE TABLE "
	      + AERODROMES + "(" 
		  + ID_KEY 	+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
	      + ICAO_KEY + " TEXT, "
	      + AIRPORT_NAME_KEY + " TEXT );";
	  
	//Google Analytics
    private Tracker mTracker;
    private Activity mActivity;

	//07.07.2015 Constructor changed to accept Activity instead of Context
	public AerodromesSQLite(Activity activity) {
		super(activity, DB_NAME, null, DB_VERSION);
		context = activity.getBaseContext();
        mActivity = activity;
		DB_PATH = context.getDatabasePath(AERODROMES).getPath()+ ".db";


		//Google Analytics:
		mTracker = AnalyticsApplication.getDefaultTracker(mActivity);
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		//Create A new Database
		Log.w("DB","DataBase Creation Qry: " + DATABASE_CREATE);
		
		try {
			createDataBase(db);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method will create database in application package /databases
	 * directory when first time application launched
	 **/
	public void createDataBase(SQLiteDatabase db) throws IOException {
	    boolean mDataBaseExist = checkDataBase(db);
	    if (!mDataBaseExist) {
	        try {
	            CopyDatabase mCopyDatabase = new CopyDatabase();
	            mCopyDatabase.execute();
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	            
	          //Google Analytics
	            mTracker.send(new HitBuilders.ExceptionBuilder()
	                .setDescription("AerodromesSQLite : createDataBase")
	                .setFatal(false)
	                .build());
	        } finally {}
	    }else{
	    	Log.d("Aerodromes DB","Aerodromes DB Already Exists");
	    }
	}
	
	/**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
	/** This method checks whether database exists or not **/
	public boolean checkDataBase(SQLiteDatabase db) {
	    try {
	        final String mPath = DB_PATH;
	        final File file = new File(mPath);
			if (file.exists()){
				try{
					Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+ AERODROMES + "'", null);
				        if(cursor.getCount()>0) {
	                        cursor.close();
	                        return true;
				        }else{
		                    cursor.close();
		                    return false;
				        }

				}catch(Exception e){
					Log.w(LOG_TAG,"AERODROMES DB file Exist, but failed to open - creating new DB");
					Log.w(LOG_TAG,"Error: " + e);
					return false;
				}
			}else{return false;}
	    } catch (SQLiteException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
    


    
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(AerodromesSQLite.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		try {
            CopyDatabase mCopyDatabase = new CopyDatabase();
            mCopyDatabase.execute();
            
            //Google Analytics
	        mTracker.send(new HitBuilders.EventBuilder()
	        .setCategory("Aerodromes")
	        .setAction("DB Upgraded")
	        .build());
        } catch (Exception e) {
            e.printStackTrace();          
        } finally {}
	    this.onCreate(db);
	}

	   /**
     * This Task will copy database from /assets directory to application
     * package /databases directory
     **/
	private class CopyDatabase extends AsyncTask<String, String, Boolean> {
	    private ProgressDialog dialog;
        
	    @Override
	    protected void onPreExecute(){
	        dialog = new ProgressDialog(mActivity);
	        dialog.setMessage(mActivity.getResources().getString(R.string.please_wait_progress));
	        dialog.show();
	    }
	    
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                InputStream mInputStream = context.getAssets().open(DB_NAME);
                String outFileName = DB_PATH;
                OutputStream mOutputStream = new FileOutputStream(outFileName);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = mInputStream.read(buffer)) > 0) {
                    mOutputStream.write(buffer, 0, length);
                }
                mOutputStream.flush();
                mOutputStream.close();
                mInputStream.close();
            } catch (Exception e) {
                Log.e("AerodromesSQLite","Error Copying Database" + e);
                e.printStackTrace();
            }
            return true;
        }
        
        @Override
        protected void onPostExecute(final Boolean success) {
            if (success){
              //Google Analytics
    	        mTracker.send(new HitBuilders.EventBuilder()
    	        .setCategory("Aerodromes")
    	        .setAction("DB Created From Image")
    	        .build());
            }
            if (this.dialog.isShowing()){
                this.dialog.dismiss();
            }
        }
    }
}

