package com.gilvitzi.uavlogbookpro;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by Gil on 20/10/2015.
 */
public class UAVLogbookApplication extends AnalyticsApplication {
    private PackageInfo packageInfo;

    public UAVLogbookApplication() {
        parsePackageInfo();
    }

    private void parsePackageInfo() {
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }catch (PackageManager.NameNotFoundException ex) {
            // do nothing
        }
    }

    public String getVersionName(){
        return packageInfo.versionName;
    }

    public int getVersionCode() {
        return packageInfo.versionCode;
    }
}
