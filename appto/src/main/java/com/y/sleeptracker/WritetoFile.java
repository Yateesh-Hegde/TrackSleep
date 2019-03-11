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
import java.util.ArrayList;

public class WritetoFile extends AsyncTask<Context, Integer, Void> {

    private final Activity activity;
    Dialog dialog;

    public WritetoFile(Activity activity) {
        super();
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(Context... contexts) {
        if (checkExternalMedia())
            writeToSDFile(contexts);
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        setDialog(true);
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        dialog.dismiss();

    }
    private void setDialog(boolean show) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.progress_bar, null);
        TextView tv = view.findViewById(R.id.loading_msg);
        tv.setText("Writing To File");
        builder.setView(view);
        dialog = builder.create();
        if (show)
            dialog.show();
    }


    private boolean checkExternalMedia() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        return mExternalStorageWriteable;
    }

    private void writeToSDFile(Context[] contexts) {

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

        File root = Environment.getExternalStorageDirectory();

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        File dir = new File(root.getAbsolutePath() + "/download");
        dir.mkdirs();
        File file = new File(dir, "myEvents.txt");
        DatabaseHelper databaseHelper=new DatabaseHelper(contexts[0]);

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            for (Event e : databaseHelper.getAllEvents()) {
                pw.println(String.format("%s ::: %s", e.getEvent(), e.getTimestamp()));
            }
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(this.getClass().getSimpleName(), "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
