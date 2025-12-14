package com.example.wastetoworth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * ProfileActivity - Displays user profile information
 * 
 * This activity shows the user's personal information including name, email,
 * and other profile details. It also provides options to edit profile and
 * view account statistics.
 */
public class ProfileActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView textName, textEmail, textPhone, textAddress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        try {
            // Initialize Firebase instances
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            
            // Setup toolbar
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("My Profile");
                }
            }
            
            // Initialize views
            textName = findViewById(R.id.textName);
            textEmail = findViewById(R.id.textEmail);
            textPhone = findViewById(R.id.textPhone);
            textAddress = findViewById(R.id.textAddress);
            
            // Check if all views are found
            if (textName == null || textEmail == null || textPhone == null || textAddress == null) {
                android.util.Log.e("ProfileActivity", "One or more views not found in layout");
                Toast.makeText(this, "Error loading profile interface", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Load user profile data
            loadUserProfile();
            
            // Set up edit profile button
            findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("ProfileActivity", "Error starting EditProfileActivity", e);
                    Toast.makeText(ProfileActivity.this, "Error opening edit profile", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadUserProfile() {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            String userId = currentUser.getUid();
            String email = currentUser.getEmail();
            
            // Set email from Firebase Auth
            if (textEmail != null) {
                textEmail.setText(email != null ? email : "Not available");
            }
            
            // Load additional profile data from Firestore
            db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    try {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String phone = documentSnapshot.getString("phone");
                            String address = documentSnapshot.getString("address");
                            
                            if (textName != null) textName.setText(name != null ? name : "Not provided");
                            if (textPhone != null) textPhone.setText(phone != null ? phone : "Not provided");
                            if (textAddress != null) textAddress.setText(address != null ? address : "Not provided");
                        } else {
                            // Create default profile if document doesn't exist
                            createDefaultProfile(userId);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ProfileActivity", "Error processing profile data", e);
                        Toast.makeText(ProfileActivity.this, "Error displaying profile data", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    android.util.Log.e("ProfileActivity", "Error loading profile", e);
                });
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "Error in loadUserProfile", e);
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    private void createDefaultProfile(String userId) {
        try {
            // Create a basic profile with Firebase Auth data
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("name", user.getDisplayName() != null ? user.getDisplayName() : "User");
                userData.put("email", user.getEmail());
                userData.put("phone", "Not provided");
                userData.put("address", "Not provided");
                userData.put("createdAt", new Date());
                
                db.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener(aVoid -> {
                            // Reload profile after creating default data
                            loadUserProfile();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error creating profile: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            android.util.Log.e("ProfileActivity", "Error creating default profile", e);
                        });
            }
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "Error in createDefaultProfile", e);
            Toast.makeText(this, "Error creating profile", Toast.LENGTH_SHORT).show();
        }
    }
}