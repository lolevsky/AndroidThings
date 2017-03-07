package com.mobapp.carmodule;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AppIdentifier;
import com.google.android.gms.nearby.connection.AppMetadata;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, Connections.ConnectionRequestListener, Connections.MessageListener{
    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient googleApiClient;
    private String serviceId;
    private String packageName;
    private ConnectivityManager connectivityManager;

    private Gpio gpio1;
    private Gpio gpio2;
    private Gpio gpio3;
    private Gpio gpio4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                                                           .addOnConnectionFailedListener(this).addApi(Nearby.CONNECTIONS_API).build();

        serviceId = getString(R.string.service_id);
        packageName = getPackageName();

        PeripheralManagerService service = new PeripheralManagerService();
        Log.d(TAG, "Available GPIO: " + service.getGpioList());

        try {
            gpio1 = service.openGpio(BoardDefaults.getGPIO1());
            gpio1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            gpio2 = service.openGpio(BoardDefaults.getGPIO2());
            gpio2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            gpio3 = service.openGpio(BoardDefaults.getGPIO3());
            gpio3.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            gpio4 = service.openGpio(BoardDefaults.getGPIO4());
            gpio4.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        googleApiClient.connect();
    }

    @Override public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        googleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (gpio1 != null) {
            try {
                gpio1.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

        if (gpio2 != null) {
            try {
                gpio2.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

        if (gpio3 != null) {
            try {
                gpio3.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

        if (gpio4 != null) {
            try {
                gpio4.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    }

    @Override public void onConnected(@Nullable Bundle bundle) {
        startAdvertising();
    }

    @Override public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended!");
    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }

    @Override public void onConnectionRequest(final String endpointId, String deviceId, String endpointName, byte[] payload) {
        Nearby.Connections.acceptConnectionRequest(googleApiClient, endpointId, payload, this)
                          .setResultCallback(new ResultCallback<Status>() {
                              @Override
                              public void onResult(@NonNull Status status) {
                                  if (status.isSuccess()) {
                                      Log.d(TAG, "acceptConnectionRequest: SUCCESS");

                                  } else {
                                      Log.d(TAG, "acceptConnectionRequest: FAILURE");
                                  }
                              }
                          });
    }

    @Override public void onMessageReceived(String s, byte[] bytes, boolean b) {
        Log.d(TAG, "onMessageReceived");
        int value = ByteBuffer.wrap(bytes).getInt();

        controll(value);
    }

    private void controll(int value){
        try {
            switch (value) {
                case 1:
                    gpio1.setValue(true);
                    gpio4.setValue(true);
                    break;
                case 2:
                    gpio3.setValue(true);
                    break;
                case 3:
                    gpio2.setValue(true);
                    break;
                case 4:
                    gpio2.setValue(true);
                    gpio3.setValue(true);
                    break;
                case -1:
                    gpio1.setValue(false);
                    gpio2.setValue(false);
                    gpio3.setValue(false);
                    gpio4.setValue(false);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override public void onDisconnected(String s) {
        controll(-1);
    }

    private void startAdvertising() {
        Log.d(TAG, "startAdvertising");
        if (!isConnectedToNetwork()) {
            Log.d(TAG, "startAdvertising: not connected to WiFi network.");
            return;
        }

        List<AppIdentifier> appIdentifierList = new ArrayList<>();
        appIdentifierList.add(new AppIdentifier(packageName));
        AppMetadata appMetadata = new AppMetadata(appIdentifierList);
        Nearby.Connections.startAdvertising(googleApiClient, serviceId, appMetadata, 0L, this)
                          .setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
                              @Override
                              public void onResult(@NonNull Connections.StartAdvertisingResult result) {
                                  Log.d(TAG, "startAdvertising:onResult:" + result);
                                  if (result.getStatus().isSuccess()) {
                                      Log.d(TAG, "startAdvertising:onResult: SUCCESS");

                                  } else {
                                      Log.d(TAG, "startAdvertising:onResult: FAILURE ");
                                      int statusCode = result.getStatus().getStatusCode();
                                      if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_ADVERTISING) {
                                          Log.d(TAG, "STATUS_ALREADY_ADVERTISING");
                                      } else {
                                          Log.d(TAG, "STATE_READY");
                                      }
                                  }
                              }
                          });
    }

    private boolean isConnectedToNetwork() {
        NetworkInfo info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo info1 = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);

        return (info != null && info.isConnectedOrConnecting()) || (info1 != null && info1.isConnectedOrConnecting());
    }

}
