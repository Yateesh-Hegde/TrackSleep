package com.y.sleeptracker.utils;

import android.app.Application;
import android.content.BroadcastReceiver;

import com.y.sleeptracker.receiverHelper.DynamicReceiver;

public class AppController extends Application {

    private BroadcastReceiver receiver;
    MyReceiver mR;

    @Override
    public void onCreate() {
        super.onCreate();
        mR = new MyReceiver();
        receiver = DynamicReceiver.with(mR)
                .register(this);

    }
}
