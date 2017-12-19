
package com.gilvitzi.uavlogbookpro.activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.UAVLogbookApplication;
import com.google.android.gms.analytics.HitBuilders;

import java.lang.reflect.Field;

public class ActivityAbout extends DatabaseActivity {

	final static String LOG_TAG = "ActivityAbout";
	final static String screen_name = "About";
    private String appVersionName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setAppVersionName();

        TextView privacyPolicyLink = (TextView) findViewById(R.id.privacy_policy_link);
        privacyPolicyLink.setMovementMethod(LinkMovementMethod.getInstance());

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

    private void setAppVersionName() {
        UAVLogbookApplication app = (UAVLogbookApplication) getApplication();
        String versionName = app.getVersionName();

        TextView versionNameTv = (TextView) findViewById(R.id.versionName);
        versionNameTv.setText(appVersionName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void goToWhatsNew(View view)
    {
    	Intent intent = new Intent(this, ActivityWhatsNew.class);
    	startActivity(intent);
    	
    	mTracker.send(new HitBuilders.EventBuilder()
        .setCategory("WhatsNew")
        .setAction("About Screen Whats New Button Clicked")
        .build());
    	
    	this.finish();
    }
}
