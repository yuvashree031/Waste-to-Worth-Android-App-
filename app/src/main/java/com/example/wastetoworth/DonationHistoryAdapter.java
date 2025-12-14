package com.example.wastetoworth;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying donation history items
 */
public class DonationHistoryAdapter extends RecyclerView.Adapter<DonationHistoryAdapter.ViewHolder> {
    
    private List<DonationHistoryActivity.DonationItem> donationList;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    
    public DonationHistoryAdapter(List<DonationHistoryActivity.DonationItem> donationList) {
        this.donationList = donationList;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_donation_history, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonationHistoryActivity.DonationItem donation = donationList.get(position);
        
        // Set item name
        holder.textFoodItem.setText(donation.getItemName());
        
        // Set quantity
        String quantity = donation.getQuantity();
        if (quantity != null && !quantity.isEmpty()) {
            holder.textQuantity.setText("Quantity: " + quantity);
        } else {
            holder.textQuantity.setText("Quantity: Not specified");
        }
        
        // Set location with improved formatting
        String location = donation.getLocation();
        if (location != null && !location.isEmpty()) {
            // Check if location is in "lat,lng" format
            if (location.matches("^-?\\d+\\.\\d+,-?\\d+\\.\\d+$")) {
                // Shorten coordinate display
                String[] parts = location.split(",");
                if (parts.length == 2) {
                    String lat = parts[0].length() > 6 ? parts[0].substring(0, 6) : parts[0];
                    String lng = parts[1].length() > 6 ? parts[1].substring(0, 6) : parts[1];
                    holder.textLocation.setText("Location: " + lat + "," + lng);
                } else {
                    holder.textLocation.setText("Location: " + location);
                }
            } else {
                holder.textLocation.setText("Location: " + location);
            }
        } else {
            holder.textLocation.setText("Location: Not specified");
        }
        
        // Set status with color coding
        String status = donation.getStatus();
        holder.textStatus.setText("Status: " + status);
        if ("Claimed".equals(status)) {
            holder.textStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.textStatus.setTextColor(Color.parseColor("#FF5722")); // Red
        }
        
        // Set timestamp
        if (donation.getTimestamp() != null) {
            holder.textDate.setText("Date: " + dateFormat.format(donation.getTimestamp()));
        } else {
            holder.textDate.setText("Date: Not specified");
        }
        
        // Set phone number
        String phone = donation.getPhone();
        if (phone != null && !phone.isEmpty()) {
            holder.btnCall.setVisibility(View.VISIBLE);
            holder.btnCall.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Unable to make call", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.btnCall.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return donationList.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textFoodItem, textQuantity, textStatus, textLocation, textDate;
        Button btnCall, btnViewMap;
        
        ViewHolder(View itemView) {
            super(itemView);
            textFoodItem = itemView.findViewById(R.id.textFoodItem);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textStatus = itemView.findViewById(R.id.textStatus);
            textLocation = itemView.findViewById(R.id.textLocation);
            textDate = itemView.findViewById(R.id.textDate);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnViewMap = itemView.findViewById(R.id.btnViewMap);
        }
    }
}