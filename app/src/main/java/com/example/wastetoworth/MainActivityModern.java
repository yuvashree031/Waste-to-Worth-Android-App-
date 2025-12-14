package com.example.wastetoworth;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * MainActivityModern - Modern redesigned main activity with ML and voice features
 * 
 * This activity features:
 * - Personalized welcome message
 * - Mood tracking
 * - Voice search integration
 * - ML-based food classification
 * - Smart donation recommendations
 * - Modern gradient UI design
 */
public class MainActivityModern extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private VoiceSearchHelper voiceSearchHelper;
    private SmartDonationRecommender recommender;
    
    // UI Components
    private TextView txtWelcomeMessage;
    private TextView txtSubtitle;
    private TextView txtTotalDonations;
    private TextView txtMealsFed;
    private TextView txtCO2Saved;
    private EditText edtSearch;
    private ImageView btnVoiceSearch;
    private ImageView btnMenu;
    private CircleImageView imgUserProfile;
    
    // Mood buttons
    private LinearLayout moodHappy;
    private LinearLayout moodCalm;
    private LinearLayout moodRelax;
    private LinearLayout moodFocused;
    
    // Action cards
    private MaterialCardView cardDonate;
    private MaterialCardView cardFoodmap;
    private MaterialCardView cardVolunteer;
    private MaterialCardView cardDonationFeed;
    private MaterialCardView cardMLClassifier;
    private MaterialCardView cardDonationHistory;
    private MaterialCardView cardFeaturedDonation;
    
    // Bottom navigation and FAB
    private BottomNavigationView bottomNavigation;
    private ExtendedFloatingActionButton fabQuickDonate;
    
    // Activity result launcher for voice recognition
    private ActivityResultLauncher<Intent> voiceSearchLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_modern);
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Initialize helpers
        recommender = new SmartDonationRecommender(this);
        
        // Initialize views
        initializeViews();
        
        // Setup voice search
        setupVoiceSearch();
        
        // Setup click listeners
        setupClickListeners();
        
        // Load user data
        loadUserData();
        
        // Load impact statistics
        loadImpactStatistics();
    }
    
    /**
     * Initialize all views
     */
    private void initializeViews() {
        // Header views
        txtWelcomeMessage = findViewById(R.id.txtWelcomeMessage);
        txtSubtitle = findViewById(R.id.txtSubtitle);
        btnMenu = findViewById(R.id.btnMenu);
        imgUserProfile = findViewById(R.id.imgUserProfile);
        
        // Search views
        edtSearch = findViewById(R.id.edtSearch);
        btnVoiceSearch = findViewById(R.id.btnVoiceSearch);
        
        // Mood buttons
        moodHappy = findViewById(R.id.moodHappy);
        moodCalm = findViewById(R.id.moodCalm);
        moodRelax = findViewById(R.id.moodRelax);
        moodFocused = findViewById(R.id.moodFocused);
        
        // Action cards
        cardDonate = findViewById(R.id.cardDonate);
        cardFoodmap = findViewById(R.id.cardFoodmap);
        cardVolunteer = findViewById(R.id.cardVolunteer);
        cardDonationFeed = findViewById(R.id.cardDonationFeed);
        cardMLClassifier = findViewById(R.id.cardMLClassifier);
        cardDonationHistory = findViewById(R.id.cardDonationHistory);
        cardFeaturedDonation = findViewById(R.id.cardFeaturedDonation);
        
        // Impact statistics
        txtTotalDonations = findViewById(R.id.txtTotalDonations);
        txtMealsFed = findViewById(R.id.txtMealsFed);
        txtCO2Saved = findViewById(R.id.txtCO2Saved);
        
        // Bottom navigation and FAB
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabQuickDonate = findViewById(R.id.fabQuickDonate);
    }
    
    /**
     * Setup voice search functionality
     */
    private void setupVoiceSearch() {
        // Initialize voice search launcher
        voiceSearchLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    voiceSearchHelper.processVoiceResult(result.getData());
                }
            }
        );
        
        // Initialize voice search helper
        voiceSearchHelper = new VoiceSearchHelper(this, new VoiceSearchHelper.VoiceSearchListener() {
            @Override
            public void onVoiceSearchResult(String searchQuery) {
                edtSearch.setText(searchQuery);
                Toast.makeText(MainActivityModern.this, 
                             "Voice command: " + searchQuery, 
                             Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onVoiceSearchError(String error) {
                Toast.makeText(MainActivityModern.this, 
                             "Voice search error: " + error, 
                             Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Setup click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Voice search button
        btnVoiceSearch.setOnClickListener(v -> {
            voiceSearchHelper.startVoiceRecognition(voiceSearchLauncher);
        });
        
        // Profile image
        imgUserProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        // Mood buttons
        moodHappy.setOnClickListener(v -> onMoodSelected("Happy"));
        moodCalm.setOnClickListener(v -> onMoodSelected("Calm"));
        moodRelax.setOnClickListener(v -> onMoodSelected("Relax"));
        moodFocused.setOnClickListener(v -> onMoodSelected("Focused"));
        
        // Action cards
        cardDonate.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(this, DonateActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                Toast.makeText(this, "Please login to donate", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
        
        cardFoodmap.setOnClickListener(v -> {
            Intent intent = new Intent(this, FoodMap.class);
            intent.putExtra("latitude", 28.6139);
            intent.putExtra("longitude", 77.2090);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        cardVolunteer.setOnClickListener(v -> {
            startActivity(new Intent(this, VolunteerActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        cardDonationFeed.setOnClickListener(v -> {
            startActivity(new Intent(this, DonationFeedActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        cardMLClassifier.setOnClickListener(v -> {
            startActivity(new Intent(this, MLClassifierActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        cardDonationHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, DonationHistoryActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        // FAB
        fabQuickDonate.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(this, DonateActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                Toast.makeText(this, "Please login to donate", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Bottom navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_feed) {
                startActivity(new Intent(this, DonationFeedActivity.class));
                return true;
            } else if (itemId == R.id.nav_map) {
                Intent intent = new Intent(this, FoodMap.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }
    
    /**
     * Handle mood selection
     */
    private void onMoodSelected(String mood) {
        Toast.makeText(this, "Feeling " + mood + " today! 😊", Toast.LENGTH_SHORT).show();
        
        // Save mood to Firestore
        if (mAuth.getCurrentUser() != null) {
            db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .update("currentMood", mood, "moodTimestamp", System.currentTimeMillis());
        }
    }
    
    /**
     * Load user data and personalize welcome message
     */
    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            
            db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            txtWelcomeMessage.setText("Welcome back, " + name + "!");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Use default message
                });
        }
    }
    
    /**
     * Load and display impact statistics
     */
    private void loadImpactStatistics() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            
            db.collection("donations")
                .whereEqualTo("donorId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalDonations = queryDocumentSnapshots.size();
                    int mealsFed = totalDonations * 3; // Estimate 3 meals per donation
                    int co2Saved = totalDonations * 2; // Estimate 2kg CO2 per donation
                    
                    txtTotalDonations.setText(String.valueOf(totalDonations));
                    txtMealsFed.setText(String.valueOf(mealsFed));
                    txtCO2Saved.setText(String.valueOf(co2Saved));
                })
                .addOnFailureListener(e -> {
                    // Keep default values
                });
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh statistics when returning to activity
        loadImpactStatistics();
    }
}
