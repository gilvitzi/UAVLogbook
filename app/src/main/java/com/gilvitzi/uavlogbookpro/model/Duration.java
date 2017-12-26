package com.gilvitzi.uavlogbookpro.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.util.DateTimeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Gil on 17/10/2015.
 */
public class Duration {

    public enum DurationFormat { HOURS_MINUTES, DECIMAL};
    public static final String ISO8601 = "yyyy-MM-dd HH:mm:ss";

    private DurationFormat mDurationFormat;
    private long millis = 0;

    private static final int DECIMAL = 0;
    private static final int MILLISECONDS = 1;
    private static final int STRING = 2;
    private static final int EXCEL = 3;

    public Duration(Context context){
        getUserFormat(context);
    }

    public Duration(Context context, long millis){
        this(context);
        this.millis = millis;
    }

    private void getUserFormat(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String durationFormatAsString = prefs.getString(context.getString(R.string.settings_key_duration_format), "");

        try {
            mDurationFormat = DurationFormat.valueOf(durationFormatAsString);
        } catch (IllegalArgumentException ex) {
            mDurationFormat = DurationFormat.DECIMAL;
        }
    }

    public void setMillis(long millis){
        this.millis = millis;
    }

    public long getMillis(){
        return this.millis;
    }

    public void setExcel(double duration){
        this.millis = (long)(duration * 24 * 60 * 60 * 1000.0);
    }

    public double getExcel(){
        return this.millis / 1000.0 / 60.0 / 60.0 / 24;
    }

    public void setString(String duration){
        String[] removedYear = duration.split(" ");
        String stringDuration = (removedYear.length > 1) ? removedYear[1] : removedYear[0];

        String[] temp = stringDuration.split(":");
        long hours = (long)(Integer.valueOf(temp[0]) * 60 * 60 * 1000.0);
        long minutes = (long)(Integer.valueOf(temp[1]) * 60 * 1000.0);
        this.millis =  hours + minutes;
    }

    public String getString(){
        int hours;
        int minutes;
        long seconds;
        String strHours = "";
        String strMinutes = "";
        seconds = this.millis / 1000;
        hours = (int) ((seconds / 60) /60);
        minutes = (int) ((seconds / 60) %60);
        if (hours<10){
            strHours = "0" + hours;
        }else{
            strHours = hours + "";
        }

        switch (mDurationFormat) {
            case HOURS_MINUTES:
                if (minutes<10){
                    strMinutes = "0" + minutes;
                }else{
                    strMinutes = minutes + "";
                }
                return strHours + ":" + strMinutes;
            case DECIMAL:
            default:
                strMinutes = String.format("%02d", Math.round(minutes / 60.0 * 100));
                return strHours + "." + strMinutes;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static Duration parseISO8601(Context context, String duration){
        Duration d = new Duration(context);
        //Parse String to Date
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ISO8601);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            //Check the type of format (for example test split of '.', and split of '/'
            Date dt = sdf.parse(duration);
            d.millis =  dt.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return d;
    }

    public String getISO8601(){
        return DateTimeConverter.getDate(this.millis, DateTimeConverter.ISO8601);
    }

    public void setDecimal(double duration){
        this.millis = (long)(duration * 60 * 60 * 1000);
    }

    public double getDurationDecimal(){
        return this.millis / 1000 / 60 /60;
    }
}