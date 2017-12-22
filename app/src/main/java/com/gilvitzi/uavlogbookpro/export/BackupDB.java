package com.gilvitzi.uavlogbookpro.export;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.gilvitzi.uavlogbookpro.view.ShareFileDialog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by User on 12/19/2017.
 */

public class BackupDB {
    private static final String LOG_TAG = "BackupDB";
    Activity mActivity;
    ExportDBToCSV mExportTask;

    public BackupDB(Activity activity) {
        mActivity = activity;
    }

    public void start() {
        mExportTask = new ExportDBToCSV(mActivity);
        mExportTask.setOnDataReadyHandler(new ExportTable.OnDataReadyHandler() {
            @Override
            public void onDataReady(String dataAsCSV) {
                String filePath = saveToFile(dataAsCSV);
                new ShareFileDialog(mActivity, filePath).show();
            }
        });

        mExportTask.execute();
    }

    private String saveToFile(String csvOutput) {
        String filePath = buildFilePath();

        try {
            File file = new File(filePath);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(csvOutput);
            writer.close();
        } catch (IOException ex) {
            Log.e(LOG_TAG, "error while writing to file");
        }

        return filePath;
    }

    private String buildFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(new Date());
        String fileName = String.format("UAVLogbook_Backup_%1$s.uav", dateStr);
        return fileName;
    }

    private String buildFilePath() {
        String fileName = buildFileName();
        File dir = mActivity.getCacheDir();
        String filePath = dir.getPath() + "/" + fileName;
        return filePath;
    }
}
