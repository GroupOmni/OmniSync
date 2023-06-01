package com.groupomni.omnisync;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MyForegroundService extends Service {
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform any initialization tasks for your service
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create a notification for the foreground service
        Notification notification = createNotification();

        // Start the service as a foreground service
        startForeground(NOTIFICATION_ID, notification);

        // Perform your desired operations here

        return START_STICKY;
    }

    private Notification createNotification() {
        // Create and customize your notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channelId")
                .setContentTitle("Foreground Service")
                .setContentText("Service is running");
//                .setSmallIcon(R.drawable.ic_notification);

        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
