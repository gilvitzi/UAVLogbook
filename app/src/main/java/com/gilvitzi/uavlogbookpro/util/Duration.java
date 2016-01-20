package com.gilvitzi.uavlogbookpro.util;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Gil on 17/10/2015.
 */
public class Duration {
    public static final String ISO8601 = "yyyy-MM-dd HH:mm:ss";

    private long millis = 0;


    private static final int DECIMAL = 0;
    private static final int MILLISECONDS = 1;
    private static final int STRING = 2;
    private static final int EXCEL = 3;

    public Duration(){}

    public Duration(long millis){
        this.millis = millis;
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
        String[] temp = duration.split(":");
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

        if (minutes<10){
            strMinutes = "0" + minutes;
        }else{
            strMinutes = minutes + "";
        }

        return strHours + ":" + strMinutes;
    }

    @SuppressLint("SimpleDateFormat")
    public void setISO8601(String duration){
        //Parse String to Date
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ISO8601);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            //Check the type of format (for example test split of '.', and split of '/'
            Date dt = sdf.parse(duration);
            this.millis =  dt.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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