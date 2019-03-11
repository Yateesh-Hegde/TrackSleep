package com.y.sleeptracker.utils;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.y.sleeptracker.MainActivity;
import com.y.sleeptracker.Restarter;
import com.y.sleeptracker.SleepTracker;
import com.y.sleeptracker.broadcasts.annotations.BroadcastReceiverActions;


@BroadcastReceiverActions({"android.intent.action.SCREEN_ON", "android.intent.action.SCREEN_OFF",
        "android.intent.action.DREAMING_STARTED", "android.intent.action.DREAMING_STOPPED",
        "android.intent.action.BOOT_COMPLETED","android.intent.action.QUICKBOOT_POWERON",
        "android.intent.action.USER_PRESENT","android.os.action.POWER_SAVE_MODE_CHANGED"
})
public class MyReceiver extends BroadcastReceiver {

    public MyReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.os.action.POWER_SAVE_MODE_CHANGED")) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (!pm.isPowerSaveMode()) {
                    if (!isMyServiceRunning(SleepTracker.class, context)) {
                        context.sendBroadcast(new Intent(context, Restarter.class));
                    }
                }
            }
        }
            else
            Session.getGlobalReceiverCallBack(context, intent);
            Log.e("yateesh", "" + intent.getAction());

    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }
}