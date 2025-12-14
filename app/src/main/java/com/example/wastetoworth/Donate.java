package com.example.wastetoworth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.wastetoworth.R;
import com.example.wastetoworth.data.DonationRepository;
import com.example.wastetoworth.databinding.ActivityDonateNewBinding;
import com.example.wastetoworth.viewmodel.DonateFormState;
import com.example.wastetoworth.viewmodel.DonateViewModel;
import com.example.wastetoworth.viewmodel.DonationSubmissionState;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.ref.WeakReference;

/**
 * Activity for handling food donation submissions with location tracking.
 * Implements OnMapReadyCallback for Google Maps integration.
 */
public class Donate extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "DonateActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float DEFAULT_ZOOM_LEVEL = 15f;

    private ActivityDonateNewBinding binding;

    // Google Maps and Location
    private GoogleMap mMap;
    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private SupportMapFragment mapFragment;
    private MyLocationCallback locationCallback;
    private Marker currentMarker;

    // UI elements
    private EditText mFullName, mFoodItem, mDescription, mPhone, mQuantity;
    private Button mSubmitBtn;
    private ProgressBar progressBar;
    private RadioGroup locationChoiceRadioGroup;
    private RadioButton radioCurrentLocation;
    private RadioButton radioDifferentLocation;
    private View rootView;

    // ViewModel
    private DonateViewModel viewModel;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String userID;

    private boolean isLocationPermissionGranted = false;

    // Permission launcher
    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    isLocationPermissionGranted = true;
                    initializeMapIfNeeded();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    isLocationPermissionGranted = true;
                    initializeMapIfNeeded();
                } else {
                    showLocationPermissionDenied();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDonateNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        rootView = binding.getRoot();

        DonationRepository repository = new DonationRepository();
        ViewModelProvider.Factory factory = new ViewModelProvider.AndroidViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(DonateViewModel.class);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userID = currentUser.getUid();
        } else {
            redirectToLogin();
            return;
        }

        initializeViews();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new MyLocationCallback(this);

        setupTextChangeListeners();
        setupLocationChoiceListener();

        mSubmitBtn.setOnClickListener(v -> submitDonation());

        viewModel.getFormState().observe(this, this::updateFormState);
        viewModel.getSubmissionState().observe(this, this::handleSubmissionState);
        viewModel.getShowMessage().observe(this, event -> {
            if (event != null && !event.hasBeenHandled()) {
                showMessage(event.peekContent());
            }
        });

        checkLocationPermission();
        initializeMapFragment();
    }

    private void setupLocationChoiceListener() {
        locationChoiceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioCurrentLocation) {
                // Use current location - enable location updates and show current location
                if (mMap != null) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            mMap.setMyLocationEnabled(true);
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        } catch (SecurityException e) {
                            Log.e(TAG, "Error enabling location", e);
                        }
                    }
                }
                startLocationUpdates();
            } else if (checkedId == R.id.radioDifferentLocation) {
                // Use different location - allow user to tap on map to select location
                if (mMap != null) {
                    mMap.setMyLocationEnabled(false);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    // Clear any existing markers
                    mMap.clear();
                    // Add a click listener for map selection
                    mMap.setOnMapClickListener(latLng -> {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                        mLastLocation = new Location("Selected");
                        mLastLocation.setLatitude(latLng.latitude);
                        mLastLocation.setLongitude(latLng.longitude);
                        updateViewModelWithFormData();
                    });
                }
                stopLocationUpdates();
            }
        });
    }

    private void setupTextChangeListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) { updateViewModelWithFormData(); }
        };
        mFullName.addTextChangedListener(textWatcher);
        mFoodItem.addTextChangedListener(textWatcher);
        mPhone.addTextChangedListener(textWatcher);
        mQuantity.addTextChangedListener(textWatcher);
        mDescription.addTextChangedListener(textWatcher);
    }

    private void updateViewModelWithFormData() {
        viewModel.setFullName(mFullName.getText().toString());
        viewModel.setFoodItem(mFoodItem.getText().toString());
        viewModel.setPhone(mPhone.getText().toString());
        viewModel.setQuantity(mQuantity.getText().toString());
        viewModel.setDescription(mDescription.getText().toString());
        if (mLastLocation != null) {
            String locationStr = String.valueOf(mLastLocation.getLatitude()) + "," + String.valueOf(mLastLocation.getLongitude());
            try {
                String[] parts = locationStr.split(",");
                double lat = Double.parseDouble(parts[0].trim());
                double lng = Double.parseDouble(parts[1].trim());
                viewModel.setLocation(lat, lng);
            } catch (Exception e) {
                // Fallback to zeros if parsing fails
                viewModel.setLocation(0.0, 0.0);
            }
        }
    }

    private void updateFormState(DonateFormState formState) {
        if (formState == null) return;
        mSubmitBtn.setEnabled(formState.isDataValid());
        if (formState.getFullNameError() != null) mFullName.setError(formState.getFullNameError());
        if (formState.getFoodItemError() != null) mFoodItem.setError(formState.getFoodItemError());
        if (formState.getPhoneError() != null) mPhone.setError(formState.getPhoneError());
        if (formState.getQuantityError() != null) mQuantity.setError(formState.getQuantityError());
    }

    private void handleSubmissionState(DonationSubmissionState submissionState) {
        if (submissionState == null) return;
        progressBar.setVisibility(submissionState.isLoading() ? View.VISIBLE : View.GONE);
        boolean isEnabled = !submissionState.isLoading();
        mFullName.setEnabled(isEnabled);
        mFoodItem.setEnabled(isEnabled);
        mPhone.setEnabled(isEnabled);
        mQuantity.setEnabled(isEnabled);
        mDescription.setEnabled(isEnabled);
        mSubmitBtn.setEnabled(isEnabled);

        if (!submissionState.isLoading()) {
            if (submissionState.isSuccess()) {
                showSuccessDialog("Success", "Donation submitted successfully!");
                clearForm();
            } else if (submissionState.getError() != null) {
                showErrorDialog("Error", submissionState.getError());
            }
        }
    }

    private void submitDonation() {
        updateViewModelWithFormData();
        viewModel.submitDonation();
    }

    private void showMessage(String message) {
        if (!isFinishing() && !isDestroyed()) {
            Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void clearForm() {
        mFullName.setText("");
        mFoodItem.setText("");
        mPhone.setText("");
        mQuantity.setText("");
        mDescription.setText("");
        // Reset location choice to current location
        radioCurrentLocation.setChecked(true);
    }

    private void initializeViews() {
        mFullName = binding.donorname;
        mFoodItem = binding.fooditem;
        mPhone = binding.phone;
        mQuantity = binding.quantity;
        mDescription = binding.description;
        mSubmitBtn = binding.submit;
        progressBar = binding.progressBar;
        locationChoiceRadioGroup = binding.locationChoiceRadioGroup;
        radioCurrentLocation = binding.radioCurrentLocation;
        radioDifferentLocation = binding.radioDifferentLocation;
    }

    private void initializeMapFragment() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Check current location choice
        boolean useCurrentLocation = radioCurrentLocation.isChecked();
        
        if (useCurrentLocation) {
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    mMap.setMyLocationEnabled(true);
                    getLastKnownLocation();
                } catch (SecurityException e) {
                    Log.e(TAG, "Error enabling location", e);
                }
            }
        } else {
            // Different location mode - allow map clicking
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setOnMapClickListener(latLng -> {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                mLastLocation = new Location("Selected");
                mLastLocation.setLatitude(latLng.latitude);
                mLastLocation.setLongitude(latLng.longitude);
                updateViewModelWithFormData();
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        mLastLocation = location;
                        updateMapWithLocation(location);
                        startLocationUpdates();
                    } else {
                        startLocationUpdates();
                    }
                })
                .addOnFailureListener(e -> startLocationUpdates());
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    private void initializeMapIfNeeded() {
        if (mMap == null && mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void showLocationPermissionDenied() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.location_permission_required)
                .setMessage(R.string.location_permission_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted, requesting permission...");
            checkLocationPermission();
            return;
        }

        try {
            mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setMinUpdateIntervalMillis(5000)
                    .build();

            if (mFusedLocationClient != null) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, null);
                Log.d(TAG, "Started location updates");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException in startLocationUpdates: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error starting location updates: " + e.getMessage());
        }
    }

    private void stopLocationUpdates() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void updateMapWithLocation(Location location) {
        if (mMap != null && location != null) {
            try {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (currentMarker != null) {
                    currentMarker.remove();
                }
                currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentLatLng)
                    .title(getString(R.string.your_location)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM_LEVEL));
                Log.d(TAG, "Map updated with location: " + currentLatLng.latitude + ", " + currentLatLng.longitude);
            } catch (Exception e) {
                Log.e(TAG, "Error updating map with location: " + e.getMessage());
            }
        }
    }

    private void showErrorDialog(String title, String message) {
        if (!isFinishing() && !isDestroyed()) {
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    private void showSuccessDialog(String title, String message) {
        if (!isFinishing() && !isDestroyed()) {
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, (d, w) -> clearForm())
                    .show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (radioCurrentLocation.isChecked()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (radioCurrentLocation.isChecked() && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mFusedLocationClient != null) {
                mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            updateMapWithLocation(location);
                        } else {
                            // If last location is not available, start location updates
                            startLocationUpdates();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting last location", e);
                        startLocationUpdates();
                    });
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (radioCurrentLocation.isChecked()) {
            stopLocationUpdates();
        }
        if (mMap != null) {
            mMap.clear();
            mMap = null;
        }
        locationCallback = null;
        currentMarker = null;
        super.onDestroy();
    }

    private void redirectToLogin() {
        // TODO: start LoginActivity
        finish();
    }

    /** Location callback */
    private static class MyLocationCallback extends LocationCallback {
        private final WeakReference<Donate> activityReference;
        MyLocationCallback(Donate activity) { this.activityReference = new WeakReference<>(activity); }

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            Donate activity = activityReference.get();
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
            Location location = locationResult.getLastLocation();
            if (location != null) {
                activity.mLastLocation = location;
                activity.updateMapWithLocation(location);
            }
        }
    }
}
