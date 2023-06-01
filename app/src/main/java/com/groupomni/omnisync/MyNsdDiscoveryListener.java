package com.groupomni.omnisync;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class MyNsdDiscoveryListener implements NsdManager.DiscoveryListener {

    private static final String TAG = "MyNsdDiscoveryListener";
    private NsdManager nsdManager;

    public MyNsdDiscoveryListener(NsdManager nsdManager) {
        this.nsdManager = nsdManager;
    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.e(TAG, "Discovery failed: " + errorCode);
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.e(TAG, "Stop discovery failed: " + errorCode);
    }

    @Override
    public void onDiscoveryStarted(String serviceType) {
        Log.d(TAG, "Discovery started");
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.d(TAG, "Discovery stopped");
    }

    @Override
    public void onServiceFound(NsdServiceInfo serviceInfo) {
        Log.d(TAG, "Service found: " + serviceInfo.getServiceName());
        // Perform any necessary actions with the discovered service
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
        Log.d(TAG, "Service lost: " + serviceInfo.getServiceName());
        // Perform any necessary actions when a service is lost
    }
}
