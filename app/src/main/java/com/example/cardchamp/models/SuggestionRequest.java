package com.example.cardchamp.models;

import com.google.gson.annotations.SerializedName;

public class SuggestionRequest {
    @SerializedName("suggestion")
    private String suggestion;

    public SuggestionRequest(String suggestion) {
        this.suggestion = suggestion;
    }
}
