package com.gilvitzi.uavlogbookpro.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.gilvitzi.uavlogbookpro.model.Session;
import com.gilvitzi.uavlogbookpro.model.Duration;
import com.gilvitzi.uavlogbookpro.util.NameValuePair;
import com.gilvitzi.uavlogbookpro.util.StringValuePair;

import java.util.ArrayList;
import java.util.List;

public class LogbookDataSource {
    private static final String LOG_TAG = "LogbookDataSource";
    private final Context context;
    public SQLiteDatabase database;
    private LogbookSQLite dbHelper;

    @SuppressWarnings("unused")
    private String[] allColumns = {
		  LogbookSQLite.COLUMN_ID,
		  LogbookSQLite.COLUMN_DATE,
		  LogbookSQLite.COLUMN_DURATION,
		  LogbookSQLite.COLUMN_DAY_NIGHT,
		  LogbookSQLite.COLUMN_SIM_ACTUAL,
		  LogbookSQLite.COLUMN_PLATFORM_TYPE,
		  LogbookSQLite.COLUMN_PLATFORM_VARIATION,
		  LogbookSQLite.COLUMN_ICAO,
		  LogbookSQLite.COLUMN_AERODROME_NAME,
		  LogbookSQLite.COLUMN_COMMAND,
		  LogbookSQLite.COLUMN_SEAT,
		  LogbookSQLite.COLUMN_FLIGHT_TYPE,
		  LogbookSQLite.COLUMN_TAKEOFFS,
		  LogbookSQLite.COLUMN_LANDINGS,
		  LogbookSQLite.COLUMN_GO_AROUNDS,
		  LogbookSQLite.COLUMN_COMMENTS
    };

    public LogbookDataSource(Context context) {
        this.context = context;
        dbHelper = new LogbookSQLite(context);
    }

    public void open() {
        if (database == null) {
            tryOpenDatabase();
        } else if (!database.isOpen()) {
            database.close();
            tryOpenDatabase();
        }
    }

    private void tryOpenDatabase(){
        try {
            database = dbHelper.getWritableDatabase();
        }catch(SQLiteException e) {
            Log.e("DB", "DB Error: " + e.getMessage());
        }
    }

    public void close() {
        dbHelper.close();
    }

    public SQLiteDatabase getDB(){
      return database;
    }

    public long addSession(Session session) {
        ContentValues values = session.getContentValues();
        long insertId = database.insert( LogbookSQLite.TABLE_LOGBOOK, null, values);
        Log.i(LOG_TAG,"Session " + insertId + " added");
        return insertId;
    }

    public void deleteSession(long id) {
        Log.i(LOG_TAG, "Session id:" + id + " was deleted");
        database.delete(LogbookSQLite.TABLE_LOGBOOK, LogbookSQLite.COLUMN_ID
            + " = " + id, null);
    }

    public List<Session> getAllSessions() {
        List<Session> sessions = new ArrayList<Session>();
        String selectQuery = LogbookSQLite.SELECT_ALL_SESSIONS;
        Cursor cursor = database.rawQuery(selectQuery, null);

        try {

            // looping through all rows and adding to list
            Session session = null;
            if (cursor.moveToFirst()) {
                do {
                    session = cursorToSession(cursor);
                    sessions.add(session);
                } while (cursor.moveToNext());
            }

        } finally {
            try {
                cursor.close();
                return sessions;

            } catch (Exception ignore) {}
        }

        cursor.moveToNext();
        cursor.close();
        return sessions;
    }

    public List<Session> getAllSessionsDesc() {
        List<Session> sessions = new ArrayList<Session>();
        String selectQuery = "SELECT  * FROM " +  LogbookSQLite.TABLE_LOGBOOK + " ORDER BY " +  LogbookSQLite.COLUMN_DATE + " DESC";
        Cursor cursor = database.rawQuery(selectQuery, null);
        try {

            // looping through all rows and adding to list
            Session session = null;
            if (cursor.moveToFirst()) {
                do {
                    session = cursorToSession(cursor);
                    sessions.add(session);
                } while (cursor.moveToNext());
            }

        } finally {
            try {
                cursor.close();
                return sessions;

            } catch (Exception ignore) {}
        }
        cursor.moveToNext();
        return sessions;
    }

