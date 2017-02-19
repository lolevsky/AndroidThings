package com.mobapp.androidthings;

import android.app.Application;

public class MainApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();

        DeviceUuidFactory.initUserID(this);
        Tracker.init(this);
    }
}
