package com.example.cardchamp.database;

import androidx.room.TypeConverter;

import com.example.cardchamp.models.MatchTable;
import com.example.cardchamp.models.Round;
import com.example.cardchamp.models.Tournament;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Converters {

    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromStandingPlayerList(List<Tournament.StandingPlayer> standings) {
        if (standings == null) return null;
        Type type = new TypeToken<List<Tournament.StandingPlayer>>() {}.getType();
        return gson.toJson(standings, type);
    }

    @TypeConverter
    public static List<Tournament.StandingPlayer> toStandingPlayerList(String standingsString) {
        if (standingsString == null) return null;
        Type type = new TypeToken<List<Tournament.StandingPlayer>>() {}.getType();
        return gson.fromJson(standingsString, type);
    }

    @TypeConverter
    public static String fromRoundList(List<Round> rounds) {
        if (rounds == null) return null;
        Type type = new TypeToken<List<Round>>() {}.getType();
        return gson.toJson(rounds, type);
    }

    @TypeConverter
    public static List<Round> toRoundList(String roundsString) {
        if (roundsString == null) return null;
        Type type = new TypeToken<List<Round>>() {}.getType();
        return gson.fromJson(roundsString, type);
    }

    @TypeConverter
    public static String fromMatchTableList(List<MatchTable> tables) {
        if (tables == null) return null;
        Type type = new TypeToken<List<MatchTable>>() {}.getType();
        return gson.toJson(tables, type);
    }

    @TypeConverter
    public static List<MatchTable> toMatchTableList(String tablesString) {
        if (tablesString == null) return null;
        Type type = new TypeToken<List<MatchTable>>() {}.getType();
        return gson.fromJson(tablesString, type);
    }

    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) return null;
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.toJson(list, type);
    }

    @TypeConverter
    public static List<String> toStringList(String listString) {
        if (listString == null) return null;
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(listString, type);
    }
}
