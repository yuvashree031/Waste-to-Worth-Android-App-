package com.example.wastetoworth;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
public class SplashScreen extends AppCompatActivity {
    private static final String TAG = "SplashScreen";
    private static final long SPLASH_DELAY = 2000; // 2 seconds
    private static final String PREFS_NAME = "WasteToWorthPrefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "SplashScreen created");
            setContentView(R.layout.activity_splash);
            Log.d(TAG, "Splash layout set");
            // Log app version for debugging
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                Log.d(TAG, "App version: " + pInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Package name not found", e);
            }
            // Delay for splash screen
            new Handler().postDelayed(this::startLandingPage, SPLASH_DELAY);
        } catch (Exception e) {
            Log.e(TAG, "Fatal error in SplashScreen.onCreate: " + Log.getStackTraceString(e));
            showErrorAndExit("Fatal error during app initialization");
        }
    }
    private void startLandingPage() {
        try {
            Log.d(TAG, "Starting appropriate activity based on auth status");
            
            // Check if this is the first launch
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true);
            
            if (isFirstLaunch) {
                // First time user - show onboarding
                Log.d(TAG, "First time user, starting OnboardingActivity");
                Intent intent = new Intent(this, OnboardingActivity.class);
                startActivity(intent);
                
                // Mark as not first launch anymore
                prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
            } else {
                // Returning user - check authentication status
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                
                if (mAuth.getCurrentUser() != null) {
                    // User is already authenticated, go to MainActivity
                    Log.d(TAG, "User authenticated, starting MainActivity");
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // User not authenticated, go to RoleSelectionActivity
                    Log.d(TAG, "User not authenticated, starting RoleSelectionActivity");
                    Intent intent = new Intent(this, RoleSelectionActivity.class);
                    startActivity(intent);
                }
            }
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error starting appropriate activity: " + Log.getStackTraceString(e));
            showErrorAndExit("Cannot start the application");
        }
    }
    private void showErrorAndExit(String message) {
        try {
            Toast.makeText(this, message + " (Check logs for details)", Toast.LENGTH_LONG).show();
            Log.e(TAG, "App initialization error: " + message);
        } catch (Exception e) {
            Log.e(TAG, "Error showing error message: " + e.getMessage());
        }
        // Delay finish so user can see error
        new android.os.Handler().postDelayed(this::finish, 4000);
    }
}