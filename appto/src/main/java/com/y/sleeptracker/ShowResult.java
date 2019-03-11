package com.y.sleeptracker;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.y.sleeptracker.database.DatabaseHelper;
import com.y.sleeptracker.database.Event;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class ShowResult extends AsyncTask<Context, Integer, Void> {
    private static final int WRITE_REQUEST_CODE = 0;
    Activity activity;
    Dialog dialog;
    int j = 0;
    CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        public void callBack();
    }


    public ShowResult(Activity activity) {
        super();
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(Context... contexts) {

        DatabaseHelper databaseHelper = new DatabaseHelper(contexts[0]);
        ((MainActivity) activity).eventList = new ArrayList<>();
        ((MainActivity) activity).eventList = databaseHelper.getAllEvents();

        try {
            calculateSleep(((MainActivity) activity).eventList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*if (checkExternalMedia())
            writeToSDFile();*/
        return null;
    }

    private void calculateSleep(List<Event> eventList) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        SimpleDateFormat timeformat = new SimpleDateFormat("kk:mm:ss");
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        DatabaseHelper databaseHelper = new DatabaseHelper(this.activity);
        Date parsedDate = null;
        HashMap<String, Float> sleepCount = ((MainActivity) activity).sleepCount;
        String timeS = null, dateS = null,datesN = null,datesP = null;
        Timestamp eventTS;
        String previousDay,theDay,nextDay;

        for (int i = 0; i < eventList.size(); i++) {
            float time = 0;
            Event event = eventList.get(i);
            try {
                parsedDate = dateFormat.parse(event.getTimestamp());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (System.currentTimeMillis() - 345600000L >= parsedDate.getTime()) {
                //delete event
                databaseHelper.deleteEvent(event.getId());
            } else if (event.getEvent().equals("android.intent.action.USER_PRESENT")) {
                try {
                    parsedDate = dateFormat.parse(event.getTimestamp());
                    timeS = timeformat.format(parsedDate);
                    dateS = dateFormat1.format(parsedDate);

                    Calendar c = Calendar.getInstance();

                    c.setTime(parsedDate);
                    c.add(Calendar.DATE, 1);
                    datesN = dateFormat1.format(c.getTime());

                    c.setTime(parsedDate);
                    c.add(Calendar.DATE, -1);
                    datesP = dateFormat1.format(c.getTime());

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                int hour = Integer.parseInt(timeS.split(":")[0]);
                int minute = Integer.parseInt(timeS.split(":")[1]);

                if (hour >= 15 || hour <= 5) {
                    time = checkWheatherSleeping(event.getId(), eventList.subList(eventList.indexOf(event), (eventList.size()) - 1));
                    if (time > 8500000f) {
                        String day = dateS.split("-")[2];
                        if (hour >= 15) {

                            Float existing = sleepCount.get(day+"-"+
                                    String.valueOf(datesN.split("-")[2]));
                            if (existing == null)
                                sleepCount.put(day+"-"+
                                        String.valueOf(datesN.split("-")[2]), (time / 3600000));
                            else
                                sleepCount.put(day+"-"+
                                                String.valueOf(datesN.split("-")[2]),
                                        existing + (time / 3600000));

                        }
                        else {

                            Float existing = sleepCount.get(String.valueOf(datesP.split("-")[2])+
                                            "-"+day
                                    );
                            if (existing == null)
                                sleepCount.put(String.valueOf(datesP.split("-")[2])+
                                        "-"+day, time / 3600000);
                            else
                                sleepCount.put(String.valueOf(datesP.split("-")[2])+
                                        "-"+String.valueOf(day), existing + (time / 3600000));

                        }

                    }

                }
            }
        }


    }

    private float checkWheatherSleeping(int id, List<Event> events) {

        Event iniEvent = events.get(0);
        Event lastScreenOffEvent = null;

        Long tT = 1L;
        for (int i = 1; i < events.size(); i++) {
            Event eventToVerify = events.get(i);
            if (eventToVerify.getEvent().equals("android.intent.action.USER_PRESENT")) {
                if (lastScreenOffEvent == null)
                    lastScreenOffEvent = eventToVerify;
                tT = CheckinBetweentime(iniEvent, eventToVerify, lastScreenOffEvent);
                break;
            } else if (eventToVerify.getEvent().equals("android.intent.action.SCREEN_OFF")) {
                lastScreenOffEvent = eventToVerify;

            }


        }
        return (float) (tT);
    }

    private Long CheckinBetweentime(Event iniEvent, Event eventToVerify, Event lastScreenOffEvent) {

        if (lastScreenOffEvent == null)
            lastScreenOffEvent = eventToVerify;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parsedDate = null;
        Long td = null;
        Timestamp initEventTS = null, lastScreenOffEventTS = null;
        try {
            parsedDate = dateFormat.parse(iniEvent.getTimestamp());
            initEventTS = new java.sql.Timestamp(parsedDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            parsedDate = dateFormat.parse(lastScreenOffEvent.getTimestamp());
            lastScreenOffEventTS = new java.sql.Timestamp(parsedDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            td = initEventTS.getTime() - lastScreenOffEventTS.getTime();
        } catch (Exception e) {
            Log.d("TAg", "Not Enough Data");
        }

        return td;//Milliseconds


    }



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        setDialog(true);
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        ((MainActivity) activity).list = new ArrayList<>(((MainActivity) activity).sleepCount.keySet());
        callBack.callBack();
        dialog.dismiss();

    }

    private void setDialog(boolean show) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.progress_bar, null);
        TextView tv = view.findViewById(R.id.loading_msg);
        tv.setText("Fetching and computing data");
        builder.setView(view);
        dialog = builder.create();
        if (show)
            dialog.show();
    }
}
