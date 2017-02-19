package com.mobapp.androidthings1;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Gpio buttonGpio;
    private Gpio ledGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        Tracker.logScreen(TAG);

        TextView deviceIdTextView = (TextView) findViewById(R.id.device_id);
        deviceIdTextView.setText(BoardDefaults.getBoardVariant() + DeviceUuidFactory.getDeviceUuid());

        PeripheralManagerService service = new PeripheralManagerService();
        Log.d(TAG, "Available GPIO: " + service.getGpioList());

        try {
            buttonGpio = service.openGpio(BoardDefaults.getGPIOForButton());
            buttonGpio.setDirection(Gpio.DIRECTION_IN);
            buttonGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            buttonGpio.registerGpioCallback(mCallback);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }

        try {
            ledGpio = service.openGpio(BoardDefaults.getGPIOForLed());
            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    private GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            Log.i(TAG, "GPIO changed, button pressed");

            if (ledGpio == null) {
                return true;
            }

            try {
                Tracker.logClick(BoardDefaults.getGPIOForButton() + " " + gpio.getValue());

                ledGpio.setValue(!gpio.getValue());

                Database.setNewLedValue(!gpio.getValue());
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
            return true;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (buttonGpio != null) {
            buttonGpio.unregisterGpioCallback(mCallback);
            try {
                buttonGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

        if (ledGpio != null) {
            try {
                ledGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    }
}
