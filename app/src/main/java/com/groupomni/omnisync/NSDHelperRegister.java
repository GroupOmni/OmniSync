package com.groupomni.omnisync;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;

class NSDHelperRegister {

    private final Context context;

    private String serviceName;
    private int localPort;

    private final NsdManager nsdManager;
    private ServerSocket serverSocket;
    private NsdManager.RegistrationListener registrationListener;

    private OmniSyncApplication app;

    NSDHelperRegister(Context context) {
        this.context = context;

        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        app = (OmniSyncApplication) context;


        app.registration_state = "being";
        Log.d("TAG", "NSDHelperRegister init reg state : "+ app.registration_state);
        initializeServerSocket();
    }

    public int getLocalPort() {
        return localPort;
    }

    /***********************************************************************************************
     * Register service on the network
     **********************************************************************************************/
    private void initializeServerSocket() {
        try {
            // Initialize a server socket on the next available port.
            serverSocket = new ServerSocket(56000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Store the chosen port.
        localPort = serverSocket.getLocalPort();

        registerService(localPort);

        Log.d("TAG", "NSDHelperRegister init reg state : " + app.registration_state);
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(Constants.NSD_SERVICE_NAME);
        serviceInfo.setServiceType(Constants.NSD_SERVICE_TYPE);
        serviceInfo.setPort(port);

        initializeRegistrationListener();
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    private void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                serviceName = NsdServiceInfo.getServiceName();
                app.registration_state = "done";
                Toast.makeText(context, "Service Name: " + serviceName + " Port No: " + localPort, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
//                serviceName = NsdServiceInfo.getServiceName();
                Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                Toast.makeText(context, "Service unregistered", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
                Toast.makeText(context, "Unregistration failed", Toast.LENGTH_SHORT).show();
            }
        };
    }

    /***********************************************************************************************
     * Unregister your service on application close
     **********************************************************************************************/
    public void tearDown() {
        nsdManager.unregisterService(registrationListener);
    }
}

