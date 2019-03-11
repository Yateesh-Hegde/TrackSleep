package com.y.sleeptracker.utils;

import android.content.Context;
import android.content.Intent;

import com.y.sleeptracker.interfaces.GlobalReceiverCallBack;

public class Session {
    public static GlobalReceiverCallBack mGlobalReceiverCallback;

    public static void setmGlobalReceiverCallback(GlobalReceiverCallBack listener) {
        if (listener != null) {
           mGlobalReceiverCallback = listener;
        }
    }

    public static void getGlobalReceiverCallBack(Context context, Intent intent) {
        if (mGlobalReceiverCallback != null) {
            mGlobalReceiverCallback.onCallBackReceived(context, intent);
        }


    }
}
