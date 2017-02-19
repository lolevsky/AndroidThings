package com.mobapp.androidthings1;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class DeviceUuidFactory {
    protected static final String PREFS_DEVICE_ID = "device_id";
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private static String uniqueID;

    public static void initUserID(MainApplication context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREFS_DEVICE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();

                //backup the changes
                BackupManager backupManager = new BackupManager(context);
                backupManager.dataChanged();
            }
        }
    }

    public static String getDeviceUuid() {
        return uniqueID;
    }
}
