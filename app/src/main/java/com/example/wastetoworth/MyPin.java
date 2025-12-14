package com.example.wastetoworth;
// AndroidX imports
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
// Android imports
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
// Google Play Services imports
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
// Firebase imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
/**
 * MyPin Activity
 * 
 * This activity displays a map showing the user's current location and markers
 * for food donation/request locations from Firestore. It implements several interfaces
 * for Google Maps and Location Services integration.
 */
public class MyPin extends AppCompatActivity implements 
    OnMapReadyCallback {
    // Google Maps related variables
    private GoogleMap mMap;                   // Instance of GoogleMap for map operations
    private FusedLocationProviderClient mFusedLocationClient; // Client for Location Services
    private Location mLastLocation;           // Last known location of the device
    private LocationRequest mLocationRequest; // Request for location updates
    private SupportMapFragment mapFragment;   // Fragment that contains the map
    private LocationCallback locationCallback; // Callback for location updates
    // Request code for location permission
    private static final int REQUEST_CODE = 11;
    // Firebase instances
    private FirebaseFirestore fStore;         // Firestore database instance
    private FirebaseAuth fAuth;               // Firebase Authentication instance
    private FirebaseFirestore cloudstorage;    // Additional Firestore instance for data access
    // Tag for logging
    public static final String TAG = "MyPinActivity";
    /**
     * Called when the activity is first created.
     * Initializes the activity, sets up the map fragment, and requests location permissions.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view using the layout defined in activity_my_pin.xml
        setContentView(R.layout.activity_my_pin);
        
        // Initialize Firebase Authentication instance
        fAuth = FirebaseAuth.getInstance();
        
        // Initialize FusedLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Initialize location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        onLocationChanged(location);
                    }
                }
            }
        };
        
        // Get the SupportMapFragment and register for the map ready callback
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        
        // Check if we have location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // If we have permission, load the map asynchronously
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        } else {
            // If we don't have permission, request it from the user
            ActivityCompat.requestPermissions(this, 
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                }, 
                REQUEST_CODE);
        }
    }
    /**
     * Called when the map is ready to be used.
     * Sets up the map and enables the location layer if permission is granted.
     *
     * @param googleMap The GoogleMap object representing the map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(this, 
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                }, 
                REQUEST_CODE);
            return; // Permission not granted, return early
        }
        // Start location updates
        startLocationUpdates();
        // Enable the location layer on the map
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Log.e(TAG, "Error enabling location layer", e);
        }
    }
    /**
     * Builds and configures the GoogleApiClient with the necessary APIs and callbacks.
     * This client is used to interact with Google Play services like Location Services.
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000 // 10 seconds
            ).setMinUpdateIntervalMillis(5000) // 5 seconds
             .build();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                locationCallback,
                Looper.getMainLooper()
            );
            
            // Also get the last known location
            mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        onLocationChanged(location);
                    }
                });
        }
    }
    /**
     * Called when the device's location changes.
     * Updates the map to show the new location and adds a marker.
     *
     * @param location The new location of the device.
     */
    private void onLocationChanged(@NonNull Location location) {
        // Store the last known location
        mLastLocation = location;
        // Show nearby locations from Firestore
        showLocation();
        // Create a LatLng object from the location
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        // Create a marker at the current location
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        // Animate the camera to the current location with zoom level 15
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        // Add the marker to the map and show its info window
        mMap.addMarker(markerOptions).showInfoWindow();
    }
    public void showLocation() {
        this.cloudstorage = FirebaseFirestore.getInstance();
        cloudstorage.collection("donations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());//
                                try {
                                    Object locObj = document.get("location");
                                    GeoPoint location = null;
                                    if (locObj instanceof GeoPoint) {
                                        location = (GeoPoint) locObj;
                                    } else if (locObj instanceof String) {
                                        String locStr = (String) locObj;
                                        if (locStr != null && !locStr.isEmpty()) {
                                            try {
                                                String[] parts = locStr.split(",");
                                                if (parts.length == 2) {
                                                    double lat = Double.parseDouble(parts[0].trim());
                                                    double lng = Double.parseDouble(parts[1].trim());
                                                    location = new GeoPoint(lat, lng);
                                                }
                                            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                                                Log.e(TAG, "Error parsing location string for donation: " + document.getId(), e);
                                                location = null;
                                            }
                                        }
                                    }
                                    String title = document.getString("foodName");
                                    if (title == null || title.trim().isEmpty()) title = document.getString("name");
                                    if (title == null || title.trim().isEmpty()) title = document.getString("category");
                                    if (title == null || title.trim().isEmpty()) title = "Donation";
                                    String type = document.getString("type");
                                    String description = document.getString("description");
                                    String donorId = document.getString("donorId");
                                    String userID = fAuth.getCurrentUser() != null ? fAuth.getCurrentUser().getUid() : null;
                                    if (location != null && userID != null && donorId != null && donorId.equals(userID)) {
                                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        mMap.addMarker(new MarkerOptions().position(latLng).title(title + (type != null ? "(" + type + ")" : "")).snippet(description != null ? description : "" ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing donation document: " + document.getId(), e);
                                }
                            }
                        } else {
                            Log.d(TAG, "Error fetching data: ", task.getException());
                        }
                    }
                });
    }
    private void stopLocationUpdates() {
        if (locationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            boolean granted = false;
            
            // Check if either FINE or COARSE location permission was granted
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if ((permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
                     permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) &&
                    grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }
            
            if (granted) {
                mapFragment.getMapAsync(this);
            }else{
                Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
