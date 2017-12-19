package com.gilvitzi.uavlogbookpro.export;

import android.app.Activity;
import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;
import com.google.android.gms.analytics.HitBuilders;
import java.util.List;

/*
 * Class ExportTableToExcel gets a query string and filepath and exports the query result to an Excel file (.xls) in the given path
 *  
 */
public class ExportTableToCSV extends ExportTable {

    private static final String VALUE_SEPERATOR = ",";
    private static final String NEW_LINE = System.getProperty("line.separator");
    private final String LOG_TAG = "ExportTableToCSV";

    public ExportTableToCSV(Activity activity, LogbookDataSource datasource, String query) {
        super(activity, datasource, query);
	}

    @Override
    protected void createDataObject() {
        StringBuilder csvOutput = new StringBuilder();
        appendHeaders(csvOutput);
        appendRows(csvOutput);
        setData(csvOutput);
    }

    private void appendHeaders(StringBuilder csvOutput) {

        for (String column : getColumnNames())
        {
            csvOutput.append(column);
            csvOutput.append(VALUE_SEPERATOR);
        }
        csvOutput.append(NEW_LINE);
    }

    private void appendRows(StringBuilder csvOutput) {
        List<List<String>> records = getRecords();

        for(int row =0; row < records.size(); row++){
            List<String> record = records.get(row);
            appendSessionAsCommaSeperatedRow(csvOutput, record);
            publishProgress(String.valueOf(1 + row), String.valueOf(records.size()));
        }
    }

    private void appendSessionAsCommaSeperatedRow(StringBuilder builder, List<String> record){
	    for (int i = 0;i < record.size();i++){
            builder.append(record.get(i));
            builder.append(VALUE_SEPERATOR);
	    }
        builder.append(NEW_LINE);
	}
}