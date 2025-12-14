package com.example.wastetoworth;

import com.google.firebase.firestore.DocumentSnapshot;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wastetoworth.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
public class VolunteerOrdersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private VolunteerOrdersAdapter adapter;
    private List<DonationModel> donationList;
    private FirebaseFirestore db;
    private ListenerRegistration orderListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_orders);
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewOrders);
        progressBar = findViewById(R.id.progress_bar);
        // Setup RecyclerView
        donationList = new ArrayList<>();
        adapter = new VolunteerOrdersAdapter(donationList, new VolunteerOrdersAdapter.OnOrderActionListener() {
            @Override
            public void onVolunteer(DonationModel donation) {
                // Handle volunteer action
                handleVolunteerAction(donation);
            }
            @Override
            public void onUpdateStatus(DonationModel donation) {
                // Handle status update
                updateDonationStatus(donation);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        // Load orders
        loadOrders();
    }
    private void loadOrders() {
        // Check network connectivity
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        
        // Check Firebase initialization
        if (db == null) {
            Toast.makeText(this, "Database not initialized. Please try again.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);

        orderListener = db.collection("donations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50) // Limit results for better performance
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Log.e("VolunteerOrdersActivity", "Error loading orders: " + error.getMessage(), error);
                        Toast.makeText(this, "Error loading orders: " + error.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        donationList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            DonationModel donation = doc.toObject(DonationModel.class);
                            if (donation != null && !donation.isReceived()) {
                                donation.setDocumentId(doc.getId());
                                donationList.add(donation);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        donationList.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "No orders found", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void handleVolunteerAction(DonationModel donation) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Update donation with volunteer info
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        
        db.collection("donations").document(donation.getDonationId())
                .update(
                        "volunteerId", userId,
                        "volunteerEmail", userEmail != null ? userEmail : "",
                        "volunteerStatus", "volunteered"
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "You've volunteered for this order!", 
                            Toast.LENGTH_SHORT).show();
                    // Listener will update UI automatically
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show()
                );
    }
    
    private void updateDonationStatus(DonationModel donation) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Update donation status to mark as received
        db.collection("donations").document(donation.getDonationId())
                .update(
                        "isReceived", true,
                        "volunteerStatus", "completed"
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Order marked as completed!", 
                            Toast.LENGTH_SHORT).show();
                    // Listener will update UI automatically
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show()
                );
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (orderListener != null) {
            orderListener.remove();
            orderListener = null;
        }
    }
}
