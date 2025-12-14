package com.example.wastetoworth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText editName, editPhone, editAddress;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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
                    getSupportActionBar().setTitle("Edit Profile");
                }
            }

            // Initialize views
            editName = findViewById(R.id.editName);
            editPhone = findViewById(R.id.editPhone);
            editAddress = findViewById(R.id.editAddress);
            btnSave = findViewById(R.id.btnSave);

            // Check if all views are found
            if (editName == null || editPhone == null || editAddress == null || btnSave == null) {
                android.util.Log.e("EditProfileActivity", "One or more views not found in layout");
                Toast.makeText(this, "Error loading edit profile interface", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Load current profile data
            loadCurrentProfile();

            // Set up save button
            btnSave.setOnClickListener(v -> saveProfile());
        } catch (Exception e) {
            android.util.Log.e("EditProfileActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error loading edit profile", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadCurrentProfile() {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String userId = currentUser.getUid();

            // Load current profile data from Firestore
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        try {
                            if (documentSnapshot.exists()) {
                                String name = documentSnapshot.getString("name");
                                String phone = documentSnapshot.getString("phone");
                                String address = documentSnapshot.getString("address");

                                if (editName != null) editName.setText(name != null ? name : "");
                                if (editPhone != null) editPhone.setText(phone != null ? phone : "");
                                if (editAddress != null) editAddress.setText(address != null ? address : "");
                            }
                        } catch (Exception e) {
                            android.util.Log.e("EditProfileActivity", "Error processing profile data", e);
                            Toast.makeText(EditProfileActivity.this, "Error displaying profile data", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error loading profile: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        android.util.Log.e("EditProfileActivity", "Error loading profile", e);
                    });
        } catch (Exception e) {
            android.util.Log.e("EditProfileActivity", "Error in loadCurrentProfile", e);
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate input fields
            if (editName == null || editPhone == null || editAddress == null) {
                Toast.makeText(this, "Error: Form fields not initialized", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = currentUser.getUid();
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String address = editAddress.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create profile data map
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("name", name);
            profileData.put("phone", phone);
            profileData.put("address", address);

            // Save to Firestore
            db.collection("users").document(userId)
                    .set(profileData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error updating profile: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        android.util.Log.e("EditProfileActivity", "Error updating profile", e);
                    });
        } catch (Exception e) {
            android.util.Log.e("EditProfileActivity", "Error in saveProfile", e);
            Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}