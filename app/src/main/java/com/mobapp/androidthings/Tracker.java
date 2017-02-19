package com.mobapp.androidthings;

import com.google.firebase.analytics.FirebaseAnalytics;

import android.app.Application;
import android.os.Bundle;

public class Tracker {
    private static FirebaseAnalytics firebaseAnalytics;

    private static final String CATEGORY_SCREEN = "screen";
    private static final String CATEGORY_CLICK = "click";

    Tracker(){

    }

    public static void init(Application context){
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public static void logScreen(String eventName) {
        logEvent(CATEGORY_SCREEN, eventName);
    }

    public static void logClick(String eventName) {
        logEvent(CATEGORY_CLICK, eventName);
    }

    private static void logEvent(String category, String eventName) {
        Bundle parameters = new Bundle();
        parameters.putString(FirebaseAnalytics.Param.CONTENT_TYPE, eventName);

        synchronized (firebaseAnalytics) {
            firebaseAnalytics.logEvent(category, parameters);
        }
    }

}
