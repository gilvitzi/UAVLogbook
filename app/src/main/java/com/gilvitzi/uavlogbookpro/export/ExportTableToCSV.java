package com.gilvitzi.uavlogbookpro.export;

import android.app.Activity;

import com.gilvitzi.uavlogbookpro.activity.DatabaseActivity;
import com.gilvitzi.uavlogbookpro.database.GetTableValues;
import com.gilvitzi.uavlogbookpro.util.OnResult;

import java.util.List;

/*
 * Class ExportTableToExcel gets a query string and filepath and exports the query result to an Excel file (.xls) in the given path
 *  
 */
public class ExportTableToCSV extends ExportTable {

    private static final String VALUE_SEPERATOR = ",";
    private static final String NEW_LINE = System.getProperty("line.separator");
    private final String LOG_TAG = "ExportTableToCSV";
    private GetTableValues getValuesTask;
    public OnResult<String> onFinished;

    protected String dialogMessage;

    public ExportTableToCSV(Activity activity, String query) {
        super(activity, query);
    }

    @Override
    public void onResult(boolean success, GetTableValues.QueryResults queryResults) {
        QueryResultsToCSVBuilder builder = new QueryResultsToCSVBuilder(queryResults);
        StringBuilder csvOutput = builder.build();
        onFinished.onResult(success, csvOutput.toString());
    }

    private class QueryResultsToCSVBuilder {
        private final List<GetTableValues.ColumnNameType> columns;
        private final List<List<String>> rows;

        public QueryResultsToCSVBuilder(GetTableValues.QueryResults queryResults) {
            this.columns = queryResults.columns;
            this.rows = queryResults.rows;
        }

        public StringBuilder build() {
            StringBuilder csvOutput = new StringBuilder();
            appendHeaders(csvOutput);
            appendRows(csvOutput);
            return csvOutput;
        }

        private void appendHeaders(StringBuilder csvOutput) {
            for (GetTableValues.ColumnNameType column : columns) {
                csvOutput.append(column.Name);
                csvOutput.append(VALUE_SEPERATOR);
            }
            csvOutput.append(NEW_LINE);
        }

        private void appendRows(StringBuilder csvOutput) {
            for(int row =0; row < rows.size(); row++){
                List<String> record = rows.get(row);
                appendSessionAsCommaSeperatedRow(csvOutput, record);
                //publishProgress(String.valueOf(1 + row), String.valueOf(rows.size()));
            }
        }

        private void appendSessionAsCommaSeperatedRow(StringBuilder builder, List<String> record){
            for (int i = 0;i < record.size();i++){
                builder.append(record.get(i));
                if (i < record.size()-1)
                    builder.append(VALUE_SEPERATOR);
            }
            builder.append(NEW_LINE);
        }
    }
}