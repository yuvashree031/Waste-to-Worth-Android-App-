package com.example.wastetoworth;

import com.google.firebase.firestore.ListenerRegistration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * DonationHistoryActivity - Displays user's donation history
 * 
 * This activity shows a list of all donations made by the current user,
 * including food items, dates, and status information.
 */
public class DonationHistoryActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private TextView textEmpty;
    private DonationHistoryAdapter adapter;
    private List<DonationItem> donationList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_history);
        
        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Donation History");
        }
        
        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        textEmpty = findViewById(R.id.textEmpty);
        
        // Initialize donation list and adapter
        donationList = new ArrayList<>();
        adapter = new DonationHistoryAdapter(donationList);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Load donation history
        loadDonationHistory();
    }
    
    private ListenerRegistration historyListener;

    private void loadDonationHistory() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        
        if (userId == null) {
            Toast.makeText(this, "Please login to view donation history", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Check network connectivity
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show();
            textEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
        
        // Check Firebase initialization
        if (db == null) {
            Toast.makeText(this, "Database not initialized. Please try again.", Toast.LENGTH_SHORT).show();
            textEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
        
        // Load donations from Firestore with real-time listener
        // Note: Avoiding composite index by filtering in memory
        historyListener = db.collection("donations")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100) // Get more documents to filter in memory
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("DonationHistoryActivity", "Error loading donation history: " + e.getMessage(), e);
                        Toast.makeText(this, "Error loading donation history: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        textEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        return;
                    }
                    
                    donationList.clear();
                    
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        textEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        textEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Filter donations by donorId in memory to avoid composite index
                            String documentDonorId = document.getString("donorId");
                            if (documentDonorId == null) {
                                // Try alternative field names
                                documentDonorId = document.getString("userId");
                            }
                            if (documentDonorId == null) {
                                documentDonorId = document.getString("userid");
                            }
                            if (documentDonorId == null || !documentDonorId.equals(userId)) {
                                continue; // Skip donations not made by current user
                            }
                            
                            DonationItem donation = new DonationItem();
                            donation.setId(document.getId());
                            
                            // Handle different donation types
                            String category = document.getString("type");
                            String itemName = "";
                            
                            if ("Clothes".equals(category)) {
                                String clothesType = document.getString("clothesType");
                                String clothesSize = document.getString("clothesSize");
                                String clothesGender = document.getString("clothesGender");
                                itemName = "Clothes - " + (clothesType != null ? clothesType : "") + " (" + (clothesSize != null ? clothesSize : "") + ", " + (clothesGender != null ? clothesGender : "") + ")";
                            } else {
                                // Default to food or other types
                                itemName = document.getString("itemName");
                                if (itemName == null || itemName.isEmpty()) {
                                    itemName = document.getString("foodName");
                                }
                                if (itemName == null || itemName.isEmpty()) {
                                    itemName = category != null ? category + " donation" : "Donation";
                                }
                            }
                            
                            // Check if this is an urgent request
                            Boolean isUrgent = document.getBoolean("urgent");
                            String urgentReason = document.getString("urgentReason");
                            if (isUrgent != null && isUrgent && urgentReason != null && !urgentReason.isEmpty()) {
                                itemName = "\uD83D\uDEA8 URGENT: " + itemName;
                                donation.setUrgent(true);
                                donation.setUrgentReason(urgentReason);
                            }
                            
                            donation.setItemName(itemName);
                            // Handle quantity field - could be String or Number
                            Object quantityObj = document.get("quantity");
                            String quantityString = "";
                            if (quantityObj instanceof String) {
                                quantityString = (String) quantityObj;
                            } else if (quantityObj instanceof Number) {
                                quantityString = String.valueOf(quantityObj);
                            }
                            donation.setQuantity(quantityString);
                            // Handle location field - check if it's stored as GeoPoint or String
                            Object locationObj = document.get("location");
                            String locationString = "";
                            
                            if (locationObj instanceof com.google.firebase.firestore.GeoPoint) {
                                com.google.firebase.firestore.GeoPoint geoPoint = (com.google.firebase.firestore.GeoPoint) locationObj;
                                locationString = geoPoint.getLatitude() + "," + geoPoint.getLongitude();
                            } else if (locationObj instanceof String) {
                                locationString = (String) locationObj;
                            } else {
                                String address = document.getString("address");
                                if (address != null) locationString = address;
                            }
                            
                            donation.setLocation(locationString);
                            donation.setCategory(category);
                            
                            // Handle timestamp - check for both timestamp and createdAt fields
                            java.util.Date date = null;
                            Object tsObj = document.get("timestamp");
                            if (tsObj instanceof com.google.firebase.Timestamp) {
                                date = ((com.google.firebase.Timestamp) tsObj).toDate();
                            } else if (tsObj instanceof Number) {
                                date = new java.util.Date(((Number) tsObj).longValue());
                            }
                            if (date != null) {
                                donation.setTimestamp(date);
                            }
                            
                            // Set status based on isReceived field
                            Boolean isReceived = document.getBoolean("isReceived");
                            if (isReceived != null && isReceived) {
                                donation.setStatus("Claimed");
                            } else {
                                donation.setStatus("Available");
                            }
                            
                            donation.setReceiverId(document.getString("receiverId"));
                            donation.setDonorName(document.getString("donorName"));
                            
                            // Add phone number for call functionality
                            String phone = document.getString("phone");
                            if (phone != null && !phone.isEmpty()) {
                                donation.setPhone(phone);
                            }
                            
                            donationList.add(donation);
                            
                            // Limit to 50 donations after filtering
                            if (donationList.size() >= 50) {
                                break;
                            }
                        }
                        
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (historyListener != null) {
            historyListener.remove();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    /**
     * Donation item model class
     */
    public static class DonationItem {
        private String id;
        private String itemName;
        private String quantity;
        private String location;
        private String category;
        private java.util.Date timestamp;
        private String status;
        private String receiverId;
        private String donorName;
        private String phone;
        private boolean urgent;
        private String urgentReason;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        
        public String getFoodName() { return itemName; } // Backward compatibility
        public void setFoodName(String foodName) { this.itemName = foodName; } // Backward compatibility
        
        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public java.util.Date getTimestamp() { return timestamp; }
        public void setTimestamp(java.util.Date timestamp) { this.timestamp = timestamp; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getReceiverId() { return receiverId; }
        public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
        
        public String getDonorName() { return donorName; }
        public void setDonorName(String donorName) { this.donorName = donorName; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public void setUrgent(boolean urgent) { this.urgent = urgent; }
        public boolean isUrgent() { return urgent; }
        public void setUrgentReason(String urgentReason) { this.urgentReason = urgentReason; }
        public String getUrgentReason() { return urgentReason; }
    }
}