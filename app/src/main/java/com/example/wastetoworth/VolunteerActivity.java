package com.example.wastetoworth;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.text.TextUtils;

public class VolunteerActivity extends AppCompatActivity implements VolunteerDeliveryAdapter.OnDeliveryActionListener {
    private static final String TAG = "VolunteerActivity";
    private RecyclerView recyclerView;
    private VolunteerDeliveryAdapter adapter;
    private List<DonationModel> availableDonations;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView emptyView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private NetworkCallback networkCallback;
    private ListenerRegistration donationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_delivery);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupToolbar();
        initViews();
        setupRecyclerView();
        setupNetworkMonitoring();

        if (isNetworkAvailable()) {
            loadAvailableDonations();
        } else {
            loadCachedDonations();
            showOfflineMessage();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Volunteer Delivery");
            }
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewDeliveries);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        swipeRefresh.setOnRefreshListener(this::loadAvailableDonations);
    }

    private void setupRecyclerView() {
        availableDonations = new ArrayList<>();
        adapter = new VolunteerDeliveryAdapter(this, availableDonations, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            @SuppressWarnings("deprecation")
            android.net.NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
    }

    private void loadCachedDonations() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        db.collection("donations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get(Source.CACHE)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DonationModel> donations = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        DonationModel donation = createDonationFromDocument(document);
                        if (donation != null) {
                            donations.add(donation);
                        }
                    }
                    handleDonationsResponse(filterForVolunteer(donations), true);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading cached donations", e);
                    Toast.makeText(VolunteerActivity.this, "Error loading cached data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("No cached data available\nTap to refresh");
                });
    }

    private void loadAvailableDonations() {
        swipeRefresh.setRefreshing(true);
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        if (donationListener != null) {
            donationListener.remove();
        }

        donationListener = db.collection("donations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);

                    if (e != null) {
                        Log.e(TAG, "Error loading donations", e);
                        Toast.makeText(VolunteerActivity.this, "Error loading donations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<DonationModel> donations = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            DonationModel donation = createDonationFromDocument(document);
                            if (donation != null) {
                                String foodName = document.getString("foodName");
                                if (TextUtils.isEmpty(foodName)) foodName = document.getString("name");
                                if (TextUtils.isEmpty(foodName)) foodName = document.getString("foodType");
                                if (TextUtils.isEmpty(foodName)) foodName = "Unknown Food";
                                donation.setFoodName(foodName);
                                donations.add(donation);
                            }
                        }
                    }

                    handleDonationsResponse(filterForVolunteer(donations), false);
                });
    }

    private List<DonationModel> filterForVolunteer(List<DonationModel> donations) {
        List<DonationModel> filtered = new ArrayList<>();
        for (DonationModel d : donations) {
            try {
                boolean isReceived = d.isReceived();
                boolean isVolunteerAssigned = d.isVolunteerAssigned();
                if (!isReceived && !isVolunteerAssigned) {
                    filtered.add(d);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Filter error", ex);
            }
        }
        return filtered;
    }

    private void handleDonationsResponse(List<DonationModel> donations, boolean isFromCache) {
        swipeRefresh.setRefreshing(false);
        progressBar.setVisibility(View.GONE);

        if (donations != null && !donations.isEmpty()) {
            availableDonations.clear();
            availableDonations.addAll(donations);
            adapter.notifyDataSetChanged();
            emptyView.setVisibility(View.GONE);
        } else {
            String errorMsg = isFromCache ? "No cached data available" : "No donations available for delivery";
            Toast.makeText(VolunteerActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(errorMsg + "\nTap to refresh");
            if (!isFromCache) loadCachedDonations();
        }
    }

    @Override
    public void onAcceptDelivery(DonationModel donation) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Cannot accept delivery while offline", Toast.LENGTH_SHORT).show();
            return;
        }
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to volunteer", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("isVolunteerAssigned", true);
        updates.put("volunteerId", auth.getCurrentUser().getUid());
        updates.put("volunteerName", auth.getCurrentUser().getDisplayName() != null ? auth.getCurrentUser().getDisplayName() : "Volunteer");
        updates.put("deliveryStatus", "accepted");
        db.collection("donations").document(donation.getDocumentId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Delivery accepted! Navigate to pickup location.", Toast.LENGTH_LONG).show();
                    availableDonations.remove(donation);
                    adapter.notifyDataSetChanged();
                    navigateToLocation(donation.getLocation(), "Pickup Location");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error accepting delivery: ", e);
                    Toast.makeText(this, "Error accepting delivery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onViewLocation(DonationModel donation) {
        if (donation.getLocation() == null || donation.getLocation().isEmpty()) {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            return;
        }
        navigateToLocation(donation.getLocation(), "Donation Location");
    }

    private void navigateToLocation(String locationStr, String label) {
        if (locationStr != null && !locationStr.isEmpty()) {
            try {
                if (locationStr.contains(",")) {
                    String[] parts = locationStr.split(",");
                    if (parts.length == 2) {
                        double lat = Double.parseDouble(parts[0].trim());
                        double lng = Double.parseDouble(parts[1].trim());
                        String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%s(%f,%f)", lat, lng, label, lat, lng);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                            return;
                        }
                    }
                }
                String encodedAddress = Uri.encode(locationStr);
                String uri = String.format("geo:0,0?q=%s(%s)", encodedAddress, label);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "No maps app available", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to location: " + e.getMessage(), e);
                Toast.makeText(this, "Unable to open location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNetworkMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerNetworkCallback();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void registerNetworkCallback() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (connectivityManager == null) return;

            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .build();

            networkCallback = new NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    runOnUiThread(() -> {
                        if (availableDonations == null || availableDonations.isEmpty()) {
                            loadAvailableDonations();
                        } else if (emptyView != null && emptyView.getText().toString().contains("Offline")) {
                            loadAvailableDonations();
                        }
                    });
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    runOnUiThread(() -> {
                        if (availableDonations == null || availableDonations.isEmpty()) {
                            loadCachedDonations();
                        }
                        showOfflineMessage();
                    });
                }
            };

            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } catch (Exception e) {
            Log.e(TAG, "Error registering network callback", e);
        }
    }

    private void unregisterNetworkCallback() {
        try {
            if (networkCallback != null) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    connectivityManager.unregisterNetworkCallback(networkCallback);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering network callback", e);
        }
    }

    private void showOfflineMessage() {
        runOnUiThread(() -> {
            if (emptyView != null) {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("Offline mode: Showing cached data");
            }
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }
            Toast.makeText(this, "Offline mode: Showing cached data", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            unregisterNetworkCallback();
        }
        if (donationListener != null) {
            donationListener.remove();
        }
        super.onDestroy();
    }

    private DonationModel createDonationFromDocument(DocumentSnapshot document) {
        try {
            DonationModel donation = new DonationModel();
            donation.setDocumentId(document.getId());

            String fn = document.getString("foodName");
            String nn = document.getString("name");
            String ft = document.getString("foodType");
            String display = !TextUtils.isEmpty(fn) ? fn : (!TextUtils.isEmpty(nn) ? nn : (!TextUtils.isEmpty(ft) ? ft : "Donation"));
            donation.setFoodName(display);

            Object q = document.get("quantity");
            donation.setQuantity(q instanceof String ? (String) q : (q instanceof Number ? String.valueOf(q) : ""));
            donation.setPhone(document.getString("phoneNumber"));

            Object tsObj = document.get("timestamp");
            if (tsObj instanceof Timestamp) donation.setTimestamp((Timestamp) tsObj);
            else if (tsObj instanceof Number) donation.setTimestamp(new Timestamp(new java.util.Date(((Number) tsObj).longValue())));

            Boolean rec = document.getBoolean("isReceived");
            if (rec != null) donation.setReceived(rec);
            Boolean va = document.getBoolean("isVolunteerAssigned");
            if (va != null) donation.setVolunteerAssigned(va);

            Object locationObj = document.get("location");
            String locationStr = null;
            if (locationObj instanceof GeoPoint) {
                GeoPoint geo = (GeoPoint) locationObj;
                locationStr = geo.getLatitude() + "," + geo.getLongitude();
            } else if (locationObj instanceof String) {
                locationStr = (String) locationObj;
            }
            if (locationStr != null && !locationStr.isEmpty()) {
                donation.setLocation(locationStr);
                donation.setAddress(locationStr);
            }

            donation.setVolunteerId(document.getString("volunteerId"));
            donation.setVolunteerName(document.getString("volunteerName"));
            donation.setDeliveryStatus(document.getString("deliveryStatus"));

            return donation;
        } catch (Exception e) {
            Log.e(TAG, "Error creating donation from document: " + document.getId(), e);
            return null;
        }
    }
}