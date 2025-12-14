package com.example.wastetoworth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * HelpActivity - Help and support information
 * 
 * This activity provides users with help documentation, FAQ,
 * contact information, and support options.
 */
public class HelpActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        
        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Help & Support");
        }
        
        // Set up help options
        findViewById(R.id.cardFAQ).setOnClickListener(v -> {
            Toast.makeText(this, "FAQ feature coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.cardContactUs).setOnClickListener(v -> {
            openEmailClient();
        });
        
        findViewById(R.id.cardUserGuide).setOnClickListener(v -> {
            Toast.makeText(this, "User Guide feature coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.cardReportIssue).setOnClickListener(v -> {
            openEmailClient();
        });
        
        findViewById(R.id.cardAppTutorial).setOnClickListener(v -> {
            Toast.makeText(this, "App Tutorial feature coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.cardRateApp).setOnClickListener(v -> {
            openPlayStore();
        });
    }
    
    private void openEmailClient() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:support@wastetoworth.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "WasteToWorth Support Request");
        
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        } else {
            Toast.makeText(this, "No email client found", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, 
                Uri.parse("market://details?id=" + getPackageName())));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}