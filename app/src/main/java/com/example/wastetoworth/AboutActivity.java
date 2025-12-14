package com.example.wastetoworth;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import android.content.Intent;
import android.widget.Toast;
public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setupToolbar();
        setupContent();
    }
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("About WasteToWorth");
            }
        }
    }
    private void setupContent() {
        // Set up social media click listeners
        setupSocialMediaLinks();
    }
    
    private void setupSocialMediaLinks() {
        // Instagram click listener
        android.widget.ImageView instagram = findViewById(R.id.instagram);
        if (instagram != null) {
            instagram.setOnClickListener(v -> {
                try {
                    String instagramUrl = "https://instagram.com/wastetoworth";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(instagramUrl));
                    if (browserIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(browserIntent);
                    } else {
                        android.widget.Toast.makeText(this, "Instagram: @wastetoworth", android.widget.Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.widget.Toast.makeText(this, "Unable to open Instagram", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Facebook click listener
        android.widget.ImageView facebook = findViewById(R.id.facebook);
        if (facebook != null) {
            facebook.setOnClickListener(v -> {
                try {
                    String facebookUrl = "https://facebook.com/wastetoworth";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(facebookUrl));
                    if (browserIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(browserIntent);
                    } else {
                        android.widget.Toast.makeText(this, "Facebook: @wastetoworth", android.widget.Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.widget.Toast.makeText(this, "Unable to open Facebook", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Twitter click listener
        android.widget.ImageView twitter = findViewById(R.id.twitter);
        if (twitter != null) {
            twitter.setOnClickListener(v -> {
                try {
                    String twitterUrl = "https://twitter.com/wastetoworth";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(twitterUrl));
                    if (browserIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(browserIntent);
                    } else {
                        android.widget.Toast.makeText(this, "Twitter: @wastetoworth", android.widget.Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.widget.Toast.makeText(this, "Unable to open Twitter", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}