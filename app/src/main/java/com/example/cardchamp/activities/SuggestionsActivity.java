package com.example.cardchamp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardchamp.R;
import com.example.cardchamp.database.AppDatabase;
import com.example.cardchamp.models.Suggestion;
import com.example.cardchamp.network.SuggestionSync;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SuggestionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        db = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recycler_suggestions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //FAB for suggestions changes into add suggestion
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_suggestion);
        fabAdd.setOnClickListener(v -> showAddDialog());

        loadLocalSuggestions();
    }
    //Caching suggestion data when internet is down
    private void loadLocalSuggestions() {
        SuggestionSync.fetch(serverList -> {
            if (serverList != null && !serverList.isEmpty()) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    db.suggestionDao().deleteAll();
                    for (Suggestion s : serverList) {
                        db.suggestionDao().insertSuggestion(s);
                    }
                    List<Suggestion> updated = db.suggestionDao().getAllSuggestions();
                    runOnUiThread(() -> showList(updated));
                });
            } else {
                Executors.newSingleThreadExecutor().execute(() -> {
                    List<Suggestion> local = db.suggestionDao().getAllSuggestions();
                    runOnUiThread(() -> showList(local));
                });
            }
        });
    }

    private void showList(List<Suggestion> list) {
        if (list.isEmpty()) {
            showEmpty();
        } else {
            recyclerView.setAdapter(new SuggestionsAdapter(list, text -> confirmDelete(text)));
            recyclerView.setVisibility(RecyclerView.VISIBLE);
        }
    }
    //Fallback for when list is empty
    private void showEmpty() {
        TextView emptyView = findViewById(R.id.text_empty_suggestions);
        emptyView.setVisibility(TextView.VISIBLE);
        recyclerView.setVisibility(RecyclerView.GONE);
    }
    //Dialog to add a suggestion
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Suggest a Game");

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("e.g. Riftbound, Yu-Gi-Oh!...");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(params);
        input.setPadding(48, 24, 48, 24);
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String raw = input.getText().toString().trim();
            if (!raw.isEmpty()) {
                String text = toTitleCase(raw);
                long now = System.currentTimeMillis() / 1000L;
                Executors.newSingleThreadExecutor().execute(() -> {
                    Suggestion existing = db.suggestionDao().findByText(text);
                    if (existing != null) {
                        db.suggestionDao().incrementCount(existing.getId(), now);
                    } else {
                        db.suggestionDao().insertSuggestion(new Suggestion(text, now, 1));
                    }
                    List<Suggestion> all = db.suggestionDao().getAllSuggestions();
                    SuggestionSync.push(all);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Thanks for the suggestion!", Toast.LENGTH_SHORT).show();
                        loadLocalSuggestions();
                    });
                });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private String toTitleCase(String input) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = true;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c) || c == '-' || c == '/') {
                result.append(c);
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    private void confirmDelete(String text) {
        new AlertDialog.Builder(this)
                .setTitle("Remove suggestion?")
                .setMessage("\"" + text + "\"")
                .setPositiveButton("Remove", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.suggestionDao().deleteByText(text);
                        List<Suggestion> all = db.suggestionDao().getAllSuggestions();
                        SuggestionSync.push(all);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Removed", Toast.LENGTH_SHORT).show();
                            loadLocalSuggestions();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private interface OnItemLongPress {
        void onLongPress(String text);
    }

    private static class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {
        private final List<Suggestion> suggestions;
        private final OnItemLongPress listener;
        private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        SuggestionsAdapter(List<Suggestion> suggestions, OnItemLongPress listener) {
            this.suggestions = suggestions;
            this.listener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Suggestion s = suggestions.get(position);
            String display = s.getText();
            if (s.getCount() > 1) display += " (x" + s.getCount() + ")";
            holder.text1.setText(display);
            holder.text2.setText(sdf.format(new Date(s.getTimestamp() * 1000L)));
            holder.itemView.setOnLongClickListener(v -> {
                listener.onLongPress(s.getText());
                return true;
            });
        }

        @Override
        public int getItemCount() { return suggestions.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(android.view.View itemView) { super(itemView); text1 = itemView.findViewById(android.R.id.text1); text2 = itemView.findViewById(android.R.id.text2); }
        }
    }
}
