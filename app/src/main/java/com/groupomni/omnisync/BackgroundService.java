package com.groupomni.omnisync;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class BackgroundService extends Service {
    // Implement necessary methods
    // ...
    private static final int NOTIFICATION_ID = 1;
    private OmniSyncApplication app;

    private class FileResolver implements  HTTPClient.PeerManagerCallBack{
        private String hostIP;
        private  int port;


        public FileResolver(String hostIP){
            this.hostIP = hostIP;
            port = 8085;
        }

        @Override
        public void reportResponse() {

        }

        @Override
        public void processResponse(JSONObject response) {
            HashMap<String, HashMap<String, Object>> responseHM = app.fileMangerUtils.jsonToHashMap(String.valueOf(response));
            HashMap<String, HashMap<String, Object>> localHM = app.fileMangerUtils.scanFolder(Uri.parse(app.syncFolder));

            HashMap<String, HashMap<String, Object>> difference = app.fileMangerUtils.compareHashMaps(localHM,responseHM);

            Log.d("DIRECTORY",app.fileMangerUtils.hashMapToJson(difference));

            String[] files = difference.keySet().toArray(new String[0]);

            for(String file : files){
                if(!app.fileMangerUtils.isFileLockedFromHashMap(localHM,file)) {
                    app.ftpClient.connectAndDownloadFile(this.hostIP, this.port, file, app.syncFolder);
                }
            }

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());

        app = (OmniSyncApplication) getApplicationContext();

        app.forgroundOnStartCommand += 1;

        Thread backgroundLog = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("BACKGROUND", "Background : " + String.valueOf(app.forgroundOnStartCommand));
            }
        });

        backgroundLog.start();

        Thread peerManagement = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if (app.nsdHelperDiscover != null) {
                        Log.d("BACKGROUND", "nsdHelperDiscover is NOT NULL");
                        Thread stopDLThread = app.nsdHelperDiscover.newStopDLListener();
                        stopDLThread.start();
                    }

                    Log.d("BACKGROUND", "Updating peers list in background");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        peerManagement.start();

        Thread fileSync = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if (app.fileMangerUtils != null && app.syncFolder != null) {
                        if(!app.ftpServer.isRunning){
                            app.ftpServer.startServer();
                        }
                        HashMap<String, HashMap<String, Object>> localFiles = app.fileMangerUtils.scanFolder(Uri.parse(app.syncFolder));
                        for(DeviceListItem peer : app.deviceList){

                            String hostIP = peer.getHostIP();
                            int port = peer.getPort();

                            String requestURI = "http://" + hostIP + ":" + String.valueOf(8080);
                            Log.d("NETWORK", requestURI);
                            String response;

                            try {
                                String fileListResponse = app.httpClient.getFilesOnRemote(requestURI, new FileResolver(hostIP));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        fileSync.start();

        return super.onStartCommand(intent, flags, startId);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Return an instance of IBinder to communicate with the service
        return null;
    }

    public class LocalBinder extends Binder {
        public BackgroundService getService() {
            // Return the service instance
            return BackgroundService.this;
        }
    }

    private Notification createNotification() {
        // Create and return a notification for the foreground service
        // You can customize the notification based on your requirements
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "My Notification")
                .setContentTitle("Foreground Service")
                .setContentText("Service is running")
                .setSmallIcon(R.drawable.ic_launcher_background);

        return builder.build();
    }
}
