package com.example.wastetoworth;
// Android imports
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
// AndroidX imports
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
// Firebase imports
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
/**
 * Logup - Handles user authentication (login) functionality
 * 
 * This activity provides a login interface for users to authenticate with their
 * email and password. It also includes a link to the signup screen for new users.
 * The authentication is handled using Firebase Authentication.
 */
public class Logup extends AppCompatActivity {
    // UI Components
    private EditText mEmail;        // Email input field
    private EditText mPassword;     // Password input field
    private Button mLoginBtn;       // Login button
    private TextView mRegisterBtn;  // Signup prompt text
    // Firebase Authentication instance
    private FirebaseAuth fAuth;
    // Role parameter from intent
    private String userRole;
    /**
     * Called when the activity is first created.
     * Initializes the UI components and sets up click listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view using the layout defined in activity_logup.xml
        setContentView(R.layout.activity_logup);
        
        // Get the role parameter from intent (if coming from role selection)
        userRole = getIntent().getStringExtra("user_role");
        if (userRole == null) {
            userRole = "user"; // Default role
        }
        // Initialize UI components by finding them in the layout
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mLoginBtn = findViewById(R.id.login_button);
        mRegisterBtn = findViewById(R.id.login_text); // TextView that links to signup
        // Get the FirebaseAuth instance for authentication operations
        fAuth = FirebaseAuth.getInstance();
        // Check if user is already authenticated
        if (fAuth.getCurrentUser() != null) {
            // User is already logged in, redirect to MainActivity
            redirectToMainActivity();
            return;
        }
        // Set up click listener for the login button
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to handle login attempt
                attemptLogin();
            }
        });
        // Set up click listener for the signup text
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the Signup activity for new user registration
                Intent intent = new Intent(Logup.this, Signup.class);
                intent.putExtra("user_role", userRole); // Pass the role to signup
                startActivity(intent);
                // Finish this activity to remove it from the back stack
                finish();
            }
        });
    }
    /**
     * Attempts to log in the user with the provided credentials.
     * Validates the input fields and initiates the Firebase authentication process.
     */
    private void attemptLogin() {
        // Get and trim the email and password from input fields
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        // Validate email field
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Email is required");
            mEmail.requestFocus(); // Set focus to email field
            return;
        }
        // Validate password field
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Password is required");
            mPassword.requestFocus(); // Set focus to password field
            return;
        }
        // Validate password length
        if (password.length() < 6) {
            mPassword.setError("Password must be at least 6 characters");
            mPassword.requestFocus();
            return;
        }
        // Note: Consider adding a progress bar to show login in progress
        // progressBar.setVisibility(View.VISIBLE);
        // Attempt to sign in with email and password using Firebase Authentication
        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Hide progress indicator when authentication completes
                        // progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Authentication successful
                            Log.d("AUTH", "signInWithEmail:success");
                            // Redirect to main activity
                            redirectToMainActivity();
                        } else {
                            // Authentication failed
                            Log.w("AUTH", "signInWithEmail:failure", task.getException());
                            // Show error message to user
                            String errorMessage = "Authentication failed";
                            if (task.getException() != null) {
                                errorMessage = task.getException().getMessage();
                            }
                            Toast.makeText(Logup.this, errorMessage,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    /**
     * Redirects the user to the MainActivity and clears the activity stack.
     * This prevents the user from returning to the login screen by pressing the back button.
     */
    private void redirectToMainActivity() {
        // Create an intent to start MainActivity
        Intent intent = new Intent(Logup.this, MainActivity.class);
        // Clear the activity stack to prevent going back to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                       Intent.FLAG_ACTIVITY_CLEAR_TASK |
                       Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Start the MainActivity
        startActivity(intent);
        // Finish the current activity to remove it from the back stack
        finish();
    }
}