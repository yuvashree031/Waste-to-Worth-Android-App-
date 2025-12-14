package com.example.wastetoworth;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
public class landingpage extends AppCompatActivity {
    private static final String TAG = "LandingPage";
    private MaterialToolbar toolbar;
    private Spinner spinnerFilter;
    private MaterialButton btnRefresh;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewDonations;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting landing page");
        setContentView(R.layout.activity_landingpage);
        Log.d(TAG, "onCreate: Layout set");
        try {
            // Initialize UI components
            toolbar = findViewById(R.id.toolbar);
            spinnerFilter = findViewById(R.id.spinnerFilter);
            btnRefresh = findViewById(R.id.btnRefresh);
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            recyclerViewDonations = findViewById(R.id.recyclerViewDonations);
            layoutEmptyState = findViewById(R.id.layoutEmptyState);
            progressBar = findViewById(R.id.progressBar);
            Log.d(TAG, "UI components initialized");
            // Toolbar back button click
            toolbar.setNavigationOnClickListener(v -> {
                Log.d(TAG, "Toolbar back pressed");
                onBackPressed();
            });
            // Refresh button click
            btnRefresh.setOnClickListener(v -> {
                Log.d(TAG, "Refresh button clicked");
                fetchDonations();
            });
            // Pull-to-refresh
            swipeRefreshLayout.setOnRefreshListener(this::fetchDonations);
            // Initial load
            fetchDonations();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing landing page: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing UI", Toast.LENGTH_LONG).show();
        }
    }
    private void fetchDonations() {
        Log.d(TAG, "Fetching donation data...");
        // Show progress bar and hide empty state initially
        progressBar.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        recyclerViewDonations.setVisibility(View.GONE);
        // TODO: Replace with real Firebase/Server fetching logic
        new android.os.Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            // Sample logic: simulate no data
            boolean hasData = false;
            if (hasData) {
                recyclerViewDonations.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
                Log.d(TAG, "Donations loaded");
            } else {
                layoutEmptyState.setVisibility(View.VISIBLE);
                recyclerViewDonations.setVisibility(View.GONE);
                Log.d(TAG, "No donations available");
            }
        }, 2000);
    }
}