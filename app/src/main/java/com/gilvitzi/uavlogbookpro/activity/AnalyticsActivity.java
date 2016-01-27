package com.gilvitzi.uavlogbookpro.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.gilvitzi.uavlogbookpro.AnalyticsApplication;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Gil on 17/10/2015.
 */
public class AnalyticsActivity extends ActionBarActivity {
    //Google Analytics
    protected Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get a Tracker (should auto-report)
        mTracker = ((AnalyticsApplication) getApplication()).getDefaultTracker(this);

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //Get an Analytics tracker to report app starts and uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    public Tracker getTracker() {
        return mTracker;
    }
}
