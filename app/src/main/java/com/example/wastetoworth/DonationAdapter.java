package com.example.wastetoworth;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Locale;

// RecyclerView Adapter for displaying donation items
public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.DonationViewHolder> {

    public interface OnReceiveClickListener {
        void onReceiveClick(DonationModel donation);
    }

    public interface OnItemClickListener {
        void onItemClick(DonationModel donation);
    }

    private OnReceiveClickListener receiveClickListener;
    private final Context context;
    private final List<DonationModel> donationList;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final OnItemClickListener listener;

    public DonationAdapter(Context context, List<DonationModel> donationList, OnItemClickListener listener) {
        this.context = context;
        this.donationList = donationList;
        this.listener = listener;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void setOnReceiveClickListener(OnReceiveClickListener listener) {
        this.receiveClickListener = listener;
    }

    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donation, parent, false);
        return new DonationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationViewHolder holder, int position) {
        if (donationList == null || position < 0 || position >= donationList.size()) {
            return;
        }
        DonationModel donation = donationList.get(position);
        if (donation == null) {
            return;
        }

        // Title and type
        holder.tvFoodName.setText(donation.getFoodName() != null ? donation.getFoodName() : (donation.getName() != null ? donation.getName() : "Donation"));
        holder.tvFoodType.setText(donation.getType() != null ? donation.getType() : "");

        // Description and quantity
        holder.tvDescription.setText(donation.getDescription() != null ? donation.getDescription() : "");
        holder.tvQuantity.setText(donation.getQuantity() != null ? ("Quantity: " + donation.getQuantity()) : "Quantity: Not specified");

        // Donor and category
        holder.tvDonorName.setText("By: " + (donation.getDonorName() != null ? donation.getDonorName() : "Anonymous"));
        String category = donation.getCategory();
        holder.tvCategory.setText(category != null && !category.isEmpty() ? category : "Food");

        // Location
        String locationToShow = donation.getLocation();
        if ((locationToShow == null || locationToShow.isEmpty()) && donation.getAddress() != null) {
            locationToShow = donation.getAddress();
        }
        holder.tvLocation.setText(locationToShow != null && !locationToShow.isEmpty() ? ("Location: " + locationToShow) : "Location: Not specified");

        // Timestamp
        if (donation.getTimestamp() != null) {
            holder.tvTimestamp.setText(getTimeAgo(donation.getTimestamp()));
        } else {
            holder.tvTimestamp.setText("Just now");
        }

        // Received / urgent UI
        boolean isUrgent = donation.getType() != null && donation.getType().equals("urgent_request");
        if (donation.isReceived()) {
            holder.btnReceive.setVisibility(View.GONE);
            holder.tvReceivedStatus.setVisibility(View.VISIBLE);
            holder.tvReceivedStatus.setText(donation.getReceiverName() != null ? ("\u2713 Received by " + donation.getReceiverName()) : "\u2713 Already received");
        } else if (isUrgent) {
            holder.btnReceive.setVisibility(View.VISIBLE);
            holder.btnReceive.setText("FULFILL REQUEST");
            holder.tvReceivedStatus.setVisibility(View.GONE);
        } else {
            holder.tvReceivedStatus.setVisibility(View.GONE);
            holder.btnReceive.setVisibility(View.VISIBLE);
        }

        // Disable receive for own donation
        if (auth.getCurrentUser() != null && donation.getDonorId() != null && donation.getDonorId().equals(auth.getCurrentUser().getUid())) {
            holder.btnReceive.setText("Your Donation");
            holder.btnReceive.setEnabled(false);
        } else {
            holder.btnReceive.setEnabled(true);
        }

        // Receive click
        if (!donation.isReceived() && receiveClickListener != null) {
            holder.btnReceive.setOnClickListener(v -> receiveClickListener.onReceiveClick(donation));
        } else {
            holder.btnReceive.setOnClickListener(null);
        }

        // Call button opens dialer if phone exists, otherwise opens map to location
        String location = locationToShow;
        holder.btnCall.setOnClickListener(v -> {
            if (donation.getDonorPhone() != null && !donation.getDonorPhone().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + donation.getDonorPhone()));
                context.startActivity(intent);
            } else if (location != null && !location.isEmpty()) {
                String uri;
                if (location.contains(",")) {
                    uri = "geo:" + location;
                } else {
                    uri = String.format(Locale.ENGLISH, "geo:0,0?q=%s", Uri.encode(location));
                }
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                context.startActivity(mapIntent);
            } else {
                Toast.makeText(context, "No contact or location available", Toast.LENGTH_SHORT).show();
            }
        });

        // View location button opens maps
        holder.btnViewLocation.setOnClickListener(v -> {
            if (location != null && !location.isEmpty()) {
                String uri;
                if (location.contains(",")) {
                    uri = "geo:" + location;
                } else {
                    uri = String.format(Locale.ENGLISH, "geo:0,0?q=%s", Uri.encode(location));
                }
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                context.startActivity(mapIntent);
            } else {
                Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Card click
        holder.bind(donation, listener);
    }

    @Override
    public int getItemCount() {
        return donationList != null ? donationList.size() : 0;
    }

    private String getTimeAgo(Timestamp timestamp) {
        Date date = timestamp.toDate();
        long diff = System.currentTimeMillis() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) return days + " day" + (days > 1 ? "s" : "") + " ago";
        if (hours > 0) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        if (minutes > 0) return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        return "Just now";
    }

    public static class DonationViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvFoodName;
        public final TextView tvFoodType;
        public final TextView tvDescription;
        public final TextView tvQuantity;
        public final TextView tvDonorName;
        public final TextView tvLocation;
        public final TextView tvTimestamp;
        public final TextView tvReceivedStatus;
        public final TextView tvCategory;
        public final Button btnReceive;
        public final Button btnViewLocation;
        public final Button btnCall;
        public final LinearLayout layoutLocation;

        public DonationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodType = itemView.findViewById(R.id.tvFoodType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvDonorName = itemView.findViewById(R.id.tvDonorName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvReceivedStatus = itemView.findViewById(R.id.tvReceivedStatus);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnReceive = itemView.findViewById(R.id.btnReceive);
            btnViewLocation = itemView.findViewById(R.id.btnViewLocation);
            btnCall = itemView.findViewById(R.id.btnCall);
            layoutLocation = itemView.findViewById(R.id.layoutLocation);
        }

        public void bind(DonationModel donation, OnItemClickListener listener) {
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(donation);
                }
            });
        }
    }
}
