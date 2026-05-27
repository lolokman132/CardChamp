package com.example.cardchamp.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cardchamp.R;
import com.example.cardchamp.models.MatchTable;

public class DecklistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decklist);

        TextView winnerTitle = findViewById(R.id.text_winner_title);
        TextView winnerContent = findViewById(R.id.text_winner_decklist);
        TextView loserTitle = findViewById(R.id.text_loser_title);
        TextView loserContent = findViewById(R.id.text_loser_decklist);

        winnerContent.setTypeface(Typeface.MONOSPACE);
        loserContent.setTypeface(Typeface.MONOSPACE);

        if (getIntent() != null) {
            String winnerName = getIntent().getStringExtra("WINNER_NAME");
            String loserName = getIntent().getStringExtra("LOSER_NAME");

            MatchTable.DeckObject winnerDeck = (MatchTable.DeckObject) getIntent().getSerializableExtra("WINNER_DECK_OBJ");
            MatchTable.DeckObject loserDeck = (MatchTable.DeckObject) getIntent().getSerializableExtra("LOSER_DECK_OBJ");

            if (winnerDeck != null && winnerDeck.getRawList() != null && !winnerDeck.getRawList().trim().isEmpty()) {
                winnerTitle.setText("Winner: " + winnerName);
                winnerContent.setText(formatRawList(winnerDeck.getRawList()));
            } else {
                winnerTitle.setText("Winner: " + (winnerName != null ? winnerName : "Unknown"));
                winnerContent.setText("This player did not submit a decklist to TopDeck.gg.");
            }

            if (loserDeck != null && loserDeck.getRawList() != null && !loserDeck.getRawList().trim().isEmpty()) {
                loserTitle.setText("Loser: " + loserName);
                loserContent.setText(formatRawList(loserDeck.getRawList()));
            } else {
                loserTitle.setText("Loser: " + (loserName != null ? loserName : "Unknown"));
                loserContent.setText("This player did not submit a decklist to TopDeck.gg.");
            }
        }
    }

    //Used to format the raw list given by the API
    private String formatRawList(String raw) {
        String s = raw
                .replace("\\r\\n", "\n")
                .replace("\\r", "\n")
                .replace("\\n", "\n")
                .replace("\\t", " ");
        if (s.contains("~~Leader~~")) {
            s = s
                .replace("~~Leader~~", "─── Leader ───")
                .replace("~~leader~~", "─── Leader ───")
                .replace("~~Decklist~~", "─── Decklist ───")
                .replace("~~decklist~~", "─── Decklist ───");
        } else {
            s = s
                .replace("~~Mainboard~~", "─── Mainboard ───")
                .replace("~~Sideboard~~", "─── Sideboard ───")
                .replace("~~sideboard~~", "─── Sideboard ───");
        }
        return s.trim();
    }
}
