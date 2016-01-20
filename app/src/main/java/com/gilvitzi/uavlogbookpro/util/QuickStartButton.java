package com.gilvitzi.uavlogbookpro.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

import com.gilvitzi.uavlogbookpro.activity.ActivityAddSession;
import com.gilvitzi.uavlogbookpro.activity.AnalyticsActivity;
import com.google.android.gms.analytics.HitBuilders;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gil on 24/10/2015.
 */
public class QuickStartButton extends Button {




    public enum TimerStatus {STOPPED, RUNNING}

    private static final long REFRESH_RATE = 1000;
    private static final long MINIMUM_SESSION_DURATION = 1000*60; // One Minute
    private static final String ANALYTICS_CATEGORY = "QuickStart";
    private static final String LOG_TAG = "QuickStartButton";
    private final String DURATION_EXTRA_NAME = "QS_DurationMillis";
    private final String ANALYTICS_EVENT_STOP = "QuickStart Stopped";
    private final String ANALYTICS_EVENT_START = "QuickStart Started";
    private final String STOPPED_BUTTON_TEXT = "Quick Start";

    private static long timerStartTime;
    public static TimerStatus timerStatus;


    private Handler timerHandler = new Handler();
    private TimerTick timerTick  = new TimerTick();;

    private AnalyticsActivity activity;

    public QuickStartButton(Context context) {
        super(context);
        init(context);
    }

    public QuickStartButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QuickStartButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public QuickStartButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode())
            setText(STOPPED_BUTTON_TEXT);
        else
            activity = (AnalyticsActivity) context;

        Log.d(LOG_TAG, "init method");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refresh();
    }

    public void refresh()
    {
        timerStartTime = getStartTimeFromUserSettings();
        refreshStatus();

        if (timerIsStopped())
            setText(STOPPED_BUTTON_TEXT);
        else
            resume();
    }

    private void refreshStatus() {
        if (timerStartTime == 0)
            timerStatus = TimerStatus.STOPPED;
        else
            timerStatus = TimerStatus.RUNNING;

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP)
            onAction();
        return true;
    }

    private void onAction() {
        if (timerIsStopped())
            start();
        else
            stop();
    }


    public boolean timerIsStopped() {
        return timerStatus.equals(TimerStatus.STOPPED) ;
    }
    
    public void start()
    {
        timerStartTime = System.currentTimeMillis();
        saveStartTimeInUserSettings(); //TODO: Do in Activity ?
        resume();
        sendAnalyticsEvent(ANALYTICS_EVENT_START);//TODO: Do in Activity ?
        Log.d(LOG_TAG, "QuickStart Timer Started");
    }

    private void resume() {
        timerStatus = TimerStatus.RUNNING;
        timerHandler.postDelayed(timerTick, 0);
        refreshDuration();
        Log.d(LOG_TAG, "QuickStart Timer Resumed");
    }


    public void stop()
    {
        timerStatus = TimerStatus.STOPPED ;
        setText(STOPPED_BUTTON_TEXT);

        timerHandler.removeCallbacks(timerTick);

        Log.d(LOG_TAG, "QuickStart Stopped");
        invokeActionIfNeeded();
    }

    private void invokeActionIfNeeded() {
        long duration = getDuration();

        String hms = durationToHMS(duration); //TODO: remove (DEBUG)
        Log.d(LOG_TAG, "Duration: " + hms); //TODO: remove (DEBUG)

        if (duration >= MINIMUM_SESSION_DURATION)
            invokeAddSessionActivityFromQuickStart(duration);
        else
            showNotEnoughTimeMessage();

        timerStartTime = 0;
        resetStartTimeInUserSettings();

        sendAnalyticsEvent(ANALYTICS_EVENT_STOP); //TODO: Do in Activity

    }

    private long getDuration() {
        if (timerStartTime != 0)
            return durationMillis(new Date(timerStartTime),new Date());
        else
            return 0;
    }

    private void sendAnalyticsEvent(String eventName) {
        activity
            .getTracker()
                .send(new HitBuilders.EventBuilder()
                        .setCategory(ANALYTICS_CATEGORY)
                        .setAction(eventName)
                        .build());
    }

    private void showNotEnoughTimeMessage() {
        String msg = String.format("Minimum time for a Session is %d Minute.", MINIMUM_SESSION_DURATION / 1000 / 60);
        UIMessage.makeToast(activity, msg);
    }

    private void refreshDuration() {
        long millis = System.currentTimeMillis() - timerStartTime;
        QuickStartButton.this.setText("Stop \n" + durationToHMS(millis));
    }


    private String durationToHMS(long millis)
    {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1);
        return String.format("%02d:%02d:%02d",hours , minutes, seconds );
    }



    private class TimerTick implements Runnable{

        @Override
        public void run() {
            QuickStartButton.this.refreshDuration();
            timerHandler.postDelayed(this, REFRESH_RATE);
        }
    }


    private long getStartTimeFromUserSettings()
    {
        long millis = 0;
        try{

            SharedPreferences settings = activity.getSharedPreferences("UserInfo", 0);
            millis = settings.getLong("quick_start_time", 0L);

        }catch(Exception e){
            Log.e(LOG_TAG,"Shared Pref. Get Failed: " + e);
        }

        return millis;
    }

    private void saveStartTimeInUserSettings()
    {
        try{
            SharedPreferences settings = activity.getSharedPreferences("UserInfo", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("quick_start_time", timerStartTime);

            editor.commit();
        }catch(Exception e)
        {
            Log.e(LOG_TAG,"Shared Pref. Save Failed: " + e);
        }
    }

    private void resetStartTimeInUserSettings()
    {
        try{
            SharedPreferences settings = activity.getSharedPreferences("UserInfo", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("quick_start_time", 0);
            editor.commit();
        }catch(Exception e)
        {
            Log.e(LOG_TAG,"Shared Pref. Reset Failed: " + e);
        }
    }

    private long durationMillis(Date dt1 , Date dt2)
    {
        return  dt2.getTime() - dt1.getTime();
    }

    private void invokeAddSessionActivityFromQuickStart(long millis) {
        Intent intent = new Intent(this.getContext(), ActivityAddSession.class);
        Bundle extras = new Bundle();
        extras.putLong(DURATION_EXTRA_NAME, millis);
        intent.putExtras(extras);
        this.getContext().startActivity(intent);
    }


}
