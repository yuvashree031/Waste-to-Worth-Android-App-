package com.example.wastetoworth;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import java.util.ArrayList;
import java.util.Locale;

/**
 * VoiceSearchHelper - Utility class for handling voice search functionality
 * 
 * This class provides voice recognition capabilities for the app, allowing users
 * to search for donations, navigate to different screens, and perform actions using voice commands.
 */
public class VoiceSearchHelper {
    
    private Activity activity;
    private VoiceSearchListener listener;
    
    /**
     * Interface for voice search callbacks
     */
    public interface VoiceSearchListener {
        void onVoiceSearchResult(String searchQuery);
        void onVoiceSearchError(String error);
    }
    
    /**
     * Constructor
     * @param activity The activity context
     * @param listener Callback listener for voice search results
     */
    public VoiceSearchHelper(Activity activity, VoiceSearchListener listener) {
        this.activity = activity;
        this.listener = listener;
    }
    
    /**
     * Check if speech recognition is available on the device
     * @return true if available, false otherwise
     */
    public boolean isSpeechRecognitionAvailable() {
        return SpeechRecognizer.isRecognitionAvailable(activity);
    }
    
    /**
     * Start voice recognition
     * @param launcher ActivityResultLauncher to handle the result
     */
    public void startVoiceRecognition(ActivityResultLauncher<Intent> launcher) {
        if (!isSpeechRecognitionAvailable()) {
            Toast.makeText(activity, "Voice recognition not available on this device", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onVoiceSearchError("Voice recognition not available");
            }
            return;
        }
        
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search donations or navigate...");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        
        try {
            launcher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(activity, "Error starting voice recognition: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onVoiceSearchError(e.getMessage());
            }
        }
    }
    
    /**
     * Process voice recognition result
     * @param data The intent data from the voice recognition activity
     */
    public void processVoiceResult(Intent data) {
        if (data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                if (listener != null) {
                    listener.onVoiceSearchResult(spokenText);
                }
                processVoiceCommand(spokenText);
            }
        }
    }
    
    /**
     * Process voice commands for navigation and actions
     * @param command The voice command to process
     */
    private void processVoiceCommand(String command) {
        String lowerCommand = command.toLowerCase();
        
        Intent intent = null;
        
        // Navigation commands
        if (lowerCommand.contains("donate") || lowerCommand.contains("donation")) {
            intent = new Intent(activity, DonateActivity.class);
        } else if (lowerCommand.contains("map") || lowerCommand.contains("location")) {
            intent = new Intent(activity, FoodMap.class);
        } else if (lowerCommand.contains("volunteer")) {
            intent = new Intent(activity, VolunteerActivity.class);
        } else if (lowerCommand.contains("history") || lowerCommand.contains("my donations")) {
            intent = new Intent(activity, DonationHistoryActivity.class);
        } else if (lowerCommand.contains("profile")) {
            intent = new Intent(activity, ProfileActivity.class);
        } else if (lowerCommand.contains("feed") || lowerCommand.contains("available")) {
            intent = new Intent(activity, DonationFeedActivity.class);
        } else if (lowerCommand.contains("urgent") || lowerCommand.contains("request")) {
            intent = new Intent(activity, UrgentRequestActivity.class);
        } else if (lowerCommand.contains("help") || lowerCommand.contains("support")) {
            intent = new Intent(activity, HelpActivity.class);
        } else if (lowerCommand.contains("settings")) {
            intent = new Intent(activity, SettingsActivity.class);
        }
        
        // Launch the appropriate activity if a command was recognized
        if (intent != null) {
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
    
    /**
     * Get suggestions based on partial voice input
     * @param partialInput The partial voice input
     * @return List of suggestions
     */
    public ArrayList<String> getSuggestions(String partialInput) {
        ArrayList<String> suggestions = new ArrayList<>();
        String lower = partialInput.toLowerCase();
        
        if (lower.contains("don")) {
            suggestions.add("Donate Food");
            suggestions.add("Donation History");
            suggestions.add("Donation Feed");
        }
        if (lower.contains("vol")) {
            suggestions.add("Volunteer");
        }
        if (lower.contains("map") || lower.contains("loc")) {
            suggestions.add("Food Map");
        }
        if (lower.contains("prof")) {
            suggestions.add("My Profile");
        }
        if (lower.contains("urg")) {
            suggestions.add("Urgent Request");
        }
        
        return suggestions;
    }
}
