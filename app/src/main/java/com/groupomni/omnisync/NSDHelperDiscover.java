package com.groupomni.omnisync;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class NSDHelperDiscover {

    private String TAG = MainActivity.class.getName();
    private Context context;

    OmniSyncApplication app;

    private List<NsdServiceInfo> discoveredServices;
    private List<NsdServiceInfo> resolvedServices;
    private List<NsdManager.ResolveListener> localResolveListenerList;

    private final Object resolveLock;
    private boolean isResolved;
    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.ResolveListener resolveListener;

    private OnResolveCompleted onResolveCompletedListUpdate;

//    public Thread startDLthread;
//    public Thread stopDLthread;


    NSDHelperDiscover(Context context) {
        this.context = context;

        app = (OmniSyncApplication) context;

        app.serviceList = new ArrayList<>();

        discoveredServices = new ArrayList<>();
        resolvedServices = new ArrayList<>();
        localResolveListenerList = new ArrayList<>();
        resolveLock = new Object();
        isResolved = true;

        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        Thread startDLthread = new Thread(new StartDiscoverListener());
        Thread stopDLthread = new Thread(new StopDiscoverListener());

        startDLthread.start();
        stopDLthread.start();
    }

    public interface OnResolveCompleted{
        void updateDeviceList();
    }

    public Thread newStopDLListener(){
        return new Thread(new StopDiscoverListener());
    }

    private void resolveDiscoveredServices() {
        for (NsdServiceInfo service : discoveredServices) {
            NsdManager.ResolveListener localResolveListener = initializeResolveListener();

            synchronized (resolveLock) {
//                Log.d(TAG, "resolveLock acquired");
                localResolveListenerList.add(localResolveListener);

                Thread resolveThread = new Thread(){

                    @Override
                    public void run() {
                        try {
                            nsdManager.resolveService(service, localResolveListenerList.get(localResolveListenerList.size() - 1));
                        }catch (IllegalArgumentException e){

                        }
                    }
                };

                resolveThread.start();

                try {
                    resolveLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                isResolved = false;
//                Log.d(TAG, "resolveLock FALSE");
//                Log.d(TAG, "EXIT");

            }
        }

        if(onResolveCompletedListUpdate != null) {
            onResolveCompletedListUpdate.updateDeviceList();
        }
        app.peerManagerUtils.askPeersForCapabilities(app.httpClient);
    }

    public void setOnResolveCompletedListUpdate(OnResolveCompleted callBackAPI){
        onResolveCompletedListUpdate = callBackAPI;
    }

    private class StopDiscoverListener implements Runnable{
        @Override
        public void run() {
            try {
                Thread.sleep(5000);
                resolveDiscoveredServices();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class StartDiscoverListener implements Runnable{
        @Override
        public void run() {
            initializeDiscoveryListener();
            nsdManager.discoverServices(Constants.NSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        }
    }

    /***********************************************************************************************
     * Discover services on the network
     **********************************************************************************************/
    private void initializeDiscoveryListener() {
        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
//                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
//                Log.d(TAG, "Service discovery success " + service);
                if (!service.getServiceType().equals(Constants.NSD_SERVICE_TYPE)) {
//                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().contains(Constants.NSD_SERVICE_NAME)) {
                    discoveredServices.add(service);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
//                resolveDiscoveredServices();
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
//                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
//                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }

    /***********************************************************************************************
     * Connect to services on the network
     **********************************************************************************************/
    private NsdManager.ResolveListener initializeResolveListener() {
        NsdManager.ResolveListener localResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
//                Log.e(TAG, "Resolve failed for service: " + serviceInfo + ", Error code: " + errorCode);
                if (errorCode == NsdManager.FAILURE_ALREADY_ACTIVE) {
                    Log.e(TAG, "FAILURE_ALREADY_ACTIVE");
                }

                synchronized (resolveLock) {
                    isResolved = true;
//                    Log.d(TAG, "isResolved TRUE");
                    resolveLock.notifyAll();
//                    Log.d(TAG, "notify ALL");
                }
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
//                Log.d(TAG, "Resolve Succeeded. " + serviceInfo);

                resolvedServices.add(serviceInfo);
//                Log.d(TAG, "No of discovered devices in onServiceFound : " + String.valueOf(resolvedServices.size()));

                app.deviceList = new ArrayList<>();

                for(NsdServiceInfo disSer : resolvedServices){
                    String hostIP = disSer.getHost().toString();
                    hostIP = hostIP.replaceFirst("/", "");
                    DeviceListItem device = new DeviceListItem(hostIP, disSer.getPort());

                    if(!app.deviceList.contains(device)){
                        app.deviceList.add(device);
                        app.serviceList.add(disSer);
                    }
                }
                synchronized (resolveLock) {
                    isResolved = true;
//                    Log.d(TAG, "isResolved TRUE");
                    resolveLock.notifyAll();
//                    Log.d(TAG, "notify ALL");
                }


            }
        };


        return localResolveListener;
    }



    /***********************************************************************************************
     * Unregister your service on application close
     **********************************************************************************************/
    public void tearDown() {
        nsdManager.stopServiceDiscovery(discoveryListener);
    }
}
