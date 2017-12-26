package com.gilvitzi.uavlogbookpro.database;

/**
 * Created by User on 12/25/2017.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.util.DateTimeConverter;
import com.gilvitzi.uavlogbookpro.model.Duration;
import com.gilvitzi.uavlogbookpro.util.OnResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This AsyncTask will query the database and create a Table View from the Results
 * @author Gil Laptop
 *
 */
public class GetTableValues extends AsyncTask<String, String, Boolean> {
    private static final String LOG_TAG = "GetTableValues";

    public class QueryResults {
        public List<GetTableValues.ColumnNameType> columns;
        public List<List<String>> rows;
    }

    private Activity activity;
    private final String query;
    private LogbookDataSource datasource;
    private final ProgressDialog progressDialog;
    public OnResult<QueryResults> onFinished;
    Cursor cursor;
    QueryResults queryResults = new QueryResults();
    
    public GetTableValues(Activity activity, String query) {
        this.activity = activity;
        this.query = query;

        this.datasource = new LogbookDataSource(activity);
        progressDialog = ProgressDialog.show(activity, "", activity.getResources().getString(R.string.please_wait_progress), true);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            datasource.open();
            cursor = datasource.database.rawQuery(query, null);
            queryResults.columns = getColumnNamesAndTypes(cursor.getColumnNames());

            //Iteration
            queryResults.rows = new ArrayList<List<String>>();

            getAllDataRows(queryResults);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Retriving Data from DB Failed: " + e);
            return false;
        } finally {
            datasource.close();
        }
        return true;
    }

    private ArrayList<ColumnNameType> getColumnNamesAndTypes(String[] columnNamesArray) {
        ArrayList<ColumnNameType> columns = new ArrayList<ColumnNameType>();
        for (String columnName : columnNamesArray) {
            columns.add(new ColumnNameType(columnName));
        }

        return columns;
    }

    private void getAllDataRows(QueryResults queryResults) {
        List<String> row;
        try {
            if (cursor.moveToFirst()) {
                do {
                    row = getRowFromQuery();

                    for (int j = 0; j < row.size(); j++) {
                        String columnType = queryResults.columns.get(j).Type;
                        String rawValue = row.get(j);
                        String value = ParseCellPerColumnType(rawValue, columnType);
                        row.set(j, value);
                    }

                    queryResults.rows.add(row);
                } while (cursor.moveToNext());
            }
        } finally {
            try {
                cursor.close();
            } catch (Exception ignore) {
            }
        }
    }

    @NonNull
    private List<String> getRowFromQuery() {
        List<String> row;
        String value;
        row = new ArrayList<String>();
        for (int columnIndex = 0; columnIndex < cursor.getColumnCount(); columnIndex++) {
//            int type = cursor.getType(columnIndex); //only on API 10+
//            switch(type) {
//                case FIELD_TYPE_FLOAT:
//                case FIELD_TYPE_INTEGER:
//                case FIELD_TYPE_NULL:
//                case FIELD_TYPE_BLOB:
//                case FIELD_TYPE_STRING:
//                default:
//            }
            try {
                value = cursor.getString(columnIndex);
                row.add(value);
            } catch (Exception ignore) {
                try {
                    value = String.valueOf(cursor.getInt(columnIndex));
                    row.add(value);
                } catch (Exception ignored) {
                }
            }
        }
        return row;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        onFinished.onResult(success, queryResults);
        whenFinishedTask();
    }

    private void whenFinishedTask() {
        progressDialog.dismiss();
    }

    private String ParseCellPerColumnType(String rawValue, String columnType) {
        String value = "";
        switch(columnType) {
            case "Duration":
                long seconds = Integer.parseInt(rawValue);
                value = new Duration(activity, seconds * 1000).getString();
                break;
            case "Date":
                try {
                    Date prasedDate = DateTimeConverter.parseDate(rawValue, DateTimeConverter.ISO8601);
                    value = DateTimeConverter.getFormattedDate(activity, prasedDate);
                } catch (Exception e) {
                    value = rawValue;
                }
                break;
            case "String":
                value = rawValue;
                break;
            default:
                Log.e(LOG_TAG,"Unknown columnType " + columnType);
        }
        return value;
    }

    public class ColumnNameType {
        public String Name;
        public String Type;

        ColumnNameType(String SQLColumnName) {
            if (SQLColumnName.contains("^")) {
                String[] parts = SQLColumnName.split("\\^");
                Type = parts[0];
                Name = parts[1];
            } else {
                Type = "String";
                Name = SQLColumnName;
            }
        }
    }
}