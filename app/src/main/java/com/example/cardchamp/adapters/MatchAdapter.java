package com.example.cardchamp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardchamp.R;
import com.example.cardchamp.models.MatchTable;

import java.util.ArrayList;
import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.ViewHolder> {

    private List<MatchTable> matchTables = new ArrayList<>();
    private OnMatchClickListener matchClickListener;

    public interface OnMatchClickListener {
        void onMatchClick(MatchTable table);
    }

    public void setOnMatchClickListener(OnMatchClickListener listener) {
        this.matchClickListener = listener;
    }

    public void setTables(List<MatchTable> tables) {
        this.matchTables = tables != null ? tables : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MatchTable table = matchTables.get(position);
        if (table == null) return;

        holder.tableNumberText.setText("Table " + table.getTableNumber());

        String status = table.getStatus();
        holder.statusText.setText(status != null ? status : "Unknown");

        if ("ongoing".equalsIgnoreCase(status) || "active".equalsIgnoreCase(status)) {
            holder.statusText.setTextColor(Color.parseColor("#FFCC00"));
        } else if ("completed".equalsIgnoreCase(status) || "done".equalsIgnoreCase(status)) {
            holder.statusText.setTextColor(Color.parseColor("#00FF88"));
        } else {
            holder.statusText.setTextColor(Color.parseColor("#888888"));
        }

        StringBuilder playersBuilder = new StringBuilder();
        if (table.getPlayers() != null && !table.getPlayers().isEmpty()) {
            for (int i = 0; i < table.getPlayers().size(); i++) {
                String name = table.getPlayers().get(i).getName();
                playersBuilder.append(name != null ? name.trim() : "Unknown Player");
                if (i < table.getPlayers().size() - 1) {
                    playersBuilder.append("  vs  "); //seperator for each matchup
                }
            }
        } else {
            playersBuilder.append("No pairings assigned yet"); //fallback in case tounaments isn't planned yet
        }
        holder.pairingsText.setText(playersBuilder.toString());

        holder.itemView.setOnClickListener(v -> {
            if (matchClickListener != null) {
                matchClickListener.onMatchClick(table);
            }
        });
    }

    @Override
    public int getItemCount() {
        return matchTables != null ? matchTables.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tableNumberText, statusText, pairingsText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tableNumberText = itemView.findViewById(R.id.text_table_number);
            statusText = itemView.findViewById(R.id.text_match_status);
            pairingsText = itemView.findViewById(R.id.text_player_pairings);
        }
    }
}
