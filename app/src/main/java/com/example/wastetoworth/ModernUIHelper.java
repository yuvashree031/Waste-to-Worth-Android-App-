package com.example.wastetoworth;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

/**
 * ModernUIHelper - Utility class to add modern features to any activity
 * 
 * This helper class provides easy integration of:
 * - Voice search functionality
 * - ML features
 * - Modern UI elements
 * 
 * Usage: Simply call setupModernFeatures() in any activity's onCreate()
 */
public class ModernUIHelper {
    
    private Activity activity;
    private VoiceSearchHelper voiceSearchHelper;
    
    public ModernUIHelper(Activity activity) {
        this.activity = activity;
    }
    
    /**
     * Setup modern features for an activity
     * @param voiceSearchLauncher The activity result launcher for voice search
     */
    public void setupModernFeatures(ActivityResultLauncher<Intent> voiceSearchLauncher) {
        // Initialize voice search helper
        voiceSearchHelper = new VoiceSearchHelper(activity, new VoiceSearchHelper.VoiceSearchListener() {
            @Override
            public void onVoiceSearchResult(String searchQuery) {
                Toast.makeText(activity, "Voice: " + searchQuery, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onVoiceSearchError(String error) {
                Toast.makeText(activity, "Voice error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Setup voice button if exists
        ImageView btnVoice = activity.findViewById(R.id.btnVoiceSearch);
        if (btnVoice != null) {
            btnVoice.setOnClickListener(v -> {
                if (voiceSearchHelper != null) {
                    voiceSearchHelper.startVoiceRecognition(voiceSearchLauncher);
                }
            });
        }
    }
    
    /**
     * Get the voice search helper instance
     */
    public VoiceSearchHelper getVoiceSearchHelper() {
        return voiceSearchHelper;
    }
    
    /**
     * Apply modern theme to activity
     */
    public static void applyModernTheme(Activity activity) {
        // Set status bar color
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(
                activity.getResources().getColor(R.color.gradient_purple_start, null)
            );
        }
    }
}
