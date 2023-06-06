package com.groupomni.omnisync;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class BackgroundService extends Service {
    // Implement necessary methods
    // ...
    private static final int NOTIFICATION_ID = 1;
    private OmniSyncApplication app;

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
