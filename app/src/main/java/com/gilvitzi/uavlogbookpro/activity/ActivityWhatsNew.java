package com.gilvitzi.uavlogbookpro.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.gilvitzi.uavlogbookpro.AnalyticsApplication;
import com.gilvitzi.uavlogbookpro.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class ActivityWhatsNew extends Activity {
	private final String LOG_TAG = "ActivityWhatsNew";
	private static final String screen_name = "Whats New";

	//Google Analytics
    private Tracker mTracker;

    private int page;
    final static int MAX_PAGES = 1;
    private View page1;
    private View page2;
    private Button btn;
    
    @Override
    protected void onResume()
    {
		super.onResume();
		//Analytics
    	Log.i(LOG_TAG, "Setting screen name: " + screen_name);
    	mTracker.setScreenName("Image~" + screen_name);
    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_whats_new);

		page1 = findViewById(R.id.page1);
		btn = (Button) findViewById(R.id.whats_new_skip_button);
		page = 1; //set to page 2 so that the first page (containing google drive features) will not be visible

		//Google Analytics:
		mTracker = AnalyticsApplication.getDefaultTracker(this);

	}

	public void goToNext(View view)
	{
		if (page == MAX_PAGES)
			finishActivity();
		else
			loadNextPage();
			
	}

	private void finishActivity()
	{	    	
		Intent intent = new Intent(this, ActivityHome.class);
		startActivity(intent);
		this.finish();
	}
	
	private void loadNextPage()
	{
		page1.setVisibility(View.GONE);
		page2.setVisibility(View.VISIBLE);
		page++;
		if (page == MAX_PAGES)
			btn.setText(R.string.whats_new_skip_button);
		
	}
}
