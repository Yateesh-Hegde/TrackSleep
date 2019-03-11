package com.y.sleeptracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {


    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "sleepTime_db";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Event.CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertEvent(String event) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(Event.COLUMN_EVENT, event);

        // insert row
        long id = db.insert(Event.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }
    public long insertEvent(String event,String timestamp) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(Event.COLUMN_EVENT, event);
        values.put(Event.COLUMN_TIMESTAMP,timestamp);

        // insert row
        long id = db.insert(Event.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public void  deleteEvent(long id){
        String deletQuery = "DELETE  FROM " + Event.TABLE_NAME + " WHERE " +
                Event.COLUMN_ID + " =  "+id;

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(deletQuery);

    }



    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Event.TABLE_NAME + " ORDER BY " +
                Event.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Event event = new Event();
                event.setId(cursor.getInt(cursor.getColumnIndex(Event.COLUMN_ID)));
                event.setEvent(cursor.getString(cursor.getColumnIndex(Event.COLUMN_EVENT)));
                event.setTimestamp(cursor.getString(cursor.getColumnIndex(Event.COLUMN_TIMESTAMP)));

                events.add(event);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return event list
        return events;
    }
}
