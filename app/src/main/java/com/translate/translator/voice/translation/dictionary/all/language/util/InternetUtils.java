package com.translate.translator.voice.translation.dictionary.all.language.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;

public class InternetUtils extends LiveData<Boolean> {
    private static final String TAG = "InternetUtils";
    private static InternetUtils sInstance = null;
    private final Context mContext;
    private ConnectivityManager.NetworkCallback networkCallback = null;
    private NetworkReceiver networkReceiver;
    private final ConnectivityManager connectivityManager;

    private InternetUtils(Context context) {
        mContext = context;
        connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new NetworkCallback(this);
    }
    @MainThread
    public static InternetUtils get(Context context) {
        if (sInstance == null) {
            sInstance = new InternetUtils(context);
        }
        return sInstance;
    }


    @Override
    protected void onActive() {
        super.onActive();
        updateConnection();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build();
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        } else {
            mContext.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } else {
            mContext.unregisterReceiver(networkReceiver);
        }
    }

    static class NetworkCallback extends ConnectivityManager.NetworkCallback {

        private final InternetUtils mInternetUtils;

        public NetworkCallback(InternetUtils internetUtils) {
            mInternetUtils = internetUtils;
        }

        @Override
        public void onAvailable(Network network) {
            if (network != null) {
                //Network is Available. Network Info: [type: WIFI[] - WIFI, state: CONNECTED/CONNECTED, reason: (unspecified), extra: "PTCL-BB", failover: false, available: true, roaming: false, metered: false]
                //Network is Available. Network Info: [type: MOBILE[UMTS] - MOBILE, state: CONNECTED/CONNECTED, reason: connected, extra: internet, failover: false, available: true, roaming: false, metered: true]
                mInternetUtils.postValue(true);
            }
        }

        @Override
        public void onLost(Network network) {
            mInternetUtils.postValue(false);
        }
    }

    private void updateConnection() {
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                postValue(true);
            }else{
                postValue(false);
            }
        }

    }

    class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                updateConnection();
            }
        }
    }
}
