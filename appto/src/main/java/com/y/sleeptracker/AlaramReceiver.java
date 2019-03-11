package com.y.sleeptracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlaramReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context,Restarter.class);
        context.sendBroadcast(i);

    }
}
