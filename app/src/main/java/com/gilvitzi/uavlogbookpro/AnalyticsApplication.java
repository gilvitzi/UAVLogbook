
/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gilvitzi.uavlogbookpro;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class AnalyticsApplication extends Application {
    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-43257514-1";

    public static int GENERAL_TRACKER = 0;

    public static Tracker getDefaultTracker(Context context) {
        AnalyticsApplication appContext = (AnalyticsApplication) context.getApplicationContext();
        return appContext.getTracker(TrackerName.APP_TRACKER);
    }

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public AnalyticsApplication() {
        super();
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId))
            addNewTracker(trackerId);

        return mTrackers.get(trackerId);
    }

    private void addNewTracker(TrackerName trackerId) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        Tracker tracker = null;
        if (trackerId == TrackerName.APP_TRACKER)
            tracker = analytics.newTracker(R.xml.app_tracker);
        else if (trackerId == TrackerName.GLOBAL_TRACKER)
            tracker = analytics.newTracker(R.xml.global_tracker);

        if (tracker == null)
            Log.e(this.getClass().getSimpleName(), "Google Analytics Tracker is null");

        mTrackers.put(trackerId, tracker);
    }
}