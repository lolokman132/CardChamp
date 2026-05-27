package com.example.cardchamp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardchamp.R;
import com.example.cardchamp.database.AppDatabase;
import com.example.cardchamp.models.Tournament;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class TournamentAdapter extends RecyclerView.Adapter<TournamentAdapter.ViewHolder> {

    private List<Tournament> tournamentList;
    private OnTournamentClickListener clickListener;
    //Date formats. First is for Start Date only and End Date, second is used for Start Date if there's an End Date
    private final SimpleDateFormat fullDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

    public interface OnTournamentClickListener {
        void onTournamentClick(Tournament tournament);
    }

    public TournamentAdapter(List<Tournament> tournamentList, OnTournamentClickListener clickListener) {
        this.tournamentList = tournamentList != null ? tournamentList : new ArrayList<>();
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tournament, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tournament tournament = tournamentList.get(position);
        if (tournament == null) return;

        holder.nameText.setText(tournament.getName() != null ? tournament.getName() : "Unnamed Tournament");

        long startMillis = tournament.getDate() * 1000L;
        long endDate = tournament.getEndDate();
        String dateStr;
        if (endDate != 0) {
            dateStr = monthDayFormat.format(new Date(startMillis))
                    + " - " + fullDateFormat.format(new Date(endDate * 1000L));
        } else {
            dateStr = fullDateFormat.format(new Date(startMillis));
        }
        holder.dateText.setText(dateStr);

        String game = tournament.getGame();
        //Icons for each game type
        if (game != null) {
            if (game.toLowerCase().contains("magic")) {
                holder.gameIcon.setImageResource(R.drawable.ic_mtg);
            } else if (game.toLowerCase().contains("one piece") || game.toLowerCase().contains("onepiece")) {
                holder.gameIcon.setImageResource(R.drawable.ic_one_piece);
            }
        }

        updateFavoriteIcon(holder.favouriteBtn, tournament.isFavourite());
        //Sets a tournament as being favourited when the button is pressed
        holder.favouriteBtn.setOnClickListener(v -> {
            boolean currentFavState = tournament.isFavourite();
            boolean newFavState = !currentFavState;

            tournament.setFavourite(newFavState);
            updateFavoriteIcon(holder.favouriteBtn, newFavState);

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(v.getContext());
                if (newFavState) {
                    db.tournamentDao().insertTournament(tournament);
                } else {
                    db.tournamentDao().deleteTournament(tournament);
                }
            });
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTournamentClick(tournament);
            }
        });
    }

    private void updateFavoriteIcon(ImageButton btn, boolean isFav) {
        if (isFav) {
            //Fills in the heart and pink to show the user that the tournament is favourited
            btn.setImageResource(R.drawable.ic_heart_filled);
            btn.setColorFilter(Color.parseColor("#FF69B4"));
        } else {
            //Empties the heart and makes it grey for when the tournament is unfavourited
            btn.setImageResource(R.drawable.ic_heart_border);
            btn.setColorFilter(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return tournamentList != null ? tournamentList.size() : 0;
    }

    public void updateData(List<Tournament> newTournaments) {
        this.tournamentList.clear();
        if (newTournaments != null) {
            this.tournamentList.addAll(newTournaments);
        }
        notifyDataSetChanged();
    }

    public void updateList(List<Tournament> newList) {
        this.tournamentList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView dateText;
        ImageButton favouriteBtn;
        ImageView gameIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_tournament_name);
            dateText = itemView.findViewById(R.id.text_date);
            favouriteBtn = itemView.findViewById(R.id.btn_favorite);
            gameIcon = itemView.findViewById(R.id.img_game_icon);
        }
    }
}
