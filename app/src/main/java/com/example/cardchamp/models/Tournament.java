package com.example.cardchamp.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Entity(tableName = "tournament_table")
public class Tournament {

    @PrimaryKey
    @NonNull
    @SerializedName(value = "TID")
    @ColumnInfo(name = "id")
    private String id = "";

    @SerializedName("tournamentName")
    private String name;

    @SerializedName(value = "startDate", alternate = {"start", "start_date"})
    private long Date;

    @SerializedName(value = "endDate", alternate = {"end", "end_date"})
    private long endDate;

    @SerializedName("game")
    private String game;

    private boolean isFavourite;

    @Ignore
    @SerializedName("rounds")
    private List<Round> rounds;

    public List<Round> getRounds() { return rounds; }
    public void setRounds(List<Round> rounds) { this.rounds = rounds; }

    public Tournament() {
        this.id = "";
    }

    public Tournament(String name, long Date, String game) {
        this.id = "";
        this.name = name;
        this.Date = Date;
        this.game = game;
        this.isFavourite = false;
    }

    @NonNull
    public String getId() { return id != null ? id : ""; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isFavourite() { return isFavourite; }
    public void setFavourite(boolean favourite) { isFavourite = favourite; }

    public long getDate() { return Date; }
    public void setDate(long Date) { this.Date = Date; }

    public long getEndDate() { return endDate; }
    public void setEndDate(long endDate) { this.endDate = endDate; }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(Date * 1000L));
    }

    public String getFormattedEndDate() {
        if (endDate == 0) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(endDate * 1000L));
    }

    public boolean isMockTournament() {
        if (id != null && (id.toLowerCase().startsWith("mock") || id.contains("mock_test"))) return true;
        if (name != null && (name.toLowerCase().contains("debug") || name.toLowerCase().contains("test"))) return true;
        return false;
    }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    @ColumnInfo(name = "cached_rounds")
    private String cachedRoundsJson;

    @ColumnInfo(name = "cached_standings")
    private String cachedStandingsJson;

    public String getCachedRoundsJson() { return cachedRoundsJson; }
    public void setCachedRoundsJson(String json) { this.cachedRoundsJson = json; }
    public String getCachedStandingsJson() { return cachedStandingsJson; }
    public void setCachedStandingsJson(String json) { this.cachedStandingsJson = json; }

    @Ignore
    @SerializedName(value = "standings", alternate = {"players"})
    private List<StandingPlayer> standings;

    public List<StandingPlayer> getStandings() { return standings; }
    public void setStandings(List<StandingPlayer> standings) { this.standings = standings; }

    public static class StandingPlayer {
        @SerializedName("name")
        private String name;

        @SerializedName("decklist")
        private com.google.gson.JsonElement rawDecklistData;

        private transient MatchTable.DeckObject deckObject;

        public String getName() { return name; }

        public MatchTable.DeckObject getDeckObject() {
            if (deckObject != null) return deckObject;

            if (rawDecklistData == null || rawDecklistData.isJsonNull()) {
                return null;
            }

            com.google.gson.Gson gson = new com.google.gson.Gson();

            if (rawDecklistData.isJsonObject()) {
                deckObject = gson.fromJson(rawDecklistData, MatchTable.DeckObject.class);
            } else if (rawDecklistData.isJsonPrimitive() && rawDecklistData.getAsJsonPrimitive().isString()) {
                String stringData = rawDecklistData.getAsString().trim();

                if (stringData.startsWith("{")) {
                    try {
                        deckObject = gson.fromJson(stringData, MatchTable.DeckObject.class);
                    } catch (Exception e) {
                        deckObject = new MatchTable.DeckObject();
                        deckObject.setUrl(stringData);
                    }
                } else {
                    deckObject = new MatchTable.DeckObject();
                    deckObject.setUrl(stringData);
                }
            }

            return deckObject;
        }
    }
}