package com.android.socialnetworks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStateReceiver";

    private static NetworkStateListener networkStateListener;

    public interface NetworkStateListener{
        void onNetworkAvailable();
        void onNetworkNotAvailable();
    }

    public NetworkStateReceiver(){}

    public NetworkStateReceiver(NetworkStateListener networkStateListener){
        NetworkStateReceiver.networkStateListener = networkStateListener;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (intent.getExtras() != null) {
            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if (ni != null && ni.isConnectedOrConnecting()) {
                networkStateListener.onNetworkAvailable();
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                networkStateListener.onNetworkNotAvailable();
            }
        }
    }
}