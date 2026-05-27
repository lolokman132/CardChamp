package com.example.cardchamp.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardchamp.R;
import com.example.cardchamp.adapters.TournamentAdapter;
import com.example.cardchamp.database.AppDatabase;
import com.example.cardchamp.database.TournamentDao;
import com.example.cardchamp.models.Tournament;
import com.example.cardchamp.models.TournamentRequest;
import com.example.cardchamp.network.TournamentApiService;
import com.example.cardchamp.notification.MatchCheckReceiver;
import com.example.cardchamp.notification.NotificationHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private TournamentAdapter adapter;
    private TournamentDao tournamentDao;
    private final List<Tournament> masterTournamentList = new ArrayList<>();
    private com.google.android.material.chip.ChipGroup filterChipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view); //Displays tournaments
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Tournament> allTournaments = new ArrayList<>();
        adapter = new TournamentAdapter(allTournaments, tournament -> {
            if (tournament != null && tournament.getId() != null) {
                Intent intent = new Intent(MainActivity.this, TournamentDetailActivity.class);
                intent.putExtra("TOURNAMENT_ID", tournament.getId());
                intent.putExtra("TOURNAMENT_NAME", tournament.getName());
                intent.putExtra("TOURNAMENT_GAME", tournament.getGame());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        AppDatabase db = AppDatabase.getInstance(this);
        tournamentDao = db.tournamentDao();

        filterChipGroup = findViewById(R.id.chip_group_filter);
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> applySelectedFilter());

        //FAB for suggestions
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = findViewById(R.id.fab_suggestions);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SuggestionsActivity.class);
                startActivity(intent);
            });
        }

        TextView header = findViewById(R.id.header);
        if (header != null) {
            header.setOnLongClickListener(v -> {
                NotificationHelper.sendMatchStartNotification(//sends fake Test notification
                        this,
                        "TEST TOURNAMENT",
                        "Round 1: Player A vs Player B"
                );
                return true;
            });
        }

        loadCachedData();
        fetchTournaments();
        //sends notifications when round starts
        NotificationHelper.createNotificationChannel(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        MatchCheckReceiver.MatchCheckScheduler.scheduleAlarm(this);
    }

    private void applySelectedFilter() { //Filters for the chips8
        int checkedId = filterChipGroup.getCheckedChipId();
        List<Tournament> filteredList = new ArrayList<>();

        for (Tournament t : masterTournamentList) {
            if (checkedId == R.id.chip_all || checkedId == android.view.View.NO_ID) {
                filteredList.add(t);
            } else if (checkedId == R.id.chip_mtg && "Magic: The Gathering".equals(t.getGame())) {
                filteredList.add(t);
            } else if (checkedId == R.id.chip_one_piece && "One Piece".equals(t.getGame())) {
                filteredList.add(t);
            } else if (checkedId == R.id.chip_favourites && t.isFavourite()) {
                filteredList.add(t);
            }
        }

        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }

    private void fetchTournaments() {
        OkHttpClient client = new OkHttpClient.Builder()//Fetches API for tournaments
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request().newBuilder()
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
        //Games the API will fetch, followed by format (for magic)
        String[][] games = {
                {"Magic: The Gathering", "Modern"},
                {"One Piece", ""}
        };

        for (String[] gameInfo : games) {
            TournamentRequest request = new TournamentRequest(gameInfo[0], gameInfo[1], 14)//fetches latest 14 tournaments

            service.getTournaments(request).enqueue(new Callback<List<Tournament>>() {
                @Override
                public void onResponse(Call<List<Tournament>> call, Response<List<Tournament>> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        return;
                    }
                    List<Tournament> tournaments = response.body();
                    Executors.newSingleThreadExecutor().execute(() -> {
                        List<String> favouriteIds = tournamentDao.getAllFavouriteIds();
                        Set<String> favSet = new HashSet<>(favouriteIds != null ? favouriteIds : new ArrayList<>());
                        for (Tournament t : tournaments) {
                            if (favSet.contains(t.getId())) {
                                t.setFavourite(true);
                            }
                        }
                        synchronized (masterTournamentList) {
                            for (Tournament t : tournaments) {
                                if (t.getName() != null && t.getName().toLowerCase().contains("test")) continue;//skips test tournaments (since the API does not filter them out)
                                if (loadedTournamentIds.add(t.getId())) {
                                    masterTournamentList.add(t);
                                }
                            }
                            Collections.sort(masterTournamentList, (t1, t2) -> Long.compare(t2.getDate(), t1.getDate()));//sorts tournaments by most recent
                        }
                        runOnUiThread(() -> applySelectedFilter());
                    });
                }

                @Override
                public void onFailure(Call<List<Tournament>> call, Throwable t) {
                }
            });
        }
    }

    private final Set<String> loadedTournamentIds = new HashSet<>();
    //loads cached data from database
    private void loadCachedData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Tournament> cachedList = tournamentDao.getAllTournaments();
            if (cachedList != null && !cachedList.isEmpty()) {
                Collections.sort(cachedList, (t1, t2) -> Long.compare(t2.getDate(), t1.getDate()));
                runOnUiThread(() -> {
                    for (Tournament t : cachedList) {
                        if (t.getName() != null && t.getName().toLowerCase().contains("test")) continue;
                        if (loadedTournamentIds.add(t.getId())) {
                            masterTournamentList.add(t);
                        }
                    }
                    applySelectedFilter();
                });
            }
        });
    }
}
