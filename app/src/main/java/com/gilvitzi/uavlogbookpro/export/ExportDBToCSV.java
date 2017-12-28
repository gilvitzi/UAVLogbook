package com.gilvitzi.uavlogbookpro.export;

import android.app.Activity;
import android.content.Context;

import com.gilvitzi.uavlogbookpro.activity.DatabaseActivity;
import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;
import com.gilvitzi.uavlogbookpro.database.LogbookReportQuery;
import com.gilvitzi.uavlogbookpro.database.LogbookSQLite;

/**
 * Created by User on 12/19/2017.
 */

public class ExportDBToCSV extends ExportTableToCSV{
    public ExportDBToCSV(Activity activity) {
        super(activity, LogbookReportQuery.getAllSessions());
        dialogMessage = "Building Backup";
    }
}
