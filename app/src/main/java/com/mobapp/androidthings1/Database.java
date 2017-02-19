package com.mobapp.androidthings1;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Database {

    public static void setNewLedValue(Boolean isLight){
        getDatabaseReference().setValue(isLight);
    }

    private static DatabaseReference getDatabaseReference() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String uniqueID = DeviceUuidFactory.getDeviceUuid();

        return database.getReference("Devices").child(BoardDefaults.getBoardVariant() + uniqueID);
    }
}
