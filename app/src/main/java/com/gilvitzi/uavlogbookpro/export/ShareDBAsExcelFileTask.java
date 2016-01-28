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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Gil on 28/01/2016.
 */
public class ShareDBAsExcelFileTask {
    private static final String FILE_MIME_TYPE = "application/excel";
    private static final String FILE_EXTENTION_XLS = ".xls";
    private static final String FILE_NAME_PREFIX = "UAV Logbook - ";
    private static final String FILE_PROVIDER_DOMAIN = "com.gilvitzi.fileprovider";
    private static final String TEMP_CACHE_FOLDER = "excel_files";
    private static final String FILE_PATH_PREFIX = "file://";
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
                showShareFileDialog(filePath);
            }
        });
    }

    public void execute() {
        mExportTask.execute();
    }

    private void showShareFileDialog(String filePath) {

        File fileWithinMyDir = new File(filePath);
        String sharingFileString = mContext.getResources().getString(R.string.sharing_file);
        String sharingFileSubject = mContext.getResources().getString(R.string.sharing_file_subject);
        String shareFileString = mContext.getResources().getString(R.string.share_file);

        Uri contentUri = FileProvider.getUriForFile(mContext, FILE_PROVIDER_DOMAIN, new File(filePath));

        Intent intentShareFile = new Intent(Intent.ACTION_SEND);

        if(fileWithinMyDir.exists()) {
            intentShareFile.setType(FILE_MIME_TYPE);
            intentShareFile.putExtra(Intent.EXTRA_STREAM, contentUri);

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, sharingFileSubject);
            intentShareFile.putExtra(Intent.EXTRA_TEXT, sharingFileString);

            mContext.startActivity(Intent.createChooser(intentShareFile, shareFileString));
        } else {
            Toast.makeText(mContext,"File was not found. Share action cancelled",Toast.LENGTH_LONG);
            Log.e(LOG_TAG, String.format("File %1$s was not found for share",fileWithinMyDir.getPath()));
        }
    }
}
