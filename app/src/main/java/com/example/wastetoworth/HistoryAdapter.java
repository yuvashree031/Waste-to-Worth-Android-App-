package com.example.wastetoworth;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private static final String TAG = "HistoryAdapter";
    
    private List<HistoryItem> historyItems;
    private final OnHistoryItemClickListener listener;
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateFormat;
    
    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(HistoryItem item);
    }
    public HistoryAdapter(@Nullable List<HistoryItem> historyItems, @Nullable OnHistoryItemClickListener listener) {
        this.historyItems = historyItems != null ? historyItems : new ArrayList<>();
        this.listener = listener;
        this.timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    }
    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        if (position < 0 || position >= historyItems.size()) {
            Log.w(TAG, "Invalid position: " + position);
            return;
        }

        HistoryItem item = historyItems.get(position);
        if (item == null) {
            Log.w(TAG, "Item at position " + position + " is null");
            return;
        }

        // Set title and description with null checks
        holder.historyTitle.setText(item.getTitle() != null ? item.getTitle() : "");
        holder.historyDescription.setText(item.getDescription() != null ? item.getDescription() : "");
        
        // Format and set date and time
        try {
            Date timestamp = item.getTimestamp();
            if (timestamp != null) {
                holder.historyDate.setText(dateFormat.format(timestamp));
                holder.historyTime.setText(timeFormat.format(timestamp));
            } else {
                holder.historyDate.setText("");
                holder.historyTime.setText("");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date", e);
            holder.historyDate.setText("");
            holder.historyTime.setText("");
        }
        
        // Set status icon based on status
        int statusIconResId = getStatusIcon(item.getStatus());
        if (statusIconResId != 0) {
            holder.historyIcon.setImageResource(statusIconResId);
            holder.historyIcon.setVisibility(View.VISIBLE);
        } else {
            holder.historyIcon.setVisibility(View.GONE);
        }
        
        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHistoryItemClick(item);
            }
        });
    }
    private int getStatusIcon(@Nullable String status) {
        if (status == null || status.isEmpty()) {
            return 0;
        }
        
        switch (status.toLowerCase(Locale.ROOT)) {
            case "completed":
                return R.drawable.ic_check_circle;
            case "in_progress":
            case "in progress":
                return R.drawable.ic_in_progress;
            case "failed":
            case "error":
                return R.drawable.ic_error;
            default:
                Log.d(TAG, "Unknown status: " + status);
                return 0;
        }
    }
    @Override
    public int getItemCount() {
        return historyItems != null ? historyItems.size() : 0;
    }
    public void updateData(@Nullable List<HistoryItem> newItems) {
        if (historyItems == null) {
            historyItems = new ArrayList<>();
        } else {
            historyItems.clear();
        }
        
        if (newItems != null && !newItems.isEmpty()) {
            historyItems.addAll(newItems);
        }
        
        try {
            notifyDataSetChanged();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error notifying data set changed", e);
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    
    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < historyItems.size() && historyItems.get(position) != null) {
            String id = historyItems.get(position).getId();
            return id != null ? id.hashCode() : super.getItemId(position);
        }
        return super.getItemId(position);
    }
    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView historyIcon;
        TextView historyTitle, historyDescription, historyTime, historyDate;
        
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            historyIcon = itemView.findViewById(R.id.history_icon);
            historyTitle = itemView.findViewById(R.id.history_title);
            historyDescription = itemView.findViewById(R.id.history_description);
            historyTime = itemView.findViewById(R.id.history_time);
            historyDate = itemView.findViewById(R.id.history_date);
        }
    }
}
