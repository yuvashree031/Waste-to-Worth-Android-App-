package com.example.wastetoworth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import android.annotation.SuppressLint;
import com.google.android.gms.maps.model.MarkerOptions;


public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker selectedMarker;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LatLng selectedLocation = new LatLng(0.0, 0.0); // Default to (0,0)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnGetCurrentLocation = findViewById(R.id.btnGetCurrentLocation);
        Button btnConfirmLocation = findViewById(R.id.btnConfirmLocation);

        btnGetCurrentLocation.setOnClickListener(v -> getCurrentLocation());

        btnConfirmLocation.setOnClickListener(v -> {
            if (selectedMarker != null) {
                selectedLocation = selectedMarker.getPosition();
                String locationStr = selectedLocation.latitude + "," + selectedLocation.longitude;
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_location", locationStr);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            }
        });

        // Request location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable location if permissions granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Default marker at (0,0) - will be updated
        selectedMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(0.0, 0.0)).title("Selected Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0.0, 0.0), 10));

        // Allow manual marker placement - for simplicity, use long press
        mMap.setOnMapLongClickListener(latLng -> {
            if (selectedMarker != null) {
                selectedMarker.remove();
            }
            selectedMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            selectedLocation = latLng;
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (selectedMarker != null) {
                    selectedMarker.remove();
                }
                selectedMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                selectedLocation = currentLatLng;
            } else {
                Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }
}