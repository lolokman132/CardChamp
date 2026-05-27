package com.example.cardchamp.models;

import com.google.gson.annotations.SerializedName;

public class GlobalSuggestion {
    @SerializedName("text")
    private String text;

    @SerializedName("count")
    private int count;

    @SerializedName(value = "timestamp", alternate = {"date", "lastUpdated", "updatedAt"})
    private long timestamp;

    public String getText() { return text; }
    public int getCount() { return count; }
    public long getTimestamp() { return timestamp; }
}
