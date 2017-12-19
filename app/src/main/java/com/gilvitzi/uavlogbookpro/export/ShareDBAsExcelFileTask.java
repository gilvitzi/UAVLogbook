package com.gilvitzi.uavlogbookpro.export;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;
import com.gilvitzi.uavlogbookpro.view.ShareFileDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Gil on 28/01/2016.
 */
public class ShareDBAsExcelFileTask {
    public static final String LOG_TAG = "ShareDBAsExcelFileTask";

    private ExportDBExcelTask mExportTask;
    private Context mContext;
    private Activity mActivity;
    private String mFileName;

    public ShareDBAsExcelFileTask(Activity activity, LogbookDataSource datasource, String query) {
        mActivity = activity;
        mContext = activity;
        mFileName = buildFileName();
        initExportTask(activity, datasource, query);
    }

    private String buildFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(new Date());
        String fileName = String.format("UAV Logbook %1$s.xls", dateStr);

        return fileName;
    }

    private void initExportTask(Activity activity, LogbookDataSource datasource, String query) {
        File dir = mContext.getCacheDir();
        final String filePath = dir.getPath() + "/" + mFileName;
        mExportTask = new ExportDBExcelTask(activity, datasource, dir.getPath(), mFileName);

        //When Finished:
        mExportTask.addListnener(new ExportDBExcelTask.Listener() {
            @Override
            public void onTaskCompleted() {
                new ShareFileDialog(mContext, filePath).show();
            }
        });
    }

    public void execute() {
        mExportTask.execute();
    }
}
