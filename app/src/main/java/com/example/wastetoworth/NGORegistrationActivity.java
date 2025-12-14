package com.example.wastetoworth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

/**
 * NGORegistrationActivity - Dedicated registration activity for NGO organizations
 * 
 * This activity provides NGO-specific registration with additional fields like
 * organization name, registration number, mission statement, and contact details.
 */
public class NGORegistrationActivity extends AppCompatActivity {
    
    // UI Components
    private EditText etOrganizationName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private EditText etRegistrationNumber;
    private EditText etPhone;
    private EditText etMissionStatement;
    private EditText etAddress;
    private Button btnRegisterNGO;
    private TextView tvBackToLogin;
    private TextView tvTitle;
    
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private static final String TAG = "NGORegistrationActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_ngo_registration);
            
            // Initialize Firebase
            mAuth = FirebaseAuth.getInstance();
            fStore = FirebaseFirestore.getInstance();
            
            // Initialize UI components
            initializeViews();
            
            // Check if all views are found
            if (etOrganizationName == null || etEmail == null || etPassword == null || 
                etConfirmPassword == null || etRegistrationNumber == null || etPhone == null ||
                etMissionStatement == null || etAddress == null || btnRegisterNGO == null || 
                tvBackToLogin == null || tvTitle == null) {
                Log.e(TAG, "Some views are null - layout may be incomplete");
                Toast.makeText(this, "Error loading registration interface", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            // Set up click listeners
            setupClickListeners();
            
            // Check if user is already logged in
            checkExistingAuth();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing NGO registration", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    /**
     * Initialize all view components
     */
    private void initializeViews() {
        try {
            etOrganizationName = findViewById(R.id.et_organization_name);
            etEmail = findViewById(R.id.et_ngo_email);
            etPassword = findViewById(R.id.et_ngo_password);
            etConfirmPassword = findViewById(R.id.et_ngo_confirm_password);
            etRegistrationNumber = findViewById(R.id.et_registration_number);
            etPhone = findViewById(R.id.et_ngo_phone);
            etMissionStatement = findViewById(R.id.et_mission_statement);
            etAddress = findViewById(R.id.et_ngo_address);
            btnRegisterNGO = findViewById(R.id.btn_register_ngo);
            tvBackToLogin = findViewById(R.id.tv_back_to_login);
            tvTitle = findViewById(R.id.tv_ngo_registration_title);
            
            Log.d(TAG, "All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize NGO registration views");
        }
    }
    
    /**
     * Set up click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Register NGO button click
        btnRegisterNGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptNGORegistration();
            }
        });
        
        // Back to login
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToNGOLogin();
            }
        });
    }
    
    /**
     * Check if user is already authenticated
     */
    private void checkExistingAuth() {
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "User already authenticated");
            navigateToMainActivity();
        }
    }
    
