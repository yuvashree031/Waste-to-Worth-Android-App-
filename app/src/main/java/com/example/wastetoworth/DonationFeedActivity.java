package com.example.wastetoworth;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonationFeedActivity extends AppCompatActivity {

    private static final String TAG = "DonationFeedActivity";

    private RecyclerView recyclerViewDonations;
    private DonationAdapter donationAdapter;
    private List<DonationModel> donationList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private Spinner spinnerFilter;
    private Button btnRefresh;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_feed);

        initializeViews();
        setupFirebase();
        setupRecyclerView();
        setupFilters();
        setupRefreshListener();
        setupFAB();
        loadAllFeed();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewDonations = findViewById(R.id.recyclerViewDonations);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    private void setupFirebase() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupRecyclerView() {
        donationList = new ArrayList<>();
        donationAdapter = new DonationAdapter(this, donationList, donation -> {
            // Handle item click if needed
        });
        
        // Set up receive click listener
        donationAdapter.setOnReceiveClickListener(new DonationAdapter.OnReceiveClickListener() {
            @Override
            public void onReceiveClick(DonationModel donation) {
                handleReceiveDonation(donation);
            }
        });
        
        recyclerViewDonations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDonations.setAdapter(donationAdapter);
    }

    private void setupFilters() {
        String[] filterOptions = {"All", "Available"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);
        spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadAllFeed();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupRefreshListener() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadAllFeed);
        }
        btnRefresh.setOnClickListener(v -> loadAllFeed());
    }

    private void setupFAB() {
        View fab = findViewById(R.id.fabAddDonation);
        if (fab != null) {
            fab.setOnClickListener(v -> startActivity(new android.content.Intent(this, Donate.class)));
        }
    }

    private void loadAllFeed() {
        showLoading(true);
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);

        firestore.collection("donations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(donationsSnap -> {
                    List<DonationModel> combined = new ArrayList<>();
                    for (DocumentSnapshot document : donationsSnap) {
                        DonationModel model = createDonationFromDocument(document);
                        if (model != null) combined.add(model);
                    }
                    firestore.collection("urgent_requests")
                            .whereEqualTo("status", "pending")
                            .get()
                            .addOnSuccessListener(urgentSnap -> {
                                for (DocumentSnapshot document : urgentSnap) {
                                    DonationModel urgent = createUrgentFromDocument(document);
                                    if (urgent != null) combined.add(urgent);
                                }
                                Collections.sort(combined, new Comparator<DonationModel>() {
                                    @Override
                                    public int compare(DonationModel o1, DonationModel o2) {
                                        if (o1.getTimestamp() == null && o2.getTimestamp() == null) return 0;
                                        if (o1.getTimestamp() == null) return 1;
                                        if (o2.getTimestamp() == null) return -1;
                                        return o2.getTimestamp().compareTo(o1.getTimestamp());
                                    }
                                });
                                showLoading(false);
                                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                                updateDonationList(combined);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "urgent_requests load error", e);
                                Toast.makeText(this, "Failed to load urgent requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                showLoading(false);
                                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                                updateDonationList(new ArrayList<>());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "donations load error", e);
                    Toast.makeText(this, "Failed to load donations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    updateDonationList(new ArrayList<>());
                });
    }

    private DonationModel createDonationFromDocument(DocumentSnapshot document) {
        try {
            String documentId = document.getId();
            String itemName = safe(document.getString("itemName"));
            String name = safe(document.getString("name"));
            String foodName = safe(document.getString("foodName"));
            String type = safe(document.getString("type"));
            String category = safe(document.getString("category"));
            String resolvedName = !itemName.isEmpty() ? itemName : (!foodName.isEmpty() ? foodName : (!name.isEmpty() ? name : (!category.isEmpty() ? category : "Donation")));
            String description = safe(document.getString("description"));
            String quantity = extractQuantity(document.get("quantity"));
            String donorName = safe(document.getString("donorName"));
            String donorId = coalesce(document.getString("donorId"), document.getString("userId"), document.getString("userid"));

            String address = safe(document.getString("address"));
            String locationStr = "";
            Object locObj = document.get("location");
            if (locObj instanceof String) {
                locationStr = (String) locObj;
            } else if (locObj instanceof GeoPoint) {
                GeoPoint gp = (GeoPoint) locObj;
                locationStr = gp.getLatitude() + "," + gp.getLongitude();
            }
            if (address.isEmpty()) address = locationStr;

            Timestamp timestamp = null;
            Object tsObj = document.get("timestamp");
            if (tsObj instanceof Timestamp) {
                timestamp = (Timestamp) tsObj;
            } else if (tsObj instanceof Number) {
                timestamp = new Timestamp(new Date(((Number) tsObj).longValue()));
            }

            DonationModel donation = new DonationModel();
            donation.setDocumentId(documentId);
            donation.setName(resolvedName);
            donation.setFoodName(!foodName.isEmpty() ? foodName : resolvedName);
            donation.setDescription(description);
            donation.setType(type.isEmpty() ? "food" : type);
            donation.setCategory(category);
            donation.setQuantity(quantity);
            donation.setDonorName(donorName);
            donation.setDonorId(donorId);
            donation.setAddress(address);
            donation.setTimestamp(timestamp);
            if (!locationStr.isEmpty()) donation.setLocation(locationStr);
            String status = document.getString("status");
            if (status != null) donation.setStatus(status);
            String phone = coalesce(document.getString("phone"), document.getString("donorPhone"));
            if (phone != null) donation.setPhone(phone);
            String imageUrl = document.getString("imageUrl");
            if (imageUrl != null) donation.setImageUrl(imageUrl);
            Boolean isReceived = document.getBoolean("isReceived");
            if (isReceived != null) donation.setReceived(isReceived);
            return donation;
        } catch (Exception e) {
            Log.e(TAG, "Error creating donation from document: " + e.getMessage());
            return null;
        }
    }

    private DonationModel createUrgentFromDocument(DocumentSnapshot document) {
        try {
            String documentId = document.getId();
            String itemName = coalesce(document.getString("itemName"), document.getString("foodType"), "Urgent Request");
            String description = safe(document.getString("description"));
            if (description.isEmpty()) {
                String qty = extractQuantity(document.get("quantity"));
                description = "Urgent food request: " + qty + " items needed";
            }

            String address = safe(document.getString("deliveryAddress"));
            String locationStr = "";
            Object locObj = document.get("location");
            if (locObj instanceof String) {
                locationStr = (String) locObj;
            } else if (locObj instanceof GeoPoint) {
                GeoPoint gp = (GeoPoint) locObj;
                locationStr = gp.getLatitude() + "," + gp.getLongitude();
            }
            if (address.isEmpty()) address = locationStr;

            Timestamp timestamp = null;
            Object tsObj = document.get("timestamp");
            if (tsObj instanceof Timestamp) {
                timestamp = (Timestamp) tsObj;
            } else if (tsObj instanceof Number) {
                timestamp = new Timestamp(new Date(((Number) tsObj).longValue()));
            }

            DonationModel urgent = new DonationModel();
            urgent.setDocumentId(documentId);
            urgent.setName(itemName);
            urgent.setFoodName(itemName != null ? itemName : "Urgent Request");
            urgent.setDescription(description);
            urgent.setType("urgent_request");
            urgent.setCategory("Urgent Request");
            urgent.setAddress(address);
            if (!locationStr.isEmpty()) urgent.setLocation(locationStr);
            urgent.setTimestamp(timestamp);
            urgent.setReceived(false);
            return urgent;
        } catch (Exception e) {
            Log.e(TAG, "Error creating urgent from document: " + e.getMessage());
            return null;
        }
    }

    private void updateDonationList(List<DonationModel> donations) {
        donationList.clear();
        donationList.addAll(donations);
        donationAdapter.notifyDataSetChanged();
        if (donations.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerViewDonations.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerViewDonations.setVisibility(View.VISIBLE);
        }
    }

    private void handleReceiveDonation(DonationModel donation) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to receive donations", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user is trying to receive their own donation
        if (donation.getDonorId() != null && donation.getDonorId().equals(auth.getCurrentUser().getUid())) {
            Toast.makeText(this, "You cannot receive your own donation", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if donation is already received
        if (donation.isReceived()) {
            Toast.makeText(this, "This donation has already been received", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        String currentUserEmail = auth.getCurrentUser().getEmail();
        String currentUserName = auth.getCurrentUser().getDisplayName();
        
        if (currentUserName == null || currentUserName.isEmpty()) {
            currentUserName = currentUserEmail != null ? currentUserEmail.split("@")[0] : "Anonymous";
        }

        // Determine which collection to update based on donation type
        String collectionName = "donations";
        if ("urgent_request".equals(donation.getType())) {
            collectionName = "urgent_requests";
        }

        // Firestore reference to donation document
        DocumentReference donationRef = firestore.collection(collectionName).document(donation.getDocumentId());

        // Update fields in Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("isReceived", true);
        updates.put("receiverId", currentUserId);
        updates.put("receiverEmail", currentUserEmail);
        updates.put("receiverName", currentUserName);
        updates.put("receivedTimestamp", Timestamp.now());
        updates.put("status", "fulfilled"); // For urgent requests

        // Show loading state
        showLoading(true);

        // Apply updates to Firestore
        donationRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    
                    // Refresh the adapter to show updated status
                    donationAdapter.notifyDataSetChanged();
                    
                    String message = "urgent_request".equals(donation.getType()) ? 
                        "Request fulfilled successfully!" : "Donation received successfully!";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    
                    // Reload the feed to get latest data
                    loadAllFeed();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to receive donation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error receiving donation", e);
                });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewDonations.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String coalesce(String a, String b, String c) { return a != null ? a : (b != null ? b : c); }
    private static String coalesce(String a, String b, String c, String d) { return a != null ? a : (b != null ? b : (c != null ? c : d)); }
    private static String coalesce(String a, String b) { return a != null ? a : b; }
    private static String extractQuantity(Object q) {
        if (q == null) return "";
        if (q instanceof String) return (String) q;
        if (q instanceof Number) return String.valueOf(q);
        return "";
    }
}
