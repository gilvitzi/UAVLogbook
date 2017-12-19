package com.gilvitzi.uavlogbookpro.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.gilvitzi.uavlogbookpro.R;

import java.io.File;

/**
 * Created by User on 12/19/2017.
 */

public class ShareFileDialog {
    private static final String LOG_TAG = "ShareFileDialog";
    private static final String FILE_PROVIDER_DOMAIN = "com.gilvitzi.fileprovider";

    private Context mContext;
    private String mFilePath;

    public ShareFileDialog(Context context, String filePath) {
        mContext = context;
        mFilePath = filePath;
    }

    public void show() {
        File fileWithinMyDir = new File(mFilePath);
        String sharingFileString = mContext.getResources().getString(R.string.sharing_file);
        String sharingFileSubject = mContext.getResources().getString(R.string.sharing_file_subject);
        String shareFileString = mContext.getResources().getString(R.string.share_file);

        Uri contentUri = FileProvider.getUriForFile(mContext, FILE_PROVIDER_DOMAIN, new File(mFilePath));

        Intent intentShareFile = new Intent(Intent.ACTION_SEND);

        if(fileWithinMyDir.exists()) {
            intentShareFile.setType(getMimeType(mFilePath));
            intentShareFile.putExtra(Intent.EXTRA_STREAM, contentUri);

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, sharingFileSubject);
            intentShareFile.putExtra(Intent.EXTRA_TEXT, sharingFileString);

            mContext.startActivity(Intent.createChooser(intentShareFile, shareFileString));
        } else {
            String msg = String.format("File %1$s was not found for share, sharing cancelled", fileWithinMyDir.getPath());
            Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
            Log.e(LOG_TAG, msg);
        }
    }

    private static String getMimeType(String filePath) {
        int lastDot = filePath.lastIndexOf(".");
        if (lastDot == -1)
            return "";

        String extension = filePath.substring(lastDot + 1);

        switch (extension) {
            case "xls":
                return "application/excel";
            case "csv":
                return "text/csv";
            case "json":
                return "application/json";
            default:
                return "";
        }
    }
}
