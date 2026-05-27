package com.example.cardchamp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.cardchamp.models.Suggestion;

import java.util.List;

@Dao
public interface SuggestionDao {
    @Query("SELECT * FROM suggestion_table ORDER BY timestamp DESC")
    List<Suggestion> getAllSuggestions();

    @Query("SELECT * FROM suggestion_table WHERE LOWER(text) = LOWER(:text) LIMIT 1")
    Suggestion findByText(String text);

    @Query("UPDATE suggestion_table SET count = count + 1, timestamp = :timestamp WHERE id = :id")
    void incrementCount(int id, long timestamp);//displays number of times tcg was submitted

    @Insert
    void insertSuggestion(Suggestion suggestion);

    @Query("DELETE FROM suggestion_table WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM suggestion_table WHERE LOWER(text) = LOWER(:text)")
    void deleteByText(String text);

    @Query("DELETE FROM suggestion_table")
    void deleteAll();
}
