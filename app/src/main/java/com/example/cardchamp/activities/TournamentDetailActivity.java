package com.example.cardchamp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardchamp.R;
import com.example.cardchamp.adapters.MatchAdapter;
import com.example.cardchamp.database.AppDatabase;
import com.example.cardchamp.models.MatchTable;
import com.example.cardchamp.models.Round;
import com.example.cardchamp.models.Tournament;
import com.example.cardchamp.models.TournamentRequest;
import com.example.cardchamp.network.TournamentApiService;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TournamentDetailActivity extends AppCompatActivity {

    private String tournamentId;
    private TabLayout tabLayout;
    private MatchAdapter matchAdapter;
    private Tournament fullTournamentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament_detail);

        TextView titleTextView = findViewById(R.id.detail_tournament_name);
        tabLayout = findViewById(R.id.tab_layout_rounds);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_matches);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        matchAdapter = new MatchAdapter();
        recyclerView.setAdapter(matchAdapter);

        matchAdapter.setOnMatchClickListener(table -> {
            if (table.getPlayers() == null || table.getPlayers().size() < 2) {
                Toast.makeText(this, "Incomplete pairing data.", Toast.LENGTH_SHORT).show();
                return;
            }

            String winnerName = table.getWinnerName();
            String player1Name = table.getPlayers().get(0).getName();
            String player2Name = table.getPlayers().get(1).getName();

            String actualWinnerName;
            String actualLoserName;
            if (winnerName != null && !winnerName.isEmpty()) {
                actualWinnerName = winnerName;
                actualLoserName = winnerName.equalsIgnoreCase(player1Name) ? player2Name : player1Name;
            } else {
                actualWinnerName = player1Name;
                actualLoserName = player2Name;
            }

            MatchTable.DeckObject winnerDeck = null;
            MatchTable.DeckObject loserDeck = null;
            for (MatchTable.TournamentPlayer p : table.getPlayers()) {
                if (p.getName().equalsIgnoreCase(actualWinnerName)) {
                    winnerDeck = p.getDeckObject();
                } else {
                    loserDeck = p.getDeckObject();
                }
            }
            if (winnerDeck == null) winnerDeck = findDeckObjectForPlayer(actualWinnerName);
            if (loserDeck == null) loserDeck = findDeckObjectForPlayer(actualLoserName);

            Intent intent = new Intent(TournamentDetailActivity.this, DecklistActivity.class);
            intent.putExtra("WINNER_NAME", actualWinnerName);
            intent.putExtra("LOSER_NAME", actualLoserName);
            intent.putExtra("WINNER_DECK_OBJ", winnerDeck);
            intent.putExtra("LOSER_DECK_OBJ", loserDeck);
            startActivity(intent);
        });

        if (getIntent() != null) {
            tournamentId = getIntent().getStringExtra("TOURNAMENT_ID");
            String name = getIntent().getStringExtra("TOURNAMENT_NAME");
            String game = getIntent().getStringExtra("TOURNAMENT_GAME");
            TextView infoView = findViewById(R.id.detail_tournament_info);
            if (game != null) infoView.setText(game);
            if (name != null) titleTextView.setText(name);
        }

        fetchBracketData();
    }

    private void fetchBracketData() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "715a77bb-a0f1-4151-9f49-157e14270ee9")
                            .addHeader("Content-Type", "application/json")
                            .build();
                    return chain.proceed(request);
                }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://topdeck.gg/api/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TournamentApiService service = retrofit.create(TournamentApiService.class);
        TournamentRequest detailRequest = new TournamentRequest(tournamentId);
        //Uses API to fetch decklists
        detailRequest.setColumns(Arrays.asList("name", "decklist", "deckObj"));
        detailRequest.setPlayers(Arrays.asList("name", "decklist"));

        service.getTournaments(detailRequest).enqueue(new Callback<List<Tournament>>() {
            @Override
            public void onResponse(Call<List<Tournament>> call, Response<List<Tournament>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    fullTournamentData = response.body().get(0);
                    setupRoundsUI(fullTournamentData);
                    cacheTournamentDetail(fullTournamentData);
                } else {
                    handleUnsupportedFormat();
                }
            }

            @Override
            public void onFailure(Call<List<Tournament>> call, Throwable t) {
                loadCachedTournament();
            }
        });
    }

    private void setupRoundsUI(Tournament tournament) {
        if (tournament == null) {
            handleUnsupportedFormat();
            return;
        }

        TextView nameHeader = findViewById(R.id.detail_tournament_name);
        if (nameHeader != null) nameHeader.setText(tournament.getName());

        List<Round> tournamentRounds = tournament.getRounds();
        if (tournamentRounds == null || tournamentRounds.isEmpty() || tournamentRounds.get(0).getTables() == null) {
            handleUnsupportedFormat();
            return;
        }

        List<Round> swiss = new ArrayList<>();
        List<Round> bracket = new ArrayList<>();
        Map<String, Integer> bracketOrder = new HashMap<>();
        bracketOrder.put("Top 8", 1);
        bracketOrder.put("Top 4", 2);
        bracketOrder.put("Finals", 3);

        for (Round round : tournamentRounds) {
            try {
                Integer.parseInt(round.getRoundNumber());
                swiss.add(round);
            } catch (NumberFormatException e) {
                bracket.add(round);
            }
        }
        bracket.sort((a, b) -> {
            int ra = bracketOrder.getOrDefault(a.getRoundNumber(), 99);
            int rb = bracketOrder.getOrDefault(b.getRoundNumber(), 99);
            return Integer.compare(ra, rb);
        });

        List<Round> sorted = new ArrayList<>();
        sorted.addAll(swiss);
        sorted.addAll(bracket);

        tabLayout.removeAllTabs();
        for (Round round : sorted) {
            String label;
            try {
                int num = Integer.parseInt(round.getRoundNumber());
                label = "Round " + num;
            } catch (NumberFormatException e) {
                label = round.getRoundNumber();
            }
            tabLayout.addTab(tabLayout.newTab().setText(label));
        }

        matchAdapter.setTables(sorted.get(0).getTables());
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position < sorted.size()) {
                    matchAdapter.setTables(sorted.get(position).getTables());
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void cacheTournamentDetail(Tournament data) {
        if (data == null || data.getRounds() == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            Gson gson = new Gson();
            String roundsJson = gson.toJson(data.getRounds());
            String standingsJson = gson.toJson(data.getStandings());
            AppDatabase.getInstance(TournamentDetailActivity.this)
                .tournamentDao()
                .cacheTournamentDetail(data.getId(), roundsJson, standingsJson);
        });
    }

    private void loadCachedTournament() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Tournament cached = AppDatabase.getInstance(TournamentDetailActivity.this)
                .tournamentDao().getTournamentById(tournamentId);
            if (cached != null && cached.getCachedRoundsJson() != null) {
                Gson gson = new Gson();
                Type roundType = new TypeToken<List<Round>>() {}.getType();
                List<Round> rounds = gson.fromJson(cached.getCachedRoundsJson(), roundType);
                if (rounds != null && !rounds.isEmpty()) {
                    cached.setRounds(rounds);
                    if (cached.getCachedStandingsJson() != null) {
                        Type standingType = new TypeToken<List<Tournament.StandingPlayer>>() {}.getType();
                        cached.setStandings(gson.fromJson(cached.getCachedStandingsJson(), standingType));
                    }
                    Tournament cachedData = cached;
                    runOnUiThread(() -> {
                        fullTournamentData = cachedData;
                        setupRoundsUI(fullTournamentData);
                    });
                    return;
                }
            }
            runOnUiThread(() -> handleUnsupportedFormat());
        });
    }

    private void handleUnsupportedFormat() {
        Toast.makeText(TournamentDetailActivity.this, "Format not supported.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private MatchTable.DeckObject findDeckObjectForPlayer(String playerName) {
        if (fullTournamentData == null || fullTournamentData.getStandings() == null || playerName == null) {
            return null;
        }

        String cleanTarget = playerName.trim();
        for (Tournament.StandingPlayer standing : fullTournamentData.getStandings()) {
            if (standing.getName() != null) {
                if (cleanTarget.equalsIgnoreCase(standing.getName().trim())) {
                    return standing.getDeckObject();
                }
            }
        }
        return null;
    }
}
