package com.example.wastetoworth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * SettingsActivity - Application settings and preferences
 * 
 * This activity provides users with various app settings including
 * notification preferences, dark mode toggle, and other configuration options.
 */
public class SettingsActivity extends AppCompatActivity {
    
    private SwitchCompat switchNotifications, switchDarkMode, switchLocation;
    private SharedPreferences sharedPreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Settings");
            }
        }
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        
        // Initialize switches with null checks
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchLocation = findViewById(R.id.switchLocation);
        
        // Check if all views are found
        if (switchNotifications == null || switchDarkMode == null || switchLocation == null) {
            Log.e("SettingsActivity", "Some views not found in layout");
            Toast.makeText(this, "Error loading settings interface", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Load saved preferences
        loadSettings();
        
        // Set up switch listeners
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting("notifications_enabled", isChecked);
            Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
            if (isChecked) {
                enableNotifications();
            } else {
                disableNotifications();
            }
        });
        
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting("dark_mode_enabled", isChecked);
            applyDarkMode(isChecked);
            Toast.makeText(this, "Dark mode " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
            // Apply dark mode and recreate activity
            new android.os.Handler().postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    try {
                        this.recreate();
                    } catch (Exception e) {
                        Log.e("SettingsActivity", "Error recreating activity", e);
                        Toast.makeText(this, "Theme applied. Restart app for full changes.", Toast.LENGTH_SHORT).show();
                    }
                }
            }, 300);
        });
        
        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting("location_enabled", isChecked);
            Toast.makeText(this, "Location services " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
            if (isChecked) {
                enableLocationServices();
            } else {
                disableLocationServices();
            }
        });
        
        // Set up other settings options with null checks
        android.view.View privacyButton = findViewById(R.id.btnPrivacyPolicy);
        if (privacyButton != null) {
            privacyButton.setOnClickListener(v -> {
                openPrivacyPolicy();
            });
        }
        
        android.view.View termsButton = findViewById(R.id.btnTermsConditions);
        if (termsButton != null) {
            termsButton.setOnClickListener(v -> {
                openTermsOfService();
            });
        }
        
        android.view.View aboutButton = findViewById(R.id.btnAboutApp);
        if (aboutButton != null) {
            aboutButton.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "About page not available", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("SettingsActivity", "Error starting AboutActivity", e);
                    Toast.makeText(this, "Unable to open About page", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void loadSettings() {
        boolean notifications = sharedPreferences.getBoolean("notifications_enabled", true);
        boolean darkMode = sharedPreferences.getBoolean("dark_mode_enabled", false);
        boolean location = sharedPreferences.getBoolean("location_enabled", true);
        
        switchNotifications.setChecked(notifications);
        switchDarkMode.setChecked(darkMode);
        switchLocation.setChecked(location);
        
        // Apply dark mode if enabled
        applyDarkMode(darkMode);
    }
    
    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    
    private void applyDarkMode(boolean enabled) {
        AppCompatDelegate.setDefaultNightMode(
            enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    private void enableNotifications() {
        // Enable push notifications
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notifications_enabled", true);
        editor.apply();
        
        // Here you would typically register for Firebase Cloud Messaging
        // or enable local notifications
        Log.d("Settings", "Notifications enabled");
    }
    
    private void disableNotifications() {
        // Disable push notifications
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notifications_enabled", false);
        editor.apply();
        
        // Here you would typically unregister from Firebase Cloud Messaging
        // or disable local notifications
        Log.d("Settings", "Notifications disabled");
    }
    
    private void enableLocationServices() {
        // Enable location services
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("location_enabled", true);
        editor.apply();
        
        // Here you would typically request location permissions
        // and start location services
        Log.d("Settings", "Location services enabled");
    }
    
    private void disableLocationServices() {
        // Disable location services
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("location_enabled", false);
        editor.apply();
        
        // Here you would typically stop location services
        Log.d("Settings", "Location services disabled");
    }
    
    private void enableAutoSync() {
        // Enable auto-sync
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("auto_sync_enabled", true);
        editor.apply();
        
        // Here you would typically enable background sync
        Log.d("Settings", "Auto-sync enabled");
    }
    
    private void disableAutoSync() {
        // Disable auto-sync
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("auto_sync_enabled", false);
        editor.apply();
        
        // Here you would typically disable background sync
        Log.d("Settings", "Auto-sync disabled");
    }
    
    private void openPrivacyPolicy() {
        try {
            // Open privacy policy in browser or web view
            String privacyPolicyUrl = "https://your-app-domain.com/privacy-policy";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(privacyPolicyUrl));
            
            // Check if there's a browser available
            if (browserIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "Privacy Policy: Your data is secure and will not be shared with third parties.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("SettingsActivity", "Error opening privacy policy", e);
            Toast.makeText(this, "Unable to open privacy policy", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openTermsOfService() {
        try {
            // Open terms of service in browser or web view
            String termsUrl = "https://your-app-domain.com/terms-of-service";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(termsUrl));
            
            // Check if there's a browser available
            if (browserIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "Terms of Service: By using this app, you agree to our terms and conditions.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("SettingsActivity", "Error opening terms of service", e);
            Toast.makeText(this, "Unable to open terms of service", Toast.LENGTH_SHORT).show();
        }
    }
}