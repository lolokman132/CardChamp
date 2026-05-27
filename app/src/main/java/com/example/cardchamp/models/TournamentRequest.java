package com.example.cardchamp.models;

public class TournamentRequest {
    private String game;
    private String format;
    private Integer last;

    @com.google.gson.annotations.SerializedName("TID")
    private String tid;

    @com.google.gson.annotations.SerializedName("rounds")
    private Boolean includeRounds;

    @com.google.gson.annotations.SerializedName("standings")
    private Boolean includeStandings;

    @com.google.gson.annotations.SerializedName("columns")
    private java.util.List<String> columns;

    @com.google.gson.annotations.SerializedName("players")
    private java.util.List<String> players;

    public void setColumns(java.util.List<String> columns) {
        this.columns = columns;
    }

    public void setPlayers(java.util.List<String> players) {
        this.players = players;
    }

    public TournamentRequest() {}

    // Constructor for fetching a specific tournament's bracket
    public TournamentRequest(String tid) {
        this.tid = tid;
        this.includeRounds = true;
        this.includeStandings = true;
    }

    public TournamentRequest(String game, String format, int last) {
        this.game = game;
        this.format = format;
        this.last = last;
    }
    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public int getLast() { return last != null ? last : 0; }
    public void setLast(int last) { this.last = last; }
}