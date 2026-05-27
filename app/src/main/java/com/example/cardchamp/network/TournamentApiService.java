package com.example.cardchamp.network;

import com.example.cardchamp.models.GlobalSuggestion;
import com.example.cardchamp.models.SuggestionRequest;
import com.example.cardchamp.models.SuggestionResponse;
import com.example.cardchamp.models.Tournament;
import com.example.cardchamp.models.TournamentRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TournamentApiService {

    @POST("v2/tournaments")
    Call<List<Tournament>> getTournaments(@Body TournamentRequest body);

    @GET("v2/suggestions")
    Call<List<GlobalSuggestion>> getSuggestions();

    @POST("v2/suggestions")
    Call<SuggestionResponse> submitSuggestion(@Body SuggestionRequest body);

    @DELETE("v2/suggestions")
    Call<SuggestionResponse> deleteSuggestion(@Query("text") String text);
}