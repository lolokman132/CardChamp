package com.example.cardchamp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "suggestion_table")
public class Suggestion {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "text")
    private String text;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "count")
    private int count;

    public Suggestion(String text, long timestamp, int count) {
        this.text = text;
        this.timestamp = timestamp;
        this.count = count;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
