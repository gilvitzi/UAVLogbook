package com.gilvitzi.uavlogbookpro.export;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.activity.ActivityTableView;
import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;
import com.google.android.gms.analytics.HitBuilders;

import java.util.List;

/*
 * Class ExportTableToExcel gets a query string and filepath and exports the query result to an Excel file (.xls) in the given path
 *  
 */
public class ExportTableToHTML extends ExportTable {

    private final String LOG_TAG = "ExportTableToHTML";
    StringBuilder htmlOutput;

	public ExportTableToHTML(ActivityTableView activity, LogbookDataSource datasource, String query) {
        super(activity, datasource, query);
	}

    @Override
    protected void createDataObject() {
        htmlOutput = new StringBuilder();
        htmlOutput.append("<html><body><table>");
        appendHeaders();
        appendRows();
        htmlOutput.append("</table></body></html>");
        setData(htmlOutput);
    }

    private void appendHeaders() {
        htmlOutput.append("<tr>");
        for (String column : getColumnNames())
        {
            htmlOutput.append("<th>");
            htmlOutput.append(column);
            htmlOutput.append("</th>");
        }
        htmlOutput.append("</tr>");
    }

    private void appendRows() {
        List<List<String>> records = getRecords();

        for(int row =0; row < records.size(); row++){
            List<String> record = records.get(row);
            appendSessionAsCommaSeperatedRow(record);
            publishProgress(String.valueOf(1 + row), String.valueOf(records.size()));
        }
    }

    private void appendSessionAsCommaSeperatedRow(List<String> record){
        htmlOutput.append("<tr>");
        for (int i = 0;i < record.size();i++){
            htmlOutput.append("<td>");
            htmlOutput.append(record.get(i));
            htmlOutput.append("</td>");
	    }
        htmlOutput.append("</tr>");
    }

    protected void sendExportSuccessfulHit()
    {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(mActivity.getString(R.string.analytics_event_category_export))
                .setAction(mActivity.getString(R.string.analytics_event_action_export_csv_successful))
                .build());
    }

    protected void sendExportFailedHit()
    {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(mActivity.getString(R.string.analytics_event_category_export))
                .setAction(mActivity.getString(R.string.analytics_event_action_export_html_failed))
                .build());
    }


}