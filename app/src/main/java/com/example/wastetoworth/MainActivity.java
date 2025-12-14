package com.example.wastetoworth;
// Android imports
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
// AndroidX imports
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
// Activity imports
// Google Material Design imports
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
// Firebase imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * MainActivity - The main dashboard/landing page of the application
 * 
 * This activity serves as the central hub where users can access all the main features
 * of the application including donation, receiving food, viewing food map, volunteering,
 * and accessing the donation feed. It also handles user authentication state and navigation
 * between different features of the app.
 */
public class MainActivity extends AppCompatActivity {
    // Firebase Authentication instance for user management
    private FirebaseAuth mAuth;
    // Firestore database instance for data operations
    private FirebaseFirestore db;
    // Voice search helper
    private VoiceSearchHelper voiceSearchHelper;
    // Activity result launcher for voice recognition
    private ActivityResultLauncher<Intent> voiceSearchLauncher;

    /**
     * Called when the activity is first created.
     * Initializes the UI components and sets up click listeners for navigation.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view using the layout defined in activity_main.xml
        setContentView(R.layout.activity_main);
        // Initialize Firebase Authentication and Firestore instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Setup the material toolbar at the top of the activity
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Initialize all card views from the layout
        CardView donateCard = findViewById(R.id.cardDonate);
        CardView urgentRequestCard = findViewById(R.id.cardUrgentRequest);
        CardView foodMapCard = findViewById(R.id.cardFoodmap);
        CardView donationFeedCard = findViewById(R.id.cardDonationFeed);
        CardView donationHistoryCard = findViewById(R.id.cardDonationHistory);
        CardView volunteerCard = findViewById(R.id.cardVolunteer);
        CardView aboutCard = findViewById(R.id.cardAboutus);
        CardView profileCard = findViewById(R.id.cardProfile);
        CardView settingsCard = findViewById(R.id.cardSettings);
        CardView helpCard = findViewById(R.id.cardHelp);
        CardView logoutCard = findViewById(R.id.cardLogout);
        // Set click listener for Donate card
        donateCard.setOnClickListener(v -> {
            // Check if user is logged in before allowing donation
            if (mAuth.getCurrentUser() != null) {
                // Navigate directly to Donate activity with category selection via spinner
                startActivity(new Intent(MainActivity.this, DonateActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                // Prompt user to login and navigate to login screen
                Toast.makeText(this, "Please login to donate", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        // Set click listener for Urgent Request card
        urgentRequestCard.setOnClickListener(v -> {
            // Check if user is logged in before allowing to make urgent requests
            if (mAuth.getCurrentUser() != null) {
                // Navigate to Urgent Request activity with slide animation
                startActivity(new Intent(MainActivity.this, UrgentRequestActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                // Prompt user to login and navigate to login screen
                Toast.makeText(this, "Please login to make urgent requests", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        // Set click listener for Food Map card
        foodMapCard.setOnClickListener(v -> {
            // Navigate to FoodMapActivity with slide animation
            // No login required to view the food map
            Intent intent = new Intent(MainActivity.this, FoodMap.class);
            // Pass default location (New Delhi) for the map center
            intent.putExtra("latitude", 28.6139);
            intent.putExtra("longitude", 77.2090);
            intent.putExtra("donorName", "Food Map Center");
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        // Set click listener for Volunteer card
        volunteerCard.setOnClickListener(v -> {
            // Check if user is logged in before allowing volunteer access
            if (mAuth.getCurrentUser() != null) {
                // Navigate to VolunteerActivity with slide animation
                startActivity(new Intent(MainActivity.this, VolunteerActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                // Prompt user to login and navigate to login screen
                Toast.makeText(this, "Please login to volunteer", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        // Set click listener for About Us card
        aboutCard.setOnClickListener(v -> {
            // Navigate to AboutActivity with slide animation
            // No login required to view about information
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        // Set click listener for Donation Feed card
        donationFeedCard.setOnClickListener(v -> {
            // Navigate to DonationFeedActivity with slide animation
            // No login required to view donation feed
            startActivity(new Intent(MainActivity.this, DonationFeedActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        // Set click listener for Donation History card
        donationHistoryCard.setOnClickListener(v -> {
            // Check if user is logged in before allowing donation history access
            if (mAuth.getCurrentUser() != null) {
                // Navigate to DonationHistoryActivity with slide animation
                startActivity(new Intent(MainActivity.this, DonationHistoryActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                // Prompt user to login and navigate to login screen
                Toast.makeText(this, "Please login to view your donation history", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        // Set click listener for Profile card
        profileCard.setOnClickListener(v -> {
            // Check if user is logged in before allowing profile access
            if (mAuth.getCurrentUser() != null) {
                // Navigate to Profile activity with slide animation
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                // Prompt user to login and navigate to login screen
                Toast.makeText(this, "Please login to view your profile", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        
        // Set click listener for Settings card
        settingsCard.setOnClickListener(v -> {
            // Navigate to Settings activity with slide animation
            // No login required to view settings
            try {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                // Verify the activity is declared in manifest before starting
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    Toast.makeText(this, "Settings activity not available", Toast.LENGTH_SHORT).show();
                    Log.e("MainActivity", "SettingsActivity not found in manifest");
                }
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Settings activity not available", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "ActivityNotFoundException launching SettingsActivity", e);
            }
        });
        
        // Set click listener for Help card
        helpCard.setOnClickListener(v -> {
            // Navigate to Help activity with slide animation
            // No login required to view help
            startActivity(new Intent(MainActivity.this, HelpActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        // Set click listener for Logout card
        logoutCard.setOnClickListener(v -> {
            // Sign out the current user from Firebase Authentication
            mAuth.signOut();
            // Show logout success message
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            // Create intent to navigate to login screen
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            // Clear the activity stack to prevent going back to MainActivity after logout
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // Finish the current activity
            finish();
        });
        // Set click listener for Floating Action Button (FAB)
        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            // Check if user is logged in before allowing quick donation
            if (mAuth.getCurrentUser() != null) {
                // Navigate directly to Donate activity with category selection via spinner
                startActivity(new Intent(MainActivity.this, DonateActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
            } else {
                // Prompt user to login and navigate to login screen
                Toast.makeText(this, "Please login to donate", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        
        // ========== NEW FEATURES: Voice Search & ML Classifier ==========
        
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
        
        // Initialize voice search helper
        voiceSearchHelper = new VoiceSearchHelper(this, new VoiceSearchHelper.VoiceSearchListener() {
            @Override
            public void onVoiceSearchResult(String searchQuery) {
                Toast.makeText(MainActivity.this, "Voice: " + searchQuery, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onVoiceSearchError(String error) {
                Toast.makeText(MainActivity.this, "Voice error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Add voice search button to toolbar (if exists in layout)
        ImageView btnVoiceSearch = findViewById(R.id.btnVoiceSearch);
        if (btnVoiceSearch != null) {
            btnVoiceSearch.setOnClickListener(v -> {
                if (voiceSearchHelper != null) {
                    voiceSearchHelper.startVoiceRecognition(voiceSearchLauncher);
                }
            });
        }
        
        // Add ML Classifier card if exists
        CardView mlClassifierCard = findViewById(R.id.cardMLClassifier);
        if (mlClassifierCard != null) {
            mlClassifierCard.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, MLClassifierActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
        
        // Load and display user's name in welcome message
        loadUserName();
    }
    
    /**
     * Loads the current user's name from Firestore and updates the welcome message
     */
    private void loadUserName() {
        // Get the welcome text view
        android.widget.TextView welcomeText = findViewById(R.id.welcomeText);
        
        // Check if user is logged in
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            
            // First, try to get display name from Firebase Auth
            String displayName = mAuth.getCurrentUser().getDisplayName();
            if (displayName != null && !displayName.isEmpty() && !displayName.equals("user")) {
                welcomeText.setText("Welcome back, " + displayName + "!");
                return;
            }
            
            // If no display name, fetch user data from Firestore
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get the user's name from the document
                        String userName = documentSnapshot.getString("name");
                        
                        Log.d("MainActivity", "Firestore user data: " + documentSnapshot.getData());
                        Log.d("MainActivity", "User name from Firestore: " + userName);
                        
                        if (userName != null && !userName.isEmpty() && !userName.equals("user")) {
                            // Update the welcome message with the user's name
                            welcomeText.setText("Welcome back, " + userName + "!");
                        } else {
                            // Fallback if name is not available or is "user"
                            welcomeText.setText("Welcome back!");
                        }
                    } else {
                        // Document doesn't exist, use default message
                        Log.d("MainActivity", "User document does not exist in Firestore");
                        welcomeText.setText("Welcome back!");
                    }
                })
                .addOnFailureListener(e -> {
                    // Error loading user data, use default message
                    Log.e("MainActivity", "Error loading user name from Firestore", e);
                    welcomeText.setText("Welcome back!");
                });
        } else {
            // User not logged in, use default message
            welcomeText.setText("Welcome back!");
        }
    }
    /**
     * Called when the activity is becoming visible to the user.
     * Checks if the user is authenticated and redirects to login if not.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        if (mAuth.getCurrentUser() == null) {
            // Create intent to navigate to login screen
            Intent intent = new Intent(this, Logup.class);
            // Clear the activity stack to prevent going back to MainActivity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // Finish the current activity
            finish();
        }
    }
}