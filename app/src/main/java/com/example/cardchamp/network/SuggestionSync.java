package com.example.cardchamp.network;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.cardchamp.models.Suggestion;

import java.lang.reflect.Type;
import java.util.concurrent.Executors;

public class SuggestionSync {
    //Hooks up Suggestions with databasefor suggestions
    private static final String FIREBASE_URL = "https://cardchamp-suggestions-default-rtdb.europe-west1.firebasedatabase.app/suggestions.json";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    public interface SyncCallback {
        void onResult(List<Suggestion> suggestions);
    }

    public static void fetch(SyncCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Request request = new Request.Builder().url(FIREBASE_URL).get().build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    String arrayJson = gson.fromJson(body, String.class);
                    if (arrayJson != null) {
                        Type type = new TypeToken<List<Suggestion>>() {}.getType();
                        List<Suggestion> list = gson.fromJson(arrayJson, type);
                        if (list != null) {
                            callback.onResult(list);
                            return;
                        }
                    }
                }
            } catch (Exception ignored) {}
            callback.onResult(null);
        });
    }

    public static void push(List<Suggestion> suggestions) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String arrayJson = gson.toJson(suggestions);
                String payload = gson.toJson(arrayJson);
                RequestBody body = RequestBody.create(payload, JSON);
                Request request = new Request.Builder().url(FIREBASE_URL).put(body).build();
                client.newCall(request).execute();
            } catch (Exception ignored) {}
        });
    }
}
