package com.example.wastetoworth;
// Android imports
import android.content.Intent;
import android.os.Bundle;
// AndroidX imports
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
/**
 * LoginActivity - A redirector activity for login functionality
 * 
 * This activity serves as a placeholder that automatically redirects users to the Logup activity,
 * which contains the actual login and signup functionality. This design pattern is used to maintain
 * backward compatibility or to provide a clean URL/launcher activity while keeping the actual
 * authentication logic in a separate activity.
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * Called when the activity is first created.
     * This method immediately redirects to the Logup activity and finishes itself.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create an intent to navigate to the RoleSelectionActivity
        Intent intent = new Intent(this, RoleSelectionActivity.class);
        // Start the RoleSelectionActivity
        startActivity(intent);
        // Finish this activity to remove it from the back stack
        // This prevents users from coming back to this redirector activity
        finish();
    }
}