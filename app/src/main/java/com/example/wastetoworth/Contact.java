package com.example.wastetoworth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wastetoworth.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
public class Contact extends AppCompatActivity {
    private static final String TAG = "ContactActivity";
    
    private EditText name, email, message;
    private Button submit;
    private boolean isNameValid, isEmailValid, isMessageValid;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private TextInputLayout nameError, emailError, messageError;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        
        initializeViews();
        initializeFirebase();
        
        // Set click listener using lambda
        submit.setOnClickListener(v -> validateAndSubmit());
    }
    
    private void initializeViews() {
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        message = findViewById(R.id.message);
        submit = findViewById(R.id.submit);
        nameError = findViewById(R.id.nameError);
        emailError = findViewById(R.id.emailError);
        messageError = findViewById(R.id.messageError);
    }
    
    private void initializeFirebase() {
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }
    private void validateAndSubmit() {
        try {
            // Validate name
            String nameStr = name.getText().toString().trim();
            if (nameStr.isEmpty()) {
                nameError.setError(getString(R.string.error_name_required));
                isNameValid = false;
            } else if (nameStr.length() < 2) {
                nameError.setError(getString(R.string.error_name_too_short));
                isNameValid = false;
            } else {
                isNameValid = true;
                nameError.setError(null);
                nameError.setErrorEnabled(false);
            }

            // Validate email
            String emailStr = email.getText().toString().trim();
            if (emailStr.isEmpty()) {
                emailError.setError(getString(R.string.error_email_required));
                isEmailValid = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
                emailError.setError(getString(R.string.error_invalid_email));
                isEmailValid = false;
            } else {
                isEmailValid = true;
                emailError.setError(null);
                emailError.setErrorEnabled(false);
            }

            // Validate message
            String messageStr = message.getText().toString().trim();
            if (messageStr.isEmpty()) {
                messageError.setError(getString(R.string.error_message_required));
                isMessageValid = false;
            } else if (messageStr.length() < 10) {
                messageError.setError(getString(R.string.error_message_too_short));
                isMessageValid = false;
            } else {
                isMessageValid = true;
                messageError.setError(null);
                messageError.setErrorEnabled(false);
            }

            if (isNameValid && isEmailValid && isMessageValid) {
                submitContactForm(nameStr, emailStr, messageStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in form validation: " + e.getMessage(), e);
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
        }
    }

    private void submitContactForm(String name, String email, String message) {
        try {
            if (fAuth == null || fStore == null) {
                Log.e(TAG, "Firebase not properly initialized");
                showError(getString(R.string.error_initialization_failed));
                return;
            }

            if (fAuth.getCurrentUser() == null) {
                showError(getString(R.string.error_not_authenticated));
                // Optionally redirect to login
                // startActivity(new Intent(this, LoginActivity.class));
                return;
            }

            userID = fAuth.getCurrentUser().getUid();
            if (userID == null || userID.isEmpty()) {
                showError(getString(R.string.error_user_not_found));
                return;
            }

            // Show loading indicator if needed
            // progressBar.setVisibility(View.VISIBLE);
            submit.setEnabled(false);

            CollectionReference collectionReference = fStore.collection("contact_data");
            
            Map<String, Object> contactData = new HashMap<>();
            contactData.put("timestamp", FieldValue.serverTimestamp());
            contactData.put("name", name);
            contactData.put("email", email);
            contactData.put("message", message);
            contactData.put("userid", userID);
            contactData.put("status", "new");

            collectionReference.add(contactData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Contact form submitted successfully with ID: " + documentReference.getId());
                        showSuccess(getString(R.string.contact_submit_success));
                        clearForm();
                        navigateToMainActivity();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error submitting contact form", e);
                        showError(getString(R.string.contact_submit_error));
                    })
                    .addOnCompleteListener(task -> {
                        // Hide loading indicator
                        // progressBar.setVisibility(View.GONE);
                        submit.setEnabled(true);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in submitContactForm: " + e.getMessage(), e);
            showError(getString(R.string.error_unexpected));
            // progressBar.setVisibility(View.GONE);
            submit.setEnabled(true);
        }
    }

    private void navigateToMainActivity() {
        try {
            Intent intent = new Intent(Contact.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                           Intent.FLAG_ACTIVITY_CLEAR_TASK | 
                           Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to MainActivity: " + e.getMessage(), e);
            // Fallback to default behavior
            super.onBackPressed();
        }
    }
    
    private void showError(String message) {
        if (!isFinishing() && !isDestroyed()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
    
    private void showSuccess(String message) {
        if (!isFinishing() && !isDestroyed()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void clearForm() {
        if (name != null) name.getText().clear();
        if (email != null) email.getText().clear();
        if (message != null) message.getText().clear();
        
        if (nameError != null) nameError.setError(null);
        if (emailError != null) emailError.setError(null);
        if (messageError != null) messageError.setError(null);
    }
    }
