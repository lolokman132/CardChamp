package com.example.cardchamp.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Round {
    @SerializedName(value = "round", alternate = {"roundNumber"})
    private String roundNumber;

    @SerializedName("tables")
    private List<MatchTable> tables;

    public String getRoundNumber() {
        return roundNumber;
    }

    public List<MatchTable> getTables() {
        return tables;
    }
}
