package com.gilvitzi.uavlogbookpro;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by Gil on 20/10/2015.
 */
public class UAVLogbookApplication extends AnalyticsApplication {

    public UAVLogbookApplication() {
    }

    public String getVersionName(){
        String versionName = "";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

         return versionName;
    }

    public int getVersionCode() {
        int versionCode = 0;
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionCode;
    }
}
