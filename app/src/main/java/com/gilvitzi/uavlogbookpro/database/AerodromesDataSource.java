package com.gilvitzi.uavlogbookpro.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.gilvitzi.uavlogbookpro.model.Aerodrome;

import java.util.ArrayList;
import java.util.List;

//TODO: Get Better Aerodromes DB with Countries (and maybe longer Aerodrome Names)
public class AerodromesDataSource {
    private static final String LOG_TAG = "AerodromesDataSource";
	// Database fields
	  private SQLiteDatabase database;
	  private AerodromesSQLite dbHelper;
	  @SuppressWarnings("unused")
	private String[] allColumns = { 	
			  AerodromesSQLite.ID_KEY,
			  AerodromesSQLite.ICAO_KEY,
			  AerodromesSQLite.AIRPORT_NAME_KEY,
	  };

	  public AerodromesDataSource(Activity activity) {
	    dbHelper = new AerodromesSQLite(activity);
	  }

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	    boolean create = false;
	    if (!dbExists()){
	        create = true;
	    }
	    
	    if (create){
	        try{
                dbHelper.createDataBase(database);
            }catch(SQLException sqle){
                Log.e(LOG_TAG,"Error Creating DB - " + sqle);
            }catch(Exception e){
                Log.e(LOG_TAG,"Error Init DB - " + e);
            }
	    }
	  }

	  public void close() {
	    dbHelper.close();
	  }

	  public void initiate(){
	      open();
		  close();
	  }
	  
	  public long addAerodrome(Aerodrome aerodrome) {
	      open();
		  ContentValues values = new ContentValues();
		    values.put(AerodromesSQLite.ID_KEY, aerodrome.getID());
		    values.put(AerodromesSQLite.ICAO_KEY, aerodrome.getICAO());
		    values.put(AerodromesSQLite.AIRPORT_NAME_KEY, aerodrome.getAerodromeName());		    
		    
		    long insertId = database.insert(AerodromesSQLite.AERODROMES, null,
		        values);
		    Log.i(LOG_TAG,"Attempt insert aerodrome");
		    return insertId;

	  }

	  public void deleteAerodrome(long id) {
	    open();
	    Log.w(LOG_TAG,"Aerodrome id:" + id + " was deleted");
	    database.delete(AerodromesSQLite.AERODROMES, AerodromesSQLite.ID_KEY
	        + " = " + id, null);
	  }

	  public ArrayList<Aerodrome> getAllAerodromes() {
	    open();
	    ArrayList<Aerodrome> aerodromes = new ArrayList<Aerodrome>();
	    String selectQuery = "SELECT  * FROM " + AerodromesSQLite.AERODROMES;
	    Cursor cursor = database.rawQuery(selectQuery, null);
	    try {

            // looping through all rows and adding to list
	    	Aerodrome aerodrome = null;
            if (cursor.moveToFirst()) {
                do {                   
                	aerodrome = cursorToAerodrome(cursor);
                	aerodromes.add(aerodrome);
                } while (cursor.moveToNext());
            }

        } finally {
            try { 
            	cursor.close(); 
            	return aerodromes;
            	
            } catch (Exception ignore) {}
        }
	    cursor.moveToNext();
	    return aerodromes;
	  }

	  public void getNameAndICAO(List<String> icao,List<String> name){
	      open();
		  String selectQuery = "SELECT  icao,name FROM " + AerodromesSQLite.AERODROMES;
		  Cursor cursor = database.rawQuery(selectQuery, null);
		  try {
		
			  // looping through all rows and adding to lists
			  if (cursor.moveToFirst()) {
	       		do {                   
		        	icao.add(cursor.getString(0));
		        	name.add(cursor.getString(1));
		        } while (cursor.moveToNext());
			  }
		
		  }catch(SQLiteException e){
			  Log.e(LOG_TAG,"SQLite Error: " + e);
		  }catch(Exception e){
			  Log.e(LOG_TAG,"Error: " + e);
		  }
		  cursor.moveToNext();
		  
		  try { 
		    	cursor.close(); 		    	
		  } catch (Exception ignore) {}
	  }
	  
	  private Aerodrome cursorToAerodrome(Cursor cursor) {
			//long id = cursor.getLong(cursor.getColumnIndex(AerodromesSQLite.ID_KEY));
			String icao = cursor.getString(cursor.getColumnIndex(AerodromesSQLite.ICAO_KEY));
			String name = cursor.getString(cursor.getColumnIndex(AerodromesSQLite.AIRPORT_NAME_KEY));
			
			Aerodrome aerodrome = new Aerodrome((long)0,icao,name);
	    return aerodrome;
	  }
	  
	  
	  public int countRecords(){
		  int counter = 0;
		  String query = "SELECT Count(*) FROM " + AerodromesSQLite.AERODROMES;
		  Cursor  cursor = database.rawQuery(query,null);
		  if (cursor.moveToFirst()) {
			  counter = cursor.getInt(0);
		  }
		  cursor.close();
		  return counter;
	  }
	  
	  private boolean dbExists(){
	      if (this.database==null){
	          this.open();
	      }
	      return dbHelper.checkDataBase(this.database);
	  }
}
