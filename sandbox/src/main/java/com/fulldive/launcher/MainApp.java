package com.fulldive.launcher;

import android.support.multidex.MultiDexApplication;
import com.androidnetworking.AndroidNetworking;

public class MainApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        AndroidNetworking.initialize(getApplicationContext());
        super.onCreate();
    }
}