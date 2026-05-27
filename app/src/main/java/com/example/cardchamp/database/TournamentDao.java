package com.example.cardchamp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.cardchamp.models.Tournament;
import java.util.List;

@Dao
public interface TournamentDao {
    @Query("SELECT id FROM tournament_table")
    List<String> getAllFavouriteIds();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTournament(Tournament tournament);

    @Delete
    void deleteTournament(Tournament tournament);
    @Query("SELECT * FROM tournament_table")
    List<Tournament> getAllTournaments();

    @Query("UPDATE tournament_table SET cached_rounds = :roundsJson, cached_standings = :standingsJson WHERE id = :id")
    void cacheTournamentDetail(String id, String roundsJson, String standingsJson);

    @Query("SELECT * FROM tournament_table WHERE id = :id LIMIT 1")
    Tournament getTournamentById(String id);
}