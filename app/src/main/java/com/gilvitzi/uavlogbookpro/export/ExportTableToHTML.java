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

    private String HEADER_BG_COLOR = "#930B0B";
    private String HEADER_TEXT_COLOR = "#FFFFFF";
    private String ODD_ROW_BG_COLOR;
    private String EVEN_ROW_BG_COLOR;

    public ExportTableToHTML(ActivityTableView activity, LogbookDataSource datasource, String query) {
        super(activity, datasource, query);

        initColorsFromResources();
	}

    private void initColorsFromResources() {
        HEADER_BG_COLOR = Integer.toHexString(context.getResources().getColor(R.color.dark_red_style));;
        HEADER_TEXT_COLOR = Integer.toHexString(context.getResources().getColor(R.color.table_header_text));
        ODD_ROW_BG_COLOR = Integer.toHexString(context.getResources().getColor(R.color.table_row_odd));
        EVEN_ROW_BG_COLOR = Integer.toHexString(context.getResources().getColor(R.color.table_row_even));
    }

    @Override
    protected void createDataObject() {
        htmlOutput = new StringBuilder();
        //htmlOutput.append("<html><body>");
        htmlOutput.append("<table>");
        appendHeaders();
        appendRows();
        htmlOutput.append("</table>");
        //htmlOutput.append("</body></html>");
        setData(htmlOutput);
    }

    private void appendHeaders() {
        htmlOutput.append("<tr>");
        for (String column : getColumnNames())
        {
            htmlOutput.append(String.format("<th align='right' style='background-color:%1$s;color:%1$s'>", HEADER_BG_COLOR, HEADER_TEXT_COLOR));
            htmlOutput.append(column);
            htmlOutput.append("</th>");
        }
        htmlOutput.append("</tr>");
    }

    private void appendRows() {

        List<List<String>> records = getRecords();

        for(int row =0; row < records.size(); row++){
            List<String> record = records.get(row);
            appendSessionAsCommaSeperatedRow(record, row);
            publishProgress(String.valueOf(1 + row), String.valueOf(records.size()));
        }
    }

    private void appendSessionAsCommaSeperatedRow(List<String> record,int index){
        String rowBGColor = (index % 2 == 0) ? EVEN_ROW_BG_COLOR : ODD_ROW_BG_COLOR;

        htmlOutput.append(String.format("<tr style='background-color:%1$s'>",rowBGColor));
        for (int i = 0;i < record.size();i++){
            htmlOutput.append("<td align='right'>");
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