package com.example.wastetoworth;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import timber.log.Timber;

/**
 * Custom Application class for Waste to Worth app
 * Handles global initialization and logging configuration
 */
public class WasteToWorthApplication extends Application {
    
    private static final String TAG = "WasteToWorthApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize logging
        initializeLogging();
        
        // Check Google Play Services availability
        checkGooglePlayServices();
        
        // Initialize Firebase with optimized settings
        initializeFirebase();
        
        Log.d(TAG, "Application initialized successfully");
    }
    
    /**
     * Initialize logging configuration
     * Prevents Flogger spam and configures Timber
     */
    private void initializeLogging() {
        // Plant Timber for debug builds only
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return String.format("(%s:%s)#%s",
                            element.getFileName(),
                            element.getLineNumber(),
                            element.getMethodName());
                }
            });
            Timber.d("Timber logging initialized for debug build");
        }
        
        // Configure system logging to reduce spam
        configureSystemLogging();
    }
    
    /**
     * Configure system-wide logging to prevent spam
     */
    private void configureSystemLogging() {
        // Disable verbose logging for Firebase Performance
        System.setProperty("firebase_performance_log_level", "WARN");
        
        // Disable Flogger debug logging
        System.setProperty("flogger.backend_factory", 
            "com.google.common.flogger.backend.system.DefaultPlatform$SimpleBackendFactory");
        
        // Configure ProxyAndroidBackend to reduce spam
        System.setProperty("proxy_android_backend_log_level", "ERROR");
        
        // Disable Firebase Performance verbose logging
        System.setProperty("firebase_performance_collection_enabled", "false");
        System.setProperty("firebase_performance_logging_enabled", "false");
        
        // Disable Google API client verbose logging
        System.setProperty("google.api.client.verbose", "false");
        
        // Configure Flogger to use simple backend and reduce verbosity
        System.setProperty("flogger.backend_factory", 
            "com.google.common.flogger.backend.system.SimpleBackendFactory");
        System.setProperty("flogger.level", "WARNING");
        
        // Additional properties to handle Google Play Services security issues
        System.setProperty("google.api.client.disable_gmscore", "true");
        System.setProperty("firebase.analytics.debug_mode", "false");
        System.setProperty("firebase.crashlytics.debug_mode", "false");
        
        // Disable Phenotype API to prevent security exceptions
        System.setProperty("phenotype.api.enabled", "false");
        System.setProperty("phenotype.flags.enabled", "false");
        
        // Disable Google Play Services provider installer
        System.setProperty("providerinstaller.enable", "false");
        System.setProperty("gmscore.provider.installer.enabled", "false");
        
        // Disable Google API client strict mode
        System.setProperty("google.api.client.strict_mode", "false");
        
        Timber.d("System logging configured to reduce spam");
    }
    
    /**
     * Initialize Firebase with optimized settings
     */
    private void initializeFirebase() {
        try {
            // Initialize Firebase App
            FirebaseApp.initializeApp(this);
            
            // Configure Firestore settings with offline persistence
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build();
            
            // Apply settings to default Firestore instance
            FirebaseFirestore.getInstance().setFirestoreSettings(settings);
            
            Timber.d("Firebase initialized with optimized settings");
            
        } catch (SecurityException e) {
            // Handle Google Play Services security exceptions
            Timber.w(e, "Google Play Services security exception - Firebase may have limited functionality");
            Log.w(TAG, "Google Play Services security exception during Firebase initialization", e);
        } catch (Exception e) {
            Timber.e(e, "Failed to initialize Firebase");
            Log.e(TAG, "Firebase initialization failed", e);
        }
    }
    
    /**
     * Check Google Play Services availability and configure accordingly
     */
    private void checkGooglePlayServices() {
        try {
            GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
            int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
            
            if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                Timber.w("Google Play Services not available or outdated. Result code: %d", resultCode);
                
                // Disable Google Play Services dependent features
                System.setProperty("google.api.client.use_play_services", "false");
                System.setProperty("firebase.appcheck.debug", "false");
                
                // Check if this is a debug build
                boolean isDebugBuild = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
                
                if (isDebugBuild) {
                    Timber.d("Debug build detected - allowing limited Firebase functionality");
                } else {
                    Timber.w("Production build with missing Google Play Services - some features may be limited");
                }
            } else {
                Timber.d("Google Play Services available and up to date");
            }
            
        } catch (SecurityException e) {
            Timber.w(e, "Security exception during Google Play Services check");
            // Disable Google Play Services features if security exception occurs
            System.setProperty("google.api.client.use_play_services", "false");
        } catch (Exception e) {
            Timber.e(e, "Error checking Google Play Services availability");
        }
    }
}