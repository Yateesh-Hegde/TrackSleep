package com.y.sleeptracker;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class Restarter extends BroadcastReceiver {
    private PendingIntent pendingIntent;
    private AlarmManager manager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "Service tried to stop");
        setAlaram(context,intent);



        //Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SleepTracker.enqueueWork(context, new Intent(context, SleepTracker.class));
        }
        else
        if (!isMyServiceRunning(SleepTracker.class,context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, SleepTracker.class));
            } else {
                context.startService(new Intent(context, SleepTracker.class));
            }
        }
    }

    private void setAlaram(Context context, Intent intent) {
        Intent alarmIntent = new Intent(context, AlaramReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int interval = 3600000;
        manager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+interval,pendingIntent);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }
}