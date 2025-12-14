package com.example.wastetoworth;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wastetoworth.R;
import java.util.List;
public class VolunteerOrdersAdapter extends RecyclerView.Adapter<VolunteerOrdersAdapter.OrderViewHolder> {
    public interface OnOrderActionListener {
        void onVolunteer(DonationModel donation);
        void onUpdateStatus(DonationModel donation);
    }
    private List<DonationModel> donationList;
    private OnOrderActionListener listener;
    private Context context;
    public VolunteerOrdersAdapter(List<DonationModel> donationList, OnOrderActionListener listener) {
        this.donationList = donationList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_volunteer_order, parent, false);
        return new OrderViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        DonationModel donation = donationList.get(position);
        
        // Set basic donation information
        String foodInfo = String.format("%s - %s", 
            donation.getFoodName() != null ? donation.getFoodName() : "Food Donation",
            donation.getQuantity() != null ? donation.getQuantity() : "N/A");
            
        holder.txtOrderName.setText(foodInfo);
        
        // Format address if available, otherwise use a generic location
        String address = donation.getAddress() != null ? 
            donation.getAddress() : "Location not specified";
        holder.txtOrderAddress.setText(address);
        
        // Format status with proper capitalization
        String status = donation.getDeliveryStatus();
        if (status != null && !status.isEmpty()) {
            status = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
        } else {
            status = "Available";
        }
        holder.txtOrderStatus.setText(String.format("Status: %s", status));
        
        // Set volunteer button state based on status
        if ("completed".equalsIgnoreCase(status) || "delivered".equalsIgnoreCase(status)) {
            holder.btnVolunteer.setEnabled(false);
            holder.btnVolunteer.setText("Completed");
        } else {
            holder.btnVolunteer.setEnabled(true);
            holder.btnVolunteer.setText("Volunteer");
            
            holder.btnVolunteer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVolunteer(donation);
                }
            });
        }
        
        // Set update status click listener if the button is visible
        if (holder.btnUpdateStatus != null && holder.btnUpdateStatus.getVisibility() == View.VISIBLE) {
            holder.btnUpdateStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpdateStatus(donation);
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return donationList != null ? donationList.size() : 0;
    }
    public void updateList(List<DonationModel> newList) {
        donationList = newList;
        notifyDataSetChanged();
    }
    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderName, txtOrderAddress, txtOrderStatus;
        Button btnVolunteer, btnUpdateStatus;
        
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderName = itemView.findViewById(R.id.txtOrderName);
            txtOrderAddress = itemView.findViewById(R.id.txtOrderAddress);
            txtOrderStatus = itemView.findViewById(R.id.txtOrderStatus);
            btnVolunteer = itemView.findViewById(R.id.btnVolunteer);
            // Note: btnUpdateStatus is not in the layout, so it's removed
        }
    }
}
