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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
/**
 * NGOLoginActivity - Dedicated login activity for NGO organizations
 * 
 * This activity provides NGO-specific authentication with additional fields
 * and validation requirements for organizational accounts.
 */
public class NGOLoginActivity extends AppCompatActivity {
    
    // UI Components
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnRegisterNGO;
    private TextView tvBackToPeople;
    private TextView tvTitle;
    
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private static final String TAG = "NGOLoginActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_ngo_login);
            
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();
            fStore = FirebaseFirestore.getInstance();
            
            // Initialize UI components
            initializeViews();
            
            // Check if all views are found
            if (etEmail == null || etPassword == null || btnLogin == null || 
                btnRegisterNGO == null || tvBackToPeople == null || tvTitle == null) {
                Log.e(TAG, "Some views are null - layout may be incomplete");
                Toast.makeText(this, "Error loading login interface", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            // Set up click listeners
            setupClickListeners();
            
            // Check if user is already logged in
            checkExistingAuth();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing NGO login", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    /**
     * Initialize all view components
     */
    private void initializeViews() {
        try {
            etEmail = findViewById(R.id.et_ngo_email);
            etPassword = findViewById(R.id.et_ngo_password);
            btnLogin = findViewById(R.id.btn_ngo_login);
            btnRegisterNGO = findViewById(R.id.btn_register_ngo);
            tvBackToPeople = findViewById(R.id.tv_back_to_people);
            tvTitle = findViewById(R.id.tv_ngo_title);
            
            Log.d(TAG, "All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize NGO login views");
        }
    }
    
    /**
     * Set up click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Login button click
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptNGOLogin();
            }
        });
        
        // Register NGO button click
        btnRegisterNGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToNGORegistration();
            }
        });
        
        // Back to people login
        tvBackToPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPeopleLogin();
            }
        });
    }
    
    /**
     * Check if user is already authenticated
     */
    private void checkExistingAuth() {
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "User already authenticated: " + mAuth.getCurrentUser().getEmail());
            navigateToMainActivity();
        }
    }
    
    /**
     * Attempt NGO login with validation
     */
    private void attemptNGOLogin() {
        try {
            if (etEmail == null || etPassword == null) {
                Log.e(TAG, "Email or password field is null");
                Toast.makeText(this, "Login interface error", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            
            // Validation
            if (!validateLoginInputs(email, password)) {
                return;
            }
            
            // Perform Firebase authentication
            performFirebaseLogin(email, password);
            
        } catch (Exception e) {
            Log.e(TAG, "Error during login attempt: " + e.getMessage(), e);
            Toast.makeText(this, "Login attempt failed", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Validate login input fields
     */
    private boolean validateLoginInputs(String email, String password) {
        try {
            if (etEmail == null || etPassword == null) {
                Log.e(TAG, "Email or password field is null during validation");
                return false;
            }
            
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("NGO email is required");
                return false;
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Please enter a valid email address");
                return false;
            }
            
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password is required");
                return false;
            }
            
            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
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
     * Perform Firebase authentication
     */
    private void performFirebaseLogin(String email, String password) {
        try {
            Log.d(TAG, "Attempting NGO login for: " + email);
            
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            try {
                                if (task.isSuccessful()) {
                                    // Login successful
                                    Log.d(TAG, "NGO login successful");
                                    
                                    // Check if user is NGO
                                    String userID = mAuth.getCurrentUser().getUid();
                                    checkNGOUser(userID);
                                    
                                } else {
                                    // Login failed
                                    Log.w(TAG, "NGO login failed", task.getException());
                                    Toast.makeText(NGOLoginActivity.this, 
                                            "Login failed: " + task.getException().getMessage(), 
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error in login callback: " + e.getMessage(), e);
                                Toast.makeText(NGOLoginActivity.this, 
                                        "Login error occurred", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "Error during Firebase login: " + e.getMessage(), e);
            Toast.makeText(this, "Login system error", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Check if the logged-in user is an NGO
     */
    private void checkNGOUser(String userID) {
        try {
            Log.d(TAG, "Checking if user is NGO: " + userID);
            
            fStore.collection("ngos").document(userID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            try {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document != null && document.exists()) {
                                        // User is an NGO
                                        Log.d(TAG, "User confirmed as NGO");
                                        Toast.makeText(NGOLoginActivity.this, 
                                                "NGO login successful!", Toast.LENGTH_SHORT).show();
                                        navigateToMainActivity();
                                    } else {
                                        // User is not an NGO, sign them out
                                        Log.w(TAG, "User is not an NGO, signing out");
                                        mAuth.signOut();
                                        Toast.makeText(NGOLoginActivity.this, 
                                                "This account is not registered as an NGO", 
                                                Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Log.e(TAG, "Error checking NGO status", task.getException());
                                    // Sign out user due to error
                                    mAuth.signOut();
                                    Toast.makeText(NGOLoginActivity.this, 
                                            "Error verifying NGO account", Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error in NGO check callback: " + e.getMessage(), e);
                                mAuth.signOut();
                                Toast.makeText(NGOLoginActivity.this, 
                                        "Error verifying NGO account", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "Error during NGO user check: " + e.getMessage(), e);
            mAuth.signOut();
            Toast.makeText(this, "Error verifying NGO account", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Navigate to NGO-specific registration
     */
    private void navigateToNGORegistration() {
        Intent intent = new Intent(NGOLoginActivity.this, NGORegistrationActivity.class);
        startActivity(intent);
    }
    
    /**
     * Navigate to people login
     */
    private void navigateToPeopleLogin() {
        Intent intent = new Intent(NGOLoginActivity.this, Logup.class);
        startActivity(intent);
        finish();
    }
    
    /**
     * Navigate to main activity after successful login
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(NGOLoginActivity.this, MainActivity.class);
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
        // Go back to role selection
        Intent intent = new Intent(NGOLoginActivity.this, RoleSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}