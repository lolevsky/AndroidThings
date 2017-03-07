package com.mobapp.carmoduleapp;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.nio.ByteBuffer;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, Connections.EndpointDiscoveryListener, Connections.MessageListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static int[] NETWORK_TYPES = {
            ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_ETHERNET};

    private static final long TIMEOUT_DISCOVER = 1000L;
    private GoogleApiClient googleApiClient;
    private String serviceId;
    private String otherEndpointId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");

        serviceId = getString(R.string.service_id);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.CONNECTIONS_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void showConnectedToMessage(final String endpointName) {
        Toast.makeText(getApplicationContext(), getString(R.string.connected_to, endpointName), Toast.LENGTH_LONG)
             .show();

    }

    public void showApiNotConnected() {
        Toast.makeText(getApplicationContext(), getString(R.string.google_api_not_connected), Toast.LENGTH_LONG).show();
    }

    @Override public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        googleApiClient.connect();
    }

    @Override public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        startDiscovery();
    }

    @Override public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended!");
    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult.getErrorCode());
    }

    @Override public void onEndpointFound(final String endpointId, String deviceId, String serviceId, final String endpointName) {
        Log.d(TAG, "onEndpointFound:" + endpointId + ":" + endpointName);

        connectTo(endpointId, endpointName);
    }

    @Override public void onEndpointLost(String endpointId) {
        Log.d(TAG, "onEndpointLost:" + endpointId);
    }

    @Override public void onMessageReceived(String s, byte[] bytes, boolean b) {

    }

    @Override public void onDisconnected(String s) {

    }

    private void connectTo(String endpointId, final String endpointName) {
        Log.d(TAG, "connectTo:" + endpointId + ":" + endpointName);
        Nearby.Connections.sendConnectionRequest(googleApiClient, null, endpointId, null,
                new Connections.ConnectionResponseCallback() {
                    @Override
                    public void onConnectionResponse(String endpointId, Status status, byte[] bytes) {
                        Log.d(TAG, "onConnectionResponse:" + endpointId + ":" + status);
                        if (googleApiClient != null && googleApiClient.isConnected()) {
                            if (status.isSuccess()) {
                                Log.d(TAG, "onConnectionResponse: " + endpointName + " SUCCESS");
                                showConnectedToMessage(endpointName);

                                otherEndpointId = endpointId;
                            } else {
                                Log.d(TAG, "onConnectionResponse: " + endpointName + " FAILURE");
                            }
                        }
                    }
                }, this);
    }

    private boolean isConnectedToNetwork() {
        ConnectivityManager connManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        for (int networkType : NETWORK_TYPES) {
            NetworkInfo info = connManager.getNetworkInfo(networkType);
            if (info != null && info.isConnectedOrConnecting()) {
                return true;
            }
        }
        return false;
    }

    private void startDiscovery() {
        Log.d(TAG, "startDiscovery");
        if (!isConnectedToNetwork()) {
            Log.d(TAG, "startDiscovery: not connected to WiFi network.");
            return;
        }

        Nearby.Connections.startDiscovery(googleApiClient, serviceId, TIMEOUT_DISCOVER, this)
                          .setResultCallback(new ResultCallback<Status>() {
                              @Override
                              public void onResult(@NonNull Status status) {
                                  if (googleApiClient != null && googleApiClient.isConnected()) {
                                      if (status.isSuccess()) {
                                          Log.d(TAG, "startDiscovery:onResult: SUCCESS");
                                      } else {
                                          Log.d(TAG, "startDiscovery:onResult: FAILURE");

                                          int statusCode = status.getStatusCode();
                                          if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_DISCOVERING) {
                                              Log.d(TAG, "STATUS_ALREADY_DISCOVERING");
                                          }
                                      }
                                  }
                              }
                          });
    }

    private void sendCommand(final int value) {
        if (!googleApiClient.isConnected()) {
            showApiNotConnected();
            return;
        }

        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putInt(value);

        Nearby.Connections.sendReliableMessage(googleApiClient, otherEndpointId, bytes);
    }

    public void forwardButtonClick(View view){
        sendCommand(1);
    }

    public void leftButtonClick(View view){
        sendCommand(2);
    }

    public void stopButtonClick(View view){
        sendCommand(-1);
    }

    public void rightButtonClick(View view){
        sendCommand(3);
    }

    public void backwardButtonClick(View view){
        sendCommand(4);
    }
}
