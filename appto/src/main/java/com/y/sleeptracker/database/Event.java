package com.y.sleeptracker.database;

public class Event {
    public static final String TABLE_NAME = "device_track";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EVENT = "event";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private int id;
    private String event;
    private String timestamp;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_EVENT + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";

    public Event() {
    }

    public Event(int id, String event, String timestamp) {
        this.id = id;
        this.event = event;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
