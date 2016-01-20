package com.gilvitzi.uavlogbookpro.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewConfiguration;

import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;

import java.lang.reflect.Field;


/**
 * Created by Gil on 16/10/2015.
 */
public class DatabaseActivity extends AnalyticsActivity {
    protected LogbookDataSource datasource;
    protected Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this.getBaseContext();
        datasource = new LogbookDataSource(this);
        datasource.open();

        tryForcingActionBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        datasource.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        datasource.close();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (datasource != null) datasource.close();
    }

    public LogbookDataSource getDatasource() {
        if (datasource == null)
            Log.w(this.getClass().getName(),"Datasource null returned from getDataSource()");
        return datasource;
    }

//    public void setDatasource(LogbookDataSource datasource) {
//        this.datasource = datasource;
//    }

    //Not Necessarily related to Database
    private void tryForcingActionBar() {
        //Try Forcing ActionBar
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}
