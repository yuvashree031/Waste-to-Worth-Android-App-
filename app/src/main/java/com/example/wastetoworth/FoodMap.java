package com.example.wastetoworth;
// Import necessary Android, Google Maps, Firebase, and utility classes
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FoodMap extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private static final String TAG = "FoodMap";
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private LatLng donorLocation;
    private String donorName = "Donor Location";
    private MaterialButton btnGetDirections;
    private SupportMapFragment mapFragment;
    private final int REQUEST_CODE = 11;
    private FirebaseFirestore cloudstorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_map);

        cloudstorage = FirebaseFirestore.getInstance();

        btnGetDirections = findViewById(R.id.btnGetDirections);
        if (btnGetDirections != null) {
            btnGetDirections.setOnClickListener(v -> openDirectionsInMaps());
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            double lat = intent.getDoubleExtra("latitude", 0);
            double lng = intent.getDoubleExtra("longitude", 0);
            if (lat == 0 && lng == 0) { lat = 28.6139; lng = 77.2090; }
            donorLocation = new LatLng(lat, lng);
            donorName = intent.hasExtra("donorName") ? intent.getStringExtra("donorName") : "Your Location";
        } else {
            donorLocation = new LatLng(28.6139, 77.2090);
            donorName = "Default Location";
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    private void openDirectionsInMaps() {
        if (mLastLocation != null) {
            String uri = "google.navigation:q=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        } else {
            Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap == null) {
            Toast.makeText(this, "Map not available", Toast.LENGTH_SHORT).show();
            return;
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            buildGoogleApiClient();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

        if (donorLocation != null) {
            mMap.addMarker(new MarkerOptions().position(donorLocation).title(donorName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(donorLocation, 15));
            mMap.setPadding(0, 0, 0, 200);
        }

        loadMarkers();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (donorLocation != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(currentLatLng);
            builder.include(donorLocation);
            int padding = 100;
            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
            } catch (Exception e) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12));
            }
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
        }
        mMap.addMarker(new MarkerOptions()
                .position(currentLatLng)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                .showInfoWindow();
    }

    private void loadMarkers() {
        // Donations
        cloudstorage.collection("donations")
                .get()
                .addOnSuccessListener(snap -> {
                    int added = 0;
                    for (QueryDocumentSnapshot doc : snap) {
                        try {
                            String itemName = coalesce(doc.getString("itemName"), doc.getString("foodName"), doc.getString("name"), "Donation");
                            String description = safe(doc.getString("description"));
                            String locationStr = safeStringField(doc.get("location"));
                            String address = safe(doc.getString("address"));
                            String loc = !locationStr.isEmpty() ? locationStr : address;
                            LatLng latLng = toLatLng(loc);
                            if (latLng == null) continue;
                            boolean isReceived = Boolean.TRUE.equals(doc.getBoolean("isReceived"));
                            float color = isReceived ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_BLUE;
                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(itemName)
                                    .snippet(description)
                                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
                            added++;
                        } catch (Exception e) {
                            Log.e(TAG, "Donation marker error: " + e.getMessage());
                        }
                    }
                    if (added == 0) Log.d(TAG, "No donation markers added (string location parse)");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Donations load failed", e);
                    Toast.makeText(this, "Failed to load donations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Urgent requests
        cloudstorage.collection("urgent_requests")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(snap -> {
                    int added = 0;
                    for (QueryDocumentSnapshot doc : snap) {
                        try {
                            String itemName = coalesce(doc.getString("itemName"), doc.getString("foodType"), "Urgent Request");
                            String description = safe(doc.getString("description"));
                            if (description.isEmpty()) {
                                String qty = extractQuantity(doc.get("quantity"));
                                description = "Urgent food request: " + qty;
                            }
                            String locationStr = safeStringField(doc.get("location"));
                            String address = safe(doc.getString("deliveryAddress"));
                            String loc = !locationStr.isEmpty() ? locationStr : address;
                            LatLng latLng = toLatLng(loc);
                            if (latLng == null) continue;
                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(itemName)
                                    .snippet(description)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                            added++;
                        } catch (Exception e) {
                            Log.e(TAG, "Urgent marker error: " + e.getMessage());
                        }
                    }
                    if (added == 0) Log.d(TAG, "No urgent markers added (string location parse)");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Urgent requests load failed", e);
                    Toast.makeText(this, "Failed to load urgent requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private LatLng toLatLng(String locationOrAddress) {
        if (locationOrAddress == null) return null;
        String val = locationOrAddress.trim();
        if (val.isEmpty() || val.equals(".")) return null;
        if (val.contains(",")) {
            try {
                String[] parts = val.split(",");
                double lat = Double.parseDouble(parts[0].trim());
                double lng = Double.parseDouble(parts[1].trim());
                if (Math.abs(lat) <= 90 && Math.abs(lng) <= 180) return new LatLng(lat, lng);
            } catch (Exception ignored) {}
        }
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> results = geocoder.getFromLocationName(val, 1);
            if (results != null && !results.isEmpty()) {
                Address a = results.get(0);
                return new LatLng(a.getLatitude(), a.getLongitude());
            }
        } catch (IOException io) {
            Log.e(TAG, "Geocoding failed: " + io.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Geocoding error: " + e.getMessage());
        }
        return null;
    }

    private static String extractQuantity(Object q) {
        if (q == null) return "";
        if (q instanceof String) return (String) q;
        if (q instanceof Number) return String.valueOf(q);
        return "";
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String coalesce(String a, String b, String c, String d) { return a != null ? a : (b != null ? b : (c != null ? c : d)); }
    private static String coalesce(String a, String b, String c) { return a != null ? a : (b != null ? b : c); }
    private static String safeStringField(Object o) { return (o instanceof String) ? (String)o : ""; }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                    loadMarkers();
                }
            } else {
                Toast.makeText(this, "Location permission is required to show your location on the map", Toast.LENGTH_LONG).show();
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Location Permission Required")
                            .setMessage("This app needs location permission to show your location on the map.")
                            .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE))
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                }
            }
        }
    }
}