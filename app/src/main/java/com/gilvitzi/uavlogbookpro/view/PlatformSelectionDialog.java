package com.gilvitzi.uavlogbookpro.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.database.LogbookDataSource;
import com.gilvitzi.uavlogbookpro.util.StringValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gil on 16/02/2016.
 */
public class PlatformSelectionDialog {
    private Context context;
    private LogbookDataSource datasource;
    private ArrayAdapter<String> platform_type_values_adp;
    private ArrayAdapter<String> platform_variation_values_adp;
    private Spinner platform_spinner;
    private OnSelectedPlatform actionToPerform;


    public PlatformSelectionDialog(Context context, LogbookDataSource datasource,OnSelectedPlatform actionToPerform) {
        this.context = context;
        this.datasource = datasource;
        this.actionToPerform = actionToPerform;

        new GetAllPlatformTypeAndVariationTask().execute();
    }

    public void show(){
        platform_spinner = new Spinner(context);
        platform_spinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        //set Title
        String dialogTitle = context.getResources().getString(R.string.dialog_platform_selection_title);

        //set Default Text For Buttons
        String selectString = context.getResources().getString(R.string.select);
        String cancelString = context.getResources().getString(R.string.cancel);


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(dialogTitle)
                .setCancelable(true)
                .setView(platform_spinner)
                .setPositiveButton(selectString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        actionToPerform.selected((StringValuePair) platform_spinner.getSelectedItem());
                    }
                })
                .setNegativeButton(cancelString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });;
        builder.create().show();
    }

    private class GetAllPlatformTypeAndVariationTask extends AsyncTask<String, String, Boolean> {
        private static final String LOG_TAG = "GetPlatformsTask";
        private List<StringValuePair> platform_value_pairs_list = new ArrayList<StringValuePair>();

        private SpinnerAdapter platform_values_adp;

        @Override
        protected Boolean doInBackground(String... params) {
            try{
                datasource.open();
                platform_value_pairs_list = datasource.getDistinctPlatformTypeAndVariation();
                datasource.close();
                return true;
            }catch(Exception e){
                Log.e(LOG_TAG, "Error: " + e);
                datasource.close();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success){
                //Update Views:
                platform_values_adp = new ArrayAdapter<StringValuePair>(context,android.R.layout.simple_spinner_dropdown_item, platform_value_pairs_list);
                platform_spinner.setAdapter(platform_values_adp);
            }
        }
    }

    public interface OnSelectedPlatform
    {
        public void selected(StringValuePair platformTypeandVariation);
    }
}
