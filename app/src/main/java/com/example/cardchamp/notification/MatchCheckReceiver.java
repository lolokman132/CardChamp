package com.example.cardchamp.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.cardchamp.database.AppDatabase;
import com.example.cardchamp.models.MatchTable;
import com.example.cardchamp.models.Round;
import com.example.cardchamp.models.Tournament;
import com.example.cardchamp.models.TournamentRequest;
import com.example.cardchamp.network.TournamentApiService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MatchCheckReceiver extends BroadcastReceiver {
    private static final Set<String> seenOngoingMatches = new HashSet<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.createNotificationChannel(context);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            List<String> favouriteIds = db.tournamentDao().getAllFavouriteIds();
            if (favouriteIds == null || favouriteIds.isEmpty()) {
                MatchCheckScheduler.cancelAlarm(context);
                return;
            }

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

            for (String tid : favouriteIds) {
                TournamentRequest detailRequest = new TournamentRequest(tid);
                detailRequest.setColumns(Arrays.asList("name", "rounds"));

                service.getTournaments(detailRequest).enqueue(new Callback<List<Tournament>>() {
                    @Override
                    public void onResponse(Call<List<Tournament>> call, Response<List<Tournament>> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) return;
                        Tournament t = response.body().get(0);
                        if (t.getRounds() == null) return;
                        for (Round round : t.getRounds()) {
                            if (round.getTables() == null) continue;
                            for (MatchTable table : round.getTables()) {
                                if ("ongoing".equalsIgnoreCase(table.getStatus()) || "active".equalsIgnoreCase(table.getStatus())) {
                                    String matchKey = tid + "_" + round.getRoundNumber() + "_" + table.getTableNumber();
                                    if (!seenOngoingMatches.contains(matchKey)) {
                                        seenOngoingMatches.add(matchKey);
                                        String players = buildPlayersString(table);
                                    String roundLabel;
                                    try {
                                        roundLabel = "Round " + Integer.parseInt(round.getRoundNumber());
                                    } catch (NumberFormatException e) {
                                        roundLabel = round.getRoundNumber();
                                    }
                                    NotificationHelper.sendMatchStartNotification(
                                            context, t.getName(),
                                            roundLabel + ": " + players
                                    );
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Tournament>> call, Throwable t) {}
                });
            }
        }).start();
    }

    private String buildPlayersString(MatchTable table) {
        if (table.getPlayers() == null || table.getPlayers().isEmpty()) return "Match starting";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < table.getPlayers().size(); i++) {
            sb.append(table.getPlayers().get(i).getName());
            if (i < table.getPlayers().size() - 1) sb.append(" vs ");
        }
        return sb.toString();
    }

    public static class MatchCheckScheduler {
        private static final long INTERVAL_MS = 60_000L;

        public static void scheduleAlarm(Context context) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, MatchCheckReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    android.os.SystemClock.elapsedRealtime() + INTERVAL_MS,
                    INTERVAL_MS,
                    pendingIntent
            );
        }

        public static void cancelAlarm(Context context) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, MatchCheckReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntent);
        }
    }
}
