package com.example.wastetoworth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.card.MaterialCardView;
import java.io.IOException;

/**
 * MLClassifierActivity - Activity for ML-based food waste classification
 * 
 * This activity allows users to take or select photos of food items,
 * which are then analyzed using ML Kit to determine if they're suitable for donation.
 */
public class MLClassifierActivity extends AppCompatActivity {
    
    private static final int CAMERA_PERMISSION_CODE = 100;
    
    private ImageView imgPreview;
    private TextView txtCategory;
    private TextView txtFreshness;
    private TextView txtConfidence;
    private TextView txtRecommendation;
    private TextView txtDetectedLabels;
    private ProgressBar progressBar;
    private MaterialCardView cardResult;
    private Button btnCamera;
    private Button btnGallery;
    private Button btnDonateThis;
    
    private FoodWasteClassifier classifier;
    private Bitmap selectedImage;
    
    // Activity result launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ml_classifier);
        
        // Initialize views
        initializeViews();
        
        // Initialize ML classifier
        classifier = new FoodWasteClassifier(this);
        
        // Setup activity result launchers
        setupActivityLaunchers();
        
        // Setup click listeners
        setupClickListeners();
    }
    
    /**
     * Initialize all views
     */
    private void initializeViews() {
        imgPreview = findViewById(R.id.imgPreview);
        txtCategory = findViewById(R.id.txtCategory);
        txtFreshness = findViewById(R.id.txtFreshness);
        txtConfidence = findViewById(R.id.txtConfidence);
        txtRecommendation = findViewById(R.id.txtRecommendation);
        txtDetectedLabels = findViewById(R.id.txtDetectedLabels);
        progressBar = findViewById(R.id.progressBar);
        cardResult = findViewById(R.id.cardResult);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnDonateThis = findViewById(R.id.btnDonateThis);
        
        // Hide result card initially
        cardResult.setVisibility(View.GONE);
    }
    
    /**
     * Setup activity result launchers for camera and gallery
     */
    private void setupActivityLaunchers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        selectedImage = (Bitmap) extras.get("data");
                        imgPreview.setImageBitmap(selectedImage);
                        classifyImage(selectedImage);
                    }
                }
            }
        );
        
        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        imgPreview.setImageBitmap(selectedImage);
                        classifyImage(selectedImage);
                    } catch (IOException e) {
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }
    
    /**
     * Setup click listeners for buttons
     */
    private void setupClickListeners() {
        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());
        btnDonateThis.setOnClickListener(v -> proceedToDonate());
    }
    
    /**
     * Open camera to take a photo
     */
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(cameraIntent);
        }
    }
    
    /**
     * Open gallery to select an image
     */
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }
    
    /**
     * Classify the selected image using ML
     */
    private void classifyImage(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        cardResult.setVisibility(View.GONE);
        
        // Classify using ML
        classifier.classifyFoodWaste(bitmap, new FoodWasteClassifier.ClassificationCallback() {
            @Override
            public void onClassificationSuccess(FoodWasteClassifier.ClassificationResult result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    displayResults(result);
                });
            }
            
            @Override
            public void onClassificationError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MLClassifierActivity.this, 
                                 "Classification error: " + error, 
                                 Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * Display classification results
     */
    private void displayResults(FoodWasteClassifier.ClassificationResult result) {
        cardResult.setVisibility(View.VISIBLE);
        
        // Set category
        txtCategory.setText("Category: " + result.category);
        
        // Set freshness with color coding
        txtFreshness.setText("Freshness: " + result.freshness);
        int freshnessColor;
        switch (result.freshness) {
            case "Fresh":
                freshnessColor = ContextCompat.getColor(this, R.color.gradient_green_end);
                break;
            case "Good":
                freshnessColor = ContextCompat.getColor(this, R.color.gradient_blue_end);
                break;
            case "Fair":
                freshnessColor = ContextCompat.getColor(this, R.color.mood_relax);
                break;
            default:
                freshnessColor = ContextCompat.getColor(this, R.color.error);
                break;
        }
        txtFreshness.setTextColor(freshnessColor);
        
        // Set confidence
        txtConfidence.setText(String.format("Confidence: %.1f%%", result.confidence * 100));
        
        // Set recommendation
        txtRecommendation.setText(result.recommendation);
        
        // Set detected labels
        if (!result.detectedLabels.isEmpty()) {
            StringBuilder labels = new StringBuilder("Detected: ");
            for (int i = 0; i < Math.min(3, result.detectedLabels.size()); i++) {
                labels.append(result.detectedLabels.get(i));
                if (i < Math.min(2, result.detectedLabels.size() - 1)) {
                    labels.append(", ");
                }
            }
            txtDetectedLabels.setText(labels.toString());
            txtDetectedLabels.setVisibility(View.VISIBLE);
        } else {
            txtDetectedLabels.setVisibility(View.GONE);
        }
        
        // Show/hide donate button based on donatability
        btnDonateThis.setVisibility(result.donatable ? View.VISIBLE : View.GONE);
    }
    
    /**
     * Proceed to donation screen with classified item
     */
    private void proceedToDonate() {
        Intent intent = new Intent(this, DonateActivity.class);
        intent.putExtra("from_ml_classifier", true);
        if (txtCategory.getText() != null) {
            String category = txtCategory.getText().toString().replace("Category: ", "");
            intent.putExtra("food_category", category);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (classifier != null) {
            classifier.close();
        }
    }
}
