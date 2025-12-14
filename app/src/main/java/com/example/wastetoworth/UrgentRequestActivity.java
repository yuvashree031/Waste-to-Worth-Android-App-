package com.example.wastetoworth;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.firebase.Timestamp;
import android.content.Intent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.wastetoworth.DonationModel;
import com.example.wastetoworth.DonationAdapter;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.GeoPoint;

public class UrgentRequestActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DonationAdapter adapter;
    private List<DonationModel> donationList;
    private FirebaseFirestore db;
    private ListenerRegistration donationsListener;
    private ListenerRegistration urgentRequestsListener;
    private Button buttonUrgentRequest;
    private FirebaseAuth auth;
    private List<DonationModel> urgentRequestList;

    private AlertDialog urgentDialog;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isUsingCurrentLocation = false;
    private double currentLocationLat = 0.0;
    private double currentLocationLng = 0.0;
    private String currentLocationAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urgent_request);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view_donations);
        buttonUrgentRequest = findViewById(R.id.buttonUrgentRequest);

        // Setup RecyclerView
        donationList = new ArrayList<>();
        urgentRequestList = new ArrayList<>();
        adapter = new DonationAdapter(this, donationList, donation -> {
            Toast.makeText(this, "Selected: " + (donation.getFoodName() != null ? donation.getFoodName() : donation.getName()), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set receive click listener to handle marking donations as received
        adapter.setOnReceiveClickListener(donation -> markAsReceived(donation));
    }

    private void markAsReceived(DonationModel donation) {
        if (donation.getDocumentId() == null) {
            Toast.makeText(this, "Invalid donation", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("donations")
                .document(donation.getDocumentId())
                .update("isReceived", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Donation marked as received!", Toast.LENGTH_SHORT).show();
                    donationList.remove(donation);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    Log.e("UrgentRequestActivity", "Error marking as received: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to mark as received: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyState() {
        if (donationList.isEmpty()) {
            Toast.makeText(this, "No available donations", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (donationsListener != null) {
            donationsListener.remove();
        }
        if (urgentRequestsListener != null) {
            urgentRequestsListener.remove();
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        loadDonationsAndUrgentRequests();
        buttonUrgentRequest.setOnClickListener(v -> showLocationDialog());
    }

    private void loadDonationsAndUrgentRequests() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (db == null) {
            Toast.makeText(this, "Database not initialized. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        loadAvailableDonations();
        loadUrgentRequests();
    }
    
    private void loadAvailableDonations() {
        if (donationsListener != null) {
            donationsListener.remove();
        }
        
        donationsListener = db.collection("donations")
                .whereEqualTo("isReceived", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("UrgentRequestActivity", "Error loading donations: " + e.getMessage(), e);
                        Toast.makeText(this, "Error loading donations: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (queryDocumentSnapshots != null) {
                        List<DonationModel> newDonations = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                DonationModel donation = createDonationFromDocument(document);
                                if (donation != null) {
                                    newDonations.add(donation);
                                }
                            } catch (Exception ex) {
                                Log.e("UrgentRequestActivity", "Error processing document: " + ex.getMessage(), ex);
                            }
                        }
                        
                        mergeDonationsAndUrgentRequests(newDonations);
                    }
                });
    }
    
    private void loadUrgentRequests() {
        if (urgentRequestsListener != null) {
            urgentRequestsListener.remove();
        }
        
        urgentRequestsListener = db.collection("urgent_requests")
                .whereEqualTo("status", "pending")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("UrgentRequestActivity", "Error loading urgent requests: " + e.getMessage(), e);
                        return;
                    }
                    
                    if (queryDocumentSnapshots != null) {
                        urgentRequestList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                DonationModel urgentRequest = createDonationFromUrgentRequest(document);
                                if (urgentRequest != null) {
                                    urgentRequestList.add(urgentRequest);
                                }
                            } catch (Exception ex) {
                                Log.e("UrgentRequestActivity", "Error processing urgent request: " + ex.getMessage(), ex);
                            }
                        }
                        
                        mergeDonationsAndUrgentRequests(null);
                    }
                });
    }
    
    private DonationModel createDonationFromDocument(QueryDocumentSnapshot document) {
        try {
            DonationModel donation = new DonationModel();
            donation.setDocumentId(document.getId());
            
            String foodName = document.getString("foodName");
            String food = document.getString("food");
            String foodType = document.getString("foodType");
            String category = document.getString("category");
            
            String resolvedName = foodName;
            if (resolvedName == null || resolvedName.trim().isEmpty()) {
                if (food != null && !food.trim().isEmpty()) resolvedName = food;
                else if (foodType != null && !foodType.trim().isEmpty()) resolvedName = foodType;
                else if (category != null && !category.trim().isEmpty()) resolvedName = category;
                else resolvedName = "Food Item";
            }
            donation.setFoodName(resolvedName);

            donation.setDescription(document.getString("description") != null ? document.getString("description") : "");

            String quantity = document.getString("quantity");
            if (quantity == null) {
                Long qLong = document.getLong("quantity");
                if (qLong != null) quantity = String.valueOf(qLong);
            }
            donation.setQuantity(quantity != null ? quantity : "");
            
            String donorName = document.getString("donorName");
            String name = document.getString("name");
            if (donorName == null && name != null) donorName = name;
            donation.setDonorName(donorName != null ? donorName : "Anonymous");
            
            String donorId = document.getString("donorId");
            String userid = document.getString("userid");
            if (donorId == null && userid != null) donorId = userid;
            donation.setDonorId(donorId != null ? donorId : "unknown");
            
            // Location
            String locationStr = document.getString("location");
            if (locationStr == null) {
                Object locationObj = document.get("location");
                if (locationObj instanceof GeoPoint) {
                    GeoPoint gp = (GeoPoint) locationObj;
                    locationStr = gp.getLatitude() + "," + gp.getLongitude();
                }
            }
            donation.setLocation(locationStr != null ? locationStr : "");
            donation.setAddress(document.getString("address") != null ? document.getString("address") : locationStr);
            donation.setPhone(document.getString("phone") != null ? document.getString("phone") : "");
            donation.setCategory(document.getString("category") != null ? document.getString("category") : "Food");
            donation.setType(document.getString("type") != null ? document.getString("type") : "food");
            
            Boolean isReceived = document.getBoolean("isReceived");
            donation.setReceived(isReceived != null ? isReceived : false);
            
            Timestamp ts = document.getTimestamp("timestamp");
            if (ts == null) {
                Long millis = document.getLong("timestamp");
                if (millis != null) ts = new Timestamp(new Date(millis));
            }
            if (ts != null) donation.setTimestamp(ts);
            
            return donation;
            
        } catch (Exception e) {
            Log.e("UrgentRequestActivity", "Error creating donation from document: " + e.getMessage(), e);
            return null;
        }
    }

    private DonationModel createDonationFromUrgentRequest(QueryDocumentSnapshot document) {
        try {
            DonationModel donation = new DonationModel();
            donation.setDocumentId(document.getId());
            
            donation.setFoodName("\uD83D\uDEA8 URGENT REQUEST");
            String qty = document.getString("quantity");
            if (qty == null) {
                Long ql = document.getLong("quantity");
                if (ql != null) qty = String.valueOf(ql);
            }
            donation.setDescription("Urgent food request: " + (qty != null ? qty : "") + " items needed");
            donation.setQuantity(qty != null ? qty : "");
            donation.setDonorName(document.getString("requesterName") != null ? document.getString("requesterName") : "Community Request");
            donation.setDonorId(document.getString("requesterId") != null ? document.getString("requesterId") : document.getId());
            donation.setPhone("");
            donation.setCategory("Urgent Request");
            donation.setType("urgent_request");
            donation.setReceived(false);
            
            // location can be string or GeoPoint
            String locationString = document.getString("location");
            if (locationString == null) {
                Object locationObj = document.get("location");
                if (locationObj instanceof GeoPoint) {
                    GeoPoint geoPoint = (GeoPoint) locationObj;
                    locationString = geoPoint.getLatitude() + "," + geoPoint.getLongitude();
                }
            }
            if (locationString != null && !locationString.trim().isEmpty()) {
                donation.setLocation(locationString);
                donation.setAddress(locationString);
            } else {
                String deliveryAddress = document.getString("deliveryAddress");
                if (deliveryAddress != null) {
                    donation.setLocation(deliveryAddress);
                    donation.setAddress(deliveryAddress);
                } else {
                    donation.setLocation("Location not specified");
                    donation.setAddress("Address not provided");
                }
            }
            
            Timestamp timestamp = document.getTimestamp("timestamp");
            if (timestamp == null) {
                Long millis = document.getLong("timestamp");
                if (millis != null) timestamp = new Timestamp(new Date(millis));
            }
            donation.setTimestamp(timestamp != null ? timestamp : Timestamp.now());
            
            return donation;
            
        } catch (Exception e) {
            Log.e("UrgentRequestActivity", "Error creating donation from urgent request: " + e.getMessage(), e);
            return null;
        }
    }
    
    private void mergeDonationsAndUrgentRequests(List<DonationModel> newDonations) {
        donationList.clear();
        if (newDonations != null) {
            for (DonationModel donation : newDonations) {
                if (donation != null) {
                    donationList.add(donation);
                }
            }
        }
        for (DonationModel urgentRequest : urgentRequestList) {
            if (urgentRequest != null) {
                donationList.add(urgentRequest);
            }
        }
        if (!donationList.isEmpty()) {
            java.util.Collections.sort(donationList, (d1, d2) -> {
                if (d1.getTimestamp() == null || d2.getTimestamp() == null) return 0;
                return d2.getTimestamp().compareTo(d1.getTimestamp());
            });
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }
    
    private void updateDonationList() {
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Location Method");
        builder.setMessage("How would you like to set the location?");
        builder.setPositiveButton("Use Current Location", (dialog, which) -> {
            getCurrentLocation();
            dialog.dismiss();
        });
        builder.setNegativeButton("Enter Manually", (dialog, which) -> {
            showUrgentRequestDialog();
            dialog.dismiss();
        });
        builder.show();
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocationLat = location.getLatitude();
                currentLocationLng = location.getLongitude();
                getAddressFromLocation(location);
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                showUrgentRequestDialog();
            }
        });
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                currentLocationAddress = address.getAddressLine(0);
                isUsingCurrentLocation = true;
                showUrgentRequestDialog();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to get address", Toast.LENGTH_SHORT).show();
            isUsingCurrentLocation = true;
            showUrgentRequestDialog();
        }
    }

    private void showUrgentRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_urgent_request, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        urgentDialog = dialog;
        
        EditText editTextQuantity = dialogView.findViewById(R.id.editTextQuantity);
        EditText editTextLocation = dialogView.findViewById(R.id.editTextLocation);
        Button buttonSubmit = dialogView.findViewById(R.id.buttonSubmit);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        
        if (isUsingCurrentLocation && currentLocationAddress != null) {
            editTextLocation.setText(currentLocationAddress);
        }
        
        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        
        buttonSubmit.setOnClickListener(v -> {
            String quantity = editTextQuantity.getText().toString().trim();
            String location = editTextLocation.getText().toString().trim();
            
            if (quantity.isEmpty()) {
                editTextQuantity.setError("Please enter number of people");
                return;
            }
            
            if (location.isEmpty() && !isUsingCurrentLocation) {
                editTextLocation.setError("Please enter location");
                return;
            }
            
            String name = "Anonymous";
            String phone = "Not provided";
            if (auth.getCurrentUser() != null && auth.getCurrentUser().getDisplayName() != null) {
                name = auth.getCurrentUser().getDisplayName();
            }
            if (auth.getCurrentUser() != null && auth.getCurrentUser().getPhoneNumber() != null) {
                phone = auth.getCurrentUser().getPhoneNumber();
            }
            
            submitUrgentRequest(name, phone, quantity, location);
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void submitUrgentRequest(String name, String phone, String quantity, String location) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to submit urgent request", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String userEmail = auth.getCurrentUser().getEmail();
        
        Map<String, Object> urgentRequest = new HashMap<>();
        urgentRequest.put("requesterName", name);
        urgentRequest.put("requesterPhone", phone);
        urgentRequest.put("quantity", quantity);
        urgentRequest.put("deliveryAddress", location);
        urgentRequest.put("requesterId", userId);
        urgentRequest.put("requesterEmail", userEmail != null ? userEmail : "");
        urgentRequest.put("timestamp", Timestamp.now());
        urgentRequest.put("status", "pending");
        urgentRequest.put("type", "urgent_request");
        
        urgentRequest.put("location", location);
        urgentRequest.put("address", location);
        
        if (isUsingCurrentLocation) {
            urgentRequest.put("latitude", currentLocationLat);
            urgentRequest.put("longitude", currentLocationLng);
        }
        
        db.collection("urgent_requests")
            .add(urgentRequest)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Urgent request submitted successfully!", Toast.LENGTH_SHORT).show();
                createDonationRequestFromUrgent(documentReference.getId(), urgentRequest);
                
                isUsingCurrentLocation = false;
                currentLocationLat = 0.0;
                currentLocationLng = 0.0;
                currentLocationAddress = null;
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to submit urgent request: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void createDonationRequestFromUrgent(String documentId, Map<String, Object> urgentRequest) {
        Map<String, Object> donationRequest = new HashMap<>();
        donationRequest.put("foodName", "Urgent Request - " + urgentRequest.get("requesterName"));
        donationRequest.put("type", "urgent_request");
        donationRequest.put("description", "URGENT REQUEST from " + urgentRequest.get("requesterName") + " - " + urgentRequest.get("quantity") + " people need food assistance");
        donationRequest.put("quantity", urgentRequest.get("quantity"));
        donationRequest.put("category", "Urgent");
        donationRequest.put("donorName", urgentRequest.get("requesterName"));
        donationRequest.put("donorId", urgentRequest.get("requesterId"));
        donationRequest.put("donorEmail", urgentRequest.get("requesterEmail"));
        donationRequest.put("address", urgentRequest.get("address"));
        donationRequest.put("location", urgentRequest.get("address"));
        donationRequest.put("timestamp", urgentRequest.get("timestamp"));
        donationRequest.put("isReceived", false);
        donationRequest.put("isUrgentRequest", true);
        donationRequest.put("urgentRequestId", documentId);
        donationRequest.put("phone", urgentRequest.get("requesterPhone"));
        
        db.collection("donations")
            .add(donationRequest)
            .addOnSuccessListener(donationDocRef -> {
                db.collection("urgent_requests")
                    .document(documentId)
                    .update("donationId", donationDocRef.getId());
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("UrgentRequestActivity", "Failed to create donation request: " + e.getMessage());
            });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (donationsListener != null) {
            donationsListener.remove();
            donationsListener = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