    /**
     * Attempt NGO registration with comprehensive validation
     */
    private void attemptNGORegistration() {
        try {
            // Check if all required views are available
            if (etOrganizationName == null || etEmail == null || etPassword == null || 
                etConfirmPassword == null || etRegistrationNumber == null || etPhone == null ||
                etMissionStatement == null || etAddress == null) {
                Log.e(TAG, "Some input fields are null");
                Toast.makeText(this, "Registration interface error", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Get all input values
            String organizationName = etOrganizationName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String registrationNumber = etRegistrationNumber.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String missionStatement = etMissionStatement.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            
            // Validate all inputs
            if (!validateRegistrationInputs(organizationName, email, password, confirmPassword, 
                                           registrationNumber, phone, missionStatement, address)) {
                return;
            }
            
            // Perform Firebase registration
            performFirebaseRegistration(organizationName, email, password, registrationNumber, 
                                    phone, missionStatement, address);
            
        } catch (Exception e) {
            Log.e(TAG, "Error during registration attempt: " + e.getMessage(), e);
            Toast.makeText(this, "Registration attempt failed", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Comprehensive validation for NGO registration inputs
     */
    private boolean validateRegistrationInputs(String organizationName, String email, String password, 
                                             String confirmPassword, String registrationNumber, String phone, 
                                             String missionStatement, String address) {
        
        try {
            // Check if all required views are available
            if (etOrganizationName == null || etEmail == null || etPassword == null || 
                etConfirmPassword == null || etRegistrationNumber == null || etPhone == null ||
                etMissionStatement == null || etAddress == null) {
                Log.e(TAG, "Some input fields are null during validation");
                return false;
            }
            
            // Organization name validation
            if (TextUtils.isEmpty(organizationName)) {
                etOrganizationName.setError("Organization name is required");
                return false;
            }
            
            if (organizationName.length() < 3) {
                etOrganizationName.setError("Organization name must be at least 3 characters");
                return false;
            }
            
            // Email validation
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email is required");
                return false;
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Please enter a valid email address");
                return false;
            }
            
            // Password validation
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password is required");
                return false;
            }
            
            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                return false;
            }
            
            // Confirm password validation
            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                return false;
            }
            
            // Registration number validation
            if (TextUtils.isEmpty(registrationNumber)) {
                etRegistrationNumber.setError("Registration number is required");
                return false;
            }
            
            // Phone validation
            if (TextUtils.isEmpty(phone)) {
                etPhone.setError("Phone number is required");
                return false;
            }
            
            if (phone.length() < 10) {
                etPhone.setError("Please enter a valid phone number");
                return false;
            }
            
            // Mission statement validation
            if (TextUtils.isEmpty(missionStatement)) {
                etMissionStatement.setError("Mission statement is required");
                return false;
            }
            
            if (missionStatement.length() < 10) {
                etMissionStatement.setError("Mission statement must be at least 10 characters");
                return false;
            }
            
            // Address validation
            if (TextUtils.isEmpty(address)) {
                etAddress.setError("Address is required");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error during validation: " + e.getMessage(), e);
            Toast.makeText(this, "Validation error", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    /**
     * Perform Firebase registration with NGO-specific data
     */
    private void performFirebaseRegistration(final String organizationName, final String email, 
                                           final String password, final String registrationNumber, 
                                           final String phone, final String missionStatement, 
                                           final String address) {
        
        try {
            Log.d(TAG, "Attempting NGO registration for: " + organizationName);
            
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            try {
                                if (task.isSuccessful()) {
                                    // Registration successful
                                    Log.d(TAG, "NGO registration successful");
                                    
                                    String userID = mAuth.getCurrentUser().getUid();
                                    storeNGOData(userID, organizationName, email, registrationNumber, 
                                               phone, missionStatement, address);
                                    
                                } else {
                                    // Registration failed
                                    Log.w(TAG, "NGO registration failed", task.getException());
                                    Toast.makeText(NGORegistrationActivity.this, 
                                            "Registration failed: " + task.getException().getMessage(), 
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error in registration callback: " + e.getMessage(), e);
                                Toast.makeText(NGORegistrationActivity.this, 
                                        "Registration error occurred", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "Error during Firebase registration: " + e.getMessage(), e);
            Toast.makeText(this, "Registration system error", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Store NGO data in Firestore
     */
    private void storeNGOData(String userID, String organizationName, String email, 
                            String registrationNumber, String phone, String missionStatement, 
                            String address) {
        
        try {
            // Create NGO data map
            Map<String, Object> ngoData = new HashMap<>();
            ngoData.put("organizationName", organizationName);
            ngoData.put("email", email);
            ngoData.put("registrationNumber", registrationNumber);
            ngoData.put("phone", phone);
            ngoData.put("missionStatement", missionStatement);
            ngoData.put("address", address);
            ngoData.put("userType", "ngo");
            ngoData.put("verified", false);
            ngoData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
            
            // Store in Firestore
            fStore.collection("ngos").document(userID)
                    .set(ngoData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            try {
                                Log.d(TAG, "NGO data stored successfully");
                                Toast.makeText(NGORegistrationActivity.this, 
                                        "NGO registered successfully! Awaiting verification.", 
                                        Toast.LENGTH_LONG).show();
                                navigateToMainActivity();
                            } catch (Exception e) {
                                Log.e(TAG, "Error in success callback: " + e.getMessage(), e);
                                Toast.makeText(NGORegistrationActivity.this, 
                                        "Registration completed but navigation failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error storing NGO data", e);
                            Toast.makeText(NGORegistrationActivity.this, 
                                    "Error storing data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "Error during NGO data storage: " + e.getMessage(), e);
            Toast.makeText(this, "Data storage system error", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Navigate to NGO login
     */
    private void navigateToNGOLogin() {
        Intent intent = new Intent(NGORegistrationActivity.this, NGOLoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    /**
     * Navigate to main activity after successful registration
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(NGORegistrationActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                       Intent.FLAG_ACTIVITY_CLEAR_TASK | 
                       Intent.FLAG_ACTIVITY_NEW_TASK);
        // Add NGO flag to indicate NGO user
        intent.putExtra("user_type", "ngo");
        startActivity(intent);
        finish();
    }
    
    /**
     * Handle back button press
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToNGOLogin();
    }
}