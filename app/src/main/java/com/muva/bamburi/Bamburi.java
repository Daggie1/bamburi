package com.muva.bamburi;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.muva.bamburi.models.MyObjectBox;

import java.io.File;

import io.fabric.sdk.android.Fabric;
import io.objectbox.BoxStore;

/**
 * Created by Njoro on 4/25/18.
 */
public class Bamburi extends Application {
    private static Bamburi bamburi;
    private BoxStore boxStore;
    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        bamburi = this;

        createApplicationFolder(this);

        boxStore = MyObjectBox.builder().androidContext(Bamburi.this).name("Version68").build();

        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .setReadTimeout(60_000)
                .setConnectTimeout(60_000)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);

        sAnalytics = GoogleAnalytics.getInstance(this);
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }

    public static Bamburi getInstance() {
        return bamburi;
    }

    public static void createApplicationFolder(Context context){

        File f =  new File(context.getFilesDir(), "Bamburi");
        f.mkdir();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
        }

        return sTracker;
    }


}
