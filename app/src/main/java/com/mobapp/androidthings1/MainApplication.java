package com.mobapp.androidthings1;

import android.app.Application;

public class MainApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();

        DeviceUuidFactory.initUserID(this);
        Tracker.init(this);
    }
}
