package com.groupomni.omnisync;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class PeerManagerUtils implements HTTPClient.PeerManagerCallBack{

    private OmniSyncApplication app;

    private int noOfPeers;
    private int respondedHosts;

    public PeerManagerUtils(Context context){
        app = (OmniSyncApplication) context;
    }

    public void askPeersForCapabilities(HTTPClient httpClient){

        List<DeviceListItem> peers = app.deviceList;

        noOfPeers = peers.size();
        respondedHosts = 0;

        for (DeviceListItem peer : peers){
            String hostIP = peer.getHostIP();
            int port = peer.getPort();

            String requestURI = "http://" + hostIP + ":" + String.valueOf(8080);
            Log.d("NETWORK", requestURI);
            String response;

            try {
                response = httpClient.getHostCapabilities(requestURI, this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Log.d("NETWORK", response);
        }
    }

    @Override
    public void reportResponse() {
        respondedHosts += 1;
        if(respondedHosts == noOfPeers){
            Log.d("COMPLETION", "All peers responded for capability request");
        }
    }

    @Override
    public void processResponse(JSONObject response) {

    }


}