    public List<Session> getSessionsByQuery(String query) {
        List<Session> sessions = new ArrayList<Session>();
        Cursor cursor = database.rawQuery(query, null);
        try {

            // looping through all rows and adding to list
            Session session = null;
            if (cursor.moveToFirst()) {
                do {
                    session = cursorToSession(cursor);
                    sessions.add(session);
                } while (cursor.moveToNext());
            }

        } finally {
            try {
                cursor.close();
                return sessions;

            } catch (Exception ignore) {}
        }
        cursor.moveToNext();
        return sessions;
    }

    public Session cursorToSession(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex( LogbookSQLite.COLUMN_ID));
        String date = cursor.getString(cursor.getColumnIndex( "Date^" + LogbookSQLite.COLUMN_DATE));
        long durationSeconds = cursor.getLong(cursor.getColumnIndex( "Duration^" + LogbookSQLite.COLUMN_DURATION));
        Duration duration = new Duration(context, durationSeconds * 1000);
        String platform_type = cursor.getString(cursor.getColumnIndex( LogbookSQLite.COLUMN_PLATFORM_TYPE));
        String platform_variation = cursor.getString(cursor.getColumnIndex( LogbookSQLite.COLUMN_PLATFORM_VARIATION));
        String registration = cursor.getString(cursor.getColumnIndex( LogbookSQLite.COLUMN_REGISTRATION));
        String tail_number = cursor.getString(cursor.getColumnIndex( LogbookSQLite.COLUMN_TAIL_NUMBER));
        String icao = cursor.getString(cursor.getColumnIndex( LogbookSQLite.COLUMN_ICAO));
        String aerodrome_name = cursor.getString(cursor.getColumnIndex( LogbookSQLite.COLUMN_AERODROME_NAME));
        String day_night = cursor.getString(cursor.getColumnIndex( LogbookSQLite.COLUMN_DAY_NIGHT));
        String sim_actual = cursor.getString(cursor.getColumnIndex( LogbookSQLite.COLUMN_SIM_ACTUAL));
        String command = cursor.getString(cursor.getColumnIndex( LogbookSQLite.COLUMN_COMMAND));
        String seat = cursor.getString(cursor.getColumnIndex( LogbookSQLite.COLUMN_SEAT));
        String flight_type = cursor.getString(cursor.getColumnIndex( LogbookSQLite.COLUMN_FLIGHT_TYPE));
        String tags = cursor.getString(cursor.getColumnIndex( LogbookSQLite.COLUMN_TAGS));
        long takeoffs = cursor.getLong(cursor.getColumnIndex( LogbookSQLite.COLUMN_TAKEOFFS));
        long landings = cursor.getLong(cursor.getColumnIndex( LogbookSQLite.COLUMN_LANDINGS));
        long go_arounds = cursor.getLong(cursor.getColumnIndex( LogbookSQLite.COLUMN_GO_AROUNDS));
        String comments = cursor.getString(cursor.getColumnIndex( LogbookSQLite.COLUMN_COMMENTS));

