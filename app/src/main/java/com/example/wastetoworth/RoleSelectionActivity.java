package com.example.wastetoworth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.wastetoworth.NGORegistrationActivity;
import com.example.wastetoworth.NGOLoginActivity;

/**
 * RoleSelectionActivity - Allows users to select their role before authentication
 * 
 * This activity presents users with a choice between "People" (regular users) and "NGO" organizations.
 * Based on the selection, users are directed to appropriate login/registration flows.
 */
public class RoleSelectionActivity extends AppCompatActivity {
    
    // UI Components
    private Button btnPeople;
    private Button btnNGO;
    private Button btnRegisterNGO;
    private TextView tvWelcome;
    private TextView tvSubtitle;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Initialize UI components
        initializeViews();
        
        // Set up click listeners
        setupClickListeners();
    }
    
    /**
     * Initialize all view components
     */
    private void initializeViews() {
        btnPeople = findViewById(R.id.btn_people);
        btnNGO = findViewById(R.id.btn_ngo);
        tvWelcome = findViewById(R.id.tv_welcome);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        btnRegisterNGO = findViewById(R.id.btn_register_ngo);
    }
    
    /**
     * Set up click listeners for role selection buttons
     */
    private void setupClickListeners() {
        // People button click - regular users
        btnPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAuth("user");
            }
        });
        
        // NGO button click - organization users
        btnNGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAuth("ngo");
            }
        });
        
        // NGO registration button click
        btnRegisterNGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNGORegistrationAndNavigate();
            }
        });
    }
    
    /**
     * Navigate to appropriate authentication flow based on role selection
     * 
     * @param role The selected role ("people" or "ngo")
     */
    private void navigateToAuth(String role) {
        Intent intent;
        
        if ("ngo".equals(role)) {
            // Navigate to NGO-specific registration/login
            intent = new Intent(RoleSelectionActivity.this, NGOLoginActivity.class);
        } else {
            // Navigate to regular user login (existing Logup activity)
            intent = new Intent(RoleSelectionActivity.this, Logup.class);
        }
        
        // Pass the role information
        intent.putExtra("user_role", role);
        
        // Start the authentication activity
        startActivity(intent);
        
        // Finish this activity to remove it from back stack
        finish();
    }
    
    /**
     * Check if the user is already registered as NGO and navigate accordingly
     */
    private void checkNGORegistrationAndNavigate() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        
        if (userId == null) {
            // User not logged in, go to NGO registration
            Intent intent = new Intent(RoleSelectionActivity.this, NGORegistrationActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return;
        }
        
        // Check if user is already registered as NGO
        db.collection("ngo_users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User is already registered as NGO, go to NGO login
                        Intent intent = new Intent(RoleSelectionActivity.this, NGOLoginActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
                        // User not registered as NGO, go to NGO registration
                        Intent intent = new Intent(RoleSelectionActivity.this, NGORegistrationActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking NGO registration: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    // On error, default to NGO registration
                    Intent intent = new Intent(RoleSelectionActivity.this, NGORegistrationActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });
    }
    
    /**
     * Handle back button press - exit the app since this is the entry point
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Exit the app completely
        finishAffinity();
    }
}