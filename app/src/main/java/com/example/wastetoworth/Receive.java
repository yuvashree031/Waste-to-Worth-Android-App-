package com.example.wastetoworth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class Receive extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback locationCallback;
    private Location mLastLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private EditText mFullName, mDescription, mQuantity;
    private Button mSubmitBtn;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    public static final String TAG = "ReceiveActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        
        // Initialize Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        
        // Initialize views
        mFullName = findViewById(R.id.receivername);
        mDescription = findViewById(R.id.description);
        mQuantity = findViewById(R.id.quantity);
        mSubmitBtn = findViewById(R.id.submit);
        
        // Initialize location client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Initialize location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    onLocationChanged(location);
                }
            }
        };
        
        // Setup submit button
        setupSubmitButton();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Check for location permissions
        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            getLastKnownLocation();
        }
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        mFusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    mLastLocation = location;
                    updateMapWithLocation(location);
                    startLocationUpdates();
                } else {
                    startLocationUpdates();
                }
            });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mLocationRequest = new LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000) // 10 seconds
            .setMinUpdateIntervalMillis(5000) // 5 seconds
            .build();

        if (checkLocationPermission()) {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                locationCallback,
                Looper.getMainLooper()
            );
        }
    }

    private void updateMapWithLocation(Location location) {
        if (mMap == null) return;
        
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
            .position(latLng)
            .title("You are here")
        );
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private void onLocationChanged(Location location) {
        if (location != null) {
            mLastLocation = location;
            updateMapWithLocation(location);
        }
    }

    private void setupSubmitButton() {
        mSubmitBtn.setOnClickListener(v -> {
            String fullname = mFullName.getText().toString().trim();
            String description = mDescription.getText().toString().trim();
            String quantityStr = mQuantity.getText().toString().trim();
            String type = "urgent_request"; // this Receive flow posts as urgent request-like entry
            
            // Validate input
            if (TextUtils.isEmpty(fullname)) {
                mFullName.setError("Name is required");
                return;
            }
            
            if (TextUtils.isEmpty(description)) {
                mDescription.setError("Description is required");
                return;
            }
            
            if (TextUtils.isEmpty(quantityStr)) {
                mQuantity.setError("Quantity is required");
                return;
            }
            
            double quantity;
            try {
                quantity = Double.parseDouble(quantityStr);
                if (quantity <= 0) {
                    mQuantity.setError("Quantity must be greater than 0");
                    return;
                }
            } catch (NumberFormatException e) {
                mQuantity.setError("Please enter a valid number");
                return;
            }
            
            if (mLastLocation == null) {
                Toast.makeText(this, "Location not available. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (fAuth.getCurrentUser() == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }
            
            userID = fAuth.getCurrentUser().getUid();
            
            // Create data to save
            CollectionReference collectionReference = fStore.collection("donations");
            GeoPoint geoPoint = new GeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            
            Map<String, Object> donation = new HashMap<>();
            donation.put("timestamp", FieldValue.serverTimestamp());
            donation.put("name", fullname);
            donation.put("description", description);
            donation.put("quantity", String.valueOf(quantity));
            donation.put("location", geoPoint);
            donation.put("donorId", userID);
            donation.put("type", type);
            donation.put("status", "available");
            donation.put("category", "Food");
            
            // Save to Firestore
            collectionReference.add(donation)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Receive.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                                  Intent.FLAG_ACTIVITY_CLEAR_TASK | 
                                  Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving data", e);
                });
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (checkLocationPermission()) {
                        mMap.setMyLocationEnabled(true);
                        getLastKnownLocation();
                    }
                }
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}