        Session session = new Session(
                id,
                date,
                duration,
                day_night,
                sim_actual,
                platform_type,
                platform_variation,
                registration,
                tail_number,
                icao,
                aerodrome_name,
                command,
                seat,
                flight_type,
                tags,
                takeoffs,
                landings,
                go_arounds,
                comments);
        return session;
    }

    public ArrayList<String> nonDistinctValues(String columnName){
        String query = "SELECT " + columnName +
                    " FROM " +  LogbookSQLite.TABLE_LOGBOOK +
                    " ORDER BY " +  LogbookSQLite.COLUMN_ID + " DESC";
        ArrayList<String> values = new ArrayList<String>();

        Cursor  cursor = database.rawQuery(query,null);
        try {
            // looping through all rows and adding values to list

            if (cursor.moveToFirst()) {
                do {
                    String item = cursor.getString(0);
                    if (item!=null){
                        values.add(item);
                    }
                } while (cursor.moveToNext());
            }

        } finally {
            try {
                cursor.close();
                return values;

            } catch (Exception ignore) {}
        }
            cursor.moveToNext();
            return values;
    }

    public ArrayList<String> distinctValues(String columnName){
      String query = "SELECT DISTINCT " + columnName +
                    " FROM " +  LogbookSQLite.TABLE_LOGBOOK +
                    " ORDER BY " +  LogbookSQLite.COLUMN_ID + " DESC";
      ArrayList<String> values = new ArrayList<String>();

      Cursor  cursor = database.rawQuery(query,null);
      try {
            // looping through all rows and adding values to list

            if (cursor.moveToFirst()) {
                do {
                    String item = cursor.getString(0);
                    if (item!=null){
                        values.add(item);
                    }
                } while (cursor.moveToNext());
            }

        } finally {
            try {
                cursor.close();
                return values;

            } catch (Exception ignore) {}
        }
        cursor.moveToNext();
        return values;
    }

    public List<String> getDistinctTags(){
      ArrayList<String> values = new ArrayList<String>();

      String query = "SELECT DISTINCT " +  LogbookSQLite.COLUMN_TAGS +
                " FROM " +  LogbookSQLite.TABLE_LOGBOOK +
                " ORDER BY " +  LogbookSQLite.COLUMN_ID + " DESC";

      Cursor  cursor = database.rawQuery(query,null);
      try {
          // looping through all rows and adding values to list

          if (cursor.moveToFirst()) {
              do {
                String item = cursor.getString(0);
                if (item!=null){
                    for (String tag : item.split(";") ){
                        if ( !tag.equals("") && !values.contains(tag) ){
                            values.add(tag);
                        }
                    }
                }
              } while (cursor.moveToNext());
          }

      } finally {
          try {
            cursor.close();
            return values;

          } catch (Exception ignore) {}
      }
    cursor.moveToNext();
    return values;
    }

    public int countRecords(){
      int counter = 0;
      String query = "SELECT Count(*) FROM " +  LogbookSQLite.TABLE_LOGBOOK;
      Cursor  cursor = database.rawQuery(query,null);
      if (cursor.moveToFirst()) {
          counter = cursor.getInt(0);
      }
      cursor.close();
      return counter;
    }

    public int getTotalHours(){
        String query = "SELECT " +  LogbookSQLite.DURATION_SUM_HOURS + " AS 'Hours' "+ " FROM " +  LogbookSQLite.TABLE_LOGBOOK;
        Cursor cursor = database.rawQuery(query,null);
        int duration = 0;
        try {
            if (cursor.moveToFirst()) {
               duration = Integer.parseInt(cursor.getString(0));
            }
        } finally {
            try {
                cursor.close();
            } catch (Exception ignore) {}
        }
        cursor.moveToNext();
        cursor.close();

        return duration;
    }

    public ArrayList<String> distinct2Values(String columnName1, String columnName2){
      String query = "SELECT DISTINCT " + columnName1 + "," + columnName2 +
                    " FROM " +  LogbookSQLite.TABLE_LOGBOOK +
                    " ORDER BY " +  LogbookSQLite.COLUMN_ID + " DESC";
      ArrayList<String> values = new ArrayList<String>();

      Cursor  cursor = database.rawQuery(query,null);
      try {
            // looping through all rows and adding values to list

            if (cursor.moveToFirst()) {
                do {
                    String item = "";
                    if (!cursor.getString(0).isEmpty()){
                        item += cursor.getString(0);
                    }
                    if (!cursor.getString(1).isEmpty()){
                        item += " " + cursor.getString(1);
                    }
                    if (item != ""){
                        values.add(item);
                    }
                } while (cursor.moveToNext());
            }

        } finally {
            try {
                cursor.close();
                return values;

            } catch (Exception ignore) {}
        }
        cursor.moveToNext();
        return values;
    }

    public ArrayList<NameValuePair> distinct2ValuesOrdered(String columnName1, String columnName2){
      String query = "SELECT DISTINCT " + columnName1 + "," + columnName2 +
                    " FROM " +  LogbookSQLite.TABLE_LOGBOOK +
                    " ORDER BY " + columnName1 + " ASC";
      ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();

      Cursor  cursor = database.rawQuery(query,null);
      try {
            if (cursor.moveToFirst()) {
                do {
                    values.add(new NameValuePair(cursor.getString(0),cursor.getString(1)));
                } while (cursor.moveToNext());
            }

        } finally {
            try {
                cursor.close();
                return values;

            } catch (Exception ignore) {}
        }
        cursor.moveToNext();
        return values;
    }

    public Session getLastSession(){
      Session session = null;
      String query =
          "SELECT * " +
          "FROM " +  LogbookSQLite.TABLE_LOGBOOK + " " +
          "WHERE " +  LogbookSQLite.COLUMN_ID + "= (SELECT MAX(" +  LogbookSQLite.COLUMN_ID + ")  FROM " +  LogbookSQLite.TABLE_LOGBOOK + ")";
      try{
          Cursor cursor = database.rawQuery(query,null);
          if (cursor.moveToFirst()) {
                session = this.cursorToSession(cursor);
          }
          cursor.close();
      }catch(Exception e){
          Log.e(LOG_TAG,"Last Session Error: " + e);
      }

      return session;
    }

    public Session getSessionById(long sessionId){
      Session session = null;
      String query =
          "SELECT * " +
          "FROM " +  LogbookSQLite.TABLE_LOGBOOK + " " +
          "WHERE " +  LogbookSQLite.COLUMN_ID + "=" + sessionId ;
      try{
          Cursor cursor = database.rawQuery(query, null);
          if (cursor.moveToFirst()) {
                session = this.cursorToSession(cursor);
          }
          cursor.close();
      }catch(Exception e){
          Log.e(LOG_TAG, "getSessionById Error: " + e);
      }

      return session;
    }

    public boolean updateSession(Session session){
      ContentValues values = session.getContentValues();

      try{
          int res = database.update(LogbookSQLite.TABLE_LOGBOOK, values, LogbookSQLite.COLUMN_ID + "=?",
                  new String[]{String.valueOf(session.getId())});
          Log.i(LOG_TAG, "Session " + session.getId() + " Updated");
          return (res == 1);
      }catch(SQLiteException e){
          Log.e(LOG_TAG,"Update Session SQLite Error: " + e);
          return false;
      }catch(Exception e){
          e.printStackTrace();
          return false;
      }

    }


    public ArrayList<NameValuePair> getTailAndRegForPlatform(String platform_type, String platform_variation){

      String query_condition = "";
      if (platform_type.isEmpty()&&platform_variation.isEmpty()){
          query_condition = "";
      }else if (platform_type.isEmpty()){
          query_condition =
                  "WHERE " +  LogbookSQLite.COLUMN_PLATFORM_TYPE + "='" + platform_type + "'";
      }else if (platform_variation.isEmpty()){
          query_condition =
                  "WHERE " +  LogbookSQLite.COLUMN_PLATFORM_VARIATION + "='" + platform_variation + "'";
      }else{
          query_condition =
                  "WHERE " +  LogbookSQLite.COLUMN_PLATFORM_TYPE + "='" + platform_type + "'" +
                  " AND " +  LogbookSQLite.COLUMN_PLATFORM_VARIATION + "='" + platform_variation + "'";
      }

      String query =
              "SELECT " +
               LogbookSQLite.COLUMN_REGISTRATION + "," +
               LogbookSQLite.COLUMN_TAIL_NUMBER + " " +
              "FROM " +  LogbookSQLite.TABLE_LOGBOOK + " " + query_condition;

      ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();

      Cursor  cursor = database.rawQuery(query,null);
      try {
            // looping through all rows and adding values to list

            if (cursor.moveToFirst()) {
                do {

                    values.add(new NameValuePair(cursor.getString(0),cursor.getString(1)));
                } while (cursor.moveToNext());
            }

        } finally {
            try {
                cursor.close();
                return values;

            } catch (Exception ignore) {}
        }
        cursor.moveToNext();
        return values;
    }

    public List<String> getAllYears(){

        String query = String.format(
                "SELECT DISTINCT strftime('%%Y',%1$s) AS 'Year' FROM %2$s ORDER BY %1$s DESC",
                LogbookSQLite.COLUMN_DATE,
                LogbookSQLite.TABLE_LOGBOOK);

        Cursor cursor = database.rawQuery(query, null);
        List<String> values = new ArrayList<String>(cursor.getCount());

        while (cursor.moveToNext()) {
            String yearString = cursor.getString(0);
            values.add(yearString);
        }
        cursor.close();

        return values;
    }

    public List<StringValuePair> getDistinctPlatformTypeAndVariation() {
        String query = String.format(
                "SELECT DISTINCT %1$s, %2$s FROM %3$s ORDER BY %1$s DESC",
                LogbookSQLite.COLUMN_PLATFORM_TYPE,
                LogbookSQLite.COLUMN_PLATFORM_VARIATION,
                LogbookSQLite.TABLE_LOGBOOK);

        Cursor cursor = database.rawQuery(query, null);
        List<StringValuePair> values = new ArrayList<StringValuePair>(cursor.getCount());
        Log.d("cursor Debug", DatabaseUtils.dumpCursorToString(cursor));
        while (cursor.moveToNext()) {
            StringValuePair typeVariationPair = new StringValuePair(cursor.getString(0), cursor.getString(1));
            values.add(typeVariationPair);
        }
        cursor.close();

        return values;
    }
}