package com.example.cardchamp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class MatchTable {
    @SerializedName("table")
    private int tableNumber;

    @SerializedName("status")
    private String status;

    @SerializedName("winner")
    private String winnerName;

    @SerializedName("players")
    private List<TournamentPlayer> players;

    public int getTableNumber() {
        return tableNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public List<TournamentPlayer> getPlayers() {
        return players;
    }

    public static class TournamentPlayer {
        @SerializedName("name")
        private String name;

        @SerializedName("decklist")
        private com.google.gson.JsonElement rawDecklistData;

        private transient DeckObject deckObject;

        public String getName() {
            return name;
        }

        public DeckObject getDeckObject() {
            if (deckObject != null) return deckObject;
            if (rawDecklistData == null || rawDecklistData.isJsonNull()) return null;

            com.google.gson.Gson gson = new com.google.gson.Gson();

            if (rawDecklistData.isJsonObject()) {
                deckObject = gson.fromJson(rawDecklistData, DeckObject.class);
            } else if (rawDecklistData.isJsonPrimitive() && rawDecklistData.getAsJsonPrimitive().isString()) {
                String stringData = rawDecklistData.getAsString().trim();
                deckObject = new DeckObject();

                if (stringData.startsWith("{")) {
                    try {
                        deckObject = gson.fromJson(stringData, DeckObject.class);
                    } catch (Exception e) {
                        deckObject.setRawList(stringData);
                    }
                } else if (stringData.startsWith("http")) {
                    deckObject.setUrl(stringData);
                } else {
                    deckObject.setRawList(stringData);
                }
            }
            return deckObject;
        }
    }

    public static class DeckObject implements Serializable {
        @SerializedName("archetype")
        private String archetype;

        @SerializedName("url")
        private String url;

        @SerializedName("raw_list")
        private String rawList;

        public String getArchetype() {
            return archetype != null ? archetype : "Unknown Archetype";
        }

        public String getUrl() {
            return url;
        }

        public String getRawList() {
            return rawList;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setRawList(String rawList) {
            this.rawList = rawList;
        }
    }
}