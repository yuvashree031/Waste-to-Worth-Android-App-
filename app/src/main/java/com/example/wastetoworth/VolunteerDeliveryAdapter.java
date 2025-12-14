package com.example.wastetoworth;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class VolunteerDeliveryAdapter extends RecyclerView.Adapter<VolunteerDeliveryAdapter.DeliveryViewHolder> {
    private Context context;
    private List<DonationModel> donations;
    private OnDeliveryActionListener listener;
    public interface OnDeliveryActionListener {
        void onAcceptDelivery(DonationModel donation);
        void onViewLocation(DonationModel donation);
    }
    public VolunteerDeliveryAdapter(Context context, List<DonationModel> donations, OnDeliveryActionListener listener) {
        this.context = context;
        this.donations = donations;
        this.listener = listener;
    }
    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_volunteer_delivery, parent, false);
        return new DeliveryViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        DonationModel donation = donations.get(position);
        // Set donation details
        holder.tvFoodName.setText(donation.getFoodName() != null ? donation.getFoodName() : "Food Item");
        holder.tvFoodType.setText(donation.getType() != null ? donation.getType() : "Food");
        holder.tvDescription.setText(donation.getDescription() != null ? donation.getDescription() : "No description");
        holder.tvQuantity.setText("Quantity: " + (donation.getQuantity() != null ? donation.getQuantity() : "Not specified"));
        // Set donor information
        if (donation.getDonorName() != null && !donation.getDonorName().isEmpty()) {
            holder.tvDonorName.setText("Donor: " + donation.getDonorName());
        } else {
            holder.tvDonorName.setText("Donor: Anonymous");
        }
        // Set location/address
        if (donation.getAddress() != null && !donation.getAddress().isEmpty()) {
            holder.tvLocation.setText("📍 " + donation.getAddress());
        } else {
            holder.tvLocation.setText("📍 Location available on map");
        }
        // Set timestamp
        if (donation.getTimestamp() != null) {
            String timeAgo = getTimeAgo(donation.getTimestamp().toDate());
            holder.tvTimestamp.setText("⏰ " + timeAgo);
        } else {
            holder.tvTimestamp.setText("⏰ Recently posted");
        }
        // Set phone number if available
        if (donation.getPhone() != null && !donation.getPhone().isEmpty()) {
            holder.tvPhone.setText("📞 " + donation.getPhone());
            holder.tvPhone.setVisibility(View.VISIBLE);
        } else {
            holder.tvPhone.setVisibility(View.GONE);
        }
        // Set click listeners
        holder.btnAcceptDelivery.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptDelivery(donation);
            }
        });
        holder.btnViewLocation.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewLocation(donation);
            }
        });
    }
    @Override
    public int getItemCount() {
        return donations.size();
    }
    private String getTimeAgo(Date timestamp) {
        long now = System.currentTimeMillis();
        long time = timestamp.getTime();
        long diff = now - time;
        if (diff < 60000) { // Less than 1 minute
            return "Just now";
        } else if (diff < 3600000) { // Less than 1 hour
            return (diff / 60000) + " minutes ago";
        } else if (diff < 86400000) { // Less than 1 day
            return (diff / 3600000) + " hours ago";
        } else { // More than 1 day
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(timestamp);
        }
    }
    public static class DeliveryViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvFoodType, tvDescription, tvQuantity;
        TextView tvDonorName, tvLocation, tvTimestamp, tvPhone;
        Button btnAcceptDelivery, btnViewLocation;
        public DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodType = itemView.findViewById(R.id.tvFoodType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvDonorName = itemView.findViewById(R.id.tvDonorName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnAcceptDelivery = itemView.findViewById(R.id.btnAcceptDelivery);
            btnViewLocation = itemView.findViewById(R.id.btnViewLocation);
        }
    }
}