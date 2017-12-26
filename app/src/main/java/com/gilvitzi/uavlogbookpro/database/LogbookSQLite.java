package com.gilvitzi.uavlogbookpro.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class LogbookSQLite extends SQLiteOpenHelper {
	public static final String TABLE_LOGBOOK = "logbook";
	public static final String DB_NAME = "logbook.db";
	private static final int DATABASE_VERSION = 1;
	
	// Logbook - Columns
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_DURATION = "duration";
	public static final String COLUMN_PLATFORM_TYPE = "platform_type";
	public static final String COLUMN_PLATFORM_VARIATION = "platform_variation";
	public static final String COLUMN_REGISTRATION = "registration";
	public static final String COLUMN_TAIL_NUMBER = "tail_number";
	public static final String COLUMN_ICAO = "icao";
	public static final String COLUMN_AERODROME_NAME = "aerodrome_name";
	public static final String COLUMN_DAY_NIGHT = "day_night";
	public static final String COLUMN_SIM_ACTUAL = "sim_actual";
	public static final String COLUMN_COMMAND = "command";
	public static final String COLUMN_SEAT = "seat";
	public static final String COLUMN_FLIGHT_TYPE = "flight_type";
	public static final String COLUMN_TAGS = "tags";
	public static final String COLUMN_TAKEOFFS = "takeoffs";
	public static final String COLUMN_LANDINGS = "landings";
	public static final String COLUMN_GO_AROUNDS = "go_arounds";
	public static final String COLUMN_COMMENTS = "comments";
	
    //Common query Parameters
	public static final String DURATION_SUM_HOURS = "SUM(strftime('%s', duration))";

    public static final String DURATION_HOURS = "(" +
            "((strftime('%s', duration))/60/60)" +
            "|| ':' ||" +
            "CASE WHEN " +
            "(CAST(((strftime('%s', duration))/60)-((strftime('%s', duration))/60/60*60) AS INTEGER)) >= 10 " +
            "THEN " +
            "(CAST(((strftime('%s', duration))/60)-((strftime('%s', duration))/60/60*60) AS INTEGER)) " +
            "ELSE " +
            " '0' ||(CAST(((strftime('%s', duration))/60)-((strftime('%s', duration))/60/60*60) AS INTEGER)) " + 
            " END " +
            ")";
    
//    public static final String DURATION_SUM_HOURS_DECIMAL = "ROUND((CAST(SUM(strftime('%s', duration)) AS REAL )/60/60),2)";
    public static final String DURATION_HOURS_DECIMAL = "ROUND((CAST((strftime('%s', duration)) AS REAL )/60/60),2)";
    
    public static final String SELECT_ALL_SESSIONS = "SELECT "
            + COLUMN_ID   + ", "
            + COLUMN_DATE + " As 'Date^date', "
            + "strftime('%s', duration)" + " As 'Duration^duration', "
            + COLUMN_PLATFORM_TYPE + ", "
            + COLUMN_PLATFORM_VARIATION + ", "
            + COLUMN_REGISTRATION + ", "
            + COLUMN_TAIL_NUMBER + ", "
            + COLUMN_ICAO + ", "
            + COLUMN_AERODROME_NAME + ", "
            + COLUMN_DAY_NIGHT + ", "
            + COLUMN_SIM_ACTUAL + ", "
            + COLUMN_COMMAND + ", "
            + COLUMN_SEAT + ", "
            + COLUMN_FLIGHT_TYPE + ", "
            + COLUMN_TAGS + ", "
            + COLUMN_TAKEOFFS + ", "
            + COLUMN_LANDINGS + ", "
            + COLUMN_GO_AROUNDS + ", "
            + COLUMN_COMMENTS
            + " FROM " + TABLE_LOGBOOK;
            
	// Database creation sql statement
	  private static final String DATABASE_CREATE = "CREATE TABLE "
	      + TABLE_LOGBOOK + "(" 
		  + COLUMN_ID 	+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
	      + COLUMN_DATE + " TEXT NOT NULL, "
	      + COLUMN_DURATION + " TEXT, "
	      + COLUMN_PLATFORM_TYPE + " TEXT, "
	      + COLUMN_PLATFORM_VARIATION + " TEXT, "
	      + COLUMN_REGISTRATION + " TEXT, "
	      + COLUMN_TAIL_NUMBER + " TEXT, "
	      + COLUMN_ICAO + " TEXT, "
	      + COLUMN_AERODROME_NAME + " TEXT, "
	      + COLUMN_DAY_NIGHT + " TEXT, "
	      + COLUMN_SIM_ACTUAL + " TEXT, "
	      + COLUMN_COMMAND + " TEXT, "
	      + COLUMN_SEAT + " TEXT, "
	      + COLUMN_FLIGHT_TYPE + " TEXT, "
	      + COLUMN_TAGS + " TEXT, "
	      + COLUMN_TAKEOFFS + " INTEGER, "
	      + COLUMN_LANDINGS + " INTEGER, "
	      + COLUMN_GO_AROUNDS + " INTEGER, "
	      + COLUMN_COMMENTS + " TEXT );";
	  
	public LogbookSQLite(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		//Create A new Database
		Log.w("DB","DataBase Creation Qry: " + DATABASE_CREATE);
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(LogbookSQLite.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		    db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGBOOK);
		    this.onCreate(db);
	}
}