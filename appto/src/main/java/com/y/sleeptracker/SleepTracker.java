package com.y.sleeptracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;


import com.y.sleeptracker.database.DatabaseHelper;
import com.y.sleeptracker.interfaces.GlobalReceiverCallBack;
import com.y.sleeptracker.utils.Session;


public class SleepTracker extends JobIntentService  implements GlobalReceiverCallBack {
    private static final String TAG = "SleepTracker";
    public static final int JOB_ID = 0x01;

    DatabaseHelper databaseHelper = null;


    private Intent broadcastIntent;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, SleepTracker.class, JOB_ID, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new DatabaseHelper(this);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else {
            Session.setmGlobalReceiverCallback(this);
            startForeground(1, new Notification());
        }

    }



    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        Session.setmGlobalReceiverCallback(this);
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        //registerReceivers();
        return  START_STICKY;

    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        //this.sendBroadcast(broadcastIntent);
        unRegisterReceivers();

    }

    private void unRegisterReceivers() {
        Session.setmGlobalReceiverCallback(null);
        //sensorMan.unregisterListener(this);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCallBackReceived(Context context, Intent intent) {
        databaseHelper.insertEvent(intent.getAction());
    }
}
