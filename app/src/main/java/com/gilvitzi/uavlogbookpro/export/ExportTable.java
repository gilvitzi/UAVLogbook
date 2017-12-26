package com.gilvitzi.uavlogbookpro.export;

import android.app.Activity;
import android.content.Context;

import com.gilvitzi.uavlogbookpro.AnalyticsApplication;
import com.gilvitzi.uavlogbookpro.activity.DatabaseActivity;
import com.gilvitzi.uavlogbookpro.database.GetTableValues;
import com.gilvitzi.uavlogbookpro.util.OnResult;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by User on 12/25/2017.
 */

abstract class ExportTable {
    private final GetTableValues getValuesTask;
    private final Tracker mTracker;
    protected  Activity activity;
    protected Context context;

    public ExportTable(Activity activity, String query) {
        this.activity = activity;
        this.context = activity;
        final ExportTable exportTask = this;

        getValuesTask = new GetTableValues(activity, query);
        getValuesTask.onFinished = new OnResult<GetTableValues.QueryResults>() {
            @Override
            public void onResult(boolean success, GetTableValues.QueryResults queryResults) {
                exportTask.onResult(success, queryResults);
            }
        };
        mTracker = AnalyticsApplication.getDefaultTracker(activity);
    }

    public abstract void onResult(boolean success, GetTableValues.QueryResults queryResults);

    public void execute() {
        getValuesTask.execute();
    }
}
