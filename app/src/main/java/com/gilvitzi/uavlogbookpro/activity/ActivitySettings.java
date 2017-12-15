package com.gilvitzi.uavlogbookpro.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.gilvitzi.uavlogbookpro.R;

public class ActivitySettings extends Activity {
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    Switch hoursFractionFormatSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        settings = getSharedPreferences("UserInfo", 0);
        editor = settings.edit();

        hoursFractionFormatSwitch = (Switch) findViewById(R.id.switch_hours_fraction_format);

        initValues();
        setChangeListeners();
    }

    private void setChangeListeners() {

        hoursFractionFormatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("hours_fraction_format", isChecked);
                editor.commit();
            }
        });
    }

    private void initValues() {
        Boolean hoursFractionFormat = settings.getBoolean("hours_fraction_format", false);
        hoursFractionFormatSwitch.setChecked(hoursFractionFormat);
    }
}
