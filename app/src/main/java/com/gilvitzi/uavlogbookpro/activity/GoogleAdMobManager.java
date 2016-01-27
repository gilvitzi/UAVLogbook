package com.gilvitzi.uavlogbookpro.activity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.gilvitzi.uavlogbookpro.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * Created by Gil on 27/01/2016.
 * Google AdMob Ads Manager, Manages One AdView
 */

public class GoogleAdMobManager {

    private AdView adView = null;
    private Context context;
    private ViewGroup adViewContainer;

    public GoogleAdMobManager(Context context, ViewGroup adViewContainer) {
        this.context = context;
        this.adViewContainer = adViewContainer;
        adView = new AdView(context);
        adView.setAdUnitId(context.getResources().getString(R.string.ads_unit_id));
        adView.setAdSize(AdSize.BANNER);    //default size
        adViewContainer.addView(adView);
    }

    public void show(){
        AdRequest adRequest = buildAdRequest();
        adView.loadAd(adRequest);
    }

    @NonNull
    private AdRequest buildAdRequest() {
        return new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice(context.getResources().getString(R.string.test_device_id_galaxy_ace))
                    .addTestDevice(context.getResources().getString(R.string.test_device_id_thl_w8s))
                    .addTestDevice(context.getResources().getString(R.string.test_device_id_lg_g2))
                    .addTestDevice(context.getResources().getString(R.string.test_device_id_lg_g4))
                    .build();
    }

    public AdView getAdView() {
        return adView;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ViewGroup getAdViewContainer() {
        return adViewContainer;
    }

    public void setAdViewContainer(ViewGroup adViewContainer) {
        this.adViewContainer = adViewContainer;
    }

    public void resume() {
        adView.resume();
    }

    public void pause() {
        adView.pause();
    }

    public AdSize getAdSize() {
        return adView.getAdSize();
    }

    public void setAdSize(AdSize adSize) {
        adView.setAdSize(adSize);
    }

    public void destroy() {
        adView.destroy();
    }
}
