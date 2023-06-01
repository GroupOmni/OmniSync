package com.groupomni.omnisync;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private NsdManager nsdManager;
    private MyNsdDiscoveryListener discoveryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.d("TEST","DEBUG TEST");
//        this.server = new Server();

        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        discoveryListener = new MyNsdDiscoveryListener(nsdManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDiscovery();
    }

    private void startDiscovery() {
        nsdManager.discoverServices("_http._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        Log.d(TAG, "Service discovery started");
    }

    private void stopDiscovery() {
        nsdManager.stopServiceDiscovery(discoveryListener);
        Log.d(TAG, "Service discovery stopped");
    }

}