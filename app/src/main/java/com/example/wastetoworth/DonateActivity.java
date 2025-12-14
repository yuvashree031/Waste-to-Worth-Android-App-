package com.example.wastetoworth;
/**
 * Translate the following code into Java follow the following instructions:
 * - Keep the code as close to the original as possible
 * - Make the code functional in Java
 * - The output should be in Java with as few changes as possible
/*************  ✨ Smart Paste 📚  *************/
/*******  a64f1575-fb4b-4158-89a7-010003f7b092  *******/import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.wastetoworth.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DonateActivity extends AppCompatActivity implements OnMapReadyCallback {
    private EditText itemNameEditText, quantityEditText, descriptionEditText, locationEditText;
    private EditText clothesTypeEditText, clothesSizeEditText, clothesGenderEditText;
    private Spinner conditionSpinner, categorySpinner;
    private Button submitButton, locationButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;
    private String currentLocationAddress;
    private boolean isUsingCurrentLocation = false;
    private double currentLocationLat = 0.0;
    private double currentLocationLng = 0.0;
    
    // ML and Voice features
    private VoiceSearchHelper voiceSearchHelper;
    private FoodWasteClassifier foodClassifier;
    private ActivityResultLauncher<Intent> voiceSearchLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ImageView btnVoiceInput;
    private ImageView btnMLScan;
    private TextView txtMLResult;

    // Container views for showing/hiding fields
    private com.google.android.material.textfield.TextInputLayout foodNameContainer;
    private com.google.android.material.textfield.TextInputLayout clothesTypeContainer;
    private com.google.android.material.textfield.TextInputLayout clothesSizeContainer;
    private com.google.android.material.textfield.TextInputLayout clothesGenderContainer;

    // Map related
    private GoogleMap mMap;
    private Marker currentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Initialize views
        itemNameEditText = findViewById(R.id.edit_text_food_name);
        quantityEditText = findViewById(R.id.edit_text_quantity);
        descriptionEditText = findViewById(R.id.edit_text_description);
        locationEditText = findViewById(R.id.edit_text_location);
        
        // Initialize clothes-specific fields
        clothesTypeEditText = findViewById(R.id.edit_text_clothes_type);
        clothesSizeEditText = findViewById(R.id.edit_text_clothes_size);
        clothesGenderEditText = findViewById(R.id.edit_text_clothes_gender);
        
        // Initialize containers
        foodNameContainer = findViewById(R.id.food_name_container);
        clothesTypeContainer = findViewById(R.id.clothes_type_container);
        clothesSizeContainer = findViewById(R.id.clothes_size_container);
        clothesGenderContainer = findViewById(R.id.clothes_gender_container);
        
        conditionSpinner = findViewById(R.id.spinner_condition);
        categorySpinner = findViewById(R.id.spinner_category);
        submitButton = findViewById(R.id.button_submit);
        locationButton = findViewById(R.id.button_location);
        
        // Setup condition spinner
        ArrayAdapter<CharSequence> conditionAdapter = ArrayAdapter.createFromResource(this,
                R.array.food_conditions, android.R.layout.simple_spinner_item);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        conditionSpinner.setAdapter(conditionAdapter);
        
        // Setup category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.donation_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        
        // Set up category spinner listener to show/hide fields
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                updateFormFields(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Default to food fields
                updateFormFields("Food");
            }
        });
        
        // Set click listeners
        submitButton.setOnClickListener(v -> submitDonation());
        locationButton.setOnClickListener(v -> showLocationDialog());
        
        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        if (mapFragment != null) {
            Log.d("DonateActivity", "Map fragment found, requesting map async");
            mapFragment.getMapAsync(this);
        } else {
            Log.e("DonateActivity", "Map fragment not found - check layout ID");
            Toast.makeText(this, "Error: Map fragment not found", Toast.LENGTH_LONG).show();
        }
        
        // Check location permissions
        checkLocationPermission();
        
        // ========== INITIALIZE ML AND VOICE FEATURES ==========
        initializeMLAndVoiceFeatures();
    }
    
    /**
     * Initialize ML classifier and voice search features
     */
    private void initializeMLAndVoiceFeatures() {
        // Initialize ML classifier
        foodClassifier = new FoodWasteClassifier(this);
        
        // Setup voice search launcher
        voiceSearchLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (voiceSearchHelper != null) {
                        voiceSearchHelper.processVoiceResult(result.getData());
                    }
                }
            }
        );
        
        // Setup camera launcher for ML
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        // Display the photo
                        ImageView imgPreview = findViewById(R.id.imgPhotoPreview);
                        if (imgPreview != null) {
                            imgPreview.setImageBitmap(imageBitmap);
                            // Show the preview card
                            findViewById(R.id.photoPreviewCard).setVisibility(View.VISIBLE);
                        }
                        // Classify the food
                        classifyFoodWithML(imageBitmap);
                    }
                }
            }
        );
        
        // Initialize voice search helper
        voiceSearchHelper = new VoiceSearchHelper(this, new VoiceSearchHelper.VoiceSearchListener() {
            @Override
            public void onVoiceSearchResult(String searchQuery) {
                // Fill description with voice input
                if (descriptionEditText != null) {
                    descriptionEditText.setText(searchQuery);
                    Toast.makeText(DonateActivity.this, "Voice input added", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onVoiceSearchError(String error) {
                Toast.makeText(DonateActivity.this, "Voice error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Setup voice input button
        btnVoiceInput = findViewById(R.id.btnVoiceInput);
        if (btnVoiceInput != null) {
            btnVoiceInput.setOnClickListener(v -> {
                if (voiceSearchHelper != null) {
                    voiceSearchHelper.startVoiceRecognition(voiceSearchLauncher);
                }
            });
        }
        
        // Setup ML scan button
        btnMLScan = findViewById(R.id.btnMLScan);
        if (btnMLScan != null) {
            btnMLScan.setOnClickListener(v -> openCameraForML());
        }
        
        // ML result text
        txtMLResult = findViewById(R.id.txtMLResult);
        
        Log.d("DonateActivity", "ML and Voice features initialized successfully");
    }
    
    /**
     * Open camera for ML food classification
     */
    private void openCameraForML() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Classify food using ML Kit
     */
    private void classifyFoodWithML(Bitmap bitmap) {
        if (foodClassifier != null && bitmap != null) {
            foodClassifier.classifyFoodWaste(bitmap, new FoodWasteClassifier.ClassificationCallback() {
                @Override
                public void onClassificationSuccess(FoodWasteClassifier.ClassificationResult result) {
                    runOnUiThread(() -> {
                        // Auto-fill form with ML results
                        if (itemNameEditText != null) {
                            itemNameEditText.setText(result.category);
                        }
                        
                        // Show ML result
                        if (txtMLResult != null) {
                            String resultText = "AI: " + result.category + " (" + result.freshness + ")\\n" + 
                                              result.recommendation;
                            txtMLResult.setText(resultText);
                            txtMLResult.setVisibility(View.VISIBLE);
                        }
                        
                        Toast.makeText(DonateActivity.this, 
                                     "AI detected: " + result.category + " - " + result.freshness, 
                                     Toast.LENGTH_LONG).show();
                    });
                }
                
                @Override
                public void onClassificationError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(DonateActivity.this, 
                                     "ML Error: " + error, 
                                     Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    private void updateFormFields(String category) {
        if (category.equals("Clothes")) {
            // Show clothes fields, hide food field
            foodNameContainer.setVisibility(View.GONE);
            clothesTypeContainer.setVisibility(View.VISIBLE);
            clothesSizeContainer.setVisibility(View.VISIBLE);
            clothesGenderContainer.setVisibility(View.VISIBLE);
        } else {
            // Show food field, hide clothes fields
            foodNameContainer.setVisibility(View.VISIBLE);
            clothesTypeContainer.setVisibility(View.GONE);
            clothesSizeContainer.setVisibility(View.GONE);
            clothesGenderContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mMap = googleMap;
            
            if (mMap == null) {
                Log.e("DonateActivity", "GoogleMap is null");
                Toast.makeText(this, "Error: Map could not be loaded", Toast.LENGTH_LONG).show();
                return;
            }
            
            Log.d("DonateActivity", "Map loaded successfully");
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            
            checkLocationPermission();
            
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                getCurrentLocationAndDisplayOnMap();
            } else {
                // Request location permission if not granted
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            
            // Add a test marker to verify map is working
            LatLng testLocation = new LatLng(28.6139, 77.2090); // Delhi coordinates
            mMap.addMarker(new MarkerOptions()
                    .position(testLocation)
                    .title("Test Location")
                    .snippet("Map is working correctly"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(testLocation, 10));
            
        } catch (Exception e) {
            Log.e("DonateActivity", "Error in onMapReady", e);
            Toast.makeText(this, "Error initializing map: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void getCurrentLocationAndDisplayOnMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    currentLocationLat = location.getLatitude();
                    currentLocationLng = location.getLongitude();
                    // Remove previous marker
                    if (currentLocationMarker != null) {
                        currentLocationMarker.remove();
                    }
                    // Add new marker
                    currentLocationMarker = mMap.addMarker(new MarkerOptions()
                            .position(currentLatLng)
                            .title("Your Current Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    // Get address
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(currentLocationLat, currentLocationLng, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            currentLocationAddress = addresses.get(0).getAddressLine(0);
                            locationEditText.setText(currentLocationAddress);
                            isUsingCurrentLocation = true;
                        }
                    } catch (IOException e) {
                        Toast.makeText(this, "Error getting address", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Fallback to default location if no last location
                    LatLng defaultLocation = new LatLng(28.6139, 77.2090);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
                }
            });
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Location Option");
        
        String[] options = {"Use Current Location", "Enter Location Manually"};
        
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                getCurrentLocation();
            } else {
                locationEditText.setText("");
                locationEditText.setHint("Enter location manually");
                isUsingCurrentLocation = false;
                currentLocationLat = 0.0;
                currentLocationLng = 0.0;
                // Clear marker
                if (currentLocationMarker != null) {
                    currentLocationMarker.remove();
                    currentLocationMarker = null;
                }
            }
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentLocationLat = location.getLatitude();
                                currentLocationLng = location.getLongitude();
                                Geocoder geocoder = new Geocoder(DonateActivity.this, Locale.getDefault());
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(
                                            location.getLatitude(), location.getLongitude(), 1);
                                    if (addresses != null && !addresses.isEmpty()) {
                                        Address address = addresses.get(0);
                                        currentLocationAddress = address.getAddressLine(0);
                                        locationEditText.setText(currentLocationAddress);
                                        isUsingCurrentLocation = true;
                                        // Update map
                                        if (mMap != null) {
                                            LatLng currentLatLng = new LatLng(currentLocationLat, currentLocationLng);
                                            if (currentLocationMarker != null) {
                                                currentLocationMarker.remove();
                                            }
                                            currentLocationMarker = mMap.addMarker(new MarkerOptions()
                                                    .position(currentLatLng)
                                                    .title("Your Current Location")
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                                        }
                                    }
                                } catch (IOException e) {
                                    Toast.makeText(DonateActivity.this, "Error getting location", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // If last location is null, request location updates
                                if (ActivityCompat.checkSelfPermission(DonateActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    LocationRequest locationRequest = LocationRequest.create();
                                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                    locationRequest.setInterval(10000);
                                    locationRequest.setFastestInterval(5000);
                                    
                                    fusedLocationClient.requestLocationUpdates(locationRequest, 
                                        new LocationCallback() {
                                            @Override
                                            public void onLocationResult(LocationResult locationResult) {
                                                if (locationResult != null && locationResult.getLastLocation() != null) {
                                                    Location location = locationResult.getLastLocation();
                                                    currentLocationLat = location.getLatitude();
                                                    currentLocationLng = location.getLongitude();
                                                    Geocoder geocoder = new Geocoder(DonateActivity.this, Locale.getDefault());
                                                    try {
                                                        List<Address> addresses = geocoder.getFromLocation(
                                                                location.getLatitude(), location.getLongitude(), 1);
                                                        if (addresses != null && !addresses.isEmpty()) {
                                                            Address address = addresses.get(0);
                                                            currentLocationAddress = address.getAddressLine(0);
                                                            locationEditText.setText(currentLocationAddress);
                                                            isUsingCurrentLocation = true;
                                                            // Update map
                                                            if (mMap != null) {
                                                                LatLng currentLatLng = new LatLng(currentLocationLat, currentLocationLng);
                                                                if (currentLocationMarker != null) {
                                                                    currentLocationMarker.remove();
                                                                }
                                                                currentLocationMarker = mMap.addMarker(new MarkerOptions()
                                                                        .position(currentLatLng)
                                                                        .title("Your Current Location")
                                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                                                            }
                                                            fusedLocationClient.removeLocationUpdates(this);
                                                        }
                                                    } catch (IOException e) {
                                                        Toast.makeText(DonateActivity.this, "Error getting location", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        }, null);
                                } else {
                                    Toast.makeText(DonateActivity.this, "Location permission required for updates", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            checkLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    mMap.setMyLocationEnabled(true);
                    getCurrentLocationAndDisplayOnMap();
                }
                // Re-check location after permission granted
                checkLocationPermission();
            } else {
                Toast.makeText(this, "Location permission is required to use current location feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void submitDonation() {
        String quantityStr = quantityEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String condition = conditionSpinner.getSelectedItem().toString();
        String category = categorySpinner.getSelectedItem().toString();
        
        // Validate based on category
        if (category.equals("Clothes")) {
            String clothesType = clothesTypeEditText.getText().toString().trim();
            String clothesSize = clothesSizeEditText.getText().toString().trim();
            String clothesGender = clothesGenderEditText.getText().toString().trim();
            if (clothesType.isEmpty() || clothesSize.isEmpty() || clothesGender.isEmpty() || quantityStr.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // Default to item validation
            String itemName = itemNameEditText.getText().toString().trim();
            if (itemName.isEmpty() || quantityStr.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    
        double quantity;
        try {
            quantity = Double.parseDouble(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid quantity", Toast.LENGTH_SHORT).show();
            return;
        }
    
        if (quantity <= 0) {
            Toast.makeText(this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }
    
        String location;
        boolean isCurrentLocation = isUsingCurrentLocation;
        if (isCurrentLocation) {
            if (currentLocationLat == 0.0 || currentLocationLng == 0.0) {
                Toast.makeText(this, "Please fetch current location first", Toast.LENGTH_SHORT).show();
                return;
            }
            location = currentLocationLat + "," + currentLocationLng;
        } else {
            location = locationEditText.getText().toString().trim();
            if (location.isEmpty()) {
                Toast.makeText(this, "Please provide location", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    
        submitButton.setEnabled(false);
        submitButton.setText("Submitting...");
    
        Map<String, Object> donationData = new HashMap<>();
        donationData.put("quantity", quantity);
        donationData.put("description", description);
        donationData.put("location", location);
        donationData.put("condition", condition);
        donationData.put("type", category);
        donationData.put("donorId", mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "");
        donationData.put("timestamp", System.currentTimeMillis());

        // Add category-specific data
        if (category.equals("Clothes")) {
            donationData.put("clothesType", clothesTypeEditText.getText().toString().trim());
            donationData.put("clothesSize", clothesSizeEditText.getText().toString().trim());
            donationData.put("clothesGender", clothesGenderEditText.getText().toString().trim());
        } else {
            // Default to item
            donationData.put("itemName", itemNameEditText.getText().toString().trim());
        }

        db.collection("donations").add(donationData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(DonateActivity.this, "Donation submitted successfully!", Toast.LENGTH_SHORT).show();
                    // Clear fields
                    itemNameEditText.setText("");
                    quantityEditText.setText("");
                    descriptionEditText.setText("");
                    locationEditText.setText("");
                    conditionSpinner.setSelection(0);
                    categorySpinner.setSelection(0);
                    isUsingCurrentLocation = false;
                    currentLocationLat = 0.0;
                    currentLocationLng = 0.0;
                    if (currentLocationMarker != null) {
                        currentLocationMarker.remove();
                        currentLocationMarker = null;
                    }
                    submitButton.setEnabled(true);
                    submitButton.setText("Submit Donation");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DonateActivity.this, "Error submitting donation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    submitButton.setEnabled(true);
                    submitButton.setText("Submit Donation");
                });
    }
}
